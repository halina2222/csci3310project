package com.example.login.takeuserface

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.AspectRatio
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as ComposeSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors
import java.io.File

// Data class to store detected face information for the overlay
data class DetectedFace(
    val boundingBox: Rect,
    val id: Int? = null
)

@Composable
fun CameraScreen(paddingValues: PaddingValues) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Face detection state
    var faceDetected by remember { mutableStateOf(false) }
    
    // Store the detected faces for drawing the overlay
    var detectedFaces by remember { mutableStateOf<List<DetectedFace>>(emptyList()) }
    
    // For camera preview dimensions
    var previewWidth by remember { mutableStateOf(0) }
    var previewHeight by remember { mutableStateOf(0) }
    
    // For storing the ImageCapture instance with better quality settings
    val imageCaptureUseCase = remember { 
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetAspectRatio(AspectRatio.RATIO_4_3) // Using setTargetAspectRatio instead of ResolutionSelector
            .build() 
    }
    
    // Check required permissions
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // For storage permission if needed (API < 29)
    var hasStoragePermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || 
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Permission launchers
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> 
        hasCameraPermission = isGranted
    }
    
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> 
        hasStoragePermission = isGranted
    }
    
    // Check and request storage permission if needed
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !hasStoragePermission) {
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 50.dp)
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        // Text element
        Text(
            text = "Take a photo of your face",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Camera preview box with face detection status
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(if (faceDetected) Color(0xFFE0F7E0) else Color.LightGray) // Green tint when face detected
                .border(2.dp, if (faceDetected) Color(0xFF4CAF50) else Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                // Show camera preview with face detection
                CameraPreviewWithFaceDetection(
                    modifier = Modifier
                        .fillMaxWidth(),
                    lifecycleOwner = lifecycleOwner,
                    onFaceDetected = { faces, width, height -> 
                        faceDetected = faces.isNotEmpty()
                        detectedFaces = faces.map { face -> 
                            DetectedFace(face.boundingBox, face.trackingId)
                        }
                        // Update the preview dimensions for accurate overlay scaling
                        previewWidth = width
                        previewHeight = height
                    },
                    imageCaptureUseCase = imageCaptureUseCase
                )
                
                // Face overlay that draws boxes around detected faces
                if (detectedFaces.isNotEmpty() && previewWidth > 0 && previewHeight > 0) {
                    FaceOverlay(
                        faces = detectedFaces,
                        previewWidth = previewWidth,
                        previewHeight = previewHeight,
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(10f) // Make sure overlay is on top
                    )
                }
            } else {
                // Show placeholder and request permission message
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Camera permission is required")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Request permission")
                    }
                }
            }
            
            // Status text for face detection
            if (hasCameraPermission) {
                Text(
                    text = if (faceDetected) "Face detected!" else "No face detected",
                    color = if (faceDetected) Color(0xFF4CAF50) else Color.Red,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .background(Color(0x88000000))
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Two buttons in a row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { /* TODO: Add functionality for the first button */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = { 
                    if (faceDetected) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !hasStoragePermission) {
                            Toast.makeText(
                                context,
                                "Storage permission is required to save photos",
                                Toast.LENGTH_SHORT
                            ).show()
                            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        } else {
                            // Log that we're about to capture
                            Log.d("CameraCapture", "Attempting to capture photo with face detected")
                            capturePhoto(context, imageCaptureUseCase)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                enabled = hasCameraPermission && faceDetected
            ) {
                Text("Take Photo", color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun FaceOverlay(
    faces: List<DetectedFace>,
    previewWidth: Int,
    previewHeight: Int,
    modifier: Modifier = Modifier
) {
    // Draw boxes around detected faces
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Calculate scale factors between the preview and the canvas
        val scaleX = canvasWidth / previewWidth
        val scaleY = canvasHeight / previewHeight
        
        faces.forEach { face ->
            // Adding padding to make the rectangle larger (20% of face dimensions)
            val paddingX = face.boundingBox.width() * 0.2f
            val paddingY = face.boundingBox.height() * 0.2f
            
            // Scale the face bounding box to match the canvas size and add padding
            val left = (face.boundingBox.left - paddingX) * scaleX
            val top = (face.boundingBox.top - paddingY) * scaleY
            val right = (face.boundingBox.right + paddingX) * scaleX
            val bottom = (face.boundingBox.bottom + paddingY) * scaleY
            
            // Draw a red rectangle around the face with thicker stroke
            drawRect(
                color = Color.Red,
                topLeft = Offset(left, top),
                size = ComposeSize(right - left, bottom - top),
                style = Stroke(width = 4f) // Increased stroke width from 4f to 8f
            )
        }
    }
}

@Composable
private fun CameraPreviewWithFaceDetection(
    modifier: Modifier = Modifier,
    lifecycleOwner: LifecycleOwner,
    onFaceDetected: (List<Face>, Int, Int) -> Unit, // Added width and height parameters
    imageCaptureUseCase: ImageCapture
) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Store the PreviewView reference for getting dimensions
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    
    // Setup ML Kit face detector with improved options for accuracy
    val faceDetectorOptions = remember {
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()
    }
    
    val faceDetector = remember { FaceDetection.getClient(faceDetectorOptions) }
    
    // Image analysis use case with compatible configuration
    val imageAnalysis = remember {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetAspectRatio(AspectRatio.RATIO_4_3) // Using setTargetAspectRatio instead
            // Alternatively, use setTargetResolution for a specific size
            // .setTargetResolution(Size(640, 480))
            .build()
    }
    
    // Set the analyzer separately
    DisposableEffect(imageAnalysis) {
        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            // Pass the preview dimensions along with the detected faces
            processImageForFaceDetection(
                imageProxy, 
                faceDetector, 
                { faces -> 
                    val width = previewView?.width ?: 0
                    val height = previewView?.height ?: 0
                    onFaceDetected(faces, width, height)
                }
            )
        }
        
        onDispose {
            imageAnalysis.clearAnalyzer()
            cameraExecutor.shutdown()
        }
    }
    
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val view = PreviewView(ctx)
            previewView = view
            
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(view.surfaceProvider)
                }
                
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis,
                        imageCaptureUseCase // Add image capture use case
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", e)
                }
            }, executor)
            
            view
        },
        update = { view ->
            // Store the preview view reference and dimensions
            previewView = view
        }
    )
}

