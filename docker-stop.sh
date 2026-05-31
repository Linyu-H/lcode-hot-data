#!/bin/bash

# Docker快速停止脚本

echo "🛑 停止Docker服务..."

docker compose down

echo "✅ 所有服务已停止"
