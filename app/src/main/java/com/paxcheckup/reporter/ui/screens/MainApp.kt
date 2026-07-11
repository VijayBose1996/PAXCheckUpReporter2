package com.paxcheckup.reporter.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paxcheckup.reporter.viewmodel.TestViewModel

object ScreenRoutes {
    const val HOME = "home"
    const val TEST_LIST = "test_list"
    const val TEST_DETAIL = "test_detail/{testId}"
    const val REPORT = "report"
    const val CATEGORY_TESTS = "category/{categoryName}"
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val viewModel: TestViewModel = viewModel()

    Scaffold { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(navController = navController, startDestination = ScreenRoutes.HOME) {
                composable(ScreenRoutes.HOME) {
                    HomeScreen(navController, viewModel)
                }
                composable(ScreenRoutes.TEST_LIST) {
                    TestListScreen(navController, viewModel)
                }
                composable(ScreenRoutes.TEST_DETAIL) { backStackEntry ->
                    val testId = backStackEntry.arguments?.getString("testId") ?: ""
                    TestDetailScreen(navController, viewModel, testId)
                }
                composable(ScreenRoutes.REPORT) {
                    ReportScreen(navController, viewModel)
                }
                composable(ScreenRoutes.CATEGORY_TESTS) { backStackEntry ->
                    val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                    CategoryTestsScreen(navController, viewModel, categoryName)
                }
            }
        }
    }
}
