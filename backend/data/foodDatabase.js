/**
 * 本地食物数据库
 * 包含常见食物的营养数据
 */

const foods = [
  // 主食类
  { foodName: '米饭', calories: 116, protein: 2.6, carbs: 25.9, fat: 0.3, fiber: 0.3, tags: ['主食', '中餐', '谷物'], category: 'grains' },
  { foodName: '糙米饭', calories: 111, protein: 2.6, carbs: 23, fat: 0.9, fiber: 1.8, tags: ['主食', '粗粮', '健康'], category: 'grains' },
  { foodName: '面条(煮)', calories: 137, protein: 4.5, carbs: 25.0, fat: 2.1, fiber: 1.8, tags: ['主食', '面食'], category: 'grains' },
  { foodName: '馒头', calories: 223, protein: 7.0, carbs: 47.0, fat: 1.1, fiber: 1.3, tags: ['主食', '面食', '早餐'], category: 'grains' },
  { foodName: '燕麦片', calories: 389, protein: 16.9, carbs: 66.0, fat: 6.9, fiber: 10.6, tags: ['主食', '早餐', '粗粮'], category: 'grains' },
  { foodName: '红薯', calories: 86, protein: 1.6, carbs: 20.1, fat: 0.1, fiber: 3.0, tags: ['主食', '粗粮'], category: 'grains' },
  { foodName: '玉米', calories: 112, protein: 4.0, carbs: 22.8, fat: 1.2, fiber: 2.7, tags: ['主食', '粗粮'], category: 'grains' },
  
  // 肉蛋奶
  { foodName: '鸡蛋(煮)', calories: 155, protein: 13.0, carbs: 1.1, fat: 11.0, fiber: 0, tags: ['蛋', '早餐', '高蛋白'], category: 'meat' },
  { foodName: '鸡胸肉(煮)', calories: 165, protein: 31.0, carbs: 0, fat: 3.6, fiber: 0, tags: ['肉', '高蛋白', '低脂'], category: 'meat' },
  { foodName: '猪瘦肉', calories: 143, protein: 26.0, carbs: 0, fat: 3.5, fiber: 0, tags: ['肉', '高蛋白'], category: 'meat' },
  { foodName: '牛肉(瘦)', calories: 250, protein: 26.0, carbs: 0, fat: 15.0, fiber: 0, tags: ['肉', '高蛋白', '高铁'], category: 'meat' },
  { foodName: '三文鱼', calories: 208, protein: 20.0, carbs: 0, fat: 13.0, fiber: 0, tags: ['海鲜', '高蛋白', '健康脂肪'], category: 'seafood' },
  { foodName: '牛奶(全脂)', calories: 61, protein: 3.2, carbs: 4.8, fat: 3.3, fiber: 0, tags: ['奶', '饮品'], category: 'meat' },
  { foodName: '豆腐', calories: 76, protein: 8.0, carbs: 1.9, fat: 4.8, fiber: 0.3, tags: ['豆制品', '植物蛋白'], category: 'meat' },
  
  // 蔬菜
  { foodName: '西兰花', calories: 34, protein: 2.8, carbs: 7.0, fat: 0.4, fiber: 2.6, tags: ['蔬菜', '高纤维', '维生素'], category: 'vegetables' },
  { foodName: '菠菜', calories: 23, protein: 2.9, carbs: 3.6, fat: 0.4, fiber: 2.2, tags: ['蔬菜', '高铁'], category: 'vegetables' },
  { foodName: '西红柿', calories: 18, protein: 0.9, carbs: 3.9, fat: 0.2, fiber: 1.2, tags: ['蔬菜', '维生素C'], category: 'vegetables' },
  { foodName: '黄瓜', calories: 16, protein: 0.7, carbs: 3.6, fat: 0.1, fiber: 0.5, tags: ['蔬菜', '低热量'], category: 'vegetables' },
  { foodName: '胡萝卜', calories: 41, protein: 0.9, carbs: 9.6, fat: 0.2, fiber: 2.8, tags: ['蔬菜', '维生素A'], category: 'vegetables' },
  { foodName: '白菜', calories: 13, protein: 1.5, carbs: 2.2, fat: 0.2, fiber: 1.0, tags: ['蔬菜', '低热量'], category: 'vegetables' },
  
  // 水果
  { foodName: '苹果', calories: 52, protein: 0.3, carbs: 14.0, fat: 0.2, fiber: 2.4, tags: ['水果', '膳食纤维'], category: 'fruits' },
  { foodName: '香蕉', calories: 89, protein: 1.1, carbs: 22.8, fat: 0.3, fiber: 2.6, tags: ['水果', '高钾'], category: 'fruits' },
  { foodName: '橙子', calories: 47, protein: 0.9, carbs: 11.8, fat: 0.1, fiber: 2.4, tags: ['水果', '维生素C'], category: 'fruits' },
  { foodName: '蓝莓', calories: 57, protein: 0.7, carbs: 14.5, fat: 0.3, fiber: 2.4, tags: ['水果', '抗氧化'], category: 'fruits' },
  { foodName: '西瓜', calories: 30, protein: 0.6, carbs: 7.6, fat: 0.2, fiber: 0.4, tags: ['水果', '水分'], category: 'fruits' },
  { foodName: '牛油果', calories: 160, protein: 2.0, carbs: 8.5, fat: 14.7, fiber: 6.7, tags: ['水果', '健康脂肪', '高热量'], category: 'fruits' },
  
  // 中餐菜肴
  { foodName: '宫保鸡丁', calories: 180, protein: 15.0, carbs: 12.0, fat: 8.0, fiber: 2.0, tags: ['中餐', '主菜', '辣'], category: 'meat' },
  { foodName: '番茄炒蛋', calories: 95, protein: 6.0, carbs: 5.0, fat: 6.0, fiber: 1.0, tags: ['中餐', '家常菜'], category: 'meat' },
  { foodName: '麻婆豆腐', calories: 120, protein: 8.0, carbs: 6.0, fat: 8.0, fiber: 1.5, tags: ['中餐', '豆制品', '辣'], category: 'meat' },
  { foodName: '清蒸鱼', calories: 120, protein: 22.0, carbs: 0, fat: 3.0, fiber: 0, tags: ['中餐', '海鲜', '低脂'], category: 'seafood' },
  { foodName: '青菜', calories: 35, protein: 2.0, carbs: 5.0, fat: 1.0, fiber: 2.0, tags: ['中餐', '蔬菜'], category: 'vegetables' },
  { foodName: '饺子(猪肉)', calories: 220, protein: 8.0, carbs: 25.0, fat: 10.0, fiber: 1.5, tags: ['中餐', '面食'], category: 'grains' },
  
  // 零食
  { foodName: '坚果混合', calories: 607, protein: 20.0, carbs: 21.0, fat: 54.0, fiber: 8.0, tags: ['零食', '健康脂肪', '高热量'], category: 'snacks' },
  { foodName: '酸奶(无糖)', calories: 59, protein: 10.0, carbs: 3.6, fat: 0.4, fiber: 0, tags: ['零食', '益生菌'], category: 'snacks' },
  
  // 饮品
  { foodName: '黑咖啡', calories: 2, protein: 0.3, carbs: 0, fat: 0, fiber: 0, tags: ['饮品', '提神', '零热量'], category: 'beverages' },
  { foodName: '绿茶', calories: 1, protein: 0.1, carbs: 0.2, fat: 0, fiber: 0, tags: ['饮品', '抗氧化'], category: 'beverages' },
  { foodName: '橙汁', calories: 45, protein: 0.7, carbs: 10.4, fat: 0.2, fiber: 0.2, tags: ['饮品', '维生素C'], category: 'beverages' }
];

