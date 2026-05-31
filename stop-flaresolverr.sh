#!/bin/bash

# FlareSolverr停止脚本

echo "🛑 停止FlareSolverr服务..."

docker-compose -f docker-compose.flaresolverr.yml down

echo "✅ FlareSolverr已停止"
