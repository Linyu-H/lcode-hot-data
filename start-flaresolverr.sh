#!/bin/bash

# FlareSolverr启动脚本
# 用于绕过Cloudflare保护

echo "🚀 启动FlareSolverr服务..."

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker未运行，请先启动Docker"
    exit 1
fi

# 启动FlareSolverr
docker-compose -f docker-compose.flaresolverr.yml up -d

# 等待服务启动
echo "⏳ 等待FlareSolverr启动..."
sleep 5

# 检查服务状态
if curl -s http://localhost:8191 > /dev/null; then
    echo "✅ FlareSolverr已启动成功"
    echo "📍 服务地址: http://localhost:8191"
    echo ""
    echo "💡 使用说明:"
    echo "   1. 在 application.yml 中设置 flaresolverr.enabled=true"
    echo "   2. 重启后端服务"
    echo "   3. LinuxDo数据源将自动使用FlareSolverr绕过Cloudflare"
else
    echo "❌ FlareSolverr启动失败"
    docker-compose -f docker-compose.flaresolverr.yml logs
    exit 1
fi
