//  NutritionDashboard.swift
//  NutriCam
//  今日营养仪表板

import SwiftUI
import Charts

struct NutritionDashboard: View {
    @EnvironmentObject var userProfile: UserProfile
    @State private var selectedDate = Date()
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // 日期选择器
                    DatePicker("选择日期", selection: $selectedDate, displayedComponents: .date)
                        .datePickerStyle(.compact)
                        .padding(.horizontal)
                    
                    // 热量总览卡片
                    CalorieSummaryCard(
                        consumed: userProfile.todayCalories,
                        goal: userProfile.dailyCalorieGoal,
                        remaining: userProfile.remainingCalories
                    )
                    
                    // 营养分布环形图
                    MacroChart(
                        protein: userProfile.todayProtein,
                        carbs: userProfile.todayCarbs,
                        fat: userProfile.todayFat,
                        goals: userProfile.macroGoals
                    )
                    
                    // 三大营养素详情
                    MacroDetailView(
                        protein: (userProfile.todayProtein, userProfile.macroGoals.protein),
                        carbs: (userProfile.todayCarbs, userProfile.macroGoals.carbs),
                        fat: (userProfile.todayFat, userProfile.macroGoals.fat)
                    )
                    
                    // 今日食物记录
                    TodayFoodLog(entries: userProfile.todayEntries)
                }
                .padding(.vertical)
            }
            .navigationTitle("今日营养")
            .navigationBarTitleDisplayMode(.large)
        }
    }
}

// 热量总览卡片
struct CalorieSummaryCard: View {
    let consumed: Double
    let goal: Double
    let remaining: Double
    
    var progress: Double {
        min(consumed / goal, 1.0)
    }
    
    var body: some View {
        VStack(spacing: 16) {
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    Text("今日摄入")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    Text("\(Int(consumed))")
                        .font(.system(size: 48, weight: .bold))
                        .foregroundColor(.primary)
                    Text("/ \(Int(goal)) 千卡")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                
                Spacer()
                
                // 环形进度
                ZStack {
                    Circle()
                        .stroke(Color.gray.opacity(0.2), lineWidth: 12)
                    Circle()
                        .trim(from: 0, to: progress)
                        .stroke(
                            AngularGradient(
                                colors: [.green, .orange, .red],
                                center: .center,
                                startAngle: .degrees(0),
                                endAngle: .degrees(360)
                            ),
                            style: StrokeStyle(lineWidth: 12, lineCap: .round)
                        )
                        .rotationEffect(.degrees(-90))
                        .animation(.easeInOut, value: progress)
                    
                    VStack {
                        Text("\(Int(remaining))")
                            .font(.title2)
                            .fontWeight(.bold)
                        Text("剩余")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                }
                .frame(width: 120, height: 120)
            }
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .cornerRadius(16)
        .padding(.horizontal)
    }
}

// 营养分布图表 (iOS 16+ Charts)
struct MacroChart: View {
    let protein: Double
    let carbs: Double
    let fat: Double
    let goals: MacroGoals
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("营养分布")
                .font(.headline)
                .padding(.horizontal)
            
            if #available(iOS 16.0, *) {
                Chart {
                    SectorMark(
                        angle: .value("蛋白质", protein),
                        innerRadius: .ratio(0.6),
                        angularInset: 2
                    )
                    .foregroundStyle(.blue)
                    .annotation(position: .overlay) {
                        Text("蛋")
                            .font(.caption)
                            .foregroundColor(.white)
                    }
                    
                    SectorMark(
                        angle: .value("碳水", carbs),
                        innerRadius: .ratio(0.6),
                        angularInset: 2
                    )
                    .foregroundStyle(.green)
                    
                    SectorMark(
                        angle: .value("脂肪", fat),
                        innerRadius: .ratio(0.6),
                        angularInset: 2
                    )
                    .foregroundStyle(.red)
                }
                .frame(height: 200)
                .padding(.horizontal)
            } else {
                // iOS 15 fallback
                HStack(spacing: 20) {
                    MacroPieSlice(value: protein, total: protein + carbs + fat, color: .blue, label: "蛋白质")
                    MacroPieSlice(value: carbs, total: protein + carbs + fat, color: .green, label: "碳水")
                    MacroPieSlice(value: fat, total: protein + carbs + fat, color: .red, label: "脂肪")
                }
                .padding()
            }
        }
        .padding(.vertical, 8)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(16)
        .padding(.horizontal)
    }
}

