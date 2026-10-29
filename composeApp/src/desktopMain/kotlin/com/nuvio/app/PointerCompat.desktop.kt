package com.nuvio.app

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent

internal actual fun PointerEvent.isBackMouseButton(): Boolean = button == PointerButton.Back
internal actual fun PointerEvent.isForwardMouseButton(): Boolean = button == PointerButton.Forward

@OptIn(ExperimentalComposeUiApi::class)
internal actual fun Modifier.onScrollPointerEvent(action: () -> Unit): Modifier =
    onPointerEvent(PointerEventType.Scroll) { action() }
