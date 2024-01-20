package com.typhonh.suikawear.presentation

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.MaterialTheme
import com.typhonh.suikawear.R
import com.typhonh.suikawear.business.GameController
import com.typhonh.suikawear.business.GameEngineViewModel
import com.typhonh.suikawear.data.Container
import com.typhonh.suikawear.data.fruit.Fruit

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun GameCanvas(
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

    GameCanvas(
        container = uiState.value.container,
        pendingFruit = uiState.value.pendingFruit,
        nextFruit = uiState.value.nextFruit,
        droppedFruits = uiState.value.droppedFruits,
        score = uiState.value.score,
        gameController = gameController,
        modifier = modifier.onSizeChanged {
            viewModel.onCanvasSizeChange(it, isRound)
        }
    )
}

@Composable
fun GameCanvas(
    container: Container,
    pendingFruit: Fruit,
    nextFruit: Fruit,
    droppedFruits: List<Fruit>,
    score: Int,
    gameController: GameController,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val images = getImages()
    val textMeasurer = rememberTextMeasurer()
    val scoreFont = MaterialTheme.typography.title1

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
            .onRotaryScrollEvent {
                gameController.onRotateEvent(it.verticalScrollPixels)
                true
            }
            .focusRequester(focusRequester)
            .focusable()
    ) {
        // Draw background
        with(images[R.drawable.background]) {
            if (this != null) {
                draw(Size(size.width, size.height))
            }
        }

        // Draw Container
        images[container.image]?.let { draw(container, it) }

        // Draw Next Fruit
        images[R.drawable.next_frame]?.let {
            images[nextFruit.image]?.let { it1 ->
                drawNextFruit(nextFruit, it1, it)
            } }

        // Draw Score
        drawScore(score, textMeasurer, scoreFont)

        // Draw Cloud
        images[R.drawable.cloud]?.let { drawGuide(pendingFruit, container, it) }

        // Draw Pending Fruit
        images[pendingFruit.image]?.let { draw(pendingFruit, it) }

        // Draw Dropped Fruits
        droppedFruits.forEach {fruit ->
            images[fruit.image]?.let {
                draw(
                    fruit,
                    it
                )
            }
        }

        // Draw Container Top
        images[container.imageTop]?.let { draw(container, it) }

    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}


@Composable
private fun getImages(): Map<Int, Painter> {
    return mapOf(
        R.drawable.cherry to painterResource(id = R.drawable.cherry),
        R.drawable.grape to painterResource(id = R.drawable.grape),
        R.drawable.strawberry to painterResource(id = R.drawable.strawberry),
        R.drawable.satsuma to painterResource(id = R.drawable.satsuma),
        R.drawable.persimmon to painterResource(id = R.drawable.persimmon),
        R.drawable.apple to painterResource(id = R.drawable.apple),
        R.drawable.pear to painterResource(id = R.drawable.pear),
        R.drawable.peach to painterResource(id = R.drawable.peach),
        R.drawable.pineapple to painterResource(id = R.drawable.pineapple),
        R.drawable.melon to painterResource(id = R.drawable.melon),
        R.drawable.watermelon to painterResource(id = R.drawable.watermelon),
        R.drawable.background to painterResource(id = R.drawable.background),
        R.drawable.container to painterResource(id = R.drawable.container),
        R.drawable.container_top to painterResource(id = R.drawable.container_top),
        R.drawable.next_frame to painterResource(id = R.drawable.next_frame),
        R.drawable.cloud to painterResource(id = R.drawable.cloud)
    )
}

private fun DrawScope.drawScore(score: Int, textMeasurer: TextMeasurer, font: TextStyle) {

    val textToDraw = score.toString()

    val style = font.copy(
        brush = Brush.verticalGradient(
            colors = listOf(Color(250, 250, 220), Color(240, 200, 30))
        ),)

    val styleOutline = style.copy(
        color = Color(150, 100, 50),
        fontSize = TextUnit(style.fontSize.value + 0.1f, TextUnitType.Sp),
        drawStyle = Stroke(
            width = style.fontSize.value / 8
        )
    )

    listOf(styleOutline, style).forEach {
        drawText(
            textMeasurer = textMeasurer,
            text = textToDraw,
            style = it
        )
    }
}

