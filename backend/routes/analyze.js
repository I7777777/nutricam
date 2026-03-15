const express = require('express');
const router = express.Router();
const multer = require('multer');
const path = require('path');
const { analyzeFoodImage } = require('../services/visionService');
const { getNutritionData } = require('../services/nutritionService');

// 配置multer
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, 'uploads/');
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    cb(null, 'food-' + uniqueSuffix + path.extname(file.originalname));
  }
});

const upload = multer({ 
  storage,
  limits: { fileSize: 10 * 1024 * 1024 }, // 10MB
  fileFilter: (req, file, cb) => {
    if (file.mimetype.startsWith('image/')) {
      cb(null, true);
    } else {
      cb(new Error('只接受图片文件'));
    }
  }
});

/**
 * POST /api/analyze
 * 分析食物图片
 * 
 * 请求:
 * - image: Base64编码的图片 或 multipart/form-data 文件上传
 * - language: 语言 (默认 zh-CN)
 * 
 * 响应:
 * {
 *   "foodName": "宫保鸡丁",
 *   "calories": 180,
 *   "protein": 15.0,
 *   "carbs": 12.0,
 *   "fat": 8.0,
 *   "fiber": 2.0,
 *   "confidence": 0.92,
 *   "nutritionAdvice": "...",
 *   "alternatives": ["麻婆豆腐", "鱼香肉丝"]
 * }
 */
router.post('/', upload.single('image'), async (req, res) => {
  try {
    let imageData;
    
    // 处理文件上传
    if (req.file) {
      imageData = req.file.path;
    } 
    // 处理Base64编码
    else if (req.body.image) {
      imageData = req.body.image;
    } else {
      return res.status(400).json({ error: '请提供图片' });
    }
    
    const language = req.body.language || 'zh-CN';
    
    // 调用AI识别服务
    const analysisResult = await analyzeFoodImage(imageData, language);
    
    // 获取详细营养数据
    const nutritionData = await getNutritionData(analysisResult.foodName, language);
    
    // 合并结果
    const result = {
      ...analysisResult,
      ...nutritionData,
      timestamp: new Date().toISOString()
    };
    
    res.json(result);
    
  } catch (error) {
    console.error('分析失败:', error);
    res.status(500).json({ 
      error: '食物识别失败',
      message: error.message 
    });
  }
});

/**
 * POST /api/analyze/batch
 * 批量分析多张图片
 */
router.post('/batch', upload.array('images', 5), async (req, res) => {
  try {
    const files = req.files;
    if (!files || files.length === 0) {
      return res.status(400).json({ error: '请提供图片' });
    }
    
    const language = req.body.language || 'zh-CN';
    const results = [];
    
    for (const file of files) {
      const analysisResult = await analyzeFoodImage(file.path, language);
      const nutritionData = await getNutritionData(analysisResult.foodName, language);
      results.push({ ...analysisResult, ...nutritionData });
    }
    
    res.json({ results, count: results.length });
    
  } catch (error) {
    console.error('批量分析失败:', error);
    res.status(500).json({ error: '批量识别失败' });
  }
});

module.exports = router;