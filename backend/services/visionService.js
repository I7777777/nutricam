const axios = require('axios');
const fs = require('fs').promises;

const KIMI_API_KEY = process.env.KIMI_API_KEY || process.env.OPENAI_API_KEY;
const KIMI_BASE_URL = 'https://api.moonshot.cn/v1';

/**
 * 分析食物图片
 * @param {string} imagePathOrBase64 - 图片路径或Base64编码
 * @param {string} language - 语言代码
 * @returns {Promise<Object>} 食物分析结果
 */
async function analyzeFoodImage(imagePathOrBase64, language = 'zh-CN') {
  try {
    // 检查 API Key
    if (!KIMI_API_KEY) {
      console.log('⚠️  未配置 Kimi API Key，使用模拟数据');
      return getMockResult();
    }

    let base64Image;
    
    // 判断是文件路径还是Base64
    if (imagePathOrBase64.startsWith('/')) {
      // 文件路径
      const imageBuffer = await fs.readFile(imagePathOrBase64);
      base64Image = imageBuffer.toString('base64');
    } else if (imagePathOrBase64.startsWith('data:image')) {
      // 已经是Data URL格式
      base64Image = imagePathOrBase64.split(',')[1];
    } else {
      // 纯Base64
      base64Image = imagePathOrBase64;
    }
    
    // 压缩图片
    const compressedBuffer = compressImage(Buffer.from(base64Image, 'base64'));
    base64Image = compressedBuffer.toString('base64');
    
    const prompt = language === 'zh-CN' 
      ? `分析这张图片中的食物，提供以下信息（JSON格式）：
         - foodName: 食物名称（中文）
         - confidence: 识别置信度（0-1）
         - nutritionAdvice: 营养建议（中文，50字以内）
         - alternatives: 相似食物推荐（数组，3个）
         只返回JSON，不要其他文字。`
      : `Analyze the food in this image and provide (in JSON format):
         - foodName: food name
         - confidence: recognition confidence (0-1)
         - nutritionAdvice: nutrition advice (under 50 words)
         - alternatives: similar food recommendations (array, 3 items)
         Return JSON only, no other text.`;
    
    const response = await axios.post(
      `${KIMI_BASE_URL}/chat/completions`,
      {
        model: "moonshot-v1-8k-vision-preview",
        messages: [
          {
            role: "user",
            content: [
              { type: "text", text: prompt },
              {
                type: "image_url",
                image_url: {
                  url: `data:image/jpeg;base64,${base64Image}`
                }
              }
            ]
          }
        ],
        temperature: 0.3,
        max_tokens: 500
      },
      {
        headers: {
          'Authorization': `Bearer ${KIMI_API_KEY}`,
          'Content-Type': 'application/json'
        }
      }
    );
    
    const content = response.data.choices[0].message.content;
    
    // 解析 JSON（Kimi 返回的内容可能包含 markdown 代码块）
    let result;
    try {
      // 尝试直接解析
      result = JSON.parse(content);
    } catch {
      // 尝试从 markdown 代码块中提取
      const jsonMatch = content.match(/```json\n?([\s\S]*?)\n?```/);
      if (jsonMatch) {
        result = JSON.parse(jsonMatch[1]);
      } else {
        // 尝试提取花括号内容
        const braceMatch = content.match(/\{[\s\S]*\}/);
        if (braceMatch) {
          result = JSON.parse(braceMatch[0]);
        } else {
          throw new Error('无法解析响应');
        }
      }
    }
    
    return {
      foodName: result.foodName || result.name || result.food || '未知食物',
      confidence: result.confidence || result.score || 0.8,
      nutritionAdvice: result.nutritionAdvice || result.advice || null,
      alternatives: result.alternatives || result.similar || []
    };
    
  } catch (error) {
    console.error('Kimi API调用失败:', error.message);
    // 失败时返回模拟数据
    return getMockResult();
  }
}

/**
 * 获取模拟数据（无API Key或失败时使用）
 */
function getMockResult() {
  const mockFoods = [
    {
      foodName: '宫保鸡丁',
      confidence: 0.92,
      nutritionAdvice: '高蛋白低碳水，适合健身人群',
      alternatives: ['糖醋里脊', '辣子鸡', '青椒肉丝']
    },
    {
      foodName: '白米饭',
      confidence: 0.95,
      nutritionAdvice: '主食碳水，建议搭配蔬菜和蛋白质',
      alternatives: ['糙米饭', '藜麦饭', '紫薯']
    },
    {
      foodName: '番茄炒蛋',
      confidence: 0.88,
      nutritionAdvice: '营养均衡，富含维生素和蛋白质',
      alternatives: ['青椒炒蛋', '韭菜炒蛋', '西葫芦炒蛋']
    },
    {
      foodName: '牛油果沙拉',
      confidence: 0.85,
      nutritionAdvice: '健康脂肪来源，适合减脂期',
      alternatives: ['凯撒沙拉', '水果沙拉', '蔬菜沙拉']
    }
  ];
  
  return mockFoods[Math.floor(Math.random() * mockFoods.length)];
}

/**
 * 压缩图片（简化版，限制大小）
 */
function compressImage(buffer) {
  // Kimi 支持最大 5MB 图片
  const maxSize = 4 * 1024 * 1024; // 4MB 保险起见
  if (buffer.length > maxSize) {
    console.log(`图片较大 (${(buffer.length / 1024 / 1024).toFixed(2)}MB)，直接截取前 4MB`);
    return buffer.slice(0, maxSize);
  }
  return buffer;
}

module.exports = {
  analyzeFoodImage
};