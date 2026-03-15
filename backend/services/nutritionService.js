const axios = require('axios');
const foodDatabase = require('../data/foodDatabase');

const USDA_API_KEY = process.env.USDA_API_KEY;
const USDA_API_URL = 'https://api.nal.usda.gov/fdc/v1';

/**
 * 获取食物营养数据
 * @param {string} foodName - 食物名称
 * @param {string} language - 语言
 * @returns {Promise<Object>} 营养数据
 */
async function getNutritionData(foodName, language = 'zh-CN') {
  // 先查询本地数据库
  const localData = foodDatabase.getFoodByName(foodName);
  if (localData) {
    return localData;
  }
  
  // 如果本地没有，查询USDA API
  if (USDA_API_KEY) {
    try {
      const usdaData = await queryUSDA(foodName);
      if (usdaData) {
        return usdaData;
      }
    } catch (error) {
      console.log('USDA查询失败，使用估算值');
    }
  }
  
  // 返回估算值
  return estimateNutrition(foodName);
}

/**
 * 根据名称获取食物
 */
async function getFoodByName(foodName) {
  return foodDatabase.getFoodByName(foodName);
}

/**
 * 查询USDA FoodData Central API
 */
async function queryUSDA(foodName) {
  try {
    const response = await axios.get(`${USDA_API_URL}/foods/search`, {
      params: {
        query: foodName,
        api_key: USDA_API_KEY,
        pageSize: 1,
        dataType: 'Foundation,SR Legacy'
      }
    });
    
    if (response.data.foods && response.data.foods.length > 0) {
      const food = response.data.foods[0];
      return parseUSDAFood(food);
    }
    
    return null;
  } catch (error) {
    console.error('USDA API错误:', error.message);
    return null;
  }
}

/**
 * 解析USDA食物数据
 */
function parseUSDAFood(food) {
  const nutrients = {};
  
  // 提取关键营养素
  food.foodNutrients.forEach(n => {
    const name = n.nutrientName?.toLowerCase() || '';
    const value = n.value || 0;
    
    if (name.includes('energy') || name.includes('calorie')) {
      nutrients.calories = value;
    } else if (name.includes('protein')) {
      nutrients.protein = value;
    } else if (name.includes('carbohydrate')) {
      nutrients.carbs = value;
    } else if (name.includes('total lipid') || name.includes('fat')) {
      nutrients.fat = value;
    } else if (name.includes('fiber')) {
      nutrients.fiber = value;
    }
  });
  
  return {
    foodName: food.description,
    calories: nutrients.calories || 0,
    protein: nutrients.protein || 0,
    carbs: nutrients.carbs || 0,
    fat: nutrients.fat || 0,
    fiber: nutrients.fiber || 0,
    source: 'USDA'
  };
}

/**
 * 估算食物营养（基于关键词匹配）
 */
function estimateNutrition(foodName) {
  const name = foodName.toLowerCase();
  
  // 简单的关键词匹配
  if (name.includes('chicken') || name.includes('鸡')) {
    return { foodName, calories: 165, protein: 31, carbs: 0, fat: 3.6, fiber: 0, estimated: true };
  }
  if (name.includes('beef') || name.includes('牛')) {
    return { foodName, calories: 250, protein: 26, carbs: 0, fat: 17, fiber: 0, estimated: true };
  }
  if (name.includes('rice') || name.includes('米饭')) {
    return { foodName, calories: 130, protein: 2.7, carbs: 28, fat: 0.3, fiber: 0.4, estimated: true };
  }
  if (name.includes('vegetable') || name.includes('蔬菜')) {
    return { foodName, calories: 25, protein: 1.5, carbs: 5, fat: 0.1, fiber: 2, estimated: true };
  }
  if (name.includes('fruit') || name.includes('水果')) {
    return { foodName, calories: 60, protein: 0.5, carbs: 15, fat: 0.2, fiber: 2, estimated: true };
  }
  
  // 默认估算
  return { 
    foodName, 
    calories: 150, 
    protein: 5, 
    carbs: 20, 
    fat: 5, 
    fiber: 1,
    estimated: true,
    note: '此数据为估算值，仅供参考'
  };
}

module.exports = {
  getNutritionData,
  getFoodByName
};