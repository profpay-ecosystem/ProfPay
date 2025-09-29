package com.example.telegramWallet.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telegramWallet.R

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(
    goToAboutUs: () -> Unit,
    goToSecurity: () -> Unit,
) {
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartInDevelopment() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Smart",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        style = TextStyle(color = MaterialTheme.colorScheme.onPrimary),
                    )
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                navigationIcon = {},
                actions = {
                    run {
                        IconButton(onClick = { /*goToBack()*/ }) {
                            Icon(
                                modifier = Modifier.size(35.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_help_quation),
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        padding
        Column(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Страница в разработке...",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
