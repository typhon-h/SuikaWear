/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.typhonh.suikawear.presentation

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import com.typhonh.suikawear.R
import com.typhonh.suikawear.presentation.theme.SuikaWearTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

@OptIn(ExperimentalWearMaterialApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun WearApp() {
    SuikaWearTheme {
        /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
         * version of LazyColumn for wear devices with some added features. For more information,
         * see d.android.com/wear/compose.
         */

        Surface(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val navController = rememberNavController()

            Image(
                painterResource(id = R.drawable.background),
                "Background"
            )
            NavHost(
                navController = navController,
                startDestination = Screen.HomeScreen.route
            ) {
                composable(route = Screen.HomeScreen.route) {
                    HomeFragment(
                        navigate = navController::navigate
                    )
                }

                composable(route = Screen.GameScreen.route) {
                    GameFragment(
                        navigate = navController::navigate
                    )
                }
            }
        }
    }
}