// 数据库操作方法
const foodDatabase = {
  /**
   * 获取所有食物
   */
  getAllFoods() {
    return foods;
  },
  
  /**
   * 根据名称获取食物
   */
  getFoodByName(name) {
    return foods.find(f => 
      f.foodName.toLowerCase() === name.toLowerCase() ||
      f.foodName.includes(name) ||
      name.includes(f.foodName)
    );
  },
  
  /**
   * 搜索食物
   */
  searchFoods(query, limit = 10) {
    const lowerQuery = query.toLowerCase();
    return foods
      .filter(f => 
        f.foodName.toLowerCase().includes(lowerQuery) ||
        f.tags?.some(t => t.toLowerCase().includes(lowerQuery))
      )
      .slice(0, limit);
  },
  
  /**
   * 根据分类获取食物
   */
  getFoodsByCategory(category) {
    return foods.filter(f => f.category === category);
  },
  
  /**
   * 获取热门食物
   */
  getPopularFoods(limit = 20) {
    // 返回最常见的食物
    const popularNames = ['米饭', '鸡蛋', '鸡胸肉', '牛奶', '苹果', '香蕉', '西兰花', '燕麦片'];
    return popularNames
      .map(name => this.getFoodByName(name))
      .filter(Boolean)
      .slice(0, limit);
  },
  
  /**
   * 获取相似食物
   */
  getSimilarFoods(foodName, limit = 5) {
    const food = this.getFoodByName(foodName);
    if (!food) return [];
    
    // 根据分类和标签找相似
    return foods
      .filter(f => f.foodName !== food.foodName)
      .filter(f => 
        f.category === food.category ||
        f.tags?.some(t => food.tags?.includes(t))
      )
      .map(f => ({
        ...f,
        similarity: calculateSimilarity(food, f)
      }))
      .sort((a, b) => b.similarity - a.similarity)
      .slice(0, limit);
  }
};

/**
 * 计算两个食物的相似度
 */
function calculateSimilarity(food1, food2) {
  // 热量相似度
  const calorieDiff = Math.abs(food1.calories - food2.calories) / Math.max(food1.calories, 1);
  const calorieSim = Math.max(0, 1 - calorieDiff / 2);
  
  // 营养素相似度
  const proteinDiff = Math.abs(food1.protein - food2.protein) / Math.max(food1.protein, 1);
  const proteinSim = Math.max(0, 1 - proteinDiff);
  
  // 标签重合度
  const tagOverlap = food1.tags?.filter(t => food2.tags?.includes(t)).length || 0;
  const tagSim = tagOverlap / Math.max(food1.tags?.length || 1, food2.tags?.length || 1);
  
  return (calorieSim * 0.4 + proteinSim * 0.3 + tagSim * 0.3);
}

module.exports = foodDatabase;