#!/bin/bash

# 实时热点监控系统启动脚本

echo "🔥 启动实时热点监控系统..."

# 检查Java版本
if ! command -v java &> /dev/null; then
    echo "❌ 错误: 未找到Java，请先安装Java 17+"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ 错误: Java版本过低，需要Java 17+，当前版本: $JAVA_VERSION"
    exit 1
fi

# 检查Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ 错误: 未找到Maven，请先安装Maven"
    exit 1
fi

# 检查Node.js
if ! command -v node &> /dev/null; then
    echo "❌ 错误: 未找到Node.js，请先安装Node.js 16+"
    exit 1
fi

# 停止已有进程
echo "🛑 停止已有进程..."
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
lsof -ti:5173 | xargs kill -9 2>/dev/null || true

# 启动后端
echo "🚀 启动后端服务..."
cd backend
mvn spring-boot:run > ../logs/backend.log 2>&1 &
BACKEND_PID=$!
echo "   后端PID: $BACKEND_PID"
cd ..

# 等待后端启动
echo "⏳ 等待后端启动..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/hot/snapshot > /dev/null 2>&1; then
        echo "✅ 后端启动成功"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "❌ 后端启动超时"
        exit 1
    fi
    sleep 1
done

# 启动前端
echo "🚀 启动前端服务..."
cd frontend
npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
echo "   前端PID: $FRONTEND_PID"
cd ..

# 保存PID
mkdir -p logs
echo $BACKEND_PID > logs/backend.pid
echo $FRONTEND_PID > logs/frontend.pid

echo ""
echo "✅ 启动完成！"
echo ""
echo "📍 访问地址:"
echo "   前端: http://localhost:5173"
echo "   后端: http://localhost:8080"
echo ""
echo "📝 日志文件:"
echo "   后端: logs/backend.log"
echo "   前端: logs/frontend.log"
echo ""
echo "🛑 停止服务: ./stop.sh"
echo ""
