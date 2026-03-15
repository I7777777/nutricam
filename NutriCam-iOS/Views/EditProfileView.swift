//  EditProfileView.swift
//  NutriCam
//  编辑用户资料界面

import SwiftUI

struct EditProfileView: View {
    @ObservedObject var profile: UserProfile
    @Environment(\.presentationMode) var presentationMode
    
    @State private var name: String = ""
    @State private var selectedGender: Gender = .male
    @State private var age: String = ""
    @State private var height: String = ""
    @State private var weight: String = ""
    @State private var selectedActivity: ActivityLevel = .moderate
    @State private var selectedGoal: GoalType = .maintain
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("基本信息")) {
                    TextField("昵称", text: $name)
                }
                
                Section(header: Text("身体数据")) {
                    Picker("性别", selection: $selectedGender) {
                        ForEach(Gender.allCases, id: \.self) { gender in
                            Text(gender.description).tag(gender)
                        }
                    }
                    
                    HStack {
                        Text("年龄")
                        Spacer()
                        TextField("25", text: $age)
                            .keyboardType(.numberPad)
                            .multilineTextAlignment(.trailing)
                            .frame(width: 60)
                        Text("岁")
                    }
                    
                    HStack {
                        Text("身高")
                        Spacer()
                        TextField("170", text: $height)
                            .keyboardType(.decimalPad)
                            .multilineTextAlignment(.trailing)
                            .frame(width: 60)
                        Text("cm")
                    }
                    
                    HStack {
                        Text("体重")
                        Spacer()
                        TextField("65", text: $weight)
                            .keyboardType(.decimalPad)
                            .multilineTextAlignment(.trailing)
                            .frame(width: 60)
                        Text("kg")
                    }
                }
                
                Section(header: Text("活动水平")) {
                    Picker("活动量", selection: $selectedActivity) {
                        ForEach(ActivityLevel.allCases, id: \.self) { level in
                            Text(level.description).tag(level)
                        }
                    }
                }
                
                Section(header: Text("目标")) {
                    Picker("健康目标", selection: $selectedGoal) {
                        ForEach(GoalType.allCases, id: \.self) { goal in
                            Text(goal.description).tag(goal)
                        }
                    }
                }
                
                Section {
                    Button("保存") {
                        saveProfile()
                    }
                    .frame(maxWidth: .infinity)
                    .foregroundColor(.green)
                }
            }
            .navigationTitle("编辑资料")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("取消") {
                        presentationMode.wrappedValue.dismiss()
                    }
                }
            }
            .onAppear {
                loadCurrentValues()
            }
        }
    }
    
    private func loadCurrentValues() {
        name = profile.name
        selectedGender = profile.bodyStats.gender
        age = String(profile.bodyStats.age)
        height = String(Int(profile.bodyStats.height))
        weight = String(Int(profile.bodyStats.weight))
        selectedActivity = profile.bodyStats.activityLevel
        selectedGoal = profile.goalType
    }
    
    private func saveProfile() {
        profile.name = name.isEmpty ? "用户" : name
        
        let newStats = BodyStats(
            gender: selectedGender,
            age: Int(age) ?? 25,
            height: Double(height) ?? 170,
            weight: Double(weight) ?? 65,
            activityLevel: selectedActivity
        )
        
        profile.updateBodyStats(newStats)
        profile.updateGoalType(selectedGoal)
        
        presentationMode.wrappedValue.dismiss()
    }
}

// 编辑目标视图
struct EditGoalsView: View {
    @EnvironmentObject var profile: UserProfile
    @Environment(\.presentationMode) var presentationMode
    
    @State private var calories: String = ""
    @State private var protein: String = ""
    @State private var carbs: String = ""
    @State private var fat: String = ""
    @State private var fiber: String = ""
    
    var body: some View {
        Form {
            Section(header: Text("自定义目标"), footer: Text("这些数值将覆盖自动计算的目标")) {
                HStack {
                    Text("热量")
                    Spacer()
                    TextField("2000", text: $calories)
                        .keyboardType(.numberPad)
                        .multilineTextAlignment(.trailing)
                        .frame(width: 80)
                    Text("千卡")
                }
                
                HStack {
                    Text("蛋白质")
                    Spacer()
                    TextField("60", text: $protein)
                        .keyboardType(.numberPad)
                        .multilineTextAlignment(.trailing)
                        .frame(width: 80)
                    Text("g")
                }
                
                HStack {
                    Text("碳水化合物")
                    Spacer()
                    TextField("250", text: $carbs)
                        .keyboardType(.numberPad)
                        .multilineTextAlignment(.trailing)
                        .frame(width: 80)
                    Text("g")
                }
                
                HStack {
                    Text("脂肪")
                    Spacer()
                    TextField("70", text: $fat)
                        .keyboardType(.numberPad)
                        .multilineTextAlignment(.trailing)
                        .frame(width: 80)
                    Text("g")
                }
                
                HStack {
                    Text("膳食纤维")
                    Spacer()
                    TextField("25", text: $fiber)
                        .keyboardType(.numberPad)
                        .multilineTextAlignment(.trailing)
                        .frame(width: 80)
                    Text("g")
                }
            }
            
            Section {
                Button("恢复自动计算") {
                    profile.calculateGoals()
                    loadCurrentValues()
                }
                .foregroundColor(.blue)
            }
            
            Section {
                Button("保存") {
                    saveGoals()
                }
                .frame(maxWidth: .infinity)
                .foregroundColor(.green)
            }
        }
        .navigationTitle("编辑目标")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            loadCurrentValues()
        }
    }
    
    private func loadCurrentValues() {
        calories = String(Int(profile.macroGoals.calories))
        protein = String(Int(profile.macroGoals.protein))
        carbs = String(Int(profile.macroGoals.carbs))
        fat = String(Int(profile.macroGoals.fat))
        fiber = String(Int(profile.macroGoals.fiber))
    }
    
    private func saveGoals() {
        let newGoals = MacroGoals(
            calories: Double(calories) ?? 2000,
            protein: Double(protein) ?? 60,
            carbs: Double(carbs) ?? 250,
            fat: Double(fat) ?? 70,
            fiber: Double(fiber) ?? 25
        )
        
        profile.updateMacroGoals(newGoals)
        presentationMode.wrappedValue.dismiss()
    }
}