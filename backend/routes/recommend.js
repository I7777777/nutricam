const express = require('express');
const router = express.Router();
const foodDatabase = require('../data/foodDatabase');

/**
 * POST /api/recommend
 * 根据用户营养需求和偏好推荐食物
 * 
 * 请求:
 * {
 *   "remainingCalories": 500,
 *   "remainingMacros": {
 *     "protein": 30,
 *     "carbs": 60,
 *     "fat": 15
 *   },
 *   "preferences": ["chinese", "low_fat"],
 *   "exclude": ["海鲜", "坚果"]
 * }
 * 
 * 响应:
 * {
 *   "recommendations": [
 *     {
 *       "foodName": "清蒸鱼",
 *       "calories": 120,
 *       "matchScore": 0.95,
 *       "reason": "高蛋白低脂肪，符合您的营养需求"
 *     }
 *   ]
 * }
 */
router.post('/', (req, res) => {
  try {
    const { 
      remainingCalories, 
      remainingMacros, 
      preferences = [], 
      exclude = [],
      mealType = 'dinner'
    } = req.body;
    
    // 获取候选食物
    let candidates = foodDatabase.getAllFoods();
    
    // 排除过敏/不喜欢的食物
    if (exclude.length > 0) {
      candidates = candidates.filter(food => 
        !exclude.some(ex => food.foodName.includes(ex) || food.tags?.includes(ex))
      );
    }
    
    // 根据餐别筛选
    candidates = filterByMealType(candidates, mealType);
    
    // 根据偏好筛选
    if (preferences.includes('low_fat')) {
      candidates = candidates.filter(f => f.fat < 10);
    }
    if (preferences.includes('high_protein')) {
      candidates = candidates.filter(f => f.protein > 15);
    }
    if (preferences.includes('low_carb')) {
      candidates = candidates.filter(f => f.carbs < 20);
    }
    
    // 计算匹配分数并排序
    const scored = candidates.map(food => {
      const score = calculateMatchScore(food, remainingCalories, remainingMacros);
      return { ...food, matchScore: score };
    });
    
    // 按匹配分数排序并取前5个
    const recommendations = scored
      .sort((a, b) => b.matchScore - a.matchScore)
      .slice(0, 5)
      .map(food => ({
        foodName: food.foodName,
        calories: food.calories,
        protein: food.protein,
        carbs: food.carbs,
        fat: food.fat,
        matchScore: food.matchScore,
        reason: generateReason(food, remainingMacros),
        tags: food.tags || []
      }));
    
    res.json({ recommendations, count: recommendations.length });
    
  } catch (error) {
    console.error('推荐失败:', error);
    res.status(500).json({ error: '生成推荐失败' });
  }
});

/**
 * GET /api/recommend/similar/:foodName
 * 获取相似食物推荐
 */
router.get('/similar/:foodName', (req, res) => {
  try {
    const { foodName } = req.params;
    const similar = foodDatabase.getSimilarFoods(foodName, 5);
    
    res.json({ 
      original: foodName,
      similar: similar.map(f => ({
        foodName: f.foodName,
        calories: f.calories,
        similarity: f.similarity
      }))
    });
    
  } catch (error) {
    res.status(500).json({ error: '查询失败' });
  }
});

// 辅助函数
function filterByMealType(foods, mealType) {
  const mealTags = {
    'breakfast': ['早餐', '主食', '饮品'],
    'lunch': ['午餐', '主菜', '主食'],
    'dinner': ['晚餐', '主菜', '汤'],
    'snack': ['零食', '水果', '饮品']
  };
  
  const tags = mealTags[mealType] || [];
  return foods.filter(f => 
    !f.tags || f.tags.some(t => tags.includes(t)) || f.tags.length === 0
  );
}

function calculateMatchScore(food, targetCalories, targetMacros) {
  let score = 1.0;
  
  // 热量匹配度
  if (targetCalories) {
    const calorieDiff = Math.abs(food.calories - targetCalories) / targetCalories;
    score *= Math.max(0, 1 - calorieDiff);
  }
  
  // 宏量营养素匹配度
  if (targetMacros) {
    if (targetMacros.protein > 0) {
      const proteinScore = Math.min(food.protein / targetMacros.protein, 1);
      score *= (0.5 + 0.5 * proteinScore);
    }
    if (targetMacros.carbs > 0 && food.carbs > targetMacros.carbs * 1.5) {
      score *= 0.8;
    }
  }
  
  return Math.round(score * 100) / 100;
}

function generateReason(food, remainingMacros) {
  const reasons = [];
  
  if (remainingMacros?.protein > 0 && food.protein >= remainingMacros.protein * 0.3) {
    reasons.push('蛋白质丰富');
  }
  if (food.fiber > 3) {
    reasons.push('膳食纤维高');
  }
  if (food.calories < 150) {
    reasons.push('低热量');
  }
  
  if (reasons.length === 0) {
    return '营养均衡，适合您的需求';
  }
  
  return reasons.join('，') + '，适合您的需求';
}

module.exports = router;