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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typhonh.suikawear.business.GameController
import com.typhonh.suikawear.business.GameEngineViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.typhonh.suikawear.R
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
        nextFruit = uiState.value.nextFruit,
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
    nextFruit: Fruit,
    droppedFruits: List<Fruit>,
    gameController: GameController,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    val images = remember {
        mutableStateOf(mutableMapOf<Int, Painter>())
    }
    images.value[R.drawable.cherry] = painterResource(id = R.drawable.cherry)
    images.value[R.drawable.grape] = painterResource(id = R.drawable.grape)
    images.value[R.drawable.strawberry] = painterResource(id = R.drawable.strawberry)
    images.value[R.drawable.satsuma] = painterResource(id = R.drawable.satsuma)
    images.value[R.drawable.persimmon] = painterResource(id = R.drawable.persimmon)
    images.value[R.drawable.apple] = painterResource(id = R.drawable.apple)
    images.value[R.drawable.pear] = painterResource(id = R.drawable.pear)
    images.value[R.drawable.peach] = painterResource(id = R.drawable.peach)
    images.value[R.drawable.pineapple] = painterResource(id = R.drawable.pineapple)
    images.value[R.drawable.melon] = painterResource(id = R.drawable.melon)
    images.value[R.drawable.watermelon] = painterResource(id = R.drawable.watermelon)
    images.value[R.drawable.background] = painterResource(id = R.drawable.background)
    images.value[R.drawable.container] = painterResource(id = R.drawable.container)
    images.value[R.drawable.container_top] = painterResource(id = R.drawable.container_top)
    images.value[R.drawable.next_frame] = painterResource(id = R.drawable.next_frame)

    Canvas(
        modifier = modifier
            .fillMaxSize(1f)
            .pointerInput(key1 = gameController) {
                detectDragGestures(
                    onDrag = { inputChange, _ ->
                        gameController.onDragEvent(inputChange.position, size)
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
            with(images.value[R.drawable.background]) {
                if (this != null) {
                    draw(Size(size.width, size.height))
                }
            }

        images.value[container.image]?.let { draw(container, it) }

        images.value[R.drawable.next_frame]?.let {
            images.value[nextFruit.image]?.let { it1 ->
            drawNextFruit(nextFruit, it1, it)
        } }

        drawGuide(pendingFruit, container)

        images.value[pendingFruit.image]?.let { draw(pendingFruit, it) }
        droppedFruits.forEach {fruit ->
            images.value[fruit.image]?.let {
                draw(
                    fruit,
                    it
                )
            }
        }
        images.value[container.imageTop]?.let { draw(container, it) }

    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

private fun DrawScope.drawNextFruit(nextFruit: Fruit, fruitImage:Painter, frame: Painter) {
    val frameCenter = Offset(
        (((size.width - Fruit.NEXT_FRAME_RADIUS * 2) / 2) + Fruit.NEXT_X * size.width / 2 - Fruit.NEXT_FRAME_RADIUS * size.width / 2).toFloat(),
        (((size.height - Fruit.NEXT_FRAME_RADIUS * 2) / 2) + Fruit.NEXT_Y * size.height / 2 - Fruit.NEXT_FRAME_RADIUS * size.width / 2).toFloat()
    )
    val fruitCenter = Offset(
        (((size.width - nextFruit.radius * 2) / 2) + Fruit.NEXT_X * size.width / 2 - nextFruit.radius * size.width / 2).toFloat(),
        (((size.height - nextFruit.radius * 2) / 2) + Fruit.NEXT_Y * size.height / 2 - nextFruit.radius * size.width / 2).toFloat()
    )
    translate(frameCenter.x, frameCenter.y) {
        with(frame) {
                draw(Size(Fruit.NEXT_FRAME_RADIUS * size.width, Fruit.NEXT_FRAME_RADIUS * size.height))
        }
    }

    translate(fruitCenter.x, fruitCenter.y) {
        with(fruitImage) {
            draw(Size((nextFruit.radius * size.width).toFloat(), (nextFruit.radius * size.height).toFloat()))
        }
    }
}

private fun DrawScope.drawGuide(pendingFruit: Fruit, container: Container) {
    val guideWidth = 0.0025f
    drawRect( //TODO: simplify
        Color.White,
        topLeft = Offset(
            (((size.width - guideWidth * 2) / 2) + pendingFruit.body.position.x * size.width / 2 - guideWidth * size.width / 2).toFloat(),
            (((size.height - guideWidth * 2) / 2) + pendingFruit.body.position.y * size.height / 2 - guideWidth * size.width / 2).toFloat()
        ),
        size = Size(guideWidth * size.width,
            (container.height * size.height) + ((size.height - container.height * size.height) / 2)
                    + container.posY * size.height / 2
                    - (((size.height - guideWidth * 2) / 2) + pendingFruit.body.position.y * size.height / 2 - guideWidth * size.width / 2).toFloat()
        )
    )
}

private fun DrawScope.draw(container: Container, image: Painter) {
    val topLeft = Offset(
        ((size.width - container.imageWidth * size.width) / 2) + container.posX * size.width / 2,
        ((size.height - container.imageHeight * size.height) / 2) + container.posY * size.height / 2
    )

    translate (topLeft.x, topLeft.y) {
        with(image) {
            draw(Size(container.imageWidth * size.width, container.imageHeight * size.height))
        }
    }
}

private fun DrawScope.draw(fruit: Fruit, image: Painter) {
    val radius = (fruit.radius * size.width / 2).toFloat()
    val center = Offset(
        (((size.width - fruit.radius * 2) / 2) + fruit.body.position.x * size.width / 2 - radius).toFloat(),
        (((size.height - fruit.radius * 2) / 2) + fruit.body.position.y * size.height / 2 - radius).toFloat()
    )


    val path = Path().apply {
        addOval(
            Rect(
                Offset(
                    center.x + radius,
                    center.y + radius
                ),
                radius
            )
        )
    }

    //TODO: Fix the rotation (using friction??)
    rotate((fruit.body.orientation * 50 % 360).toFloat(), Offset(
        center.x + radius,
        center.y + radius
    )) {
    clipPath(path) {
        translate(center.x, center.y) {
            with(image) {
                draw(
                    Size(
                        (fruit.radius * size.width).toFloat(),
                        (fruit.radius * size.height).toFloat()
                    )
                )
            }
        }
        }
    }
}