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