@OptIn(ExperimentalGetImage::class)
private fun processImageForFaceDetection(
    imageProxy: ImageProxy,
    faceDetector: FaceDetector,
    onFaceDetected: (List<Face>) -> Unit
) {
    // Add validation to ensure image is sufficient quality for processing
    if (imageProxy.width < 480 || imageProxy.height < 360) {
        Log.w("FaceDetection", "Image too small for reliable face detection: ${imageProxy.width}x${imageProxy.height}")
        onFaceDetected(emptyList())
        imageProxy.close()
        return
    }
    
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        Log.e("FaceDetection", "Image proxy had no image")
        imageProxy.close()
        onFaceDetected(emptyList())
        return
    }
    
    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
    
    // Create InputImage with proper rotation
    val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
    
    // Track detection latency for performance monitoring
    val startTime = System.currentTimeMillis()
    
    faceDetector.process(image)
        .addOnSuccessListener { faces ->
            val detectionTime = System.currentTimeMillis() - startTime
            Log.d("FaceDetection", "Detection completed in ${detectionTime}ms, found ${faces.size} faces")
            
            // Only report faces that are large enough for accurate detection
            val validFaces = faces.filter { face ->
                val faceWidth = face.boundingBox.width()
                val faceHeight = face.boundingBox.height()
                
                // Ensure face is at least 100x100 pixels (or 200x200 if we were doing contour detection)
                val isLargeEnough = faceWidth >= 100 && faceHeight >= 100
                
                if (!isLargeEnough) {
                    Log.d("FaceDetection", "Found face too small for accurate detection: ${faceWidth}x${faceHeight}")
                }
                
                isLargeEnough
            }
            
            onFaceDetected(validFaces)
        }
        .addOnFailureListener { e ->
            Log.e("FaceDetection", "Face detection failed", e)
            onFaceDetected(emptyList())
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

private fun capturePhoto(context: Context, imageCapture: ImageCapture) {
    // Log the capture attempt
    Log.d("CameraCapture", "Starting photo capture process")
    
    try {
        // Create time-stamped output file to hold the image
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val photoFileName = "FACE_$timeStamp.jpg"
        
        // Create output options object
        val outputOptions: ImageCapture.OutputFileOptions
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10 and above, use MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, photoFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FaceAuth")
            }
            
            Log.d("CameraCapture", "Creating MediaStore output options")
            outputOptions = ImageCapture.OutputFileOptions.Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ).build()
        } else {
            // For older versions, use the external media directory
            val storageDir = context.getExternalFilesDir("Pictures/FaceAuth")
            if (storageDir == null) {
                // Fallback to internal storage
                Log.d("CameraCapture", "External storage not available, using internal storage")
                val internalDir = File(context.filesDir, "Pictures/FaceAuth").apply {
                    if (!exists()) mkdirs()
                }
                val photoFile = File(internalDir, photoFileName)
                outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            } else {
                storageDir.mkdirs() // Ensure directory exists
                val photoFile = File(storageDir, photoFileName)
                Log.d("CameraCapture", "Using external storage file: ${photoFile.absolutePath}")
                outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            }
        }
        
        // Capture the image
        Log.d("CameraCapture", "Taking picture...")
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri
                    val msg = if (savedUri != null) {
                        "Photo saved successfully: $savedUri"
                    } else {
                        "Photo saved to file"
                    }
                    Log.d("CameraCapture", msg)
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
                
                override fun onError(exception: ImageCaptureException) {
                    val errorMsg = "Photo capture failed: ${exception.message}"
                    Log.e("CameraCapture", errorMsg, exception)
                    Log.e("CameraCapture", "Error code: ${exception.imageCaptureError}")
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        )
    } catch (e: Exception) {
        Log.e("CameraCapture", "Setup failed: ${e.message}", e)
        Toast.makeText(context, "Failed to setup photo capture: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
