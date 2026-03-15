package com.nutricam.data.remote

import com.nutricam.model.AnalyzeRequest
import com.nutricam.model.AnalyzeResponse
import com.nutricam.model.NutritionCalculateRequest
import com.nutricam.model.NutritionCalculateResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface NutriCamApi {
    
    @Multipart
    @POST("api/analyze")
    suspend fun analyzeImage(
        @Part image: MultipartBody.Part
    ): Response<AnalyzeResponse>
    
    @POST("api/analyze")
    suspend fun analyzeBase64(
        @Body request: AnalyzeRequest
    ): Response<AnalyzeResponse>
    
    @POST("api/nutrition/calculate")
    suspend fun calculateNutrition(
        @Body request: NutritionCalculateRequest
    ): Response<NutritionCalculateResponse>
}