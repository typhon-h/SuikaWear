package com.typhonh.suikawear.presentation

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
    viewModel: GameEngineViewModel
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

    val infiniteTransition = rememberInfiniteTransition(label = "Floating")
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = -0.025f,
        targetValue = 0.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Floating"
    )

    val cloudWidth by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.005f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "Floating"
    )

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
        // Draw Container
        images[container.image]?.let { drawContainer(container, it) }

        // Draw Next Fruit
        images[R.drawable.next_frame]?.let {
            images[nextFruit.image]?.let { it1 ->
                drawNextFruit(nextFruit, it1, it, floatingOffset)
            } }

        // Draw Score
        drawScore(score, textMeasurer, scoreFont)

        // Draw Cloud
        images[R.drawable.cloud]?.let { drawGuide(pendingFruit, container, it, cloudWidth) }

        // Draw Pending Fruit
        images[pendingFruit.image]?.let { drawFruit(pendingFruit, it) }

        // Draw Dropped Fruits
        droppedFruits.forEach {fruit ->
            images[fruit.image]?.let {
                drawFruit(
                    fruit,
                    it
                )
            }
        }

        // Draw Container Top
        images[container.imageTop]?.let { drawContainer(container, it) }

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

    val textLayoutResult = textMeasurer.measure(textToDraw, style)

    listOf(styleOutline, style).forEach {
        drawText(
            textMeasurer = textMeasurer,
            text = textToDraw,
            style = it,
            topLeft = calcTopLeftOffset(
                textLayoutResult.size.width / size.width,
                textLayoutResult.size.height / size.width,
                0f,
                0.87f
            )
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

private fun DrawScope.drawNextFruit(nextFruit: Fruit, fruitImage: Painter, frame: Painter, posOffset: Float) {
    val frameCenter = calcCenterOffset(
        Fruit.NEXT_FRAME_RADIUS,
        Fruit.NEXT_FRAME_RADIUS,
        Fruit.NEXT_X,
        Fruit.NEXT_Y + posOffset
    )

    val textCenter = calcCenterOffset(
        Fruit.NEXT_FRAME_RADIUS,
        Fruit.NEXT_FRAME_RADIUS,
        Fruit.NEXT_X,
        Fruit.NEXT_Y
    )

    val fruitCenter = calcCenterOffset(
        nextFruit.radius,
        nextFruit.radius,
        Fruit.NEXT_X,
        Fruit.NEXT_Y + posOffset
    )

    translate(frameCenter.x, frameCenter.y) {
        with(frame) {
            draw(calcSize(Fruit.NEXT_FRAME_RADIUS, Fruit.NEXT_FRAME_RADIUS))
        }
    }

    translate(fruitCenter.x, fruitCenter.y) {
        with(fruitImage) {
            draw(calcSize(nextFruit.radius, nextFruit.radius))
        }
    }

    drawIntoCanvas {
        val path = Path().apply {
            addArc(
                RectF(
                    textCenter.x,
                    textCenter.y,
                    textCenter.x + Fruit.NEXT_FRAME_RADIUS * size.width,
                    textCenter.y + Fruit.NEXT_FRAME_RADIUS * size.height
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

private fun DrawScope.drawGuide(
    pendingFruit: Fruit,
    container: Container,
    image: Painter,
    widthScale: Float
) {
    val guideWidth = 0.0025f
    drawRect(
        Color.White,
        topLeft = calcTopLeftOffset(
            guideWidth,
            guideWidth,
            pendingFruit.position().x,
            Fruit.PENDING_Y - 0.1f
        ),
        size = calcSize(
            guideWidth,
            container.height + container.posY - Fruit.PENDING_Y - container.imageHeight
        )
    )

    val topLeft = calcTopLeftOffset(
        0.02f,
        0.21f,
        pendingFruit.position().x,
        Fruit.PENDING_Y
    )

    translate (topLeft.x, topLeft.y) {
        with(image) {
            draw(calcSize(0.165f + widthScale, 0.118f))
        }
    }
}

private fun DrawScope.drawContainer(container: Container, image: Painter) {
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

private fun DrawScope.drawFruit(fruit: Fruit, image: Painter) {
    val radius = fruit.radius * size.width / 2
    val center = calcCenterOffset(
        fruit.radius,
        fruit.radius,
        fruit.position().x,
        fruit.position().y
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
        ((fruit.orientation() * Math.PI * radius).toFloat()),
        Offset(
            center.x + radius,
            center.y + radius
        )
    ) {
        clipPath(path) {
            translate(center.x, center.y) {
                with(image) {
                    draw(calcSize(fruit.radius, fruit.radius))
                }
            }
        }
    }
}