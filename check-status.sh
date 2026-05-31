#!/bin/bash

# 实时热点监控系统状态检查脚本

echo "🔍 检查系统状态..."
echo ""

# 检查后端
echo "📡 后端服务:"
if curl -s http://localhost:8080/api/hot/snapshot > /dev/null 2>&1; then
    echo "   ✅ 运行正常 (http://localhost:8080)"
    
    # 获取数据统计
    STATS=$(curl -s http://localhost:8080/api/hot/snapshot | jq '{total: (.boards | length), active: [.boards[] | select(.items | length > 0)] | length}')
    TOTAL=$(echo $STATS | jq -r '.total')
    ACTIVE=$(echo $STATS | jq -r '.active')
    PERCENT=$((ACTIVE * 100 / TOTAL))
    
    echo "   📊 数据源: $ACTIVE/$TOTAL 正常工作 ($PERCENT%)"
else
    echo "   ❌ 未运行"
fi

echo ""

# 检查前端
echo "🌐 前端服务:"
if curl -s http://localhost:5173 > /dev/null 2>&1; then
    echo "   ✅ 运行正常 (http://localhost:5173)"
else
    echo "   ❌ 未运行"
fi

echo ""

# 检查代理
echo "🌍 代理配置:"
if [ -n "$HTTP_PROXY" ]; then
    echo "   ✅ 环境变量: $HTTP_PROXY"
else
    echo "   ℹ️  未设置环境变量"
fi

# 检查配置文件
if grep -q "enabled: true" backend/src/main/resources/application.yml 2>/dev/null; then
    PROXY_HOST=$(grep "host:" backend/src/main/resources/application.yml | awk '{print $2}' | tr -d '"')
    PROXY_PORT=$(grep "port:" backend/src/main/resources/application.yml | tail -1 | awk '{print $2}')
    echo "   ✅ 配置文件: $PROXY_HOST:$PROXY_PORT"
else
    echo "   ❌ 配置文件未启用"
fi

echo ""

# 检查进程
echo "🔧 进程信息:"
BACKEND_PID=$(lsof -ti:8080 2>/dev/null)
if [ -n "$BACKEND_PID" ]; then
    echo "   后端 PID: $BACKEND_PID"
fi

FRONTEND_PID=$(lsof -ti:5173 2>/dev/null)
if [ -n "$FRONTEND_PID" ]; then
    echo "   前端 PID: $FRONTEND_PID"
fi

echo ""

# 检查日志
echo "📝 最近日志:"
if [ -f logs/backend.log ]; then
    echo "   后端日志: logs/backend.log"
    echo "   最后更新: $(stat -f "%Sm" logs/backend.log)"
fi

echo ""
echo "✅ 检查完成！"
