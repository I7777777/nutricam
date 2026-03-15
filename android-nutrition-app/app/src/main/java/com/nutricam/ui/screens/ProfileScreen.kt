package com.nutricam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nutricam.model.*
import com.nutricam.ui.theme.*
import com.nutricam.viewmodel.NutritionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: NutritionViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val todayStats by viewModel.todayStats.collectAsState()
    
    var showEditProfile by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的") },
                actions = {
                    TextButton(onClick = { showEditProfile = true }) {
                        Text("编辑", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenPrimary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 用户信息卡片
            UserInfoCard(profile = userProfile)
            
            // 每日目标
            DailyGoalsCard(goals = userProfile.macroGoals)
            
            // 身体数据
            BodyStatsCard(stats = userProfile.bodyStats)
            
            // 健康建议
            HealthTipsCard(
                goalType = userProfile.goalType,
                todayStats = todayStats,
                goals = userProfile.macroGoals
            )
            
            // 设置选项
            SettingsSection()
        }
    }
    
    if (showEditProfile) {
        EditProfileDialog(
            profile = userProfile,
            onDismiss = { showEditProfile = false },
            onSave = { newProfile ->
                viewModel.updateUserProfile(newProfile)
                showEditProfile = false
            }
        )
    }
}

@Composable
private fun UserInfoCard(profile: UserProfile) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(GreenPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.name.take(1).uppercase(),
                    style = MaterialTheme.typography.displayMedium,
                    color = GreenPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                profile.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                color = GreenPrimary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    profile.goalType.displayName,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = GreenPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun DailyGoalsCard(goals: MacroGoals) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "每日营养目标",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { /* TODO */ }) {
                    Text("修改", color = GreenPrimary)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            GoalRow(Icons.Default.LocalFireDepartment, CalorieColor, "热量", "${goals.calories.toInt()} 千卡")
            GoalRow(Icons.Default.FitnessCenter, ProteinColor, "蛋白质", "${goals.protein.toInt()}g")
            GoalRow(Icons.Default.Grain, CarbsColor, "碳水化合物", "${goals.carbs.toInt()}g")
            GoalRow(Icons.Default.WaterDrop, FatColor, "脂肪", "${goals.fat.toInt()}g")
            GoalRow(Icons.Default.Spa, FiberColor, "膳食纤维", "${goals.fiber.toInt()}g")
        }
    }
}

@Composable
private fun GoalRow(icon: ImageVector, color: Color, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BodyStatsCard(stats: BodyStats) {
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
                "身体数据",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBox(Icons.Default.Person, "性别", stats.gender.displayName)
                StatBox(Icons.Default.Cake, "年龄", "${stats.age} 岁")
                StatBox(Icons.Default.Height, "身高", "${stats.height.toInt()} cm")
                StatBox(Icons.Default.Scale, "体重", "${stats.weight.toInt()} kg")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            stats.bmi?.let { bmi ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("BMI", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "%.1f".format(bmi),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val (category, color) = getBMICategory(bmi)
                        Surface(
                            color = color.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                color = color,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBox(icon: ImageVector, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = GreenPrimary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
private fun HealthTipsCard(
    goalType: GoalType,
    todayStats: TodayStats,
    goals: MacroGoals
) {
    val tips = generateHealthTips(goalType, todayStats, goals)
    
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
                "💡 个性化建议",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tip, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun SettingsSection() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            SettingRow(Icons.Default.Notifications, "提醒设置", Warning)
            Divider(modifier = Modifier.padding(start = 48.dp))
            SettingRow(Icons.Default.Cloud, "数据同步", Color.Blue)
            Divider(modifier = Modifier.padding(start = 48.dp))
            SettingRow(Icons.Default.Security, "隐私设置", Success)
            Divider(modifier = Modifier.padding(start = 48.dp))
            SettingRow(Icons.Default.Info, "关于 NutriCam", Color.Gray)
        }
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, iconColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var selectedGender by remember { mutableStateOf(profile.bodyStats.gender) }
    var age by remember { mutableStateOf(profile.bodyStats.age.toString()) }
    var height by remember { mutableStateOf(profile.bodyStats.height.toString()) }
    var weight by remember { mutableStateOf(profile.bodyStats.weight.toString()) }
    var selectedActivity by remember { mutableStateOf(profile.bodyStats.activityLevel) }
    var selectedGoal by remember { mutableStateOf(profile.goalType) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑资料") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("昵称") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("性别", style = MaterialTheme.typography.bodyMedium)
                Row {
                    Gender.values().forEach { gender ->
                        FilterChip(
                            selected = selectedGender == gender,
                            onClick = { selectedGender = gender },
                            label = { Text(gender.displayName) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("年龄") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("身高(cm)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("体重(kg)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("活动水平", style = MaterialTheme.typography.bodyMedium)
                ActivityLevel.values().forEach { level ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedActivity = level }
                    ) {
                        RadioButton(
                            selected = selectedActivity == level,
                            onClick = { selectedActivity = level }
                        )
                        Text(level.displayName)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("目标", style = MaterialTheme.typography.bodyMedium)
                GoalType.values().forEach { goal ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { selectedGoal = goal }
                    ) {
                        RadioButton(
                            selected = selectedGoal == goal,
                            onClick = { selectedGoal = goal }
                        )
                        Text(goal.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newProfile = UserProfile(
                        name = name,
                        goalType = selectedGoal,
                        bodyStats = BodyStats(
                            gender = selectedGender,
                            age = age.toIntOrNull() ?: 25,
                            height = height.toDoubleOrNull() ?: 170.0,
                            weight = weight.toDoubleOrNull() ?: 65.0,
                            activityLevel = selectedActivity
                        )
                    )
                    onSave(newProfile)
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun getBMICategory(bmi: Double): Pair<String, Color> {
    return when {
        bmi < 18.5 -> "偏瘦" to Color.Blue
        bmi < 24 -> "正常" to Success
        bmi < 28 -> "偏胖" to Warning
        else -> "肥胖" to Error
    }
}

private fun generateHealthTips(
    goalType: GoalType,
    todayStats: com.nutricam.viewmodel.TodayStats,
    goals: MacroGoals
): List<String> {
    val tips = mutableListOf<String>()
    
    when (goalType) {
        GoalType.LOSE_WEIGHT -> {
            tips.add("减脂期间建议每餐七分饱，细嚼慢咽")
            tips.add("优先选择高蛋白、低脂肪的食物")
            tips.add("多喝水，每天至少2000ml")
        }
        GoalType.MAINTAIN -> {
            tips.add("保持当前饮食习惯，注意营养均衡")
            tips.add("定期运动有助于维持健康体重")
        }
        GoalType.GAIN_WEIGHT -> {
            tips.add("增重期间可以适当增加优质碳水的摄入")
            tips.add("少食多餐，每天5-6餐有助于增加热量摄入")
        }
        GoalType.BUILD_MUSCLE -> {
            tips.add("增肌期每公斤体重建议摄入1.6-2.2g蛋白质")
            tips.add("训练后30分钟内补充蛋白质效果更佳")
        }
    }
    
    if (todayStats.protein < goals.protein * 0.5) {
        tips.add("今天蛋白质摄入偏少，晚餐可以多吃些瘦肉或豆制品")
    }
    
    return tips
}