//  UserProfile.swift
//  NutriCam
//  用户档案管理 - ObservableObject

import Foundation
import Combine

class UserProfile: ObservableObject {
    // MARK: - Published Properties
    @Published var name: String = "用户"
    @Published var goalType: GoalType = .maintain
    @Published var bodyStats: BodyStats = BodyStats(
        gender: .male,
        age: 25,
        height: 170,
        weight: 65,
        activityLevel: .moderate
    )
    @Published var macroGoals: MacroGoals = .default
    @Published var foodEntries: [FoodEntry] = []
    
    // MARK: - Computed Properties
    
    /// 今日已摄入热量
    var todayCalories: Double {
        todayEntries.reduce(0) { $0 + $1.calories }
    }
    
    /// 今日已摄入蛋白质
    var todayProtein: Double {
        todayEntries.reduce(0) { $0 + $1.protein }
    }
    
    /// 今日已摄入碳水
    var todayCarbs: Double {
        todayEntries.reduce(0) { $0 + $1.carbs }
    }
    
    /// 今日已摄入脂肪
    var todayFat: Double {
        todayEntries.reduce(0) { $0 + $1.fat }
    }
    
    /// 今日记录
    var todayEntries: [FoodEntry] {
        let calendar = Calendar.current
        let today = calendar.startOfDay(for: Date())
        return foodEntries.filter {
            calendar.isDate($0.timestamp, inSameDayAs: today)
        }.sorted { $0.timestamp > $1.timestamp }
    }
    
    /// 每日热量目标
    var dailyCalorieGoal: Double {
        let baseGoal = bodyStats.tdee * goalType.calorieMultiplier
        return baseGoal
    }
    
    /// 剩余可摄入热量
    var remainingCalories: Double {
        dailyCalorieGoal - todayCalories
    }
    
    /// 健康建议
    var healthTips: [String] {
        var tips: [String] = []
        
        // 基于目标类型的建议
        switch goalType {
        case .loseWeight:
            tips.append("减脂期间建议每餐七分饱，细嚼慢咽")
            tips.append("优先选择高蛋白、低脂肪的食物")
            tips.append("多喝水，每天至少2000ml")
        case .maintain:
            tips.append("保持当前饮食习惯，注意营养均衡")
            tips.append("定期运动有助于维持健康体重")
        case .gainWeight:
            tips.append("增重期间可以适当增加优质碳水的摄入")
            tips.append("少食多餐，每天5-6餐有助于增加热量摄入")
        case .buildMuscle:
            tips.append("增肌期每公斤体重建议摄入1.6-2.2g蛋白质")
            tips.append("训练后30分钟内补充蛋白质效果更佳")
        }
        
        // 基于当前摄入的建议
        if todayProtein < macroGoals.protein * 0.5 {
            tips.append("今天蛋白质摄入偏少，晚餐可以多吃些瘦肉或豆制品")
        }
        
        if todayCalories > dailyCalorieGoal {
            tips.append("今日热量已超标，晚餐建议清淡一些")
        }
        
        return tips
    }
    
    // MARK: - Initialization
    
    init() {
        loadFromStorage()
        calculateGoals()
    }
    
    // MARK: - Methods
    
    /// 添加食物记录
    func addFoodEntry(_ analysis: FoodAnalysis, image: UIImage? = nil) {
        let imageData = image?.jpegData(compressionQuality: 0.5)
        
        let entry = FoodEntry(
            foodName: analysis.foodName,
            calories: analysis.calories,
            protein: analysis.protein,
            carbs: analysis.carbs,
            fat: analysis.fat,
            fiber: analysis.fiber,
            timestamp: Date(),
            imageData: imageData,
            mealType: currentMealType()
        )
        
        foodEntries.append(entry)
        saveToStorage()
    }
    
    /// 删除食物记录
    func deleteEntry(_ entry: FoodEntry) {
        foodEntries.removeAll { $0.id == entry.id }
        saveToStorage()
    }
    
