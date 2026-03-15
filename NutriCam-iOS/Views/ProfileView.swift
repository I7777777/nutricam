//  ProfileView.swift
//  NutriCam
//  用户资料与营养目标设置

import SwiftUI

struct ProfileView: View {
    @EnvironmentObject var userProfile: UserProfile
    @State private var showEditProfile = false
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // 用户信息卡片
                    UserInfoCard(profile: userProfile)
                    
                    // 每日目标
                    DailyGoalsCard(goals: userProfile.macroGoals)
                    
                    // 身体数据
                    BodyStatsCard(stats: userProfile.bodyStats)
                    
                    // 健康建议
                    HealthTipsCard(profile: userProfile)
                    
                    // 设置选项
                    SettingsSection()
                }
                .padding(.vertical)
            }
            .navigationTitle("我的")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("编辑") {
                        showEditProfile = true
                    }
                }
            }
            .sheet(isPresented: $showEditProfile) {
                EditProfileView(profile: userProfile)
            }
        }
    }
}

// 用户信息卡片
struct UserInfoCard: View {
    let profile: UserProfile
    
    var body: some View {
        VStack(spacing: 16) {
            // 头像
            ZStack {
                Circle()
                    .fill(Color.green.opacity(0.2))
                    .frame(width: 100, height: 100)
                
                Text(profile.name.prefix(1).uppercased())
                    .font(.system(size: 40, weight: .bold))
                    .foregroundColor(.green)
            }
            
            VStack(spacing: 4) {
                Text(profile.name)
                    .font(.title2)
                    .fontWeight(.bold)
                
                Text(profile.goalType.description)
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .background(Color.green.opacity(0.1))
                    .cornerRadius(12)
            }
        }
        .padding()
        .frame(maxWidth: .infinity)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(16)
        .padding(.horizontal)
    }
}

// 每日目标卡片
struct DailyGoalsCard: View {
    let goals: MacroGoals
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Text("每日营养目标")
                    .font(.headline)
                Spacer()
                NavigationLink(destination: EditGoalsView()) {
                    Text("修改")
                        .font(.subheadline)
                        .foregroundColor(.green)
                }
            }
            
            VStack(spacing: 12) {
                GoalRow(icon: "flame.fill", color: .orange, label: "热量", value: "\(Int(goals.calories)) 千卡")
                GoalRow(icon: "fish.fill", color: .blue, label: "蛋白质", value: "\(Int(goals.protein))g")
                GoalRow(icon: "leaf.fill", color: .green, label: "碳水化合物", value: "\(Int(goals.carbs))g")
                GoalRow(icon: "drop.fill", color: .red, label: "脂肪", value: "\(Int(goals.fat))g")
                GoalRow(icon: "bolt.fill", color: .yellow, label: "膳食纤维", value: "\(Int(goals.fiber))g")
            }
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .cornerRadius(16)
        .padding(.horizontal)
    }
}

struct GoalRow: View {
    let icon: String
    let color: Color
    let label: String
    let value: String
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(color)
                .frame(width: 30)
            Text(label)
                .font(.subheadline)
            Spacer()
            Text(value)
                .font(.subheadline)
                .fontWeight(.medium)
        }
    }
}

// 身体数据卡片
struct BodyStatsCard: View {
    let stats: BodyStats
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("身体数据")
                .font(.headline)
            
            HStack(spacing: 20) {
                StatBox(icon: "person.fill", label: "性别", value: stats.gender.description)
                StatBox(icon: "number", label: "年龄", value: "\(stats.age) 岁")
                StatBox(icon: "ruler", label: "身高", value: "\(Int(stats.height)) cm")
                StatBox(icon: "scalemass.fill", label: "体重", value: "\(Int(stats.weight)) kg")
            }
            
            if let bmi = stats.bmi {
                HStack {
                    Text("BMI")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    Spacer()
                    Text("\(String(format: "%.1f", bmi))")
                        .font(.headline)
                        .fontWeight(.bold)
                    Text(bmiCategory(bmi))
                        .font(.caption)
                        .foregroundColor(bmiColor(bmi))
                        .padding(.horizontal, 8)
                        .padding(.vertical, 2)
                        .background(bmiColor(bmi).opacity(0.2))
                        .cornerRadius(4)
                }
            }
        }
        .padding()
        .background(Color(.secondarySystemBackground))
        .cornerRadius(16)
        .padding(.horizontal)
    }
    
    private func bmiCategory(_ bmi: Double) -> String {
        switch bmi {
        case ..<18.5: return "偏瘦"
        case 18.5..<24: return "正常"
        case 24..<28: return "偏胖"
        default: return "肥胖"
        }
    }
    
    private func bmiColor(_ bmi: Double) -> Color {
        switch bmi {
        case ..<18.5: return .blue
        case 18.5..<24: return .green
        case 24..<28: return .orange
        default: return .red
        }
    }
}

struct StatBox: View {
    let icon: String
    let label: String
    let value: String
    
    var body: some View {
        VStack(spacing: 4) {
            Image(systemName: icon)
                .font(.title3)
                .foregroundColor(.green)
            Text(value)
                .font(.caption)
                .fontWeight(.medium)
            Text(label)
                .font(.caption2)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity)
    }
}

// 健康建议卡片
struct HealthTipsCard: View {
    let profile: UserProfile
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("💡 个性化建议")
                .font(.headline)
            
            VStack(alignment: .leading, spacing: 12) {
                ForEach(profile.healthTips, id: \.self) { tip in
                    HStack(alignment: .top, spacing: 8) {
                        Image(systemName: "checkmark.circle.fill")
                            .foregroundColor(.green)
                            .font(.caption)
                        Text(tip)
                            .font(.subheadline)
                            .fixedSize(horizontal: false, vertical: true)
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

// 设置选项
struct SettingsSection: View {
    var body: some View {
        VStack(spacing: 0) {
            NavigationLink(destination: Text("提醒设置")) {
                SettingRow(icon: "bell.fill", iconColor: .orange, title: "提醒设置")
            }
            
            Divider().padding(.leading, 50)
            
            NavigationLink(destination: Text("数据同步")) {
                SettingRow(icon: "icloud", iconColor: .blue, title: "数据同步")
            }
            
            Divider().padding(.leading, 50)
            
            NavigationLink(destination: Text("隐私设置")) {
                SettingRow(icon: "shield.fill", iconColor: .green, title: "隐私设置")
            }
            
            Divider().padding(.leading, 50)
            
            NavigationLink(destination: Text("关于")) {
                SettingRow(icon: "info.circle.fill", iconColor: .gray, title: "关于 NutriCam")
            }
        }
        .background(Color(.secondarySystemBackground))
        .cornerRadius(16)
        .padding(.horizontal)
    }
}

struct SettingRow: View {
    let icon: String
    let iconColor: Color
    let title: String
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .foregroundColor(iconColor)
                .frame(width: 30)
            Text(title)
                .font(.subheadline)
            Spacer()
            Image(systemName: "chevron.right")
                .font(.caption)
                .foregroundColor(.gray)
        }
        .padding()
    }
}