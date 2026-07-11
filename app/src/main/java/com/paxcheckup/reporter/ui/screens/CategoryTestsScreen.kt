package com.paxcheckup.reporter.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paxcheckup.reporter.data.TestCategory
import com.paxcheckup.reporter.data.TestStatus
import com.paxcheckup.reporter.ui.theme.PassGreen
import com.paxcheckup.reporter.viewmodel.TestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTestsScreen(navController: NavController, viewModel: TestViewModel, categoryName: String) {
    val category = try {
        TestCategory.valueOf(categoryName)
    } catch (e: IllegalArgumentException) {
        null
    }

    val allTests by viewModel.testItems
    val categoryTests = if (category != null) allTests.filter { it.category == category } else emptyList()
    val passCount = categoryTests.count { it.status == TestStatus.PASS }
    val progress = if (categoryTests.isNotEmpty()) passCount.toFloat() / categoryTests.size else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category?.displayName ?: "Unknown", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (category == null) {
                Text("Category not found")
                return@Column
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Progress: $passCount / ${categoryTests.size} tests passed", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(),
                        color = PassGreen,
                        trackColor = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categoryTests) { test ->
                    TestItemCard(test = test, onClick = {
                        navController.navigate("test_detail/${test.id}")
                    })
                }
            }
        }
    }
}
package com.paxcheckup.reporter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paxcheckup.reporter.data.TestStatus
import com.paxcheckup.reporter.ui.theme.FailRed
import com.paxcheckup.reporter.ui.theme.NotTestedGray
import com.paxcheckup.reporter.ui.theme.PassGreen
import com.paxcheckup.reporter.ui.theme.SkipYellow
import com.paxcheckup.reporter.viewmodel.TestViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestDetailScreen(navController: NavController, viewModel: TestViewModel, testId: String) {
    val allTests by viewModel.testItems
    val test = allTests.find { it.id == testId }
    var notes by remember { mutableStateOf(test?.notes ?: "") }

    if (test == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Test not found", style = MaterialTheme.typography.headlineMedium)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(test.name, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Category: ${test.category.displayName}", style = MaterialTheme.typography.bodyLarge)
                    Text("Description: ${test.description}", style = MaterialTheme.typography.bodyMedium)
                    test.timestamp?.let { ts ->
                        val timeStr = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(ts)
                        Text("Last tested: $timeStr", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            Text("Current Status: ${test.status.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusButton("PASS", PassGreen, test.status == TestStatus.PASS) {
                    viewModel.updateTestStatus(test.id, TestStatus.PASS, notes)
                }
                StatusButton("FAIL", FailRed, test.status == TestStatus.FAIL) {
                    viewModel.updateTestStatus(test.id, TestStatus.FAIL, notes)
                }
                StatusButton("SKIP", SkipYellow, test.status == TestStatus.SKIPPED) {
                    viewModel.updateTestStatus(test.id, TestStatus.SKIPPED, notes)
                }
                StatusButton("RESET", NotTestedGray, test.status == TestStatus.NOT_TESTED) {
                    viewModel.updateTestStatus(test.id, TestStatus.NOT_TESTED, "")
                    notes = ""
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Observations") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            if (notes.isNotBlank() && test.status != TestStatus.NOT_TESTED) {
                Button(
                    onClick = { viewModel.updateTestStatus(test.id, test.status, notes) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update Notes")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Tip: Open PAX CheckUp app and run the '${test.name}' test. Then come back and mark the result here.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun StatusButton(label: String, color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) color else color.copy(alpha = 0.3f),
            contentColor = Color.White
        ),
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(label, fontSize = 12.sp)
    }
}
package com.paxcheckup.reporter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.paxcheckup.reporter.data.TestItem
import com.paxcheckup.reporter.data.TestStatus
import com.paxcheckup.reporter.ui.theme.FailRed
import com.paxcheckup.reporter.ui.theme.NotTestedGray
import com.paxcheckup.reporter.ui.theme.PassGreen
import com.paxcheckup.reporter.ui.theme.SkipYellow
import com.paxcheckup.reporter.viewmodel.TestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestListScreen(navController: NavController, viewModel: TestViewModel) {
    val allTests by viewModel.testItems
    val searchQuery by viewModel.searchQuery
    val showOnlyFailed by viewModel.showOnlyFailed

    val filteredTests = allTests.filter { test ->
        val matchesSearch = if (searchQuery.isBlank()) true else
            test.name.contains(searchQuery, ignoreCase = true) ||
            test.description.contains(searchQuery, ignoreCase = true) ||
            test.category.displayName.contains(searchQuery, ignoreCase = true)
        val matchesFailed = if (!showOnlyFailed) true else test.status == TestStatus.FAIL
        matchesSearch && matchesFailed
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Tests", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Search tests...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = showOnlyFailed,
                    onClick = { viewModel.setShowOnlyFailed(!showOnlyFailed) },
                    label = { Text("Show Only Failed") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTests) { test ->
                    TestItemCard(test = test, onClick = {
                        navController.navigate("test_detail/${test.id}")
                    })
                }
            }
        }
    }
}

@Composable
fun TestItemCard(test: TestItem, onClick: () -> Unit) {
    val statusColor = when (test.status) {
        TestStatus.PASS -> PassGreen
        TestStatus.FAIL -> FailRed
        TestStatus.SKIPPED -> SkipYellow
        TestStatus.PARTIAL -> SkipYellow
        else -> NotTestedGray
    }

    val statusIcon = when (test.status) {
        TestStatus.PASS -> Icons.Default.CheckCircle
        TestStatus.FAIL -> Icons.Default.Error
        TestStatus.SKIPPED -> Icons.Default.SkipNext
        TestStatus.PARTIAL -> Icons.Default.HelpOutline
        else -> Icons.Default.Pending
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(test.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(test.category.displayName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.padding(start = 8.dp))
        }
    }
}
