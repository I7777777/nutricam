package com.nutricam.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nutricam.data.repository.FoodRepository
import com.nutricam.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class NutritionViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = FoodRepository(application)
    
    // 用户档案
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()
    
    // 食物记录
    private val _foodEntries = MutableStateFlow<List<FoodEntry>>(emptyList())
    val foodEntries: StateFlow<List<FoodEntry>> = _foodEntries.asStateFlow()
    
    // 今日记录
    val todayEntries: StateFlow<List<FoodEntry>> = _foodEntries.map { entries ->
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        entries.filter { entry ->
            val entryDate = Calendar.getInstance().apply { time = entry.timestamp }
            val entryDay = Calendar.getInstance().apply {
                time = entryDate.time
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time
            entryDay == today
        }.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // 今日统计
    val todayStats: StateFlow<TodayStats> = todayEntries.map { entries ->
        TodayStats(
            calories = entries.sumOf { it.calories },
            protein = entries.sumOf { it.protein },
            carbs = entries.sumOf { it.carbs },
            fat = entries.sumOf { it.fat },
            fiber = entries.sumOf { it.fiber },
            count = entries.size
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, TodayStats())
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 分析结果
    private val _analysisResult = MutableStateFlow<FoodAnalysis?>(null)
    val analysisResult: StateFlow<FoodAnalysis?> = _analysisResult.asStateFlow()
    
    // 错误信息
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            repository.userProfile.collect {
                _userProfile.value = it
            }
        }
        viewModelScope.launch {
            repository.foodEntries.collect {
                _foodEntries.value = it
            }
        }
    }
    
    fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            repository.analyzeImage(bitmap).fold(
                onSuccess = { result ->
                    _analysisResult.value = result
                },
                onFailure = { error ->
                    _errorMessage.value = error.message ?: "识别失败"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun addToDailyLog(analysis: FoodAnalysis, imageUri: String? = null) {
        viewModelScope.launch {
            repository.addFoodEntry(analysis, imageUri)
            _analysisResult.value = null
        }
    }
    
    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            repository.deleteFoodEntry(entryId)
        }
    }
    
    fun updateUserProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveUserProfile(profile)
        }
    }
    
    fun updateGoalType(goalType: GoalType) {
        viewModelScope.launch {
            val newProfile = _userProfile.value.copy(goalType = goalType)
            // 重新计算营养目标
            val newGoals = calculateGoals(newProfile)
            repository.saveUserProfile(newProfile.copy(macroGoals = newGoals))
        }
    }
    
    fun updateBodyStats(bodyStats: BodyStats) {
        viewModelScope.launch {
            val newProfile = _userProfile.value.copy(bodyStats = bodyStats)
            val newGoals = calculateGoals(newProfile)
            repository.saveUserProfile(newProfile.copy(macroGoals = newGoals))
        }
    }
    
    fun clearAnalysisResult() {
        _analysisResult.value = null
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    private fun calculateGoals(profile: UserProfile): MacroGoals {
        val tdee = profile.bodyStats.tdee
        val targetCalories = tdee * profile.goalType.calorieMultiplier
        
        // 蛋白质
        val proteinMultiplier = when (profile.goalType) {
            GoalType.BUILD_MUSCLE -> 2.0
            GoalType.LOSE_WEIGHT -> 1.8
            else -> 1.6
        }
        val proteinGrams = profile.bodyStats.weight * proteinMultiplier
        val proteinCalories = proteinGrams * 4
        
        // 脂肪
        val fatPercentage = if (profile.goalType == GoalType.LOSE_WEIGHT) 0.25 else 0.3
        val fatCalories = targetCalories * fatPercentage
        val fatGrams = fatCalories / 9
        
        // 碳水
        val carbCalories = targetCalories - proteinCalories - fatCalories
        val carbGrams = maxOf(carbCalories / 4, 100.0)
        
        return MacroGoals(
            calories = targetCalories,
            protein = proteinGrams,
            carbs = carbGrams,
            fat = fatGrams,
            fiber = if (targetCalories > 2500) 35.0 else 25.0
        )
    }
}

data class TodayStats(
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0,
    val count: Int = 0
)