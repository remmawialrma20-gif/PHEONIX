package com.pheonix.camera.ui

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.pheonix.camera.data.CameraCapabilities
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor

@Composable
fun CameraApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var permissionGranted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionRequest = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted = it }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var capabilities by remember { mutableStateOf<CameraCapabilities?>(null) }
    var frontCamera by remember { mutableStateOf(false) }
    var flashEnabled by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Ready") }
    var zoom by remember { mutableFloatStateOf(1f) }
    val executor: Executor = remember(context) { ContextCompat.getMainExecutor(context) }

    LaunchedEffect(Unit) {
        if (!permissionGranted) permissionRequest.launch(Manifest.permission.CAMERA)
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (!permissionGranted) {
            Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera access is needed to show the viewfinder.", color = Color.White)
                Spacer(Modifier.height(12.dp))
                androidx.compose.material3.Button(onClick = { permissionRequest.launch(Manifest.permission.CAMERA) }) {
                    Text("Allow camera")
                }
            }
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize().pointerInput(camera) {
                    detectTapGestures(onTap = { point ->
                        val previewView = camera?.cameraInfo // Tap-to-focus is enabled via CameraX metering below when a view is present.
                        @Suppress("UNUSED_VARIABLE") val ignored = previewView
                    })
                },
                factory = { viewContext ->
                    PreviewView(viewContext).apply {
                        implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                        val providerFuture = ProcessCameraProvider.getInstance(viewContext)
                        providerFuture.addListener({
                            try {
                                val provider = providerFuture.get()
                                val selector = if (frontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA
                                val preview = Preview.Builder().build().also { it.surfaceProvider = surfaceProvider }
                                val capture = ImageCapture.Builder()
                                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                    .build()
                                provider.unbindAll()
                                val bound = provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
                                camera = bound
                                imageCapture = capture
                                capabilities = CameraCapabilities.read(viewContext, bound.cameraInfo)
                                bound.cameraControl.setZoomRatio(zoom)
                            } catch (error: Exception) {
                                status = "Camera unavailable: ${error.localizedMessage ?: "unknown error"}"
                            }
                        }, executor)
                    }
                },
                update = { view ->
                    val detector = ScaleGestureDetector(view.context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                        override fun onScale(detector: ScaleGestureDetector): Boolean {
                            val state = camera?.cameraInfo?.zoomState?.value ?: return false
                            zoom = (state.zoomRatio * detector.scaleFactor).coerceIn(state.minZoomRatio, state.maxZoomRatio)
                            camera?.cameraControl?.setZoomRatio(zoom)
                            return true
                        }
                    })
                    view.setOnTouchListener { _, event ->
                        detector.onTouchEvent(event)
                        if (event.action == MotionEvent.ACTION_UP) view.performClick()
                        true
                    }
                }
            )

            Column(
                Modifier.fillMaxSize().background(Color.Transparent).padding(horizontal = 20.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("LRMA CAMERA", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text("${"%.1f".format(zoom)}×  •  ${status}", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                    }
                    Row {
                        if (capabilities?.hasFlash == true && !frontCamera) {
                            IconButton(onClick = {
                                flashEnabled = !flashEnabled
                                imageCapture?.flashMode = if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
                            }) {
                                Icon(if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff, "Toggle flash", tint = Color.White)
                            }
                        }
                        IconButton(onClick = { frontCamera = !frontCamera }) {
                            Icon(Icons.Default.Cameraswitch, "Switch camera", tint = Color.White)
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        capabilities?.let { cap ->
                            val size = cap.maxPhotoSize
                            buildString {
                                append(if (cap.raw) "RAW capable" else "JPEG capture")
                                size?.let { append("  •  ${it.width}×${it.height}") }
                            }
                        } ?: "Detecting camera capabilities…",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(Modifier.height(18.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        if (capabilities == null) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        Spacer(Modifier.size(24.dp))
                        IconButton(
                            onClick = {
                                val capture = imageCapture ?: return@IconButton
                                val name = "LRMA_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.jpg"
                                val values = ContentValues().apply {
                                    put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/LRMA CAMERA")
                                }
                                val output = ImageCapture.OutputFileOptions.Builder(
                                    context.contentResolver,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    values
                                ).build()
                                capture.takePicture(output, executor, object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(result: ImageCapture.OutputFileResults) { status = "Photo saved" }
                                    override fun onError(exception: ImageCaptureException) { status = "Capture failed: ${exception.message ?: "try again"}" }
                                })
                            },
                            modifier = Modifier.size(72.dp).background(Color.White, CircleShape)
                        ) {
                            Icon(Icons.Default.PhotoCamera, "Take photo", tint = Color.Black, modifier = Modifier.size(30.dp))
                        }
                        Spacer(Modifier.size(48.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf(1f, 2f, 5f).filter { it <= (capabilities?.maxZoom ?: 1f) }.forEach { factor ->
                            androidx.compose.material3.TextButton(onClick = {
                                zoom = factor
                                camera?.cameraControl?.setZoomRatio(factor)
                            }) { Text("${factor.toInt()}×", color = Color.White) }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        onDispose { camera = null; imageCapture = null }
    }
}
