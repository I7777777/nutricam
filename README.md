# NutriCam - iOS 食物热量识别应用

一款能够用相机实时识别食物热量、分析营养成分，并根据个人身体状况提供个性化营养建议的 iOS 应用。

## 功能特性

### 1. 智能食物识别
- 📷 相机实时取景框识别
- 📸 照片识别（支持相册选择）
- 🤖 AI 驱动的食物识别（支持 OpenAI Vision API）
- 📊 精准营养数据分析

### 2. 营养追踪
- 📈 每日营养摄入统计
- 🎯 个性化营养目标设置
- 📱 直观的图表展示（热量、蛋白质、碳水、脂肪）
- 🕐 按餐别记录（早餐/午餐/晚餐/加餐）

### 3. 个性化推荐
- 👤 基于身体数据（性别、年龄、身高、体重、活动量）
- 🎯 多种健康目标（减脂/保持/增肌/增重）
- 💡 智能营养建议
- 🍽️ 食物推荐系统

### 4. 健康分析
- 📊 BMI 计算
- 🔥 BMR / TDEE 计算
- 🥗 宏量营养素目标自动计算
- 💪 个性化健康建议

## 技术架构

```
nutricam/
├── NutriCam-iOS/           # iOS 客户端 (SwiftUI)
│   ├── Views/              # 界面视图
│   ├── Models/             # 数据模型
│   ├── Managers/           # 管理器 (相机等)
│   ├── Services/           # 服务 (API调用等)
│   └── NutriCam.xcodeproj/ # Xcode 项目
├── backend/                # 后端服务 (Node.js)
│   ├── routes/             # API 路由
│   ├── services/           # 业务逻辑
│   ├── utils/              # 工具函数
│   └── data/               # 食物数据库
└── docs/                   # 文档
```

## 系统要求

### iOS 客户端
- iOS 15.0+
- iPhone 8 或更新机型
- 相机权限
- 照片库权限

### 后端服务
- Node.js 18+
- npm 或 yarn

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/yourusername/nutricam.git
cd nutricam
```

### 2. 配置后端

```bash
cd backend
npm install

# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，填入你的 API Keys
vi .env
```

**必需的 API Keys:**
- `OPENAI_API_KEY` - OpenAI Vision API（食物识别）
- `USDA_API_KEY` - USDA FoodData Central（可选，营养数据库）

启动后端服务：
```bash
npm start
# 或开发模式
npm run dev
```

### 3. 配置 iOS 客户端

在 Xcode 中打开项目：
```bash
open NutriCam-iOS/NutriCam.xcodeproj
```

修改 `FoodRecognitionService.swift` 中的 API 端点：
```swift
private let apiEndpoint = "https://your-backend-url.com/api/analyze"
```

### 4. 构建并运行

- 选择目标设备（iPhone 或模拟器）
- 点击 Run 按钮（⌘+R）

## API 文档

### 识别食物
```
POST /api/analyze
Content-Type: multipart/form-data

Body:
- image: 图片文件
- language: 语言代码 (默认 zh-CN)

Response:
{
  "foodName": "宫保鸡丁",
  "calories": 180,
  "protein": 15.0,
  "carbs": 12.0,
  "fat": 8.0,
  "fiber": 2.0,
  "confidence": 0.92,
  "nutritionAdvice": "蛋白质含量丰富...",
  "alternatives": ["麻婆豆腐", "鱼香肉丝"]
}
```

### 计算营养需求
```
POST /api/nutrition/calculate
Content-Type: application/json

Body:
{
  "gender": "male",
  "age": 25,
  "height": 170,
  "weight": 65,
  "activityLevel": "moderate",
  "goal": "lose_weight"
}

Response:
{
  "bmr": 1580,
  "tdee": 2449,
  "targetCalories": 2082,
  "macros": {
    "protein": 130,
    "carbs": 208,
    "fat": 69,
    "fiber": 30
  }
}
```

### 获取推荐
```
POST /api/recommend
Content-Type: application/json

Body:
{
  "remainingCalories": 500,
  "remainingMacros": {
    "protein": 30,
    "carbs": 60,
    "fat": 15
  },
  "preferences": ["chinese", "low_fat"]
}
```

## 部署

### 后端部署 (Docker)

```bash
cd backend

# 构建镜像
docker build -t nutricam-backend .

# 运行容器
docker run -p 3000:3000 --env-file .env nutricam-backend
```

### iOS 发布到 App Store

1. 在 Apple Developer 中心注册 App ID
2. 配置证书和描述文件
3. 在 Xcode 中选择 Product > Archive
4. 使用 Organizer 上传到 App Store Connect

## 技术栈

### 前端
- SwiftUI
- AVFoundation (相机)
- Vision (图像处理)
- Charts (数据可视化)

### 后端
- Node.js
- Express
- OpenAI API
- Multer (文件上传)

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License

## 致谢

- USDA FoodData Central API
- OpenAI GPT-4 Vision
- SwiftUI Charts