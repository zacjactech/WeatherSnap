package com.weather.snap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.weather.core.designsystem.theme.WeatherSnapTheme
import com.weather.feature.camera.CameraRoute
import com.weather.feature.history.HistoryRoute
import com.weather.feature.report.CreateReportRoute
import com.weather.feature.settings.SettingsRoute
import com.weather.feature.weather.WeatherHomeRoute
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
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
                                onNavigateToReports = { navController.navigate("history") },
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }
                        
                        composable("create_report") {
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
                                onNavigateToCamera = { navController.navigate("create_report") },
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }
                        
                        composable("report_detail/{snap_id}") { backStackEntry ->
                            val snapId = backStackEntry.arguments?.getString("snap_id")
                            if (snapId != null) {
                                com.weather.feature.report.ReportDetailRoute(
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                        
                        composable("settings") {
                            SettingsRoute(
                                onNavigateToHome = { navController.navigate("weather") },
                                onNavigateToCamera = { navController.navigate("create_report") },
                                onNavigateToReports = { navController.navigate("history") }
                            )
                        }
                    }
                }
            }
        }
    }
}
