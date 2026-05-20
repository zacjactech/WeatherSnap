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
                                onNavigateToCamera = { navController.navigate("camera") },
                                onNavigateToReports = { navController.navigate("history") },
                                onNavigateToSettings = { navController.navigate("settings") }
                            )
                        }
                        
                        composable("create_report") {
                            CreateReportRoute(
                                onNavigateToCamera = { navController.navigate("camera") },
                                onNavigateBack = { navController.popBackStack() },
                                onReportSaved = { 
                                    navController.popBackStack()
                                    navController.navigate("history")
                                }
                            )
                        }
                        
                        composable("camera") {
                            CameraRoute(
                                onPhotoTaken = { filePath ->
                                    navController.previousBackStackEntry?.savedStateHandle?.set("photo_path", filePath)
                                    navController.popBackStack()
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("history") {
                            HistoryRoute(
                                onReportClick = { /* Navigate to details */ }
                            )
                        }
                        
                        composable("settings") {
                            SettingsRoute()
                        }
                    }
                }
            }
        }
    }
}