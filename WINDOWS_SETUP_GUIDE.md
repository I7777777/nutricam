# NutriCam 项目 - Windows 验证指南

## 项目位置

两个项目已打包：
- `/root/.openclaw/workspace/ios-nutrition-app.tar.gz`
- `/root/.openclaw/workspace/android-nutrition-app.tar.gz`

---

## Windows 验证方法

### 一、Android 项目（推荐，Windows 可完整运行）

#### 1. 环境准备
需要安装：
- [Android Studio](https://developer.android.com/studio) (官方 IDE)
- [Node.js](https://nodejs.org/) (后端运行)
- JDK 17+ (Android Studio 自带)

#### 2. 解压项目
```powershell
# 使用 7-Zip 或 Bandizip 解压 android-nutrition-app.tar.gz
# 得到 android-nutrition-app 文件夹
```

#### 3. 启动后端服务
```powershell
cd android-nutrition-app\..\ios-nutrition-app\backend  # 用 iOS 项目的后端
npm install

# 创建 .env 文件，填入 OpenAI API Key
echo "OPENAI_API_KEY=你的密钥" > .env

npm start
# 看到 "🚀 NutriCam 后端服务启动在端口 3000" 即可
```

#### 4. 打开 Android 项目
1. 打开 Android Studio
2. `File` → `Open` → 选择 `android-nutrition-app` 文件夹
3. 等待 Gradle Sync 完成（首次较慢，约 5-10 分钟）

#### 5. 配置 API 地址
编辑 `app/src/main/java/com/nutricam/data/remote/RetrofitClient.kt`：
```kotlin
// 如果使用 Android 模拟器：
private const val BASE_URL = "http://10.0.2.2:3000/"

// 如果使用真机（手机和电脑同 WiFi）：
// private const val BASE_URL = "http://电脑IP:3000/"
```

#### 6. 运行
- 创建模拟器：`Tools` → `Device Manager` → `Create Device` → 选 `Pixel 7` → 下载系统镜像
- 点击 ▶️ Run 按钮

#### 7. 测试流程
1. 模拟器启动后，拖拽几张食物图片到模拟器窗口（保存到相册）
2. 在 App 里点击「相册」按钮
3. 选择图片，看识别结果

---

### 二、iOS 项目（Windows 只能查看代码）

#### ⚠️ 限制
**Windows 无法直接运行 iOS 项目**，需要：
- Mac 电脑 + Xcode
- 或付费云服务（如 MacStadium、GitHub Actions）

#### Windows 上可做的：
1. **查看代码**：用 VS Code / Android Studio 打开查看 Swift 文件
2. **修改逻辑**：编辑代码后，压缩发给有 Mac 的人编译
3. **验证结构**：检查项目文件是否完整

#### 解压查看
```powershell
# 解压 ios-nutrition-app.tar.gz
# 用 VS Code 打开 ios-nutrition-app 文件夹
```

---

### 三、后端服务（Windows 可直接运行）

#### 1. 进入后端目录
```powershell
cd ios-nutrition-app\backend
```

#### 2. 安装依赖
```powershell
npm install
```

#### 3. 配置环境变量
创建 `.env` 文件：
```env
OPENAI_API_KEY=sk-your-openai-api-key
PORT=3000
NODE_ENV=development
```

**获取 API Key**：https://platform.openai.com/api-keys

#### 4. 启动
```powershell
npm start
```

#### 5. 测试 API
浏览器访问：
```
http://localhost:3000/health
# 应返回：{"status":"ok", "timestamp":"..."}
```

---

## 快速验证清单

### ✅ Android 项目验证
- [ ] Android Studio 能正常打开项目
- [ ] Gradle Sync 成功（无红色错误）
- [ ] 能创建/启动模拟器
- [ ] App 能安装到模拟器并运行
- [ ] 三个 Tab 页面（识别/今日/我的）都能正常显示
- [ ] 相册选择功能可用
- [ ] 后端连接成功（如配置了）

### ⚠️ iOS 项目验证（Windows 有限）
- [ ] 能解压并查看所有 Swift 文件
- [ ] Xcode 项目文件存在（NutriCam.xcodeproj）
- [ ] 文件结构完整（Views/Models/Services/Manager）

### ✅ 后端验证
- [ ] `npm install` 成功
- [ ] `npm start` 启动无错误
- [ ] `http://localhost:3000/health` 返回 ok
- [ ] API 接口文档可访问

---

## 常见问题

### Q: Android Studio 提示 "Gradle sync failed"?
A: 检查网络，确保能访问 `https://plugins.gradle.org`，或配置国内镜像。

### Q: 模拟器无法连接后端？
A: 
- 模拟器用 `10.0.2.2` 访问宿主机
- 真机用电脑的实际 IP 地址
- 关闭 Windows 防火墙或添加 3000 端口例外

### Q: 没有 OpenAI API Key？
A: 代码默认开启 **开发模式**（模拟数据），无需 Key 也能测试 UI。

### Q: Windows 能开发 iOS App 吗？
A: 不能完整开发。可以写代码，但编译/运行/发布必须用 Mac + Xcode。

---

## 项目文件清单

### Android 项目 (28 个文件)
```
android-nutrition-app/
├── app/build.gradle              # 依赖配置
├── app/src/main/
│   ├── AndroidManifest.xml       # 权限
│   ├── java/com/nutricam/
│   │   ├── MainActivity.kt
│   │   ├── model/FoodModels.kt
│   │   ├── data/
│   │   │   ├── remote/NutriCamApi.kt
│   │   │   ├── local/DataStoreManager.kt
│   │   │   └── repository/FoodRepository.kt
│   │   ├── viewmodel/NutritionViewModel.kt
│   │   └── ui/
│   │       ├── NutriCamApp.kt
│   │       ├── screens/
│   │       │   ├── CameraScreen.kt     # 相机
│   │       │   ├── DashboardScreen.kt  # 仪表板
│   │       │   └── ProfileScreen.kt    # 资料
│   │       └── theme/
│   └── res/values/
└── build.gradle
```

### iOS 项目 (28 个文件)
```
ios-nutrition-app/
├── NutriCam-iOS/
│   ├── NutriCamApp.swift
│   ├── NutriCam.xcodeproj/
│   ├── Views/
│   │   ├── CameraView.swift        # 相机识别
│   │   ├── NutritionDashboard.swift
│   │   ├── ProfileView.swift
│   │   └── ...
│   ├── Models/
│   ├── Services/
│   └── Managers/
└── backend/                        # Node.js 后端
    ├── server.js
    ├── routes/
    ├── services/
    └── data/
```

---

## 推荐方案

| 你的设备 | 推荐验证方式 |
|---------|------------|
| Windows 电脑 | 优先验证 **Android 项目** |
| 有 Mac 双系统 | 两个项目都能完整验证 |
| 只有 Windows | Android + 后端可完整运行，iOS 只能看代码 |

建议先在 Windows 上跑通 **Android 项目 + 后端**，功能验证完毕后再考虑 iOS 版本。