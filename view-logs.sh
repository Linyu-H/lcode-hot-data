#!/bin/bash

# 日志查看脚本

echo "📝 实时热点监控系统 - 日志查看"
echo ""
echo "选择要查看的日志:"
echo "1) 后端日志 (全部)"
echo "2) 后端日志 (实时)"
echo "3) 后端日志 (错误)"
echo "4) 后端日志 (数据源)"
echo "5) 前端日志"
echo ""
read -p "请选择 (1-5): " choice

case $choice in
    1)
        echo "📄 后端日志 (全部):"
        cat logs/backend.log
        ;;
    2)
        echo "📡 后端日志 (实时):"
        tail -f logs/backend.log
        ;;
    3)
        echo "❌ 后端日志 (错误):"
        grep -i "error\|warn\|failed" logs/backend.log | tail -50
        ;;
    4)
        echo "📊 后端日志 (数据源):"
        grep -i "fetch" logs/backend.log | tail -50
        ;;
    5)
        echo "🌐 前端日志:"
        if [ -f logs/frontend.log ]; then
            cat logs/frontend.log
        else
            echo "前端日志文件不存在"
        fi
        ;;
    *)
        echo "无效选择"
        ;;
esac
