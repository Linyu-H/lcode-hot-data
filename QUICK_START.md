# 🚀 快速开始指南

## 5分钟快速启动

### 1️⃣ 检查环境

```bash
# 检查Java版本 (需要17+)
java -version

# 检查Maven
mvn -version

# 检查Node.js (需要16+)
node -v
npm -v
```

### 2️⃣ 一键启动

```bash
# 进入项目目录
cd /Volumes/new/WebLearn/hotdata

# 启动服务
./start.sh
```

### 3️⃣ 访问应用

- **前端界面**: http://localhost:5173
- **后端API**: http://localhost:8080/api/hot/snapshot

### 4️⃣ 停止服务

```bash
./stop.sh
```

## 📱 移动端测试

### Chrome DevTools
1. 打开 http://localhost:5173
2. 按 F12 打开开发者工具
3. 按 Ctrl+Shift+M (Mac: Cmd+Shift+M) 切换设备模式
4. 选择设备: iPhone 12, iPad, 或自定义尺寸

### 推荐测试尺寸
- 📱 手机: 375px, 390px, 414px
- 📱 大屏手机: 430px, 480px
- 📱 平板: 768px, 1024px
- 💻 桌面: 1200px, 1440px, 1920px

## 🎯 功能演示

### 查看热点数据
1. 打开首页，自动加载所有数据源
2. 点击顶部分类切换不同类别
3. 点击热点标题查看趋势图

### 实时更新
- WebSocket自动推送新数据
- 右上角显示连接状态
- 点击"刷新"按钮手动更新

### 移动端体验
- 横向滑动分类导航
- 点击卡片查看详情
- 流畅的滚动和动画

## 🔧 常见问题

### 端口被占用
```bash
# 查看占用8080端口的进程
lsof -i:8080

# 停止进程
kill -9 <PID>
```

### 前端无法连接后端
1. 确认后端已启动: `curl http://localhost:8080/api/hot/snapshot`
2. 检查防火墙设置
3. 查看浏览器控制台错误

### 数据源无数据
- 查看后端日志: `tail -f logs/backend.log`
- 某些网站可能有反爬限制
- DNS解析问题需要配置代理

## 📊 API测试

```bash
# 获取所有数据
curl http://localhost:8080/api/hot/snapshot | jq

# 获取单个平台
curl http://localhost:8080/api/hot/board/zhihu | jq

# 获取聚合排行
curl http://localhost:8080/api/hot/aggregate | jq

# 手动刷新
curl -X POST http://localhost:8080/api/hot/refresh
```

## 🎨 自定义配置

### 修改端口

**后端** (backend/src/main/resources/application.properties):
```properties
server.port=8080
```

**前端** (frontend/vite.config.js):
```javascript
export default {
  server: {
    port: 5173
  }
}
```

### 添加数据源

1. 在 `backend/src/main/java/com/hotdata/source/` 创建新类
2. 实现 `HotSource` 接口
3. 添加 `@Component` 注解
4. 重启后端服务

## 📚 更多文档

- [README.md](README.md) - 完整项目文档
- [IMPROVEMENTS.md](IMPROVEMENTS.md) - 优化详情
- [FINAL_REPORT.md](FINAL_REPORT.md) - 最终报告

---

**祝使用愉快！** 🎉
