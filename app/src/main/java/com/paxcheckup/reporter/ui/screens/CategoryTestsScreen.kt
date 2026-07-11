package com.paxcheckup.reporter.ui.screens

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
                        progress = { progress },
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
