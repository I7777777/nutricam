//  FoodRecognitionService.swift
//  NutriCam
//  食物识别服务 - 调用AI API进行食物识别

import Foundation
import UIKit

/// 食物识别服务
class FoodRecognitionService {
    static let shared = FoodRecognitionService()
    
    // API配置 - 这里可以使用多个后端
    private let apiEndpoint = "https://api.nutricam.example/v1/analyze"
    private let apiKey = "YOUR_API_KEY_HERE"
    
    private init() {}
    
    // MARK: - Public Methods
    
    /// 分析食物图片
    func analyze(image: UIImage, completion: @escaping (FoodAnalysis?) -> Void) {
        // 压缩图片
        guard let compressedData = compressImage(image) else {
            completion(nil)
            return
        }
        
        // 这里提供两种实现方式
        #if DEBUG
        // 开发模式：使用模拟数据
        simulateAnalysis(completion: completion)
        #else
        // 生产模式：调用真实API
        callVisionAPI(imageData: compressedData, completion: completion)
        #endif
    }
    
    /// 批量分析多张图片
    func analyzeBatch(images: [UIImage], completion: @escaping ([FoodAnalysis]) -> Void) {
        let group = DispatchGroup()
        var results: [FoodAnalysis] = []
        
        for image in images {
            group.enter()
            analyze(image: image) { result in
                if let result = result {
                    results.append(result)
                }
                group.leave()
            }
        }
        
        group.notify(queue: .main) {
            completion(results)
        }
    }
    
    // MARK: - Private Methods
    
    /// 压缩图片
    private func compressImage(_ image: UIImage, maxSize: CGFloat = 1024) -> Data? {
        let scale = min(maxSize / image.size.width, maxSize / image.size.height, 1.0)
        let newSize = CGSize(width: image.size.width * scale, height: image.size.height * scale)
        
        UIGraphicsBeginImageContextWithOptions(newSize, false, 0.0)
        image.draw(in: CGRect(origin: .zero, size: newSize))
        let resizedImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return resizedImage?.jpegData(compressionQuality: 0.8)
    }
    
    /// 调用Vision API (OpenAI/Google Vision/自研API)
    private func callVisionAPI(imageData: Data, completion: @escaping (FoodAnalysis?) -> Void) {
        // 构建请求
        var request = URLRequest(url: URL(string: apiEndpoint)!)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
        
        // Base64编码图片
        let base64Image = imageData.base64EncodedString()
        
        // 请求体
        let requestBody: [String: Any] = [
            "image": base64Image,
            "language": "zh-CN",
            "include_nutrition": true,
            "include_alternatives": true
        ]
        
        request.httpBody = try? JSONSerialization.data(withJSONObject: requestBody)
        
        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                print("API请求失败: \(error)")
                completion(nil)
                return
            }
            
            guard let data = data else {
                completion(nil)
                return
            }
            
            do {
                let decoder = JSONDecoder()
                let result = try decoder.decode(FoodAnalysis.self, from: data)
                completion(result)
            } catch {
                print("解析响应失败: \(error)")
                completion(nil)
            }
        }
        
        task.resume()
    }
    
    /// 模拟分析 (开发测试用)
    private func simulateAnalysis(completion: @escaping (FoodAnalysis?) -> Void) {
        // 模拟网络延迟
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            let mockFoods = [
                FoodAnalysis(
                    foodName: "宫保鸡丁",
                    calories: 180,
                    protein: 15.0,
                    carbs: 12.0,
                    fat: 8.0,
                    fiber: 2.0,
                    confidence: 0.92,
                    nutritionAdvice: "蛋白质含量丰富，适合健身人群。注意控制米饭摄入量。",
                    alternatives: ["麻婆豆腐", "鱼香肉丝", "青椒炒肉"]
                ),
                FoodAnalysis(
                    foodName: "白米饭",
                    calories: 130,
                    protein: 2.7,
                    carbs: 28.0,
                    fat: 0.3,
                    fiber: 0.4,
                    confidence: 0.98,
                    nutritionAdvice: "优质碳水来源，减脂期间建议控制在一拳大小。",
                    alternatives: ["糙米饭", "杂粮饭", "红薯"]
                ),
                FoodAnalysis(
                    foodName: "番茄炒蛋",
                    calories: 95,
                    protein: 6.0,
                    carbs: 5.0,
                    fat: 6.0,
                    fiber: 1.0,
                    confidence: 0.89,
                    nutritionAdvice: "西红柿富含番茄红素，鸡蛋提供优质蛋白，营养搭配均衡。",
                    alternatives: nil
                ),
                FoodAnalysis(
                    foodName: "牛油果沙拉",
                    calories: 160,
                    protein: 2.0,
                    carbs: 9.0,
                    fat: 15.0,
                    fiber: 7.0,
                    confidence: 0.85,
                    nutritionAdvice: "牛油果富含健康脂肪，但热量较高，注意适量食用。",
                    alternatives: ["蔬菜沙拉","水果沙拉","凯撒沙拉"]
                )
            ]
            
            completion(mockFoods.randomElement())
        }
    }
}