// iOS 15 饼图备用
struct MacroPieSlice: View {
    let value: Double
    let total: Double
    let color: Color
    let label: String
    
    var percentage: Double {
        total > 0 ? value / total : 0
    }
    
    var body: some View {
        VStack {
            Circle()
                .fill(color)
                .frame(width: 60 * CGFloat(percentage) + 20, height: 60 * CGFloat(percentage) + 20)
            Text(label)
                .font(.caption)
            Text("\(Int(percentage * 100))%")
                .font(.caption2)
                .foregroundColor(.secondary)
        }
    }
}

// 营养素详情
struct MacroDetailView: View {
    let protein: (current: Double, goal: Double)
    let carbs: (current: Double, goal: Double)
    let fat: (current: Double, goal: Double)
    
    var body: some View {
        VStack(spacing: 16) {
            Text("营养素详情")
                .font(.headline)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            MacroRow(
                icon: "fish.fill",
                iconColor: .blue,
                name: "蛋白质",
                current: protein.current,
                goal: protein.goal,
                unit: "g"
            )
            
            MacroRow(
                icon: "leaf.fill",
                iconColor: .green,
                name: "碳水化合物",
                current: carbs.current,
                goal: carbs.goal,
                unit: "g"
            )
            
            MacroRow(
                icon: "drop.fill",
                iconColor: .red,
                name: "脂肪",
                current: fat.current,
                goal: fat.goal,
                unit: "g"
            )
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .cornerRadius(16)
        .padding(.horizontal)
    }
}

struct MacroRow: View {
    let icon: String
    let iconColor: Color
    let name: String
    let current: Double
    let goal: Double
    let unit: String
    
    var progress: Double {
        min(current / goal, 1.0)
    }
    
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.title2)
                .foregroundColor(iconColor)
                .frame(width: 40)
            
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(name)
                        .font(.subheadline)
                    Spacer()
                    Text("\(String(format: "%.1f", current)) / \(Int(goal))\(unit)")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }
                
                GeometryReader { geometry in
                    ZStack(alignment: .leading) {
                        Rectangle()
                            .fill(Color.gray.opacity(0.2))
                            .cornerRadius(4)
                        
                        Rectangle()
                            .fill(iconColor)
                            .frame(width: geometry.size.width * progress)
                            .cornerRadius(4)
                    }
                }
                .frame(height: 8)
            }
        }
    }
}

// 今日食物记录
struct TodayFoodLog: View {
    let entries: [FoodEntry]
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("今日记录")
                    .font(.headline)
                Spacer()
                Text("\(entries.count) 项")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            if entries.isEmpty {
                VStack(spacing: 12) {
                    Image(systemName: "camera.badge.ellipsis")
                        .font(.system(size: 50))
                        .foregroundColor(.gray)
                    Text("还没有记录")
                        .foregroundColor(.secondary)
                    Text("使用相机识别食物开始记录")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 40)
            } else {
                LazyVStack(spacing: 12) {
                    ForEach(entries) { entry in
                        FoodEntryRow(entry: entry)
                    }
                }
            }
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .cornerRadius(16)
        .padding(.horizontal)
    }
}

struct FoodEntryRow: View {
    let entry: FoodEntry
    
    var body: some View {
        HStack(spacing: 12) {
            // 食物图片缩略图
            if let imageData = entry.imageData,
               let uiImage = UIImage(data: imageData) {
                Image(uiImage: uiImage)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 60, height: 60)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            } else {
                RoundedRectangle(cornerRadius: 8)
                    .fill(Color.gray.opacity(0.3))
                    .frame(width: 60, height: 60)
                    .overlay(
                        Image(systemName: "photo")
                            .foregroundColor(.gray)
                    )
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(entry.foodName)
                    .font(.subheadline)
                    .fontWeight(.medium)
                Text(entry.timestamp, style: .time)
                    .font(.caption)
                    .foregroundColor(.secondary)
                HStack(spacing: 8) {
                    Label("\(Int(entry.calories))", systemImage: "flame.fill")
                        .font(.caption2)
                        .foregroundColor(.orange)
                    Text("•")
                        .font(.caption2)
                        .foregroundColor(.secondary)
                    Text("\(String(format: "%.1f", entry.protein))g 蛋")
                        .font(.caption2)
                        .foregroundColor(.blue)
                }
            }
            
            Spacer()
        }
        .padding(.vertical, 4)
    }
}