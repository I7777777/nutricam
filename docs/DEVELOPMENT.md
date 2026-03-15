# NutriCam 开发指南

## 项目结构说明

### iOS 项目结构

```
NutriCam-iOS/
├── NutriCamApp.swift           # 应用入口
├── Views/                      # SwiftUI 视图
│   ├── ContentView.swift       # 主界面 TabView
│   ├── CameraView.swift        # 相机识别界面
│   ├── NutritionDashboard.swift # 营养仪表板
│   ├── ProfileView.swift       # 用户资料
│   ├── EditProfileView.swift   # 编辑资料/目标
│   └── ImagePicker.swift       # 图片选择器
├── Models/                     # 数据模型
│   ├── FoodAnalysis.swift      # 食物分析结果
│   └── UserProfile.swift       # 用户档案管理
├── Managers/                   # 管理器
│   └── CameraManager.swift     # 相机管理
├── Services/                   # 服务层
│   └── FoodRecognitionService.swift  # 食物识别API
└── NutriCam.xcodeproj/         # Xcode 项目
```

### 后端项目结构

```
backend/
├── server.js                   # 服务入口
├── routes/                     # API 路由
│   ├── analyze.js              # 食物识别
│   ├── nutrition.js            # 营养计算
│   ├── recommend.js            # 推荐系统
│   └── food.js                 # 食物数据库查询
├── services/                   # 业务逻辑
│   ├── visionService.js        # AI 视觉识别
│   └── nutritionService.js     # 营养数据服务
├── utils/                      # 工具函数
│   └── nutritionCalculator.js  # 营养计算工具
└── data/                       # 数据
    └── foodDatabase.js         # 食物数据库
```

## 关键功能实现

### 1. 相机实时预览

使用 `AVCaptureSession` 实现相机预览：

```swift
class CameraManager: NSObject, ObservableObject {
    let session = AVCaptureSession()
    
    func configureSession() {
        // 配置输入输出
        session.beginConfiguration()
        // ... 添加视频输入和照片输出
        session.commitConfiguration()
    }
}
```

### 2. 食物识别流程

```
用户拍照/选图 -> 压缩图片 -> 上传后端 -> AI识别 -> 返回营养数据 -> 展示结果
```

### 3. 营养计算算法

使用 Mifflin-St Jeor 公式计算 BMR：

```javascript
// 男性
BMR = 10 * 体重 + 6.25 * 身高 - 5 * 年龄 + 5

// 女性  
BMR = 10 * 体重 + 6.25 * 身高 - 5 * 年龄 - 161
```

TDEE = BMR × 活动系数

### 4. 宏量营养素分配

- 蛋白质：1.6-2.2g/kg（根据目标调整）
- 脂肪：25-30% 总热量
- 碳水：剩余热量

## 自定义配置

### 修改 API 端点

编辑 `NutriCam-iOS/Services/FoodRecognitionService.swift`：

```swift
private let apiEndpoint = "https://your-domain.com/api/analyze"
```

### 添加新的食物类别

编辑 `backend/data/foodDatabase.js`：

```javascript
const foods = [
  // 添加新食物
  { 
    foodName: '新食物',
    calories: 100,
    protein: 10,
    carbs: 20,
    fat: 5,
    fiber: 2,
    tags: ['标签1', '标签2'],
    category: '分类'
  }
];
```

### 自定义识别模型

在 `backend/services/visionService.js` 中修改 AI 模型：

```javascript
const response = await openai.chat.completions.create({
  model: "gpt-4-vision-preview",  // 可以更换为其他模型
  // ...
});
```

## 调试技巧

### iOS 端调试

1. 开启 DEBUG 模式显示模拟数据：
   - 在 Xcode 中 Build Settings > Swift Compiler - Custom Flags > Other Swift Flags
   - 添加 `-D DEBUG`

2. 查看网络请求日志：
   ```swift
   // 在 FoodRecognitionService.swift 中添加
   print("Request URL: \(apiEndpoint)")
   print("Response: \(result)")
   ```

### 后端调试

```bash
# 开启详细日志
DEBUG=* npm start

# 或使用 ndb 调试器
npx ndb server.js
```

## 性能优化

### iOS 端

1. 图片压缩：在识别前压缩图片到 1024x1024
2. 缓存：使用 `NSCache` 缓存识别结果
3. 异步处理：识别过程使用后台线程

### 后端

1. 启用 Redis 缓存（可选）
2. 图片压缩后再传给 AI API
3. 使用 CDN 加速静态资源

## 安全注意事项

1. **API Keys**：永远不要将 API Keys 提交到 Git
2. **用户数据**：食物记录存储在本地，不上传到服务器
3. **图片处理**：用户图片分析后自动删除，不保留

## 常见问题

### Q: 相机黑屏？
检查 `Info.plist` 中是否添加了相机权限描述：
```xml
<key>NSCameraUsageDescription</key>
<string>需要使用相机识别食物</string>
```

### Q: API 返回错误？
检查后端服务是否运行，API Key 是否配置正确。

### Q: 识别不准确？
- 确保光线充足
- 食物占画面主要部分
- 避免模糊或反光