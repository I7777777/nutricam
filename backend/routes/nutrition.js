const express = require('express');
const router = express.Router();
const { calculateTDEE, calculateMacroGoals } = require('../utils/nutritionCalculator');

/**
 * POST /api/nutrition/calculate
 * 根据用户身体数据计算每日营养需求
 * 
 * 请求:
 * {
 *   "gender": "male",
 *   "age": 25,
 *   "height": 170,
 *   "weight": 65,
 *   "activityLevel": "moderate",
 *   "goal": "lose_weight"
 * }
 * 
 * 响应:
 * {
 *   "bmr": 1580,
 *   "tdee": 2449,
 *   "targetCalories": 2082,
 *   "macros": {
 *     "protein": 130,
 *     "carbs": 208,
 *     "fat": 69,
 *     "fiber": 30
 *   },
 *   "recommendations": ["建议每公斤体重摄入2g蛋白质", "..."]
 * }
 */
router.post('/calculate', (req, res) => {
  try {
    const { gender, age, height, weight, activityLevel, goal } = req.body;
    
    // 参数验证
    if (!gender || !age || !height || !weight || !activityLevel) {
      return res.status(400).json({ 
        error: '缺少必要参数',
        required: ['gender', 'age', 'height', 'weight', 'activityLevel']
      });
    }
    
    // 计算BMR和TDEE
    const bmr = calculateTDEE(gender, age, height, weight, 'bmr');
    const tdee = calculateTDEE(gender, age, height, weight, activityLevel);
    
    // 根据目标调整热量
    const goalMultipliers = {
      'lose_weight': 0.85,
      'maintain': 1.0,
      'gain_weight': 1.15,
      'build_muscle': 1.1
    };
    
    const multiplier = goalMultipliers[goal] || 1.0;
    const targetCalories = Math.round(tdee * multiplier);
    
    // 计算宏量营养素目标
    const macros = calculateMacroGoals(targetCalories, weight, goal);
    
    // 生成建议
    const recommendations = generateRecommendations(goal, macros, weight);
    
    res.json({
      bmr: Math.round(bmr),
      tdee: Math.round(tdee),
      targetCalories,
      macros,
      recommendations,
      bmi: calculateBMI(height, weight)
    });
    
  } catch (error) {
    console.error('计算失败:', error);
    res.status(500).json({ error: '计算失败' });
  }
});

/**
 * GET /api/nutrition/food/:name
 * 查询食物营养信息
 */
router.get('/food/:name', async (req, res) => {
  try {
    const { name } = req.params;
    const { getFoodByName } = require('../services/nutritionService');
    
    const foodData = await getFoodByName(name);
    
    if (!foodData) {
      return res.status(404).json({ error: '未找到该食物' });
    }
    
    res.json(foodData);
    
  } catch (error) {
    res.status(500).json({ error: '查询失败' });
  }
});

// 辅助函数
function calculateBMI(height, weight) {
  const heightM = height / 100;
  return (weight / (heightM * heightM)).toFixed(1);
}

function generateRecommendations(goal, macros, weight) {
  const recommendations = [];
  
  switch (goal) {
    case 'lose_weight':
      recommendations.push('减脂期间建议每日热量缺口控制在300-500千卡');
      recommendations.push('蛋白质摄入量充足，有助于维持肌肉量');
      recommendations.push('优先选择低GI碳水化合物，避免血糖波动');
      break;
    case 'build_muscle':
      recommendations.push('增肌期间建议每公斤体重摄入1.6-2.2g蛋白质');
      recommendations.push('训练后30分钟内补充蛋白质和碳水，促进肌肉恢复');
      recommendations.push('保证充足睡眠，肌肉在休息时生长');
      break;
    case 'gain_weight':
      recommendations.push('增重建议少食多餐，每天5-6餐');
      recommendations.push('选择营养密度高的食物，如坚果、牛油果、全脂奶制品');
      break;
    default:
      recommendations.push('保持均衡饮食，每餐包含蛋白质、碳水化合物和健康脂肪');
      recommendations.push('每日饮水2000ml以上');
  }
  
  return recommendations;
}

module.exports = router;