package com.typhonh.suikawear.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavOptions
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.typhonh.suikawear.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Composable
fun LeaderboardFragment(
    navigate: (route: String, navOptions: NavOptions) -> Unit,
    dataStore: DataStore<Preferences>,
) {

    var scores by remember { mutableStateOf<List<Int>>(emptyList()) }

    LaunchedEffect(Unit) {
        val scoreString = dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(MainActivity.SETTINGS_KEY)] ?: ""
        }.first()

        scores = scoreString.trim().split(",").mapNotNull { s -> s.toIntOrNull() }

    }

    Column(modifier = Modifier.fillMaxSize().padding(0.dp,20.dp,0.dp,0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround) {
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(0.6f)) {
                Icon(
                    painter = painterResource(id = R.drawable.trophy),
                    tint = Color(197, 188, 66),
                    contentDescription = "Gold Trophy",
                    modifier = Modifier.size(35.dp)
                )

                Text((if (scores.isNotEmpty()) scores[0].toString() else "-"), fontSize = 30.sp, style = MaterialTheme.typography.title1.copy(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(250, 250, 220), Color(240, 200, 30))
                    ),))
        }

        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(0.6f)) {
                Icon(
                    painter = painterResource(id = R.drawable.trophy),
                    tint = Color(214,214,214),
                    contentDescription = "Silver Trophy",
                    modifier = Modifier.size(35.dp)
                )

                Text((if (scores.size >= 2) scores[1].toString() else "-"), fontSize = 30.sp,style = MaterialTheme.typography.title1.copy(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(255,255,255), Color(200,200,200))
                    ),))
        }

        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(0.6f)) {
            Icon(
                painter = painterResource(id = R.drawable.trophy),
                tint = Color(151, 117, 71),
                contentDescription = "Bronze Trophy",
                modifier = Modifier.size(35.dp)
            )

            Text((if (scores.size >= 3) scores[2].toString() else "-"), fontSize = 30.sp,style = MaterialTheme.typography.title1.copy(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(240, 200, 30), Color(151, 117, 71))
                ),))
        }

        Button(
            modifier = Modifier.size(40.dp).align(Alignment.CenterHorizontally),
            onClick = {
                navigate(Screen.HomeScreen.route, NavOptions.Builder().setPopUpTo(Screen.HomeScreen.route, true).build())
            }
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                modifier = Modifier.size(25.dp),
                contentDescription = "Back"
            )
        }
    }
}