private fun DrawScope.calcCenterOffset(width: Float, height: Float, x: Float, y: Float): Offset {
    return Offset(
        (((size.width - width * 2) / 2) + x * size.width / 2 - width * size.width / 2),
        (((size.height - height * 2) / 2) + y * size.height / 2 - height * size.height / 2)
    )
}

private fun DrawScope.calcTopLeftOffset(width: Float, height: Float, x: Float, y: Float): Offset {
    return Offset(
        ((size.width - width * size.width) / 2) + x * size.width / 2,
        ((size.height - height * size.height) / 2) + y * size.height / 2
    )
}

private fun DrawScope.calcSize(width: Float, height: Float): Size {
    return Size(width * size.width, height * size.height)
}

private fun DrawScope.drawNextFruit(nextFruit: Fruit, fruitImage: Painter, frame: Painter) {
    val frameCenter = calcCenterOffset(
        Fruit.NEXT_FRAME_RADIUS,
        Fruit.NEXT_FRAME_RADIUS,
        Fruit.NEXT_X.toFloat(),
        Fruit.NEXT_Y.toFloat()
    )

    val fruitCenter = calcCenterOffset(
        nextFruit.radius.toFloat(),
        nextFruit.radius.toFloat(),
        Fruit.NEXT_X.toFloat(),
        Fruit.NEXT_Y.toFloat()
    )

    translate(frameCenter.x, frameCenter.y) {
        with(frame) {
            draw(calcSize(Fruit.NEXT_FRAME_RADIUS, Fruit.NEXT_FRAME_RADIUS))
        }
    }

    translate(fruitCenter.x, fruitCenter.y) {
        with(fruitImage) {
            draw(calcSize(nextFruit.radius.toFloat(), nextFruit.radius.toFloat()))
        }
    }

    drawIntoCanvas {
        val path = Path().apply {
            addArc(
                RectF(
                    frameCenter.x,
                    frameCenter.y,
                    frameCenter.x + Fruit.NEXT_FRAME_RADIUS * size.width,
                    frameCenter.y + Fruit.NEXT_FRAME_RADIUS * size.height
                ),
                -150f,
                115f
            )
        }
        it.nativeCanvas.drawTextOnPath(
            "Up Next",
            path,
            0f,
            -2.5f,
            Paint().apply {
                textSize = 10.sp.toPx()
                textAlign = Paint.Align.CENTER
                color = Color(250, 250, 220).hashCode()
                typeface = Typeface.DEFAULT_BOLD
            }
        )
    }
}

private fun DrawScope.drawGuide(pendingFruit: Fruit, container: Container, image: Painter) {
    val guideWidth = 0.0025f
    drawRect(
        Color.White,
        topLeft = calcTopLeftOffset(
            guideWidth,
            guideWidth,
            pendingFruit.body.position.x.toFloat(),
            (Fruit.PENDING_Y - 0.1).toFloat()
        ),
        size = calcSize(
            guideWidth,
            (container.height + container.posY - Fruit.PENDING_Y - container.imageHeight).toFloat()
        )
    )

    val topLeft = calcTopLeftOffset(
        0.02f,
        0.21f,
        pendingFruit.body.position.x.toFloat(),
        Fruit.PENDING_Y.toFloat()
    )

    translate (topLeft.x, topLeft.y) {
        with(image) {
            draw(calcSize(0.165f, 0.118f))
        }
    }
}

private fun DrawScope.draw(container: Container, image: Painter) {
    val topLeft = calcTopLeftOffset(
        container.imageWidth,
        container.imageHeight,
        container.posX,
        container.posY
    )

    translate (topLeft.x, topLeft.y) {
        with(image) {
            draw(calcSize(container.imageWidth, container.imageHeight))
        }
    }
}

private fun DrawScope.draw(fruit: Fruit, image: Painter) {
    val radius = (fruit.radius * size.width / 2).toFloat()
    val center = calcCenterOffset(
        fruit.radius.toFloat(),
        fruit.radius.toFloat(),
        fruit.body.position.x.toFloat(),
        fruit.body.position.y.toFloat()
    )

    val path = androidx.compose.ui.graphics.Path().apply {
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

    rotate(
        (fruit.body.orientation * 60 % 360).toFloat(),
        Offset(
            center.x + radius,
            center.y + radius
        )
    ) {
        clipPath(path) {
            translate(center.x, center.y) {
                with(image) {
                    draw(calcSize(fruit.radius.toFloat(), fruit.radius.toFloat()))
                }
            }
        }
    }
}