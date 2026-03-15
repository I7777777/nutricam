#!/bin/bash
# start-backend.sh - 快速启动 NutriCam 后端服务

echo "🚀 启动 NutriCam 后端服务..."

cd "$(dirname "$0")/backend"

# 检查 Node.js
if ! command -v node &> /dev/null; then
    echo "❌ 未安装 Node.js，请先安装: https://nodejs.org/"
    exit 1
fi

# 检查依赖
if [ ! -d "node_modules" ]; then
    echo "📦 安装依赖..."
    npm install
fi

# 检查环境变量
if [ ! -f ".env" ]; then
    echo "⚠️  未找到 .env 文件，从模板创建..."
    cp .env.example .env
    echo "📝 请编辑 .env 文件，填入你的 OpenAI API Key"
    echo "   获取地址: https://platform.openai.com/api-keys"
fi

echo "✅ 启动服务中..."
npm start