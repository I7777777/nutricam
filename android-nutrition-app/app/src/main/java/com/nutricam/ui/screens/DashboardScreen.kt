package com.nutricam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nutricam.model.FoodEntry
import com.nutricam.model.MealType
import com.nutricam.ui.theme.*
import com.nutricam.viewmodel.NutritionViewModel
import com.nutricam.viewmodel.TodayStats
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: NutritionViewModel,
    modifier: Modifier = Modifier
) {
    val todayStats by viewModel.todayStats.collectAsState()
    val todayEntries by viewModel.todayEntries.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    
    val goalCalories = userProfile.macroGoals.calories
    val remainingCalories = goalCalories - todayStats.calories
    val progress = (todayStats.calories / goalCalories).toFloat().coerceIn(0f, 1f)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("今日营养") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 热量总览卡片
            item {
                CalorieSummaryCard(
                    consumed = todayStats.calories,
                    goal = goalCalories,
                    remaining = remainingCalories,
                    progress = progress
                )
            }
            
            // 营养分布
            item {
                MacroDistributionCard(
                    protein = todayStats.protein,
                    carbs = todayStats.carbs,
                    fat = todayStats.fat,
                    goals = userProfile.macroGoals
                )
            }
            
            // 今日记录
            item {
                Text(
                    "今日记录 (${todayEntries.size}项)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (todayEntries.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                items(todayEntries) { entry ->
                    FoodEntryCard(
                        entry = entry,
                        onDelete = { viewModel.deleteEntry(entry.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalorieSummaryCard(
    consumed: Double,
    goal: Double,
    remaining: Double,
    progress: Float
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("今日摄入", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(
                    consumed.toInt().toString(),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
                Text("/ ${goal.toInt()} 千卡", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
            
            // 环形进度
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = 1f,
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Gray.copy(alpha = 0.2f),
                    strokeWidth = 12.dp
                )
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxSize(),
                    color = when {
                        progress > 1f -> Error
                        progress > 0.8f -> Warning
                        else -> Success
                    },
                    strokeWidth = 12.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        remaining.toInt().toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("剩余", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun MacroDistributionCard(
    protein: Double,
    carbs: Double,
    fat: Double,
    goals: com.nutricam.model.MacroGoals
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
            .padding(16.dp)
        ) {
            Text(
                "营养分布",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 简单饼图表示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroPieSlice(
                    value = protein,
                    total = protein + carbs + fat,
                    color = ProteinColor,
                    label = "蛋白质"
                )
                MacroPieSlice(
                    value = carbs,
                    total = protein + carbs + fat,
                    color = CarbsColor,
                    label = "碳水"
                )
                MacroPieSlice(
                    value = fat,
                    total = protein + carbs + fat,
                    color = FatColor,
                    label = "脂肪"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 详细进度条
            MacroRow(
                icon = Icons.Default.FitnessCenter,
                iconColor = ProteinColor,
                name = "蛋白质",
                current = protein,
                goal = goals.protein
            )
            MacroRow(
                icon = Icons.Default.Grain,
                iconColor = CarbsColor,
                name = "碳水化合物",
                current = carbs,
                goal = goals.carbs
            )
            MacroRow(
                icon = Icons.Default.WaterDrop,
                iconColor = FatColor,
                name = "脂肪",
                current = fat,
                goal = goals.fat
            )
        }
    }
}

@Composable
private fun MacroPieSlice(value: Double, total: Double, color: Color, label: String) {
    val percentage = if (total > 0) value / total else 0.0
    val size = (60 * percentage + 20).dp
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${(percentage * 100).toInt()}%",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MacroRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    name: String,
    current: Double,
    goal: Double
) {
    val progress = (current / goal).toFloat().coerceIn(0f, 1f)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(name, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${current.toInt()} / ${goal.toInt()}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            LinearProgressIndicator(
                progress = progress,
                color = iconColor,
                trackColor = Color.Gray.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun FoodEntryCard(
    entry: FoodEntry,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 食物图标
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.foodName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(entry.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Row {
                    Text(
                        "${entry.calories.toInt()}千卡",
                        style = MaterialTheme.typography.labelSmall,
                        color = CalorieColor
                    )
                    Text(" • ", color = Color.Gray)
                    Text(
                        "${entry.protein.toInt()}g蛋",
                        style = MaterialTheme.typography.labelSmall,
                        color = ProteinColor
                    )
                }
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Error
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("还没有记录", color = Color.Gray)
        Text(
            "使用相机识别食物开始记录",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}