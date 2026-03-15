package com.nutricam.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.nutricam.model.FoodEntry
import com.nutricam.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nutricam_prefs")

class DataStoreManager(private val context: Context) {
    
    private val gson = Gson()
    
    companion object {
        val USER_PROFILE_KEY = stringPreferencesKey("user_profile")
        val FOOD_ENTRIES_KEY = stringPreferencesKey("food_entries")
    }
    
    // 用户档案
    val userProfile: Flow<UserProfile> = context.dataStore.data
        .map { preferences ->
            preferences[USER_PROFILE_KEY]?.let {
                gson.fromJson(it, UserProfile::class.java)
            } ?: UserProfile()
        }
    
    suspend fun saveUserProfile(profile: UserProfile) {
        context.dataStore.edit { preferences ->
            preferences[USER_PROFILE_KEY] = gson.toJson(profile)
        }
    }
    
    // 食物记录
    val foodEntries: Flow<List<FoodEntry>> = context.dataStore.data
        .map { preferences ->
            preferences[FOOD_ENTRIES_KEY]?.let {
                gson.fromJson(it, Array<FoodEntry>::class.java).toList()
            } ?: emptyList()
        }
    
    suspend fun saveFoodEntries(entries: List<FoodEntry>) {
        context.dataStore.edit { preferences ->
            preferences[FOOD_ENTRIES_KEY] = gson.toJson(entries)
        }
    }
    
    suspend fun addFoodEntry(entry: FoodEntry) {
        context.dataStore.edit { preferences ->
            val currentList = preferences[FOOD_ENTRIES_KEY]?.let {
                gson.fromJson(it, Array<FoodEntry>::class.java).toList()
            } ?: emptyList()
            
            val newList = currentList + entry
            preferences[FOOD_ENTRIES_KEY] = gson.toJson(newList)
        }
    }
    
    suspend fun deleteFoodEntry(entryId: String) {
        context.dataStore.edit { preferences ->
            val currentList = preferences[FOOD_ENTRIES_KEY]?.let {
                gson.fromJson(it, Array<FoodEntry>::class.java).toList()
            } ?: emptyList()
            
            val newList = currentList.filter { it.id != entryId }
            preferences[FOOD_ENTRIES_KEY] = gson.toJson(newList)
        }
    }
}