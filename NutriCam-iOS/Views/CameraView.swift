//  CameraView.swift
//  NutriCam
//  实时相机识别界面

import SwiftUI
import AVFoundation
import Vision

struct CameraView: View {
    @StateObject private var cameraManager = CameraManager()
    @State private var showImagePicker = false
    @State private var selectedImage: UIImage?
    @State private var analysisResult: FoodAnalysis?
    @State private var isAnalyzing = false
    @State private var showResult = false
    
    var body: some View {
        ZStack {
            // 相机预览层
            CameraPreviewView(session: cameraManager.session)
                .ignoresSafeArea()
            
            // 取景框UI
            VStack {
                // 顶部信息栏
                HStack {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("对准食物")
                            .font(.headline)
                            .foregroundColor(.white)
                        Text("自动识别热量与营养")
                            .font(.caption)
                            .foregroundColor(.white.opacity(0.8))
                    }
                    Spacer()
                    
                    // 闪光灯按钮
                    Button(action: { cameraManager.toggleTorch() }) {
                        Image(systemName: cameraManager.isTorchOn ? "bolt.fill" : "bolt.slash.fill")
                            .font(.title2)
                            .foregroundColor(.white)
                    }
                }
                .padding()
                .background(.ultraThinMaterial)
                
                Spacer()
                
                // 识别框动画
                if cameraManager.isDetecting {
                    ScanningFrame()
                        .frame(width: 280, height: 280)
                } else {
                    RoundedRectangle(cornerRadius: 20)
                        .stroke(Color.white.opacity(0.6), lineWidth: 2)
                        .frame(width: 280, height: 280)
                }
                
                Spacer()
                
                // 底部控制栏
                HStack(spacing: 40) {
                    // 相册选择
                    Button(action: { showImagePicker = true }) {
                        VStack {
                            Image(systemName: "photo.on.rectangle")
                                .font(.title2)
                            Text("相册")
                                .font(.caption)
                        }
                        .foregroundColor(.white)
                    }
                    
                    // 拍照按钮
                    Button(action: capturePhoto) {
                        ZStack {
                            Circle()
                                .fill(Color.white)
                                .frame(width: 72, height: 72)
                            Circle()
                                .stroke(Color.white, lineWidth: 4)
                                .frame(width: 80, height: 80)
                        }
                    }
                    
                    // 切换摄像头
                    Button(action: { cameraManager.switchCamera() }) {
                        VStack {
                            Image(systemName: "camera.rotate")
                                .font(.title2)
                            Text("切换")
                                .font(.caption)
                        }
                        .foregroundColor(.white)
                    }
                }
                .padding(.bottom, 30)
                .background(.ultraThinMaterial)
            }
            
            // 识别结果浮层
            if showResult, let result = analysisResult {
                ResultOverlay(result: result, isPresented: $showResult)
                    .transition(.move(edge: .bottom))
            }
            
            // 加载指示器
            if isAnalyzing {
                LoadingView(message: "正在分析食物...")
            }
        }
        .sheet(isPresented: $showImagePicker) {
            ImagePicker(image: $selectedImage)
        }
        .onChange(of: selectedImage) { _ in
            if selectedImage != nil {
                analyzeSelectedImage()
            }
        }
        .onAppear {
            cameraManager.checkPermissions()
        }
    }
    
    private func capturePhoto() {
        cameraManager.capturePhoto { image in
            if let image = image {
                analyzeImage(image)
            }
        }
    }
    
    private func analyzeSelectedImage() {
        guard let image = selectedImage else { return }
        analyzeImage(image)
    }
    
    private func analyzeImage(_ image: UIImage) {
        isAnalyzing = true
        
        // 调用AI识别服务
        FoodRecognitionService.shared.analyze(image: image) { result in
            DispatchQueue.main.async {
                isAnalyzing = false
                analysisResult = result
                showResult = true
            }
        }
    }
}

// 扫描动画框
struct ScanningFrame: View {
    @State private var isAnimating = false
    
