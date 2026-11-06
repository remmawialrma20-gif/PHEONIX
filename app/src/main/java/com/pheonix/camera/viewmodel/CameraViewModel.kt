package com.pheonix.camera.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** UI state shared by the capture screen. Camera hardware state is read from the bound CameraX instance. */
class CameraViewModel : ViewModel() {
    enum class CaptureMode { PHOTO, VIDEO, PRO }

    private val _mode = MutableStateFlow(CaptureMode.PHOTO)
    val mode: StateFlow<CaptureMode> = _mode.asStateFlow()

    fun setMode(mode: CaptureMode) { _mode.value = mode }
}
