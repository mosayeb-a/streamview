package com.ma.streamview.common.components.material.slider

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp


@Composable
fun DefaultTrack(
    modifier: Modifier,
    progress: Float,
    interactionSource: MutableInteractionSource,
    tickFractions: List<Float>,
    enabled: Boolean,
    colorTrack: Color = MaterialTheme.colorScheme.onSecondary.copy(alpha = .5f),
    colorProgress: Color = MaterialTheme.colorScheme.onSecondary,
    colorTickTrack: Color = colorProgress,
    colorTickProgress: Color = colorTrack,
    height: Dp = 4.dp
) {
    Canvas(
        Modifier
            .then(modifier)
            .height(height)
    ) {

        val isRtl = layoutDirection == LayoutDirection.Rtl
        val sliderLeft = Offset(0f, center.y)
        val sliderRight = Offset(size.width, center.y)
        val sliderStart = if (isRtl) sliderRight else sliderLeft
        val sliderEnd = if (isRtl) sliderLeft else sliderRight

        drawLine(
            colorTrack,
            sliderStart,
            sliderEnd,
            size.height,
            StrokeCap.Round,
            alpha = if (enabled) 1f else ALPHA_WHEN_DISABLED
        )
        val sliderValueEnd = Offset(
            sliderStart.x + (sliderEnd.x - sliderStart.x) * progress,
            center.y
        )

        val sliderValueStart = Offset(
            sliderStart.x,
            center.y
        )
        drawLine(
            colorProgress,
            sliderValueStart,
            sliderValueEnd,
            size.height,
            StrokeCap.Round,
            alpha = if (enabled) 1f else ALPHA_WHEN_DISABLED
        )

        if (tickFractions.isNotEmpty()) {
            tickFractions.groupBy { it > progress }.forEach { (afterFraction, list) ->
                drawPoints(
                    list.map {
                        Offset(lerp(sliderStart, sliderEnd, it).x, center.y)
                    },
                    PointMode.Points,
                    if (afterFraction) colorTickTrack else colorTickProgress,
                    size.height,
                    StrokeCap.Round,
                    alpha = if (enabled) 1f else ALPHA_WHEN_DISABLED
                )
            }
        }
    }
}

@Composable
fun MutableInteractionSource.ListenOnPressed(onPressChange: (Boolean) -> Unit) {
    val interactionSource = this

    val onPressChangeState = rememberUpdatedState(onPressChange)

    val interactions = remember { mutableStateListOf<Interaction>() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> interactions.add(interaction)
                is PressInteraction.Release -> interactions.remove(interaction.press)
                is PressInteraction.Cancel -> interactions.remove(interaction.press)
                is DragInteraction.Start -> interactions.add(interaction)
                is DragInteraction.Stop -> interactions.remove(interaction.start)
                is DragInteraction.Cancel -> interactions.remove(interaction.start)
            }
        }
    }
    onPressChangeState.value(interactions.isNotEmpty())
}

/**
 * @param scaleOnPress - if more than 1f uses animation of scale otherwise no animation on press
 */
@Composable
fun DefaultThumb(
    modifier: Modifier = Modifier,
    offset: Dp,
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    thumbSize: DpSize,
    color: Color = MaterialTheme.colorScheme.onSecondary,
    scaleOnPress: Float = 1.4f,
//    animationSpec: AnimationSpec<Float> = SpringSpec(0.3f)
) {

    var isPressed by remember { mutableStateOf(false) }

    if (scaleOnPress > 1f) {
        interactionSource.ListenOnPressed { isPressed = it }
    }

    val scale: Float by animateFloatAsState(
        if (isPressed) scaleOnPress else 1f,
        animationSpec = TweenSpec(durationMillis = 0)
    )

    Spacer(
        modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .background(
                if (enabled) color else
                    color.copy(alpha = ALPHA_WHEN_DISABLED), CircleShape
            )
    )
}


internal val DEFAULT_THUMB_SIZE = DpSize(18.dp, 18.dp)
private const val ALPHA_WHEN_DISABLED = 0.6f