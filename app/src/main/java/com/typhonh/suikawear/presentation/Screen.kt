package com.typhonh.suikawear.presentation

sealed class Screen(val route: String) {
    object HomeScreen: Screen("home_screen")
    object GameScreen: Screen("game_screen")
    object LeaderboardScreen: Screen("leaderboard_screen")
}
