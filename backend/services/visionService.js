const OpenAI = require('openai');
const fs = require('fs').promises;

const openai = new OpenAI({
  apiKey: process.env.OPENAI_API_KEY
});

/**
 * 分析食物图片
 * @param {string} imagePathOrBase64 - 图片路径或Base64编码
 * @param {string} language - 语言代码
 * @returns {Promise<Object>} 食物分析结果
 */
async function analyzeFoodImage(imagePathOrBase64, language = 'zh-CN') {
  try {
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
    
    // 压缩图片以减少API调用成本（简化版，直接截断Base64）
    const compressedBuffer = compressImage(Buffer.from(base64Image, 'base64'));
    base64Image = compressedBuffer.toString('base64');
    
    const prompt = language === 'zh-CN' 
      ? `分析这张图片中的食物，提供以下信息（JSON格式）：
         - foodName: 食物名称（中文）
         - confidence: 识别置信度（0-1）
         - nutritionAdvice: 营养建议（中文，50字以内）
         - alternatives: 相似食物推荐（数组，3个）`
      : `Analyze the food in this image and provide (in JSON format):
         - foodName: food name
         - confidence: recognition confidence (0-1)
         - nutritionAdvice: nutrition advice (under 50 words)
         - alternatives: similar food recommendations (array, 3 items)`;
    
    const response = await openai.chat.completions.create({
      model: "gpt-4-vision-preview",
      messages: [
        {
          role: "user",
          content: [
            { type: "text", text: prompt },
            {
              type: "image_url",
              image_url: {
                url: `data:image/jpeg;base64,${base64Image}`,
                detail: "low" // 使用低分辨率以降低成本
              }
            }
          ]
        }
      ],
      max_tokens: 500,
      response_format: { type: "json_object" }
    });
    
    const result = JSON.parse(response.choices[0].message.content);
    
    return {
      foodName: result.foodName || '未知食物',
      confidence: result.confidence || 0.5,
      nutritionAdvice: result.nutritionAdvice || null,
      alternatives: result.alternatives || []
    };
    
  } catch (error) {
    console.error('Vision API调用失败:', error);
    // 返回默认结果
    return {
      foodName: '未知食物',
      confidence: 0,
      nutritionAdvice: '无法识别该食物，请尝试更清晰的照片',
      alternatives: []
    };
  }
}

/**
 * 压缩图片（简化版，限制大小）
 */
function compressImage(buffer) {
  // 简单限制：如果图片超过 2MB，截断（OpenAI 接受最大 20MB）
  const maxSize = 2 * 1024 * 1024; // 2MB
  if (buffer.length > maxSize) {
    console.log(`图片较大 (${(buffer.length / 1024 / 1024).toFixed(2)}MB)，直接截取前 2MB`);
    return buffer.slice(0, maxSize);
  }
  return buffer;
}

module.exports = {
  analyzeFoodImage
};