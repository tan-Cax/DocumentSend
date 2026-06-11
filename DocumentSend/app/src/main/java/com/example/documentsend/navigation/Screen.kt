package com.example.documentsend.navigation

sealed class Screen(val route: String){
    object Home : Screen("home")
    object Send : Screen("send")
    object Receive : Screen("receive")
    object Text : Screen("text")
    object Settings : Screen("settings")
    object History : Screen("history")
    object Log : Screen("log")
    object About : Screen("about")
}