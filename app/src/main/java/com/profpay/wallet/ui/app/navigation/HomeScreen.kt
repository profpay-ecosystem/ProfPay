package com.profpay.wallet.ui.app.navigation

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.core.content.edit
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.profpay.wallet.ui.app.navigation.bottombar.HomeBottomNavBar
import com.profpay.wallet.ui.app.navigation.graphs.HomeNavGraph
import com.profpay.wallet.ui.shared.sharedPref

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavHostController = rememberNavController()) {
    Scaffold(
        bottomBar = { HomeBottomNavBar(navController = navController) },
    ) { padding ->
        sharedPref().edit { putFloat("bottomPadding", padding.calculateBottomPadding().value) }
        HomeNavGraph(navController = navController)
    }
}