// MARK: - 本地食物数据库 (离线识别)

/// 本地食物数据库 - 用于离线查询
class LocalFoodDatabase {
    static let shared = LocalFoodDatabase()
    
    private var foodDatabase: [String: FoodAnalysis] = [:]
    
    private init() {
        loadDatabase()
    }
    
    /// 根据关键词搜索食物
    func searchFood(keyword: String) -> [FoodAnalysis] {
        return foodDatabase.values.filter {
            $0.foodName.contains(keyword)
        }
    }
    
    /// 获取常见食物列表
    func getCommonFoods() -> [FoodAnalysis] {
        return Array(foodDatabase.values.prefix(50))
    }
    
    private func loadDatabase() {
        // 加载内置食物数据
        // 实际项目中可以从JSON文件或CoreData加载
        let commonFoods = [
            "米饭": FoodAnalysis(
                foodName: "米饭", calories: 116, protein: 2.6, carbs: 25.9, fat: 0.3, fiber: 0.3,
                confidence: 1.0, nutritionAdvice: nil, alternatives: nil
            ),
            "面条": FoodAnalysis(
                foodName: "面条", calories: 137, protein: 4.5, carbs: 25.0, fat: 2.1, fiber: 1.8,
                confidence: 1.0, nutritionAdvice: nil, alternatives: nil
            ),
            "鸡蛋": FoodAnalysis(
                foodName: "鸡蛋", calories: 155, protein: 13.0, carbs: 1.1, fat: 11.0, fiber: 0,
                confidence: 1.0, nutritionAdvice: nil, alternatives: nil
            ),
            "鸡胸肉": FoodAnalysis(
                foodName: "鸡胸肉", calories: 165, protein: 31.0, carbs: 0, fat: 3.6, fiber: 0,
                confidence: 1.0, nutritionAdvice: nil, alternatives: nil
            ),
            "西兰花": FoodAnalysis(
                foodName: "西兰花", calories: 34, protein: 2.8, carbs: 7.0, fat: 0.4, fiber: 2.6,
                confidence: 1.0, nutritionAdvice: nil, alternatives: nil
            ),
            "苹果": FoodAnalysis(
                foodName: "苹果", calories: 52, protein: 0.3, carbs: 14.0, fat: 0.2, fiber: 2.4,
                confidence: 1.0, nutritionAdvice: nil, alternatives: nil
            ),
            "香蕉": FoodAnalysis(
                foodName: "香蕉", calories: 89, protein: 1.1, carbs: 22.8, fat: 0.3, fiber: 2.6,
                confidence: 1.0, nutritionAdvice: nil, alternatives: nil
            ),
            "三文鱼": FoodAnalysis(
                foodName: "三文鱼", calories: 208, protein: 20.0, carbs: 0, fat: 13.0, fiber: 0,
                confidence: 1.0, nutritionAdvice: nil, alternatives: nil
            ),
            "燕麦": FoodAnalysis(
                foodName: "燕麦", calories: 389, protein: 16.9, carbs: 66.0, fat: 6.9, fiber: 10.6,
                confidence: 1.0, nutritionAdvice: nil, alternatives: nil
            ),
            "牛奶": FoodAnalysis(
                foodName: "牛奶", calories: 42, protein: 3.4, carbs: 5.0, fat: 1.0, fiber: 0,
                confidence: 1.0, nutritionAdvice: nil, alternatives: nil
            )
        ]
        
        for (name, food) in commonFoods {
            foodDatabase[name] = food
        }
    }
}