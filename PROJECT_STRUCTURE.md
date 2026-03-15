# NutriCam 项目文件清单

## 项目结构

```
ios-nutrition-app/
├── README.md                       # 项目说明文档
├── .gitignore                      # Git 忽略配置
│
├── NutriCam-iOS/                   # iOS 客户端
│   ├── NutriCamApp.swift           # 应用入口
│   ├── Info.plist                  # 应用配置
│   │
│   ├── Views/                      # SwiftUI 视图
│   │   ├── ContentView.swift       # 主界面 (TabView)
│   │   ├── CameraView.swift        # 相机识别界面 (主要功能)
│   │   ├── NutritionDashboard.swift # 营养仪表板
│   │   ├── ProfileView.swift       # 用户资料
│   │   ├── EditProfileView.swift   # 编辑资料/目标
│   │   └── ImagePicker.swift       # 图片选择器
│   │
│   ├── Models/                     # 数据模型
│   │   ├── FoodAnalysis.swift      # 食物分析结果
│   │   └── UserProfile.swift       # 用户档案 (ObservableObject)
│   │
│   ├── Managers/                   # 管理器
│   │   └── CameraManager.swift     # 相机管理 (AVFoundation)
│   │
│   ├── Services/                   # 服务层
│   │   └── FoodRecognitionService.swift  # 食物识别 API
│   │
│   └── NutriCam.xcodeproj/         # Xcode 项目
│       └── project.pbxproj         # 项目配置文件
│
├── backend/                        # Node.js 后端
│   ├── server.js                   # 服务入口
│   ├── package.json                # 依赖配置
│   ├── .env.example                # 环境变量模板
│   ├── Dockerfile                  # Docker 配置
│   │
│   ├── routes/                     # API 路由
│   │   ├── analyze.js              # POST /api/analyze - 食物识别
│   │   ├── nutrition.js            # POST /api/nutrition/calculate
│   │   ├── recommend.js            # POST /api/recommend
│   │   └── food.js                 # GET /api/food/*
│   │
│   ├── services/                   # 业务逻辑
│   │   ├── visionService.js        # OpenAI Vision API
│   │   └── nutritionService.js     # 营养数据服务
│   │
│   ├── utils/                      # 工具函数
│   │   └── nutritionCalculator.js  # BMR/TDEE/宏量营养素计算
│   │
│   └── data/                       # 数据
│       └── foodDatabase.js         # 本地食物数据库 (100+ 食物)
│
└── docs/                           # 文档
    ├── DEVELOPMENT.md              # 开发指南
    └── DEPLOYMENT.md               # 部署指南
```

## 核心功能实现

### 1. 相机实时识别
- 使用 AVFoundation 实现相机预览
- Vision 框架处理图像
- 支持实时取景和拍照识别
- 扫描动画效果

### 2. 食物 AI 识别
- 集成 OpenAI GPT-4 Vision API
- 后端提供图片压缩和预处理
- 返回食物名称、营养数据、建议

### 3. 营养计算
- BMR: Mifflin-St Jeor 公式
- TDEE: 根据活动量计算
- 宏量营养素: 根据目标自动分配
- BMI 计算和分类

### 4. 个性化推荐
- 基于剩余热量推荐食物
- 考虑用户偏好和限制
- 匹配分数算法

## API 端点

| 方法 | 端点 | 功能 |
|------|------|------|
| POST | /api/analyze | 识别食物图片 |
| POST | /api/nutrition/calculate | 计算营养需求 |
| POST | /api/recommend | 获取食物推荐 |
| GET | /api/food/search | 搜索食物 |
| GET | /api/food/categories | 获取分类 |
| GET | /health | 健康检查 |

## 技术栈

### iOS
- SwiftUI (界面)
- AVFoundation (相机)
- Charts (iOS 16+ 数据可视化)
- UserDefaults (本地存储)

### 后端
- Node.js + Express
- OpenAI API (GPT-4 Vision)
- Multer (文件上传)
- Sharp (图片处理)

## 部署步骤

1. **后端部署**
   ```bash
   cd backend
   npm install
   # 配置 .env 文件
   npm start
   ```

2. **iOS 配置**
   - 打开 `NutriCam.xcodeproj`
   - 修改 API 端点
   - 配置签名
   - Build & Run

3. **App Store 发布**
   - 注册 Apple Developer 账号 ($99/年)
   - 创建 App ID
   - 配置证书
   - Archive → Upload

## 环境变量

```bash
# backend/.env
OPENAI_API_KEY=sk-xxx
USDA_API_KEY=optional
PORT=3000
NODE_ENV=production
```

## 下一步开发建议

1. **增强识别**: 训练自己的食物识别模型
2. **历史记录**: 添加数据库持久化 (CoreData/SQLite)
3. **社交功能**: 添加分享和好友系统
4. **订阅服务**: Apple In-App Purchase
5. **Apple Health**: 集成健康应用

---
总计代码量: ~4000+ 行
文件数: 28 个
