#!/bin/bash

# Docker一键部署脚本

echo "🐳 开始Docker部署..."

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker未运行，请先启动Docker"
    exit 1
fi

# 检查docker-compose是否可用
if ! command -v docker &> /dev/null; then
    echo "❌ Docker未安装"
    exit 1
fi

# 停止并删除旧容器
echo "🛑 停止旧容器..."
docker compose down 2>/dev/null || true

# 构建并启动所有服务
echo "🚀 构建并启动服务..."
docker compose up -d --build

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo ""
echo "📊 服务状态："
docker compose ps

# 检查后端健康状态
echo ""
echo "🔍 检查后端服务..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/hot/snapshot > /dev/null 2>&1; then
        echo "✅ 后端服务已就绪"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ 后端服务启动超时"
        docker compose logs backend
        exit 1
    fi
    sleep 2
done

# 检查前端
echo ""
echo "🔍 检查前端服务..."
if curl -s http://localhost:80 > /dev/null 2>&1; then
    echo "✅ 前端服务已就绪"
else
    echo "⚠️  前端服务可能还在启动中"
fi

# 检查FlareSolverr
echo ""
echo "🔍 检查FlareSolverr服务..."
if curl -s http://localhost:8191 > /dev/null 2>&1; then
    echo "✅ FlareSolverr服务已就绪"
else
    echo "⚠️  FlareSolverr服务可能还在启动中"
fi

echo ""
echo "✅ 部署完成！"
echo ""
echo "📍 访问地址:"
echo "   前端: http://localhost"
echo "   后端: http://localhost:8080"
echo "   FlareSolverr: http://localhost:8191"
echo ""
echo "📝 查看日志:"
echo "   所有服务: docker compose logs -f"
echo "   后端: docker compose logs -f backend"
echo "   前端: docker compose logs -f frontend"
echo "   FlareSolverr: docker compose logs -f flaresolverr"
echo ""
echo "🛑 停止服务: docker compose down"
