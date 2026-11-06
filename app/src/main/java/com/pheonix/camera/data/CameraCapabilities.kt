package com.pheonix.camera.data

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
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
    val supportsManualSensor: Boolean = false,
    val supports8K: Boolean = false,
    val supports120Fps: Boolean = false,
    val supports240Fps: Boolean = false,
    val lensFacing: Int? = null
) {
    companion object {
        fun read(context: Context, cameraInfo: CameraInfo): CameraCapabilities {
            val cameraId = Camera2CameraInfo.from(cameraInfo).cameraId
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val characteristics = manager.getCameraCharacteristics(cameraId)
            val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val photoSizes = map?.getOutputSizes(android.graphics.ImageFormat.JPEG).orEmpty()
            val videoSizes = map?.getOutputSizes(MediaRecorder::class.java).orEmpty()
                .sortedByDescending { it.width.toLong() * it.height }
            val highSpeedRanges = map?.highSpeedVideoFpsRanges?.toList().orEmpty()
            val hardware = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            return CameraCapabilities(
                raw = map?.outputFormats?.contains(android.graphics.ImageFormat.RAW_SENSOR) == true,
                maxPhotoSize = photoSizes.maxByOrNull { it.width.toLong() * it.height },
                videoSizes = videoSizes,
                hasFlash = cameraInfo.hasFlashUnit(),
                maxZoom = cameraInfo.zoomState.value?.maxZoomRatio ?: 1f,
                minZoom = cameraInfo.zoomState.value?.minZoomRatio ?: 1f,
                supportsManualSensor = hardware == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL ||
                    hardware == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3,
                supports8K = videoSizes.any { it.width >= 7680 && it.height >= 4320 },
                supports120Fps = highSpeedRanges.any { it.upper >= 120 },
                supports240Fps = highSpeedRanges.any { it.upper >= 240 },
                lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            )
        }
    }
}
