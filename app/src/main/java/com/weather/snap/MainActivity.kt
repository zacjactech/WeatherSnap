package com.weather.snap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.weather.core.designsystem.theme.WeatherSnapTheme
import com.weather.feature.camera.CameraRoute
import com.weather.feature.history.HistoryRoute
import com.weather.feature.report.CreateReportRoute
import com.weather.feature.weather.WeatherHomeRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        androidx.core.view.WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            WeatherSnapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = "weather"
                    ) {
                        composable("weather") {
                            WeatherHomeRoute(
                                onCreateReportClicked = { navController.navigate("create_report") },
                                onNavigateToCamera = { navController.navigate("create_report") },
                                onNavigateToReports = { navController.navigate("history") }
                            )
                        }
                        
                        composable(
                            route = "create_report?draft_id={draft_id}",
                            arguments = listOf(
                                navArgument("draft_id") {
                                    nullable = true
                                    type = NavType.StringType
                                }
                            )
                        ) {
                            CreateReportRoute(
                                onNavigateToCamera = { draftId -> navController.navigate("camera/$draftId") },
                                onNavigateBack = { navController.popBackStack() },
                                onReportSaved = { 
                                    navController.popBackStack()
                                    navController.navigate("history")
                                }
                            )
                        }
                        
                        composable(
                            "camera/{draftId}",
                            arguments = listOf(androidx.navigation.navArgument("draftId") { type = androidx.navigation.NavType.StringType })
                        ) {
                            CameraRoute(
                                onPhotoTaken = {
                                    navController.popBackStack()
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("history") {
                            HistoryRoute(
                                onReportClick = { snapId -> navController.navigate("report_detail/$snapId") },
                                onCreateReportClick = { navController.navigate("create_report") },
                                onNavigateToHome = { navController.navigate("weather") },
                                onNavigateToCamera = { navController.navigate("create_report") }
                            )
                        }
                        
                        composable("report_detail/{snap_id}") { backStackEntry ->
                            val snapId = backStackEntry.arguments?.getString("snap_id")
                            if (snapId != null) {
                                com.weather.feature.report.ReportDetailRoute(
                                    onNavigateBack = { navController.popBackStack() },
                                    onEditClick = { id -> navController.navigate("create_report?draft_id=$id") }
                                )
                            }
                        }
                        
                    }
                }
            }
        }
    }
}
