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
