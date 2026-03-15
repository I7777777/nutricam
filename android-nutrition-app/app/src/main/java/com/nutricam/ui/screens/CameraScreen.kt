package com.nutricam.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.nutricam.model.FoodAnalysis
import com.nutricam.ui.theme.*
import com.nutricam.viewmodel.NutritionViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: NutritionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    val analysisResult by viewModel.analysisResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var showResult by remember { mutableStateOf(false) }
    
    LaunchedEffect(analysisResult) {
        showResult = analysisResult != null
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        when {
            cameraPermissionState.status.isGranted -> {
                CameraContent(
                    viewModel = viewModel,
                    onImageCaptured = { bitmap ->
                        viewModel.analyzeImage(bitmap)
                    }
                )
            }
            else -> {
                PermissionRequest(
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                )
            }
        }
        
        // 加载指示器
        if (isLoading) {
            LoadingOverlay()
        }
        
        // 结果浮层
        AnimatedVisibility(
            visible = showResult,
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            analysisResult?.let { result ->
                ResultBottomSheet(
                    result = result,
                    viewModel = viewModel,
                    onDismiss = {
                        showResult = false
                        viewModel.clearAnalysisResult()
                    }
                )
            }
        }
        
        // 错误提示
        errorMessage?.let { message ->
            ErrorSnackbar(
                message = message,
                onDismiss = viewModel::clearError
            )
        }
    }
}

@Composable
private fun CameraContent(
    viewModel: NutritionViewModel,
    onImageCaptured: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var isTorchOn by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }
    
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // 图片选择器
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = loadBitmapFromUri(context, it)
            bitmap?.let(onImageCaptured)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 相机预览
        CameraPreview(
            cameraSelector = cameraSelector,
            onImageCaptureReady = { imageCapture = it },
            lifecycleOwner = lifecycleOwner
        )
        
        // 扫描框
        ScanningFrame(
            isScanning = isScanning,
            modifier = Modifier.align(Alignment.Center)
        )
        
        // 顶部控制栏
        TopBar(
            isTorchOn = isTorchOn,
            onTorchToggle = { 
                isTorchOn = !isTorchOn
                // 控制闪光灯
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )
        
        // 底部控制栏
        BottomControls(
            onGalleryClick = { galleryLauncher.launch("image/*") },
            onCaptureClick = {
                isScanning = true
                captureImage(imageCapture, cameraExecutor, context) { bitmap ->
                    isScanning = false
                    onImageCaptured(bitmap)
                }
            },
            onSwitchCamera = {
                cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

@Composable
private fun CameraPreview(
    cameraSelector: CameraSelector,
    onImageCaptureReady: (ImageCapture) -> Unit,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner
) {
    val context = LocalContext.current
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                val preview = Preview.Builder()
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }
                
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                
                onImageCaptureReady(imageCapture)
                
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))
            
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun ScanningFrame(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanOffset by infiniteTransition.animateFloat(
        initialValue = -140f,
        targetValue = 140f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan"
    )
    
    Box(
        modifier = modifier
            .size(280.dp)
            .border(
                width = 3.dp,
                color = if (isScanning) GreenPrimary else Color.White.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        if (isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(GreenPrimary)
                    .offset(y = scanOffset.dp)
            )
        }
    }
}

@Composable
private fun TopBar(
    isTorchOn: Boolean,
    onTorchToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = 0.5f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "对准食物",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "自动识别热量与营养",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            
            IconButton(onClick = onTorchToggle) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "闪光灯",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomControls(
    onGalleryClick: () -> Unit,
    onCaptureClick: () -> Unit,
    onSwitchCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = 0.5f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 30.dp, horizontal = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 相册按钮
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = onGalleryClick) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "相册",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text("相册", color = Color.White, style = MaterialTheme.typography.bodySmall)
            }
            
            // 拍照按钮
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(4.dp, Color.White, CircleShape)
                    .clickable(onClick = onCaptureClick),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
            
            // 切换摄像头按钮
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(onClick = onSwitchCamera) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "切换",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text("切换", color = Color.White, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ResultBottomSheet(
    result: FoodAnalysis,
    viewModel: NutritionViewModel,
    onDismiss: () -> Unit
) {
    val todayStats by viewModel.todayStats.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    
    val goalCalories = userProfile.macroGoals.calories
    val percentage = if (goalCalories > 0) result.calories / goalCalories else 0.0
    
    Surface(
        color = Surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.fillMaxHeight(0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 拖动指示器
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(5.dp)
                    .background(Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(2.5.dp))
                    .align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 食物名称和热量
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        result.foodName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "置信度: ${(result.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${result.calories.toInt()}",
                        style = MaterialTheme.typography.displayMedium,
                        color = CalorieColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "千卡",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // 营养成分
            Text(
                "营养成分 (每100g)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            NutritionBar(label = "蛋白质", value = result.protein, total = 30.0, color = ProteinColor)
            NutritionBar(label = "碳水", value = result.carbs, total = 100.0, color = CarbsColor)
            NutritionBar(label = "脂肪", value = result.fat, total = 50.0, color = FatColor)
            NutritionBar(label = "纤维", value = result.fiber, total = 25.0, color = FiberColor)
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            // 营养建议
            result.nutritionAdvice?.let { advice ->
                Text(
                    "💡 $advice",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // 今日摄入占比
            if (goalCalories > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("占今日目标", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${(percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (percentage > 0.5) Warning else Success
                    )
                }
                
                LinearProgressIndicator(
                    progress = percentage.toFloat().coerceIn(0f, 1f),
                    color = if (percentage > 1.0) Error else Success,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 添加按钮
            Button(
                onClick = {
                    viewModel.addToDailyLog(result)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加到今日记录", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun NutritionBar(label: String, value: Double, total: Double, color: Color) {
    val percentage = (value / total).toFloat().coerceIn(0f, 1f)
    
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text("%.1fg".format(value), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        }
        
        LinearProgressIndicator(
            progress = percentage,
            color = color,
            trackColor = Color.Gray.copy(alpha = 0.2f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("正在分析食物...", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun PermissionRequest(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = GreenPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "需要相机权限",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "NutriCam 需要使用相机来识别食物热量",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRequestPermission) {
                Text("授予权限")
            }
        }
    }
}

@Composable
private fun ErrorSnackbar(message: String, onDismiss: () -> Unit) {
    LaunchedEffect(message) {
        kotlinx.coroutines.delay(3000)
        onDismiss()
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Snackbar(
            modifier = Modifier.align(Alignment.BottomCenter),
            action = {
                TextButton(onClick = onDismiss) {
                    Text("确定")
                }
            }
        ) {
            Text(message)
        }
    }
}

// 辅助函数
private fun captureImage(
    imageCapture: ImageCapture?,
    executor: ExecutorService,
    context: android.content.Context,
    onImageCaptured: (Bitmap) -> Unit
) {
    val photoFile = File(
        context.cacheDir,
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
            .format(System.currentTimeMillis()) + ".jpg"
    )
    
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    
    imageCapture?.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                bitmap?.let(onImageCaptured)
            }
            
            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
            }
        }
    )
}

private fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}