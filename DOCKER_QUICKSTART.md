# 🐳 Docker部署完整方案

## ✅ 已创建的文件

```
hotdata/
├── docker-compose.yml          # Docker Compose配置
├── Dockerfile.backend          # 后端Dockerfile
├── Dockerfile.frontend         # 前端Dockerfile
├── nginx.conf                  # Nginx配置
├── .dockerignore              # Docker忽略文件
├── docker-deploy.sh           # 一键部署脚本
├── docker-stop.sh             # 一键停止脚本
├── DOCKER_DEPLOY.md           # 详细部署文档
└── DOCKER_README.md           # 快速开始文档
```

## 🚀 快速部署命令

### 方式1：一键部署（推荐）

```bash
./docker-deploy.sh
```

### 方式2：手动部署

```bash
# 启动所有服务
docker compose up -d --build

# 查看状态
docker compose ps

# 查看日志
docker compose logs -f
```

### 方式3：分步部署

```bash
# 1. 构建镜像
docker compose build

# 2. 启动服务
docker compose up -d

# 3. 查看日志
docker compose logs -f backend
```

## 📦 服务说明

| 服务 | 端口 | 说明 |
|------|------|------|
| **前端** | 80 | Nginx + Vue3 |
| **后端** | 8080 | Spring Boot |
| **FlareSolverr** | 8191 | Cloudflare绕过 |

## 📋 常用命令速查

```bash
# 启动
./docker-deploy.sh

# 停止
./docker-stop.sh
# 或
docker compose down

# 重启
docker compose restart

# 查看日志
docker compose logs -f

# 查看状态
docker compose ps

# 更新代码后重新部署
docker compose up -d --build
```

## 🌐 访问地址

部署成功后访问：

- **前端**: http://localhost
- **后端API**: http://localhost:8080/api/hot/snapshot
- **FlareSolverr**: http://localhost:8191

## 🔧 配置修改

### 修改端口

编辑`docker-compose.yml`：

```yaml
frontend:
  ports:
    - "3000:80"  # 改为3000端口

backend:
  ports:
    - "9090:8080"  # 改为9090端口
```

### 禁用FlareSolverr

编辑`docker-compose.yml`：

```yaml
backend:
  environment:
    - FLARESOLVERR_ENABLED=false
```

### 调整资源限制

```yaml
backend:
  deploy:
    resources:
      limits:
        cpus: '4.0'
        memory: 4G
```

## 🐛 故障排查

### 端口被占用

```bash
# 查看端口占用
lsof -i :80
lsof -i :8080
lsof -i :8191

# 修改docker-compose.yml中的端口映射
```

### 构建失败

```bash
# 清理缓存重新构建
docker compose build --no-cache

# 查看详细日志
docker compose up --build
```

### 服务无法启动

```bash
# 查看日志
docker compose logs backend

# 重启服务
docker compose restart backend
```

## 📊 监控

```bash
# 查看资源使用
docker stats

# 查看容器详情
docker compose ps -a

# 实时日志
docker compose logs -f --tail=100
```

## 🔄 更新部署

```bash
# 1. 拉取最新代码
git pull

# 2. 重新构建并启动
docker compose up -d --build

# 3. 查看日志
docker compose logs -f
```

## 🌍 生产环境

### 使用域名

1. 修改`nginx.conf`中的`server_name`
2. 配置DNS解析
3. 添加SSL证书（Let's Encrypt）

### 添加HTTPS

```bash
# 使用Certbot获取证书
docker run -it --rm \
  -v /etc/letsencrypt:/etc/letsencrypt \
  certbot/certbot certonly --standalone \
  -d your-domain.com
```

## 📚 详细文档

查看完整文档：[DOCKER_DEPLOY.md](./DOCKER_DEPLOY.md)

---

**创建时间**: 2026-05-31  
**版本**: 1.0.0  
**状态**: ✅ 生产就绪
