package com.ma.streamview.common.components.material

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp


class Padding {
    val extraLarge = 36.dp
    val large = 24.dp
    val medium = 16.dp
    val small = 8.dp
    val extraSmall = 6.dp
}

val MaterialTheme.padding: Padding
    get() = Padding()
