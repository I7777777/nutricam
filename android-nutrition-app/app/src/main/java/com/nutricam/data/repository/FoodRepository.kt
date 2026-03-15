package com.nutricam.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.nutricam.data.local.DataStoreManager
import com.nutricam.data.remote.RetrofitClient
import com.nutricam.model.*
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

class FoodRepository(private val context: Context) {
    
    private val api = RetrofitClient.api
    private val dataStore = DataStoreManager(context)
    
    // 用户档案
    val userProfile: Flow<UserProfile> = dataStore.userProfile
    val foodEntries: Flow<List<FoodEntry>> = dataStore.foodEntries
    
    suspend fun saveUserProfile(profile: UserProfile) {
        dataStore.saveUserProfile(profile)
    }
    
    suspend fun addFoodEntry(analysis: FoodAnalysis, imageUri: String? = null) {
        val entry = FoodEntry(
            foodName = analysis.foodName,
            calories = analysis.calories,
            protein = analysis.protein,
            carbs = analysis.carbs,
            fat = analysis.fat,
            fiber = analysis.fiber,
            imageUri = imageUri,
            mealType = getCurrentMealType()
        )
        dataStore.addFoodEntry(entry)
    }
    
    suspend fun deleteFoodEntry(entryId: String) {
        dataStore.deleteFoodEntry(entryId)
    }
    
    // 分析图片
    suspend fun analyzeImage(bitmap: Bitmap): Result<FoodAnalysis> {
        return try {
            // 压缩并转换为 Base64
            val compressed = compressBitmap(bitmap)
            val base64Image = bitmapToBase64(compressed)
            
            // 开发模式：使用模拟数据
            if (isDebugMode()) {
                return Result.success(getMockAnalysis())
            }
            
            val request = AnalyzeRequest(image = base64Image)
            val response = api.analyzeBase64(request)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(
                        FoodAnalysis(
                            foodName = it.foodName,
                            calories = it.calories,
                            protein = it.protein,
                            carbs = it.carbs,
                            fat = it.fat,
                            fiber = it.fiber,
                            confidence = it.confidence,
                            nutritionAdvice = it.nutritionAdvice,
                            alternatives = it.alternatives
                        )
                    )
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 计算营养需求
    suspend fun calculateNutrition(
        gender: Gender,
        age: Int,
        height: Double,
        weight: Double,
        activityLevel: ActivityLevel,
        goal: GoalType
    ): Result<NutritionCalculateResponse> {
        return try {
            val request = NutritionCalculateRequest(
                gender = gender.name.lowercase(),
                age = age,
                height = height,
                weight = weight,
                activityLevel = activityLevel.name.lowercase(),
                goal = goal.name.lowercase()
            )
            
            val response = api.calculateNutrition(request)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 辅助方法
    private fun compressBitmap(bitmap: Bitmap, maxSize: Int = 1024): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val scale = if (width > height) {
            maxSize.toFloat() / width
        } else {
            maxSize.toFloat() / height
        }
        
        return if (scale < 1) {
            Bitmap.createScaledBitmap(
                bitmap,
                (width * scale).toInt(),
                (height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }
    }
    
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
    
    private fun getCurrentMealType(): MealType {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5 until 11 -> MealType.BREAKFAST
            in 11 until 14 -> MealType.LUNCH
            in 14 until 17 -> MealType.SNACK
            in 17 until 21 -> MealType.DINNER
            else -> MealType.SNACK
        }
    }
    
    private fun isDebugMode(): Boolean {
        // 开发模式返回模拟数据
        return true
    }
    
    private fun getMockAnalysis(): FoodAnalysis {
        val mockFoods = listOf(
            FoodAnalysis(
                foodName = "宫保鸡丁",
                calories = 180.0,
                protein = 15.0,
                carbs = 12.0,
                fat = 8.0,
                fiber = 2.0,
                confidence = 0.92,
                nutritionAdvice = "蛋白质含量丰富，适合健身人群。注意控制米饭摄入量。",
                alternatives = listOf("麻婆豆腐", "鱼香肉丝", "青椒炒肉")
            ),
            FoodAnalysis(
                foodName = "白米饭",
                calories = 130.0,
                protein = 2.7,
                carbs = 28.0,
                fat = 0.3,
                fiber = 0.4,
                confidence = 0.98,
                nutritionAdvice = "优质碳水来源，减脂期间建议控制在一拳大小。",
                alternatives = listOf("糙米饭", "杂粮饭", "红薯")
            ),
            FoodAnalysis(
                foodName = "番茄炒蛋",
                calories = 95.0,
                protein = 6.0,
                carbs = 5.0,
                fat = 6.0,
                fiber = 1.0,
                confidence = 0.89,
                nutritionAdvice = "西红柿富含番茄红素，鸡蛋提供优质蛋白，营养搭配均衡。",
                alternatives = emptyList()
            ),
            FoodAnalysis(
                foodName = "牛油果沙拉",
                calories = 160.0,
                protein = 2.0,
                carbs = 9.0,
                fat = 15.0,
                fiber = 7.0,
                confidence = 0.85,
                nutritionAdvice = "牛油果富含健康脂肪，但热量较高，注意适量食用。",
                alternatives = listOf("蔬菜沙拉", "水果沙拉", "凯撒沙拉")
            )
        )
        return mockFoods.random()
    }
}