package com.paxcheckup.reporter.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import com.paxcheckup.reporter.data.TestCategory
import com.paxcheckup.reporter.data.TestItem
import com.paxcheckup.reporter.data.TestSession
import com.paxcheckup.reporter.data.TestStatus
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PDFGenerator {

    fun generatePDF(context: Context, session: TestSession, allTests: List<TestItem>): String {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val marginLeft = 40f
        val marginRight = 40f
        val contentWidth = pageWidth - marginLeft - marginRight

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#1a237e")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val headerPaint = Paint().apply {
            color = Color.parseColor("#303f9f")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val normalPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
        }
        val smallPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 9f
        }
        val passPaint = Paint().apply {
            color = Color.parseColor("#2e7d32")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val failPaint = Paint().apply {
            color = Color.parseColor("#c62828")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val skipPaint = Paint().apply {
            color = Color.parseColor("#f9a825")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val notTestedPaint = Paint().apply {
            color = Color.GRAY
            textSize = 11f
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        val thickLinePaint = Paint().apply {
            color = Color.parseColor("#303f9f")
            strokeWidth = 2f
        }
        val boxPaint = Paint().apply {
            color = Color.parseColor("#e8eaf6")
        }
        val boxBorderPaint = Paint().apply {
            color = Color.parseColor("#303f9f")
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        var y = 40f

        fun drawHeader() {
            canvas.drawRect(marginLeft, y, pageWidth - marginRight, y + 60f, boxPaint)
            canvas.drawRect(marginLeft, y, pageWidth - marginRight, y + 60f, boxBorderPaint)
            canvas.drawText("PAX CHECKUP - HARDWARE TEST REPORT", marginLeft + 10f, y + 25f, titlePaint)
            val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault()).format(session.startTime)
            canvas.drawText("Generated: $dateStr", marginLeft + 10f, y + 45f, smallPaint)
            y += 75f
        }

        fun drawDeviceInfo() {
            canvas.drawText("DEVICE INFORMATION", marginLeft, y, headerPaint)
            canvas.drawLine(marginLeft, y + 5f, pageWidth - marginRight, y + 5f, thickLinePaint)
            y += 22f
            val infoItems = listOf(
                "Device Model" to session.deviceModel,
                "Serial Number" to session.serialNumber,
                "Technician" to session.technicianName,
                "Location" to session.location,
                "Android Version" to "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                "Manufacturer" to Build.MANUFACTURER,
                "Board" to Build.BOARD
            )
            infoItems.forEach { (label, value) ->
                canvas.drawText("$label:", marginLeft + 5f, y, normalPaint)
                val v = if (value.isBlank()) "N/A" else value
                canvas.drawText(v, marginLeft + 130f, y, normalPaint)
                y += 16f
            }
            y += 10f
        }

        fun drawSummary() {
            canvas.drawText("TEST SUMMARY", marginLeft, y, headerPaint)
            canvas.drawLine(marginLeft, y + 5f, pageWidth - marginRight, y + 5f, thickLinePaint)
            y += 22f

            val pass = allTests.count { it.status == TestStatus.PASS }
            val fail = allTests.count { it.status == TestStatus.FAIL }
            val skip = allTests.count { it.status == TestStatus.SKIPPED }
            val notTested = allTests.count { it.status == TestStatus.NOT_TESTED }
            val total = allTests.size

            canvas.drawText("Total Tests: $total", marginLeft + 5f, y, normalPaint)
            y += 16f
            canvas.drawText("Passed: $pass", marginLeft + 5f, y, passPaint)
            y += 16f
            canvas.drawText("Failed: $fail", marginLeft + 5f, y, failPaint)
            y += 16f
            canvas.drawText("Skipped: $skip", marginLeft + 5f, y, skipPaint)
            y += 16f
            canvas.drawText("Not Tested: $notTested", marginLeft + 5f, y, notTestedPaint)
            y += 20f

            val statusText = when {
                fail > 0 -> "OVERALL: FAILED"
                pass == total && total > 0 -> "OVERALL: ALL PASSED"
                pass > 0 -> "OVERALL: PARTIAL"
                else -> "OVERALL: NOT TESTED"
            }
            val statusPaint = when {
                fail > 0 -> failPaint
                pass == total && total > 0 -> passPaint
                pass > 0 -> skipPaint
                else -> notTestedPaint
            }
            statusPaint.textSize = 14f
            canvas.drawText(statusText, marginLeft + 5f, y, statusPaint)
            statusPaint.textSize = 11f
            y += 25f
        }

        fun checkPageBreak(requiredHeight: Float = 100f) {
            if (y + requiredHeight > pageHeight - 40f) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 40f
            }
        }

        fun drawTestCategory(category: TestCategory) {
            val catTests = allTests.filter { it.category == category }
            if (catTests.isEmpty()) return

            checkPageBreak(40f + catTests.size * 35f)

            canvas.drawText(category.displayName.uppercase(), marginLeft, y, headerPaint)
            canvas.drawLine(marginLeft, y + 5f, pageWidth - marginRight, y + 5f, thickLinePaint)
            y += 20f

            catTests.forEach { test ->
                checkPageBreak(40f)

                val statusColor = when (test.status) {
                    TestStatus.PASS -> Color.parseColor("#e8f5e9")
                    TestStatus.FAIL -> Color.parseColor("#ffebee")
                    TestStatus.SKIPPED -> Color.parseColor("#fffde7")
                    else -> Color.parseColor("#f5f5f5")
                }
                val statusText = when (test.status) {
                    TestStatus.PASS -> "PASS"
                    TestStatus.FAIL -> "FAIL"
                    TestStatus.SKIPPED -> "SKIPPED"
                    TestStatus.PARTIAL -> "PARTIAL"
                    else -> "NOT TESTED"
                }
                val statusPaint2 = when (test.status) {
                    TestStatus.PASS -> passPaint
                    TestStatus.FAIL -> failPaint
                    TestStatus.SKIPPED -> skipPaint
                    TestStatus.PARTIAL -> skipPaint
                    else -> notTestedPaint
                }

                canvas.drawRect(marginLeft, y - 12f, pageWidth - marginRight, y + 20f, Paint().apply { color = statusColor })
                canvas.drawLine(marginLeft, y + 20f, pageWidth - marginRight, y + 20f, linePaint)

                canvas.drawText("• ${test.name}", marginLeft + 5f, y, normalPaint)
                canvas.drawText(statusText, pageWidth - marginRight - 60f, y, statusPaint2)
                y += 15f

                if (test.description.isNotBlank()) {
                    canvas.drawText("  ${test.description}", marginLeft + 10f, y, smallPaint)
                    y += 12f
                }
                if (test.notes.isNotBlank()) {
                    canvas.drawText("  Notes: ${test.notes}", marginLeft + 10f, y, smallPaint)
                    y += 12f
                }
                test.timestamp?.let { ts ->
                    val tsStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(ts)
                    canvas.drawText("  Tested at: $tsStr", marginLeft + 10f, y, smallPaint)
                    y += 12f
                }
                y += 5f
            }
            y += 10f
        }

        fun drawFooter() {
            canvas.drawLine(marginLeft, pageHeight - 30f, pageWidth - marginRight, pageHeight - 30f, linePaint)
            canvas.drawText("PAX CheckUp Reporter - Page $pageNumber", marginLeft, pageHeight - 15f, smallPaint)
            canvas.drawText("Confidential - For internal use only", pageWidth - marginRight - 140f, pageHeight - 15f, smallPaint)
        }

        drawHeader()
        drawDeviceInfo()
        drawSummary()

        TestCategory.values().forEach { category ->
            drawTestCategory(category)
        }

        checkPageBreak(60f)
        canvas.drawText("END OF REPORT", marginLeft, y, headerPaint)
        y += 20f
        canvas.drawText("This report was generated automatically by PAX CheckUp Reporter.", marginLeft, y, smallPaint)
        y += 12f
        canvas.drawText("For any discrepancies, please re-run the hardware tests.", marginLeft, y, smallPaint)

        drawFooter()
        document.finishPage(page)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "PAX_CheckUp_Report_${session.deviceModel}_${timeStamp}.pdf"
        val reportsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Reports")
        reportsDir.mkdirs()
        val file = File(reportsDir, fileName)

        FileOutputStream(file).use { output ->
            document.writeTo(output)
        }
        document.close()

        return file.absolutePath
    }
}
