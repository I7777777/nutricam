//  FoodAnalysis.swift
//  NutriCam
//  食物分析数据模型

import Foundation

/// AI识别后的食物分析结果
struct FoodAnalysis: Identifiable, Codable {
    let id = UUID()
    let foodName: String           // 食物名称
    let calories: Double           // 热量 (千卡/100g)
    let protein: Double            // 蛋白质 (g/100g)
    let carbs: Double              // 碳水化合物 (g/100g)
    let fat: Double                // 脂肪 (g/100g)
    let fiber: Double              // 膳食纤维 (g/100g)
    let confidence: Double         // AI置信度 0-1
    let nutritionAdvice: String?   // 营养建议
    let alternatives: [String]?    // 相似食物推荐
    
    enum CodingKeys: String, CodingKey {
        case foodName, calories, protein, carbs, fat, fiber, confidence, nutritionAdvice, alternatives
    }
}

/// 用户记录的食物条目
struct FoodEntry: Identifiable, Codable {
    let id = UUID()
    let foodName: String
    let calories: Double
    let protein: Double
    let carbs: Double
    let fat: Double
    let fiber: Double
    let timestamp: Date
    let imageData: Data?           // 缩略图数据
    let mealType: MealType         // 餐别
    
    enum CodingKeys: String, CodingKey {
        case id, foodName, calories, protein, carbs, fat, fiber, timestamp, imageData, mealType
    }
}

enum MealType: String, Codable, CaseIterable {
    case breakfast = "早餐"
    case lunch = "午餐"
    case dinner = "晚餐"
    case snack = "加餐"
}

/// 宏量营养素目标
struct MacroGoals: Codable {
    var calories: Double   // 目标热量
    var protein: Double    // 目标蛋白质 (g)
    var carbs: Double      // 目标碳水 (g)
    var fat: Double        // 目标脂肪 (g)
    var fiber: Double      // 目标纤维 (g)
    
    static let `default` = MacroGoals(
        calories: 2000,
        protein: 60,
        carbs: 250,
        fat: 70,
        fiber: 25
    )
}

/// 身体统计数据
struct BodyStats: Codable {
    var gender: Gender
    var age: Int
    var height: Double     // cm
    var weight: Double     // kg
    var activityLevel: ActivityLevel
    
    var bmi: Double? {
        guard height > 0 else { return nil }
        return weight / pow(height / 100, 2)
    }
    
    /// 根据身体数据计算基础代谢率 (BMR) - Mifflin-St Jeor公式
    var bmr: Double {
        let weightComponent = 10 * weight
        let heightComponent = 6.25 * height
        let ageComponent = 5 * Double(age)
        
        if gender == .male {
            return weightComponent + heightComponent - ageComponent + 5
        } else {
            return weightComponent + heightComponent - ageComponent - 161
        }
    }
    
    /// 每日总能量消耗 (TDEE)
    var tdee: Double {
        return bmr * activityLevel.multiplier
    }
}

enum Gender: String, Codable, CaseIterable {
    case male = "男"
    case female = "女"
    
    var description: String { rawValue }
}

enum ActivityLevel: String, Codable, CaseIterable {
    case sedentary = "久坐不动"           // 办公室工作，很少运动
    case light = "轻度活动"               // 每周运动1-3次
    case moderate = "中度活动"            // 每周运动3-5次
    case active = "高度活动"              // 每周运动6-7次
    case veryActive = "专业运动"          // 每天高强度运动
    
    var description: String { rawValue }
    
    var multiplier: Double {
        switch self {
        case .sedentary: return 1.2
        case .light: return 1.375
        case .moderate: return 1.55
        case .active: return 1.725
        case .veryActive: return 1.9
        }
    }
}

enum GoalType: String, Codable, CaseIterable {
    case loseWeight = "减脂"
    case maintain = "保持"
    case gainWeight = "增肌"
    case buildMuscle = "增肌塑形"
    
    var description: String { rawValue }
    
    /// 热量调整系数
    var calorieMultiplier: Double {
        switch self {
        case .loseWeight: return 0.85      // 减少15%
        case .maintain: return 1.0
        case .gainWeight: return 1.15      // 增加15%
        case .buildMuscle: return 1.1      // 增加10%
        }
    }
}