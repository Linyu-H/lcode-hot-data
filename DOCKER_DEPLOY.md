# Docker部署指南

## 🚀 快速开始

### 一键部署

```bash
# 给脚本执行权限
chmod +x docker-deploy.sh

# 启动所有服务
./docker-deploy.sh
```

### 手动部署

```bash
# 1. 构建并启动所有服务
docker compose up -d --build

# 2. 查看服务状态
docker compose ps

# 3. 查看日志
docker compose logs -f
```

## 📦 服务架构

```
┌─────────────────────────────────────────┐
│           Nginx (前端)                   │
│         http://localhost:80              │
└─────────────┬───────────────────────────┘
              │
              ├─> /api/* ──────────────────┐
              │                             │
              └─> /ws/* ───────────────────┤
                                            ▼
                              ┌─────────────────────────┐
                              │   Spring Boot (后端)     │
                              │  http://localhost:8080   │
                              └────────────┬────────────┘
                                           │
                                           ▼
                              ┌─────────────────────────┐
                              │    FlareSolverr         │
                              │  http://localhost:8191   │
                              └─────────────────────────┘
```

## 🐳 Docker Compose服务

### 1. FlareSolverr
- **镜像**: ghcr.io/flaresolverr/flaresolverr:latest
- **端口**: 8191
- **作用**: 绕过Cloudflare保护
- **资源**: 1 CPU, 1GB内存

### 2. Backend (后端)
- **构建**: Dockerfile.backend
- **端口**: 8080
- **依赖**: FlareSolverr
- **资源**: 2 CPU, 2GB内存

### 3. Frontend (前端)
- **构建**: Dockerfile.frontend
- **端口**: 80
- **依赖**: Backend
- **资源**: 0.5 CPU, 256MB内存

## 📋 常用命令

### 启动服务

```bash
# 启动所有服务
docker compose up -d

# 启动并重新构建
docker compose up -d --build

# 启动指定服务
docker compose up -d backend
```

### 停止服务

```bash
# 停止所有服务
docker compose down

# 停止并删除数据卷
docker compose down -v

# 停止指定服务
docker compose stop backend
```

### 查看状态

```bash
# 查看所有服务状态
docker compose ps

# 查看服务日志
docker compose logs -f

# 查看指定服务日志
docker compose logs -f backend

# 查看最近100行日志
docker compose logs --tail=100 backend
```

### 重启服务

```bash
# 重启所有服务
docker compose restart

# 重启指定服务
docker compose restart backend
```

### 进入容器

```bash
# 进入后端容器
docker compose exec backend sh

# 进入前端容器
docker compose exec frontend sh
```

## 🔧 配置说明

### 环境变量

在`docker-compose.yml`中可以配置：

```yaml
backend:
  environment:
    - SPRING_PROFILES_ACTIVE=prod
    - FLARESOLVERR_ENABLED=true
    - FLARESOLVERR_URL=http://flaresolverr:8191
```

### 端口映射

默认端口映射：
- **前端**: 80 → 80
- **后端**: 8080 → 8080
- **FlareSolverr**: 8191 → 8191

修改端口（如果冲突）：
```yaml
frontend:
  ports:
    - "3000:80"  # 改为3000端口
```

### 资源限制

```yaml
deploy:
  resources:
    limits:
      cpus: '2.0'
      memory: 2G
    reservations:
      cpus: '1.0'
      memory: 1G
```

## 📊 监控和调试

### 查看资源使用

```bash
# 查看容器资源使用情况
docker stats

# 查看指定容器
docker stats hotdata-backend
```

### 查看容器详情

```bash
# 查看容器详细信息
docker compose ps -a

# 查看容器配置
docker inspect hotdata-backend
```

### 调试后端

```bash
# 查看后端日志
docker compose logs -f backend

# 进入后端容器
docker compose exec backend sh

# 测试后端API
curl http://localhost:8080/api/hot/snapshot
```

### 调试前端

```bash
# 查看前端日志
docker compose logs -f frontend

# 测试前端访问
curl http://localhost:80
```

## 🔄 更新部署

### 更新代码后重新部署

```bash
# 1. 拉取最新代码
git pull

# 2. 重新构建并启动
docker compose up -d --build

# 3. 查看日志确认
docker compose logs -f
```

### 仅更新后端

```bash
docker compose up -d --build backend
```

### 仅更新前端

```bash
docker compose up -d --build frontend
```

## 🌐 生产环境部署

### 1. 使用环境变量文件

创建`.env`文件：
```env
BACKEND_PORT=8080
FRONTEND_PORT=80
FLARESOLVERR_PORT=8191
TZ=Asia/Shanghai
```

### 2. 配置域名

修改`nginx.conf`：
```nginx
server {
    listen 80;
    server_name your-domain.com;
    # ...
}
```

### 3. 添加HTTPS

```bash
# 使用Let's Encrypt
docker run -it --rm \
  -v /etc/letsencrypt:/etc/letsencrypt \
  certbot/certbot certonly --standalone \
  -d your-domain.com
```

### 4. 配置反向代理

如果使用外部Nginx：
```nginx
upstream backend {
    server localhost:8080;
}

server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:80;
    }

    location /api/ {
        proxy_pass http://backend;
    }
}
```

## 🐛 故障排查

### 后端启动失败

```bash
# 查看详细日志
docker compose logs backend

# 检查端口占用
lsof -i :8080

# 重新构建
docker compose build --no-cache backend
docker compose up -d backend
```

### 前端无法访问

```bash
# 检查容器状态
docker compose ps frontend

# 查看nginx日志
docker compose logs frontend

# 测试nginx配置
docker compose exec frontend nginx -t
```

### FlareSolverr不工作

```bash
# 查看日志
docker compose logs flaresolverr

# 测试服务
curl http://localhost:8191

# 重启服务
docker compose restart flaresolverr
```

### 容器内存不足

```bash
# 增加内存限制
# 修改docker-compose.yml中的memory配置
docker compose up -d --force-recreate
```

## 📝 备份和恢复

### 备份日志

```bash
# 导出日志
docker compose logs > backup-logs-$(date +%Y%m%d).log
```

### 备份数据

```bash
# 如果有数据卷
docker run --rm -v hotdata_logs:/data -v $(pwd):/backup \
  alpine tar czf /backup/logs-backup.tar.gz /data
```

## 🎯 性能优化

### 1. 使用多阶段构建

Dockerfile已经使用多阶段构建，减小镜像大小。

### 2. 启用构建缓存

```bash
# 使用BuildKit
DOCKER_BUILDKIT=1 docker compose build
```

### 3. 清理未使用的资源

```bash
# 清理未使用的镜像
docker image prune -a

# 清理未使用的容器
docker container prune

# 清理所有未使用的资源
docker system prune -a
```

## 📚 相关文档

- [Docker官方文档](https://docs.docker.com/)
- [Docker Compose文档](https://docs.docker.com/compose/)
- [FlareSolverr文档](https://github.com/FlareSolverr/FlareSolverr)

## 🆘 获取帮助

```bash
# Docker Compose帮助
docker compose --help

# 查看服务配置
docker compose config

# 验证配置文件
docker compose config --quiet
```

---

**部署时间**: 2026-05-31  
**版本**: 1.0.0  
**状态**: ✅ 生产就绪
