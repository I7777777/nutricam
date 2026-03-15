# NutriCam 后端部署指南

## 环境要求

- Node.js 18+
- npm 或 yarn
- 服务器（推荐配置：1GB+ RAM）

## 本地开发

```bash
cd backend
npm install
npm run dev
```

## 生产部署

### 方式一：使用 PM2

```bash
# 安装 PM2
npm install -g pm2

# 启动服务
pm2 start server.js --name nutricam-api

# 保存配置
pm2 save
pm2 startup
```

### 方式二：使用 Docker

```bash
# 构建镜像
docker build -t nutricam-backend .

# 运行容器
docker run -d \
  -p 3000:3000 \
  --env-file .env \
  --name nutricam-api \
  --restart unless-stopped \
  nutricam-backend
```

### 方式三：使用 Docker Compose

```yaml
version: '3.8'

services:
  api:
    build: ./backend
    ports:
      - "3000:3000"
    environment:
      - NODE_ENV=production
      - PORT=3000
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - USDA_API_KEY=${USDA_API_KEY}
    restart: unless-stopped
    volumes:
      - ./uploads:/app/uploads
```

运行：
```bash
docker-compose up -d
```

## Nginx 反向代理

```nginx
server {
    listen 80;
    server_name api.nutricam.example.com;
    
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_cache_bypass $http_upgrade;
        
        # 增加上传限制
        client_max_body_size 20M;
    }
}
```

## 启用 HTTPS

使用 Certbot：

```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d api.nutricam.example.com
```

## 环境变量

创建 `.env` 文件：

```bash
# 必填
OPENAI_API_KEY=sk-...

# 可选
USDA_API_KEY=your_usda_api_key
REDIS_URL=redis://localhost:6379
NODE_ENV=production
PORT=3000
```

## 监控

使用 PM2 监控：

```bash
pm2 monit
pm2 logs nutricam-api
```

## 备份

定期备份：

```bash
# 备份环境变量
cp .env .env.backup

# 如果使用本地数据存储
# 备份 uploads 目录
tar -czvf uploads-backup.tar.gz uploads/
```

## 更新

```bash
# 拉取代码
git pull

# 安装新依赖
npm install

# 重启服务
pm2 restart nutricam-api
```