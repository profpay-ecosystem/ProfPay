package com.example.telegramWallet.ui.app.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.telegramWallet.ui.app.theme.BackgroundIcon

@Composable
fun HomeBottomNavBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreen.SmartContractList,
        BottomBarScreen.Profile,
        BottomBarScreen.Settings,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarDestination = screens.any { screen ->
        screen.routes.stream().filter { it == currentDestination?.route }.findFirst().isPresent }
    if (bottomBarDestination) {

    BottomAppBar(
        modifier = Modifier.padding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
    ) {
        screens.forEach { screen ->
            Column(
                modifier = Modifier.weight(0.33f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                val color = MaterialTheme.colorScheme.onPrimary

                if (currentDestination?.hierarchy?.any { item ->
                        screen.routes.stream().filter { it == item.route }.findFirst().isPresent
                    } == true) {
                    Canvas(modifier = Modifier.size(width = 70.dp, height = 0.dp)) {
                        val canvasWidth = size.width
                        drawLine(
                            start = Offset(x = canvasWidth, y = -8f),
                            end = Offset(x = 0f, y = -8f),
                            strokeWidth = 10f,
                            color = color
                        )
                    }
                }
                this@BottomAppBar.AddItem(
                    screen = screen,
                    currentDestination = currentDestination,
                    navController = navController
                )
            }
        }
    }
    }
}

@Composable
private fun RowScope.AddItem(
    screen: BottomBarScreen,
    currentDestination: NavDestination?,
    navController: NavHostController
) {

    NavigationBarItem(
        modifier = Modifier.align(Alignment.Top),
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
            selectedTextColor = MaterialTheme.colorScheme.onPrimary,
            unselectedIconColor = BackgroundIcon,
            unselectedTextColor = BackgroundIcon,
            indicatorColor = Color.Transparent
        ),
        label = {
            Text(text = screen.title)

        },
        icon = {
            Icon(
                modifier = Modifier.size(30.dp),
                imageVector = screen.icon.invoke(),
                contentDescription = "Navigation Icon"
            )
        },
        selected = currentDestination?.hierarchy?.any { item ->
            screen.routes.stream().filter { it == item.route }.findFirst().isPresent
        } == true,
        onClick = {
            navController.navigate(screen.route) {
//                popUpTo(navController.graph.findStartDestination().id)
//  если место назначения уже находится в верхней части стека возврата, оно не будет повторно создано.
                launchSingleTop = true
//  состояние места назначения будет восстановлено, если оно уже существует в стеке возврата.
                restoreState = true
            }
        }
    )
}

