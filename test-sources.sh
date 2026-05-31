#!/bin/bash

# 测试数据源脚本

echo "🧪 测试数据源..."
echo ""

# 获取数据
DATA=$(curl -s http://localhost:8080/api/hot/snapshot)

if [ -z "$DATA" ]; then
    echo "❌ 无法连接到后端服务"
    exit 1
fi

echo "📊 数据源统计:"
echo "$DATA" | jq -r '.boards[] | "\(.platformName): \(.items | length)条 \(if .error then "❌ \(.error)" else "✅" end)"' | column -t -s ':'

echo ""
echo "✅ 测试完成！"
