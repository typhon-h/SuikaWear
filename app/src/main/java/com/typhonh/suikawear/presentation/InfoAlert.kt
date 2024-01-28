package com.typhonh.suikawear.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Dialog
import com.typhonh.suikawear.R

@Composable
fun InfoAlert(show: MutableState<Boolean>) {
    Dialog(
        showDialog = show.value,
        onDismissRequest = {
            show.value = false
        },
        modifier = Modifier.fillMaxSize()
    ) {
        val lazyListState = rememberScalingLazyListState(0, 0)

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "How To Play",
                modifier = Modifier.padding(0.dp, 20.dp, 0.dp, 0.dp),
                style = MaterialTheme.typography.headlineMedium
            )
            Button(
                modifier = Modifier.size(30.dp),
                onClick = {
                    show.value = false
                }
            ) {
                Icon(Icons.Default.ArrowBack, "Back")
            }

            ScalingLazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                items(
                    listOf(
                        "Move fruits side to side by dragging or rotating the bezel.",
                        "Once the fruit is in position, tap to drop it.",
                        "Combine two of the same fruit to make bigger fruit.",
                        "If the fruit overflows the container, you lose!"
                    )
                ) {
                    Text(it)
                }
                items(
                    listOf(
                        R.drawable.cherry,
                        R.drawable.strawberry,
                        R.drawable.grape,
                        R.drawable.satsuma,
                        R.drawable.persimmon,
                        R.drawable.apple,
                        R.drawable.pear,
                        R.drawable.peach,
                        R.drawable.pineapple,
                        R.drawable.melon,
                        R.drawable.watermelon
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        Image(
                            painter = painterResource(id = it),
                            "Fruit",
                            modifier = Modifier.size(50.dp)
                        )
                        if (it != R.drawable.watermelon) {
                            Icon(Icons.Default.KeyboardArrowDown, "Becomes")
                        }
                    }
                }
            }
        }
    }
}