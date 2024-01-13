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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typhonh.suikawear.business.GameController
import com.typhonh.suikawear.business.GameEngineViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.typhonh.suikawear.data.Container
import com.typhonh.suikawear.data.fruit.Fruit
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

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun WearApp() {
    SuikaWearTheme {
        /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
         * version of LazyColumn for wear devices with some added features. For more information,
         * see d.android.com/wear/compose.
         */
        MainCanvas()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MainCanvas(
    modifier: Modifier = Modifier,
    viewModel: GameEngineViewModel = viewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val isRound = LocalConfiguration.current.isScreenRound
    val gameController = remember {
        object : GameController {
            override fun onRotateEvent(rotationPixels: Float) {
                viewModel.onRotate(rotationPixels)
            }

            override fun onDragEvent(position: Offset, size: IntSize) {
                viewModel.onDrag(position, size)
            }

            override fun onTapEvent() {
                viewModel.onTap()
            }
        }
    }

    MainCanvas(
        container = uiState.value.container,
        pendingFruit = uiState.value.pendingFruit,
        droppedFruits = uiState.value.droppedFruits,
        gameController = gameController,
        modifier = modifier.onSizeChanged {
            viewModel.onCanvasSizeChange(it, isRound)
        }
    )
}

@Composable
fun MainCanvas(
    container: Container,
    pendingFruit: Fruit,
    droppedFruits: List<Fruit>,
    gameController: GameController,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    Canvas(
        modifier = modifier.fillMaxSize(1f)
            .pointerInput(key1 = gameController) {
                detectDragGestures(
                    onDrag = { inputChange, _ ->
                        gameController.onDragEvent(inputChange.position, size)
                    },
                    onDragEnd = {
                        gameController.onTapEvent()
                    }
                )
            }
            .pointerInput(key1 = gameController) {
                detectTapGestures(
                    onTap = {
                        gameController.onTapEvent()
                    }
                )
            }
            .focusRequester(focusRequester)
            .onRotaryScrollEvent {
                gameController.onRotateEvent(it.verticalScrollPixels)
                true
            }
            .focusable()
    ) {
        draw(container)
        draw(pendingFruit)
        droppedFruits.forEach { draw(it) }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

private fun DrawScope.draw(container: Container) {
    drawRect(
        SolidColor(Color.Green),
        topLeft = Offset(((size.width - container.width * size.width) / 2) + container.posX * size.width / 2, ((size.height - container.height * size.height) / 2) + container.posY * size.height / 2),
        size = Size(container.width * size.width, container.height * size.height),
        style = Stroke(1f)
    )
}

private fun DrawScope.draw(pendingFruit: Fruit) {
    drawCircle(
        color = Color.Red,
        center = Offset(((size.width - pendingFruit.radius * 2) / 2) + pendingFruit.posX * size.width / 2, ((size.height - pendingFruit.radius * 2) / 2) + pendingFruit.posY * size.width / 2),
        radius = pendingFruit.radius * size.width / 2
    )
}