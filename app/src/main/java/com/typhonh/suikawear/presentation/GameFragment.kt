package com.typhonh.suikawear.presentation

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavOptions
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import com.typhonh.suikawear.business.GameEngineViewModel

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun GameFragment(
    navigate: (route: String, navOptions: NavOptions) -> Unit,
    viewModel: GameEngineViewModel = viewModel()
) {
    GameCanvas(viewModel = viewModel)

    var showClearConfirmation by remember { mutableStateOf(false) }
    var showBackConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(15.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    )  {
        Button(
            modifier = Modifier.size(25.dp),
            onClick = { showClearConfirmation = true }
        ) {
            Icon(
                Icons.Default.Refresh,
                modifier = Modifier.size(15.dp),
                contentDescription = "Restart"
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            modifier = Modifier.size(25.dp),
            onClick = {
                showBackConfirmation = true
            }
        ) {
            Icon(
                Icons.Default.ArrowBack,
                modifier = Modifier.size(15.dp),
                contentDescription = "Back"
            )
        }
    }

    if(showClearConfirmation) {
        Alert(
            title = { Text("Reset Game?") },
            negativeButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red, contentColor = Color.Black),
                    onClick = { showClearConfirmation = false }) {
                    Icon(
                        Icons.Default.Close,
                        "Cancel"
                    )
                }
                             },
            positiveButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green, contentColor = Color.Black),
                    onClick = {
                        showClearConfirmation = false
                        viewModel.resetGame()
                    }) {
                    Icon(
                        Icons.Default.Check,
                        "Confirm"
                    )
                }
            },
        ) {
            BackHandler(
                enabled = true,
                onBack = { showClearConfirmation = false }
            )
        }
    }

    if(showBackConfirmation) {
        Alert(
            title = { Text("Abandon Game?") },
            negativeButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red, contentColor = Color.Black),
                    onClick = { showBackConfirmation = false }) {
                    Icon(
                        Icons.Default.Close,
                        "Cancel"
                    )
                }
            },
            positiveButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green, contentColor = Color.Black),
                    onClick = {
                        showBackConfirmation = false
                        navigate(
                            Screen.HomeScreen.route,
                            NavOptions.Builder()
                                .setPopUpTo(Screen.HomeScreen.route, true)
                                .build()
                        )
                    }) {
                    Icon(
                        Icons.Default.Check,
                        "Confirm"
                    )
                }
            },
        ) {
            BackHandler(
                enabled = true,
                onBack = { showBackConfirmation = false }
            )
        }
    }
}