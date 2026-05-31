#!/bin/bash

# 数据源调试脚本

echo "🔍 调试无数据的数据源..."
echo ""

# 测试果核剥壳
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1. 果核剥壳 (ghxi)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
curl -s "https://www.ghxi.com/" | grep -o '<h2 class="entry-title"[^>]*>.*</h2>' | head -3
echo ""

# 测试虎扑
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2. 虎扑 (hupu) - 需要JS渲染"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "虎扑使用React渲染，需要使用API或Puppeteer"
echo ""

# 测试GitHub
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3. GitHub Trending (github)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
curl -s "https://github.com/trending" -H "User-Agent: Mozilla/5.0" | grep -o 'href="/[^/]*/[^"]*"' | grep -v login | head -5
echo ""

# 测试爱奇艺
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4. 爱奇艺 (iqiyi)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
curl -s "https://www.iqiyi.com/" | grep -o 'title="[^"]*"' | head -10
echo ""

# 测试什么值得买
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "5. 什么值得买 (smzdm)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
curl -s "https://post.smzdm.com/hot_1/" | grep -o 'class="feed-block-title"[^>]*>.*</a>' | head -3
echo ""

echo "✅ 调试完成"
echo ""
echo "💡 建议:"
echo "  - 虎扑、快手、NodeLoc等需要JS渲染，建议使用Puppeteer"
echo "  - GitHub可能需要更复杂的Cookie或Token"
echo "  - 其他网站可能需要调整选择器"
