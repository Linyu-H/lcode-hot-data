# Docker快速部署

## 🚀 一键部署

```bash
./docker-deploy.sh
```

## 📦 包含的服务

- **前端** (Nginx) - http://localhost
- **后端** (Spring Boot) - http://localhost:8080
- **FlareSolverr** - http://localhost:8191

## 📋 常用命令

```bash
# 启动所有服务
docker compose up -d

# 停止所有服务
docker compose down

# 查看日志
docker compose logs -f

# 重启服务
docker compose restart

# 查看状态
docker compose ps
```

详细文档请查看 [DOCKER_DEPLOY.md](./DOCKER_DEPLOY.md)
