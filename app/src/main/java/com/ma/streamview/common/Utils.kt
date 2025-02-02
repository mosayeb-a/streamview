package com.ma.streamview.common

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit




@SuppressLint("DefaultLocale")
fun Long.formatMinSec(): String {
    return if (this == 0L) {
        "..."
    } else {
        String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(this),
            TimeUnit.MILLISECONDS.toSeconds(this) -
                    TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(this)
                    )
        )
    }
}

@SuppressLint("SimpleDateFormat")
fun convertLongToTime(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("HH:mm:ss")
    return format.format(date)
}

fun findMostRepeatedValue(values: List<String>): String? {
    val nameCounts = mutableMapOf<String, Int>()
    for (name in values.takeLast(10)) {
        if (nameCounts.containsKey(name)) {
            nameCounts[name] = nameCounts[name]!! + 1
        } else {
            nameCounts[name] = 1
        }
    }
    return nameCounts.maxByOrNull { it.value }?.key
}

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.animatedScaleOnTouch(onClick: () -> Unit): Modifier = composed {
    val selected = remember { mutableStateOf(false) }
    val scale = animateFloatAsState(if (selected.value) .88f else 1f, label = "scale_animation")
    val scope = rememberCoroutineScope()
    this
        .scale(scale.value)
        .pointerInteropFilter {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    selected.value = true
                }

                MotionEvent.ACTION_UP -> {
                    scope.launch {
                        selected.value = false
                        delay(100)
                        if(it.action ==MotionEvent.ACTION_UP ){
                        onClick()
                        }

                    }
                }

                MotionEvent.ACTION_CANCEL -> {
                    selected.value = false
                }
            }
            true
        }
}

suspend fun SnackbarHostState.showSnackbar(
    message: String,
    actionLabel: String?,
    onActionClick: () -> Unit = {}
) {
    val result = this@showSnackbar.showSnackbar(
        message = message,
        actionLabel = actionLabel,
        duration = SnackbarDuration.Indefinite
    )

    when (result) {
        SnackbarResult.ActionPerformed -> {
            onActionClick.invoke()
            println("Action button clicked")
        }

        SnackbarResult.Dismissed -> {
            println("Snackbar dismissed")
        }
    }
}

class StreamRipple(
    val color: Color,
    val alpha: RippleAlpha = RippleAlpha(
        0.34f,
        0.34f,
        0.46f,
        0.22f
    )
)

fun formatRelativeDate(dateString: String): String {
    val parsedDate = Instant.parse(dateString).toLocalDateTime(TimeZone.UTC).date
    println(parsedDate)
    val today = Clock.System.now().toLocalDateTime(TimeZone.UTC).date
    println(today)
    val daysDifference = today.daysUntil(parsedDate)
    println(daysDifference)


    return when (daysDifference) {
        0 -> "today"
        -1 -> "yesterday"
        else -> "${-daysDifference} days ago"
    }
}

@Composable
fun <T> ObserveAsEvents(
    flow: Flow<T>,
    key1: Any? = null,
    key2: Any? = null,
    onEvent: (T) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner.lifecycle, key1, key2, flow) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                flow.collect(onEvent)
            }
        }
    }
}