    var body: some View {
        RoundedRectangle(cornerRadius: 20)
            .stroke(Color.green, lineWidth: 3)
            .frame(width: 280, height: 280)
            .overlay(
                VStack {
                    Rectangle()
                        .fill(Color.green)
                        .frame(height: 2)
                        .offset(y: isAnimating ? 140 : -140)
                        .animation(
                            Animation.linear(duration: 2)
                                .repeatForever(autoreverses: true),
                            value: isAnimating
                        )
                    Spacer()
                }
            )
            .onAppear { isAnimating = true }
    }
}

// 结果浮层
struct ResultOverlay: View {
    let result: FoodAnalysis
    @Binding var isPresented: Bool
    @EnvironmentObject var userProfile: UserProfile
    
    var body: some View {
        VStack(spacing: 0) {
            // 拖动指示器
            RoundedRectangle(cornerRadius: 2.5)
                .fill(Color.gray.opacity(0.5))
                .frame(width: 36, height: 5)
                .padding(.top, 8)
            
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // 食物名称和热量
                    HStack {
                        VStack(alignment: .leading) {
                            Text(result.foodName)
                                .font(.title2)
                                .fontWeight(.bold)
                            Text("置信度: \(Int(result.confidence * 100))%")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                        Spacer()
                        VStack(alignment: .trailing) {
                            Text("\(result.calories)")
                                .font(.title)
                                .fontWeight(.bold)
                                .foregroundColor(.orange)
                            Text("千卡")
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    
                    Divider()
                    
                    // 营养成分
                    Text("营养成分 (每100g)")
                        .font(.headline)
                    
                    NutritionBar(label: "蛋白质", value: result.protein, total: 30, color: .blue)
                    NutritionBar(label: "碳水", value: result.carbs, total: 100, color: .green)
                    NutritionBar(label: "脂肪", value: result.fat, total: 50, color: .red)
                    NutritionBar(label: "纤维", value: result.fiber, total: 25, color: .orange)
                    
                    Divider()
                    
                    // 营养建议
                    if let advice = result.nutritionAdvice {
                        Text("💡 \(advice)")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                    
                    // 今日摄入占比
                    if let percentage = userProfile.getCaloriePercentage(result.calories) {
                        HStack {
                            Text("占今日目标")
                                .font(.subheadline)
                            Spacer()
                            Text("\(Int(percentage * 100))%")
                                .font(.subheadline)
                                .fontWeight(.semibold)
                                .foregroundColor(percentage > 0.5 ? .orange : .green)
                        }
                        
                        ProgressView(value: min(percentage, 1.0))
                            .tint(percentage > 1.0 ? .red : .green)
                    }
                    
                    // 添加到今日记录按钮
                    Button(action: addToDailyLog) {
                        HStack {
                            Image(systemName: "plus.circle.fill")
                            Text("添加到今日记录")
                        }
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.green)
                        .cornerRadius(12)
                    }
                    .padding(.top, 10)
                }
                .padding()
            }
        }
        .background(Color(.systemBackground))
        .cornerRadius(20, corners: [.topLeft, .topRight])
        .shadow(radius: 10)
    }
    
    private func addToDailyLog() {
        userProfile.addFoodEntry(result)
        isPresented = false
    }
}

// 营养进度条
struct NutritionBar: View {
    let label: String
    let value: Double
    let total: Double
    let color: Color
    
    var percentage: Double {
        min(value / total, 1.0)
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(label)
                    .font(.caption)
                    .foregroundColor(.secondary)
                Spacer()
                Text("\(String(format: "%.1f", value))g")
                    .font(.caption)
                    .fontWeight(.medium)
            }
            GeometryReader { geometry in
                ZStack(alignment: .leading) {
                    Rectangle()
                        .fill(Color.gray.opacity(0.2))
                        .cornerRadius(4)
                    
                    Rectangle()
                        .fill(color)
                        .frame(width: geometry.size.width * percentage)
                        .cornerRadius(4)
                }
            }
            .frame(height: 8)
        }
    }
}