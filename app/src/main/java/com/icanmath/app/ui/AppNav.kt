package com.icanmath.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.icanmath.app.AppViewModel

@Composable
fun AppNav(vm: AppViewModel) {
    val nav: NavHostController = rememberNavController()
    NavHost(navController = nav, startDestination = "home") {
        composable("home") { HomeScreen(vm, nav) }
        composable("profileform") { ProfileFormScreen(vm, nav) }
        composable("dashboard") { DashboardScreen(vm, nav) }
        composable("classselect") { ClassSelectScreen(vm, nav) }
        composable(
            "mode/{grade}",
            arguments = listOf(navArgument("grade") { type = NavType.IntType })
        ) { entry -> ModeSelectScreen(vm, nav, entry.arguments?.getInt("grade") ?: 1) }
        composable("quiz") { QuizScreen(vm, nav) }
        composable("result") { ResultScreen(vm, nav) }
        composable("logicmenu") { LogicMenuScreen(vm, nav) }
        composable("puzzle") { ChoiceGameScreen(vm, nav, "PUZZLE", "Puzzle Penjumlahan", ::puzzleItems) }
        composable("compare") { ChoiceGameScreen(vm, nav, "COMPARE", "Lebih Besar atau Kecil", ::compareItems) }
        composable("group") { GroupGameScreen(vm, nav) }
        composable("profile") { ProfileScreen(vm, nav) }
        composable("settings") { SettingsScreen(vm, nav) }
        composable("print") { PrintScreen(vm, nav) }
    }
}
