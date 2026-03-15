# NutriCam Android

基于 Jetpack Compose 开发的食物热量识别 Android 应用。

## 功能特性

- 📷 相机实时预览与拍照识别
- 📸 相册选择图片识别
- 📊 营养成分分析（热量、蛋白质、碳水、脂肪、纤维）
- 📈 每日营养摄入追踪
- 👤 个性化营养目标设置
- 💡 基于目标的健康建议

## 技术栈

- **UI**: Jetpack Compose
- **相机**: CameraX
- **网络**: Retrofit + OkHttp
- **存储**: DataStore (Preferences)
- **架构**: MVVM
- **最低SDK**: API 24 (Android 7.0)
- **目标SDK**: API 34 (Android 14)

## 项目结构

```
app/src/main/java/com/nutricam/
├── MainActivity.kt              # 入口Activity
├── NutriCamApplication.kt       # Application类
├── model/                       # 数据模型
│   └── FoodModels.kt
├── data/
│   ├── remote/                  # 网络层
│   │   ├── NutriCamApi.kt
│   │   └── RetrofitClient.kt
│   ├── local/                   # 本地存储
│   │   └── DataStoreManager.kt
│   └── repository/              # 数据仓库
│       └── FoodRepository.kt
├── viewmodel/                   # ViewModel
│   └── NutritionViewModel.kt
└── ui/
    ├── NutriCamApp.kt           # 主应用
    ├── theme/                   # 主题
    │   ├── Color.kt
    │   ├── Theme.kt
    │   └── Type.kt
    └── screens/                 # 界面
        ├── CameraScreen.kt      # 相机识别
        ├── DashboardScreen.kt   # 营养仪表板
        └── ProfileScreen.kt     # 用户资料
```

## 快速开始

### 1. 配置后端

确保后端服务已启动：
```bash
cd ../ios-nutrition-app/backend
npm start
```

### 2. 修改 API 地址

编辑 `data/remote/RetrofitClient.kt`：

```kotlin
// Android 模拟器访问宿主机
private const val BASE_URL = "http://10.0.2.2:3000/"

// 真机测试使用实际 IP
// private const val BASE_URL = "http://192.168.x.x:3000/"
```

### 3. 构建运行

```bash
# 使用 Gradle
./gradlew assembleDebug

# 或使用 Android Studio
# File -> Open -> 选择本项目
# 点击 Run
```

## 开发模式

默认开启开发模式（模拟数据），无需后端和 API Key 即可测试 UI。

修改 `FoodRepository.kt` 关闭模拟：
```kotlin
private fun isDebugMode(): Boolean {
    return false  // 改为 false 使用真实 API
}
```

## 发布配置

### 签名配置

在 `app/build.gradle` 中添加：

```gradle
android {
    signingConfigs {
        release {
            storeFile file("nutricam.keystore")
            storePassword "your_password"
            keyAlias "nutricam"
            keyPassword "your_password"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile(...)
        }
    }
}
```

### 生成签名 APK

```bash
./gradlew assembleRelease
```

## 与 iOS 版本对比

| 功能 | iOS | Android |
|------|-----|---------|
| 相机实时预览 | AVFoundation | CameraX |
| UI 框架 | SwiftUI | Jetpack Compose |
| 本地存储 | UserDefaults | DataStore |
| 网络请求 | URLSession | Retrofit |
| 图表 | Swift Charts | 自定义绘制 |

## 依赖版本

- Kotlin: 1.9.20
- Compose BOM: 2023.10.01
- CameraX: 1.3.1
- Retrofit: 2.9.0
- Coil: 2.5.0

## 许可证

MIT