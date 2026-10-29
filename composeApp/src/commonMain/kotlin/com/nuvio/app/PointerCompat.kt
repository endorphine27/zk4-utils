package com.nuvio.app

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent

internal expect fun PointerEvent.isBackMouseButton(): Boolean
internal expect fun PointerEvent.isForwardMouseButton(): Boolean
internal expect fun Modifier.onScrollPointerEvent(action: () -> Unit): Modifier
