const express = require('express');
const router = express.Router();
const foodDatabase = require('../data/foodDatabase');

/**
 * GET /api/food/search?q=关键词
 * 搜索食物
 */
router.get('/search', (req, res) => {
  try {
    const { q, limit = 10 } = req.query;
    
    if (!q) {
      return res.status(400).json({ error: '请提供搜索关键词' });
    }
    
    const results = foodDatabase.searchFoods(q, parseInt(limit));
    
    res.json({
      query: q,
      results,
      count: results.length
    });
    
  } catch (error) {
    res.status(500).json({ error: '搜索失败' });
  }
});

/**
 * GET /api/food/categories
 * 获取食物分类
 */
router.get('/categories', (req, res) => {
  const categories = [
    { id: 'grains', name: '主食', icon: '🍚' },
    { id: 'meat', name: '肉蛋奶', icon: '🥩' },
    { id: 'vegetables', name: '蔬菜', icon: '🥬' },
    { id: 'fruits', name: '水果', icon: '🍎' },
    { id: 'seafood', name: '海鲜', icon: '🦐' },
    { id: 'snacks', name: '零食', icon: '🍪' },
    { id: 'beverages', name: '饮品', icon: '🥤' },
    { id: 'soup', name: '汤羹', icon: '🍲' }
  ];
  
  res.json({ categories });
});

/**
 * GET /api/food/category/:categoryId
 * 获取分类下的食物
 */
router.get('/category/:categoryId', (req, res) => {
  try {
    const { categoryId } = req.params;
    const foods = foodDatabase.getFoodsByCategory(categoryId);
    
    res.json({
      category: categoryId,
      foods,
      count: foods.length
    });
    
  } catch (error) {
    res.status(500).json({ error: '查询失败' });
  }
});

/**
 * GET /api/food/popular
 * 获取热门食物
 */
router.get('/popular', (req, res) => {
  const popular = foodDatabase.getPopularFoods(20);
  res.json({ foods: popular });
});

module.exports = router;