    /// 获取热量占比
    func getCaloriePercentage(_ calories: Double) -> Double? {
        guard dailyCalorieGoal > 0 else { return nil }
        return calories / dailyCalorieGoal
    }
    
    /// 更新身体数据并重新计算目标
    func updateBodyStats(_ stats: BodyStats) {
        self.bodyStats = stats
        calculateGoals()
        saveToStorage()
    }
    
    /// 更新目标类型
    func updateGoalType(_ type: GoalType) {
        self.goalType = type
        calculateGoals()
        saveToStorage()
    }
    
    /// 更新营养目标
    func updateMacroGoals(_ goals: MacroGoals) {
        self.macroGoals = goals
        saveToStorage()
    }
    
    // MARK: - Private Methods
    
    /// 根据当前时间判断餐别
    private func currentMealType() -> MealType {
        let hour = Calendar.current.component(.hour, from: Date())
        switch hour {
        case 5..<11: return .breakfast
        case 11..<14: return .lunch
        case 14..<17: return .snack
        case 17..<21: return .dinner
        default: return .snack
        }
    }
    
    /// 根据身体数据自动计算营养目标
    private func calculateGoals() {
        let tdee = bodyStats.tdee
        let targetCalories = tdee * goalType.calorieMultiplier
        
        // 宏量营养素分配
        // 蛋白质: 1.6-2.2g/kg (根据目标调整)
        let proteinMultiplier: Double
        switch goalType {
        case .buildMuscle: proteinMultiplier = 2.0
        case .loseWeight: proteinMultiplier = 1.8
        default: proteinMultiplier = 1.6
        }
        let proteinGrams = bodyStats.weight * proteinMultiplier
        let proteinCalories = proteinGrams * 4
        
        // 脂肪: 25-30% 总热量
        let fatCalories = targetCalories * 0.27
        let fatGrams = fatCalories / 9
        
        // 碳水: 剩余热量
        let carbCalories = targetCalories - proteinCalories - fatCalories
        let carbGrams = carbCalories / 4
        
        macroGoals = MacroGoals(
            calories: targetCalories,
            protein: proteinGrams,
            carbs: max(carbGrams, 100),    // 最低100g碳水
            fat: fatGrams,
            fiber: 25 + (bodyStats.calories > 2500 ? 10 : 0)  // 根据热量调整纤维
        )
    }
    
    // MARK: - Persistence
    
    private func saveToStorage() {
        let defaults = UserDefaults.standard
        
        // 保存用户基本信息
        defaults.set(name, forKey: "userName")
        defaults.set(goalType.rawValue, forKey: "goalType")
        
        // 保存身体数据
        if let bodyData = try? JSONEncoder().encode(bodyStats) {
            defaults.set(bodyData, forKey: "bodyStats")
        }
        
        // 保存营养目标
        if let goalsData = try? JSONEncoder().encode(macroGoals) {
            defaults.set(goalsData, forKey: "macroGoals")
        }
        
        // 保存食物记录
        if let entriesData = try? JSONEncoder().encode(foodEntries) {
            defaults.set(entriesData, forKey: "foodEntries")
        }
    }
    
    private func loadFromStorage() {
        let defaults = UserDefaults.standard
        
        // 加载基本信息
        if let name = defaults.string(forKey: "userName") {
            self.name = name
        }
        
        if let goalRaw = defaults.string(forKey: "goalType"),
           let goal = GoalType(rawValue: goalRaw) {
            self.goalType = goal
        }
        
        // 加载身体数据
        if let bodyData = defaults.data(forKey: "bodyStats"),
           let stats = try? JSONDecoder().decode(BodyStats.self, from: bodyData) {
            self.bodyStats = stats
        }
        
        // 加载营养目标
        if let goalsData = defaults.data(forKey: "macroGoals"),
           let goals = try? JSONDecoder().decode(MacroGoals.self, from: goalsData) {
            self.macroGoals = goals
        }
        
        // 加载食物记录
        if let entriesData = defaults.data(forKey: "foodEntries"),
           let entries = try? JSONDecoder().decode([FoodEntry].self, from: entriesData) {
            self.foodEntries = entries
        }
    }
}