//  NutriCamApp.swift
//  NutriCam
//  应用入口

import SwiftUI

@main
struct NutriCamApp: App {
    @StateObject private var userProfile = UserProfile()
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(userProfile)
        }
    }
}