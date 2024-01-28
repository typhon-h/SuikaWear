package com.typhonh.suikawear.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavOptions
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import com.typhonh.suikawear.R
import com.typhonh.suikawear.presentation.theme.wearColorPalette

@Composable
fun HomeFragment(
    navigate: (route: String, navOptions: NavOptions) -> Unit,
) {

    val showInfo = remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(R.drawable.title),
            contentDescription = "Suika Game",
            modifier = Modifier.fillMaxWidth(0.8f),
        )

        Row() {
            Button(
                onClick = { navigate(Screen.GameScreen.route, NavOptions.Builder().build()) }
            ) {
                Icon(
                    Icons.Rounded.PlayArrow,
                    tint = wearColorPalette.onPrimary,
                    contentDescription = "Start",
                )
            }
        }

        Row() {
            Button(
                onClick = { navigate(Screen.LeaderboardScreen.route, NavOptions.Builder().build()) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.leaderboard),
                    tint = wearColorPalette.onPrimary,
                    contentDescription = "Leaderboard",
                )
            }

            Spacer(modifier = Modifier.width(30.dp))

            Button(
                onClick = { showInfo.value = true }
            ) {
                Icon(
                    Icons.Default.Info,
                    tint = wearColorPalette.onPrimary,
                    contentDescription = "How to Play",
                )
            }
        }
    }

    InfoAlert(showInfo)
}