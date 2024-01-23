package com.typhonh.suikawear.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavOptions
import androidx.wear.compose.material.Text
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

    LazyColumn(modifier=Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        items(scores) {
            Text(it.toString())
        }
    }

}