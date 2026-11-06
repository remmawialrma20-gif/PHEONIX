package com.pheonix.camera.data

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Size
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.CameraInfo

data class CameraCapabilities(
    val raw: Boolean = false,
    val maxPhotoSize: Size? = null,
    val videoSizes: List<Size> = emptyList(),
    val hasFlash: Boolean = false,
    val maxZoom: Float = 1f,
    val minZoom: Float = 1f,
    val supportsManualSensor: Boolean = false
) {
    companion object {
        fun read(context: Context, cameraInfo: CameraInfo): CameraCapabilities {
            val camera2 = Camera2CameraInfo.from(cameraInfo)
            val characteristics = camera2.getCameraCharacteristics()
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val photoSizes = map?.getOutputSizes(android.graphics.ImageFormat.JPEG).orEmpty()
            val videoSizes = map?.getOutputSizes(android.media.MediaRecorder::class.java).orEmpty()
                .sortedByDescending { it.width.toLong() * it.height }
            val hardware = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            return CameraCapabilities(
                raw = map?.outputFormats?.contains(android.graphics.ImageFormat.RAW_SENSOR) == true,
                maxPhotoSize = photoSizes.maxByOrNull { it.width.toLong() * it.height },
                videoSizes = videoSizes,
                hasFlash = cameraInfo.hasFlashUnit(),
                maxZoom = cameraInfo.zoomState.value?.maxZoomRatio ?: 1f,
                minZoom = cameraInfo.zoomState.value?.minZoomRatio ?: 1f,
                supportsManualSensor = hardware == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL ||
                    hardware == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3
            )
        }
    }
}
