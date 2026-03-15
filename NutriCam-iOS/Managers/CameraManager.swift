//  CameraManager.swift
//  NutriCam
//  相机管理器 - 处理AVFoundation相机操作

import Foundation
import AVFoundation
import UIKit

class CameraManager: NSObject, ObservableObject {
    @Published var isTorchOn = false
    @Published var isDetecting = false
    
    let session = AVCaptureSession()
    private var videoDeviceInput: AVCaptureDeviceInput?
    private let photoOutput = AVCapturePhotoOutput()
    private var photoCaptureCompletion: ((UIImage?) -> Void)?
    
    private let sessionQueue = DispatchQueue(label: "session queue")
    
    override init() {
        super.init()
        configureSession()
    }
    
    // MARK: - Configuration
    
    private func configureSession() {
        sessionQueue.async { [weak self] in
            guard let self = self else { return }
            
            self.session.beginConfiguration()
            self.session.sessionPreset = .high
            
            // 添加视频输入
            do {
                guard let videoDevice = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back) else {
                    print("无法获取后置摄像头")
                    self.session.commitConfiguration()
                    return
                }
                
                let videoDeviceInput = try AVCaptureDeviceInput(device: videoDevice)
                
                if self.session.canAddInput(videoDeviceInput) {
                    self.session.addInput(videoDeviceInput)
                    self.videoDeviceInput = videoDeviceInput
                }
            } catch {
                print("配置视频输入失败: \(error)")
            }
            
            // 添加照片输出
            if self.session.canAddOutput(self.photoOutput) {
                self.session.addOutput(self.photoOutput)
                self.photoOutput.isHighResolutionCaptureEnabled = true
            }
            
            self.session.commitConfiguration()
        }
    }
    
    // MARK: - Permissions
    
    func checkPermissions() {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            startSession()
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
                if granted {
                    self?.startSession()
                }
            }
        case .denied, .restricted:
            print("相机权限被拒绝")
        @unknown default:
            break
        }
    }
    
    // MARK: - Session Control
    
    func startSession() {
        sessionQueue.async { [weak self] in
            guard let self = self, !self.session.isRunning else { return }
            self.session.startRunning()
        }
    }
    
    func stopSession() {
        sessionQueue.async { [weak self] in
            guard let self = self, self.session.isRunning else { return }
            self.session.stopRunning()
        }
    }
    
    // MARK: - Camera Features
    
    func toggleTorch() {
        guard let device = videoDeviceInput?.device else { return }
        
        do {
            try device.lockForConfiguration()
            
            if device.hasTorch {
                if device.torchMode == .on {
                    device.torchMode = .off
                    isTorchOn = false
                } else {
                    try device.setTorchModeOn(level: 1.0)
                    isTorchOn = true
                }
            }
            
            device.unlockForConfiguration()
        } catch {
            print("切换闪光灯失败: \(error)")
        }
    }
    
    func switchCamera() {
        guard let currentInput = videoDeviceInput else { return }
        
        sessionQueue.async { [weak self] in
            guard let self = self else { return }
            
            let currentPosition = currentInput.device.position
            let newPosition: AVCaptureDevice.Position = currentPosition == .back ? .front : .back
            
            guard let newDevice = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: newPosition) else { return }
            
            do {
                let newInput = try AVCaptureDeviceInput(device: newDevice)
                
                self.session.beginConfiguration()
                self.session.removeInput(currentInput)
                
                if self.session.canAddInput(newInput) {
                    self.session.addInput(newInput)
                    self.videoDeviceInput = newInput
                } else {
                    self.session.addInput(currentInput)
                }
                
                self.session.commitConfiguration()
            } catch {
                print("切换摄像头失败: \(error)")
            }
        }
    }
    
    // MARK: - Photo Capture
    
    func capturePhoto(completion: @escaping (UIImage?) -> Void) {
        photoCaptureCompletion = completion
        
        let photoSettings = AVCapturePhotoSettings()
        photoSettings.isHighResolutionPhotoEnabled = true
        
        photoOutput.capturePhoto(with: photoSettings, delegate: self)
    }
    
    // MARK: - Real-time Detection
    
    func startRealTimeDetection() {
        isDetecting = true
        // 这里可以集成Vision框架进行实时食物检测
    }
    
    func stopRealTimeDetection() {
        isDetecting = false
    }
}

// MARK: - AVCapturePhotoCaptureDelegate

extension CameraManager: AVCapturePhotoCaptureDelegate {
    func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
        if let error = error {
            print("照片处理失败: \(error)")
            photoCaptureCompletion?(nil)
            return
        }
        
        guard let imageData = photo.fileDataRepresentation(),
              let image = UIImage(data: imageData) else {
            photoCaptureCompletion?(nil)
            return
        }
        
        photoCaptureCompletion?(image)
    }
}

// MARK: - Camera Preview View

import SwiftUI

struct CameraPreviewView: UIViewRepresentable {
    let session: AVCaptureSession
    
    func makeUIView(context: Context) -> VideoPreviewView {
        let view = VideoPreviewView()
        view.videoPreviewLayer.session = session
        return view
    }
    
    func updateUIView(_ uiView: VideoPreviewView, context: Context) {}
}

class VideoPreviewView: UIView {
    override class var layerClass: AnyClass {
        return AVCaptureVideoPreviewLayer.self
    }
    
    var videoPreviewLayer: AVCaptureVideoPreviewLayer {
        return layer as! AVCaptureVideoPreviewLayer
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        videoPreviewLayer.frame = bounds
        videoPreviewLayer.videoGravity = .resizeAspectFill
    }
}