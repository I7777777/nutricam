package com.nutricam.model

import java.util.Date
import java.util.UUID

/**
 * AI识别后的食物分析结果
 */
data class FoodAnalysis(
    val id: String = UUID.randomUUID().toString(),
    val foodName: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val confidence: Double,
    val nutritionAdvice: String? = null,
    val alternatives: List<String>? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 用户记录的食物条目
 */
data class FoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val foodName: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val timestamp: Date = Date(),
    val imageUri: String? = null,
    val mealType: MealType = MealType.SNACK
)

enum class MealType(val displayName: String) {
    BREAKFAST("早餐"),
    LUNCH("午餐"),
    DINNER("晚餐"),
    SNACK("加餐")
}

/**
 * 宏量营养素目标
 */
data class MacroGoals(
    val calories: Double = 2000.0,
    val protein: Double = 60.0,
    val carbs: Double = 250.0,
    val fat: Double = 70.0,
    val fiber: Double = 25.0
)

/**
 * 身体统计数据
 */
data class BodyStats(
    val gender: Gender = Gender.MALE,
    val age: Int = 25,
    val height: Double = 170.0,
    val weight: Double = 65.0,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE
) {
    val bmi: Double?
        get() = if (height > 0) weight / ((height / 100) * (height / 100)) else null
    
    /**
     * 基础代谢率 (BMR) - Mifflin-St Jeor公式
     */
    val bmr: Double
        get() {
            val weightComponent = 10 * weight
            val heightComponent = 6.25 * height
            val ageComponent = 5 * age
            
            return if (gender == Gender.MALE) {
                weightComponent + heightComponent - ageComponent + 5
            } else {
                weightComponent + heightComponent - ageComponent - 161
            }
        }
    
    /**
     * 每日总能量消耗 (TDEE)
     */
    val tdee: Double
        get() = bmr * activityLevel.multiplier
}

enum class Gender(val displayName: String) {
    MALE("男"),
    FEMALE("女")
}

enum class ActivityLevel(val displayName: String, val multiplier: Double) {
    SEDENTARY("久坐不动", 1.2),
    LIGHT("轻度活动", 1.375),
    MODERATE("中度活动", 1.55),
    ACTIVE("高度活动", 1.725),
    VERY_ACTIVE("专业运动", 1.9)
}

enum class GoalType(val displayName: String, val calorieMultiplier: Double) {
    LOSE_WEIGHT("减脂", 0.85),
    MAINTAIN("保持", 1.0),
    GAIN_WEIGHT("增重", 1.15),
    BUILD_MUSCLE("增肌塑形", 1.1)
}

/**
 * 用户档案
 */
data class UserProfile(
    val name: String = "用户",
    val goalType: GoalType = GoalType.MAINTAIN,
    val bodyStats: BodyStats = BodyStats(),
    val macroGoals: MacroGoals = MacroGoals()
)

/**
 * API 请求/响应
 */
data class AnalyzeRequest(
    val image: String,
    val language: String = "zh-CN"
)

data class AnalyzeResponse(
    val foodName: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val confidence: Double,
    val nutritionAdvice: String?,
    val alternatives: List<String>?,
    val timestamp: String
)

data class NutritionCalculateRequest(
    val gender: String,
    val age: Int,
    val height: Double,
    val weight: Double,
    val activityLevel: String,
    val goal: String
)

data class NutritionCalculateResponse(
    val bmr: Int,
    val tdee: Int,
    val targetCalories: Int,
    val macros: MacroResponse,
    val recommendations: List<String>,
    val bmi: String
)

data class MacroResponse(
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val fiber: Int
)