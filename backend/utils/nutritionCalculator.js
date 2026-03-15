/**
 * 营养计算器工具
 */

/**
 * 计算基础代谢率 (BMR) - Mifflin-St Jeor 公式
 * @param {string} gender - 'male' 或 'female'
 * @param {number} age - 年龄
 * @param {number} height - 身高(cm)
 * @param {number} weight - 体重(kg)
 * @returns {number} BMR (千卡/天)
 */
function calculateBMR(gender, age, height, weight) {
  const weightComponent = 10 * weight;
  const heightComponent = 6.25 * height;
  const ageComponent = 5 * age;
  
  if (gender === 'male') {
    return weightComponent + heightComponent - ageComponent + 5;
  } else {
    return weightComponent + heightComponent - ageComponent - 161;
  }
}

/**
 * 计算每日总能量消耗 (TDEE)
 * @param {string} gender - 'male' 或 'female'
 * @param {number} age - 年龄
 * @param {number} height - 身高(cm)
 * @param {number} weight - 体重(kg)
 * @param {string} activityLevel - 活动水平
 * @returns {number} TDEE (千卡/天)
 */
function calculateTDEE(gender, age, height, weight, activityLevel) {
  const bmr = calculateBMR(gender, age, height, weight);
  
  const multipliers = {
    'bmr': 1.0,
    'sedentary': 1.2,      // 久坐不动
    'light': 1.375,        // 轻度活动
    'moderate': 1.55,      // 中度活动
    'active': 1.725,       // 高度活动
    'very_active': 1.9     // 专业运动
  };
  
  return bmr * (multipliers[activityLevel] || 1.2);
}

/**
 * 计算宏量营养素目标
 * @param {number} targetCalories - 目标热量
 * @param {number} weight - 体重(kg)
 * @param {string} goal - 目标类型
 * @returns {Object} 宏量营养素目标
 */
function calculateMacroGoals(targetCalories, weight, goal = 'maintain') {
  // 蛋白质系数 (g/kg)
  const proteinMultipliers = {
    'lose_weight': 2.0,      // 减脂高蛋白
    'maintain': 1.6,
    'gain_weight': 1.6,
    'build_muscle': 2.2      // 增肌高蛋白
  };
  
  const proteinMultiplier = proteinMultipliers[goal] || 1.6;
  const proteinGrams = weight * proteinMultiplier;
  const proteinCalories = proteinGrams * 4;
  
  // 脂肪占 25-30%
  const fatPercentage = goal === 'lose_weight' ? 0.25 : 0.3;
  const fatCalories = targetCalories * fatPercentage;
  const fatGrams = fatCalories / 9;
  
  // 碳水占剩余热量
  const carbCalories = targetCalories - proteinCalories - fatCalories;
  const carbGrams = Math.max(carbCalories / 4, 100); // 最低100g碳水
  
  // 膳食纤维
  const fiberGrams = targetCalories > 2500 ? 35 : 25;
  
  return {
    protein: Math.round(proteinGrams),
    carbs: Math.round(carbGrams),
    fat: Math.round(fatGrams),
    fiber: fiberGrams
  };
}

/**
 * 计算BMI
 * @param {number} height - 身高(cm)
 * @param {number} weight - 体重(kg)
 * @returns {number} BMI
 */
function calculateBMI(height, weight) {
  const heightM = height / 100;
  return weight / (heightM * heightM);
}

/**
 * 获取BMI分类
 * @param {number} bmi - BMI值
 * @returns {string} 分类
 */
function getBMICategory(bmi) {
  if (bmi < 18.5) return '偏瘦';
  if (bmi < 24) return '正常';
  if (bmi < 28) return '偏胖';
  return '肥胖';
}

/**
 * 计算理想体重 (Broca改良公式)
 * @param {number} height - 身高(cm)
 * @param {string} gender - 'male' 或 'female'
 * @returns {number} 理想体重(kg)
 */
function calculateIdealWeight(height, gender) {
  if (gender === 'male') {
    return (height - 80) * 0.7;
  } else {
    return (height - 70) * 0.6;
  }
}

module.exports = {
  calculateBMR,
  calculateTDEE,
  calculateMacroGoals,
  calculateBMI,
  getBMICategory,
  calculateIdealWeight
};