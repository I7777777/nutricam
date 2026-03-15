//  ContentView.swift
//  NutriCam
//  主界面 - TabView导航

import SwiftUI

struct ContentView: View {
    @EnvironmentObject var userProfile: UserProfile
    @State private var selectedTab = 0
    
    var body: some View {
        TabView(selection: $selectedTab) {
            CameraView()
                .tabItem {
                    Image(systemName: "camera.fill")
                    Text("识别")
                }
                .tag(0)
            
            NutritionDashboard()
                .tabItem {
                    Image(systemName: "chart.pie.fill")
                    Text("今日")
                }
                .tag(1)
            
            ProfileView()
                .tabItem {
                    Image(systemName: "person.fill")
                    Text("我的")
                }
                .tag(2)
        }
        .accentColor(.green)
    }
}