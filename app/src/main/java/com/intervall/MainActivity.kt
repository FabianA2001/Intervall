package com.intervall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { Home(navController) }
        composable(
            route = "timer/{secondsList}",
            arguments = listOf(
                navArgument("secondsList") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val secondsListString = backStackEntry.arguments?.getString("secondsList")
            TimerScreen(navController, secondsListString)
        }
    }
}
