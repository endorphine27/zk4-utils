package com.nuvio.app

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent

internal actual fun PointerEvent.isBackMouseButton(): Boolean = false
internal actual fun PointerEvent.isForwardMouseButton(): Boolean = false
internal actual fun Modifier.onScrollPointerEvent(action: () -> Unit): Modifier = this
