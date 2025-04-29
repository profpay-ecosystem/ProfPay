package com.example.telegramWallet.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.core.content.edit
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.telegramWallet.ui.app.navigation.HomeBottomNavBar
import com.example.telegramWallet.ui.app.navigation.graphs.HomeNavGraph
import com.example.telegramWallet.ui.shared.sharedPref

@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavHostController = rememberNavController()) {
    Scaffold(
        bottomBar = { HomeBottomNavBar(navController = navController) },
    ) { padding ->
        sharedPref().edit() { putFloat("bottomPadding", padding.calculateBottomPadding().value) }
        HomeNavGraph(navController = navController)
    }
}
