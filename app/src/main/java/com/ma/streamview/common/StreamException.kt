package com.ma.streamview.common



class StreamException(val type: Type = Type.SIMPLE, val userFriendlyMessage: String) : Throwable() {
    enum class Type {
        SIMPLE, EMPTY_SCREEN
    }
}