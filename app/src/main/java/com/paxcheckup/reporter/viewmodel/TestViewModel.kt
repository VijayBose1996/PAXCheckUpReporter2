package com.paxcheckup.reporter.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.paxcheckup.reporter.data.TestCategory
import com.paxcheckup.reporter.data.TestDefinitions
import com.paxcheckup.reporter.data.TestItem
import com.paxcheckup.reporter.data.TestSession
import com.paxcheckup.reporter.data.TestStatus
import com.paxcheckup.reporter.utils.PDFGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class TestViewModel(application: Application) : AndroidViewModel(application) {

    private val _session = mutableStateOf(TestSession())
    val session: State<TestSession> = _session

    private val _testItems = mutableStateOf<List<TestItem>>(TestDefinitions.getAllTests())
    val testItems: State<List<TestItem>> = _testItems

    private val _isGeneratingPdf = mutableStateOf(false)
    val isGeneratingPdf: State<Boolean> = _isGeneratingPdf

    private val _pdfGenerated = mutableStateOf<String?>(null)
    val pdfGenerated: State<String?> = _pdfGenerated

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _selectedCategory = mutableStateOf<TestCategory?>(null)
    val selectedCategory: State<TestCategory?> = _selectedCategory

    private val _showOnlyFailed = mutableStateOf(false)
    val showOnlyFailed: State<Boolean> = _showOnlyFailed

    init {
        val deviceModel = Build.MODEL
        val serialNumber = Build.SERIAL
        _session.value = _session.value.copy(
            deviceModel = deviceModel,
            serialNumber = serialNumber
        )
    }

    fun updateTechnicianInfo(name: String, location: String) {
        _session.value = _session.value.copy(
            technicianName = name,
            location = location
        )
    }

    fun updateTestStatus(testId: String, status: TestStatus, notes: String = "") {
        val updatedItems = _testItems.value.map { item ->
            if (item.id == testId) {
                item.copy(
                    status = status,
                    notes = notes,
                    timestamp = Date()
                )
            } else item
        }
        _testItems.value = updatedItems
        updateSessionStatus()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: TestCategory?) {
        _selectedCategory.value = category
    }

    fun setShowOnlyFailed(show: Boolean) {
        _showOnlyFailed.value = show
    }

    fun getFilteredTests(): List<TestItem> {
        var filtered = _testItems.value
        _selectedCategory.value?.let { cat ->
            filtered = filtered.filter { it.category == cat }
        }
        if (_searchQuery.value.isNotBlank()) {
            val q = _searchQuery.value.lowercase()
            filtered = filtered.filter {
                it.name.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.category.displayName.lowercase().contains(q)
            }
        }
        if (_showOnlyFailed.value) {
            filtered = filtered.filter { it.status == TestStatus.FAIL }
        }
        return filtered
    }

    fun getTestsByCategory(category: TestCategory): List<TestItem> {
        return _testItems.value.filter { it.category == category }
    }

    fun getCategoryStatus(category: TestCategory): TestStatus {
        val catTests = getTestsByCategory(category)
        return when {
            catTests.all { it.status == TestStatus.PASS } -> TestStatus.PASS
            catTests.any { it.status == TestStatus.FAIL } -> TestStatus.FAIL
            catTests.any { it.status == TestStatus.PARTIAL } -> TestStatus.PARTIAL
            catTests.all { it.status == TestStatus.SKIPPED } -> TestStatus.SKIPPED
            catTests.any { it.status == TestStatus.PASS } -> TestStatus.PARTIAL
            else -> TestStatus.NOT_TESTED
        }
    }

    fun getCategoryCounts(category: TestCategory): Triple<Int, Int, Int> {
        val catTests = getTestsByCategory(category)
        val pass = catTests.count { it.status == TestStatus.PASS }
        val fail = catTests.count { it.status == TestStatus.FAIL }
        val total = catTests.size
        return Triple(pass, fail, total)
    }

    private fun updateSessionStatus() {
        val currentItems = _testItems.value
        val overall = when {
            currentItems.all { it.status == TestStatus.PASS } -> TestStatus.PASS
            currentItems.any { it.status == TestStatus.FAIL } -> TestStatus.FAIL
            currentItems.any { it.status == TestStatus.PARTIAL } -> TestStatus.PARTIAL
            currentItems.any { it.status == TestStatus.PASS } -> TestStatus.PARTIAL
            else -> TestStatus.NOT_TESTED
        }
        _session.value = _session.value.copy(
            testItems = currentItems,
            overallStatus = overall
        )
    }

    fun finalizeSession() {
        _session.value = _session.value.copy(
            endTime = Date(),
            testItems = _testItems.value
        )
    }

    fun generatePdf(context: Context) {
        viewModelScope.launch {
            _isGeneratingPdf.value = true
            try {
                finalizeSession()
                val filePath = withContext(Dispatchers.IO) {
                    PDFGenerator.generatePDF(context, _session.value, _testItems.value)
                }
                _pdfGenerated.value = filePath
            } catch (e: Exception) {
                e.printStackTrace()
                _pdfGenerated.value = null
            } finally {
                _isGeneratingPdf.value = false
            }
        }
    }

    fun resetPdfState() {
        _pdfGenerated.value = null
    }

    fun resetAllTests() {
        _testItems.value = TestDefinitions.getAllTests()
        _session.value = TestSession(
            deviceModel = Build.MODEL,
            serialNumber = Build.SERIAL
        )
        _pdfGenerated.value = null
    }

    fun getReportFilePath(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "PAX_CheckUp_Report_${_session.value.deviceModel}_${timeStamp}.pdf"
        return File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Reports/$fileName")
    }
}
