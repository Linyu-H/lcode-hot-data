# 🔥 实时热点监控系统

一个实时聚合多个平台热点数据的监控系统，支持30+数据源，提供美观的可视化界面。

## ✨ 特性

- 🌐 **30+数据源**: 知乎、微博、百度、B站、GitHub、V2EX、虎扑、36氪等
- 📱 **完美移动端适配**: 响应式设计，支持手机、平板、桌面
- 🎨 **现代化UI**: 渐变色、毛玻璃效果、流畅动画
- 🔄 **实时更新**: WebSocket推送，数据实时同步
- 📊 **趋势分析**: 热度趋势图表，聚合排行榜
- 🌙 **深色模式**: 自动适配系统主题
- ♿ **无障碍访问**: 支持屏幕阅读器，符合WCAG标准

## 🚀 快速开始

### 前置要求

- Java 17+
- Maven 3.6+
- Node.js 16+
- npm 或 yarn

### 后端启动

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端将在 `http://localhost:8080` 启动

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端将在 `http://localhost:5173` 启动

### 生产构建

```bash
# 前端构建
cd frontend
npm run build

# 后端打包
cd backend
mvn clean package
java -jar target/hotdata-backend-1.0.0.jar
```

## 📁 项目结构

```
hotdata/
├── backend/                 # Spring Boot后端
│   ├── src/
│   │   └── main/
│   │       └── java/com/hotdata/
│   │           ├── config/          # 配置类
│   │           ├── controller/      # REST控制器
│   │           ├── model/           # 数据模型
│   │           ├── service/         # 业务逻辑
│   │           ├── source/          # 数据源实现
│   │           ├── util/            # 工具类
│   │           └── websocket/       # WebSocket处理
│   └── pom.xml
├── frontend/                # Vue 3前端
│   ├── src/
│   │   ├── components/      # Vue组件
│   │   ├── store/           # Pinia状态管理
│   │   ├── App.vue          # 主应用
│   │   ├── main.js          # 入口文件
│   │   └── styles.css       # 全局样式
│   └── package.json
└── README.md
```

## 🎯 支持的数据源

### 开发技术 (💻)
- GitHub Trending
- Hacker News
- 掘金
- V2EX

### 综合资讯 (🔥)
- 知乎热榜
- 微博热搜
- 百度热搜
- 今日头条

### 娱乐视频 (🎬)
- B站热榜
- 抖音热榜
- 快手短剧
- 腾讯视频
- 爱奇艺

### 新闻媒体 (📰)
- 澎湃新闻
- 新浪新闻
- 环球网

### 科技财经 (💰)
- 36氪
- IT之家

### 社区论坛 (💬)
- 虎扑
- Linux.do
- V2EX
- NodeLoc
- NodeSeek
- 全球主机交流
- 什么值得买社区
- 知识星球广场

### 其他 (📦)
- 豆瓣电影
- Steam
- AITO论坛
- 果核剥壳

## 🎨 界面预览

### 桌面端
- 多列卡片布局
- 侧边栏趋势图和聚合榜
- 分类导航

### 移动端
- 单列自适应布局
- 横向滚动分类
- 优化的触摸交互

## 🔧 配置

### 后端配置 (application.properties)

```properties
server.port=8080
spring.application.name=hotdata-backend

# WebSocket配置
websocket.path=/ws/hot

# HTTP客户端配置
http.timeout=15000
http.max-connections=100
```

### 前端配置 (vite.config.js)

```javascript
export default {
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true
      }
    }
  }
}
```

## 📱 移动端优化

- **响应式断点**: 1200px, 768px, 480px
- **触摸优化**: 最小44px触摸目标
- **性能优化**: CSS transform, GPU加速
- **滚动优化**: -webkit-overflow-scrolling: touch

## 🛠️ 技术栈

### 后端
- Spring Boot 3.2.5
- Spring WebFlux (异步HTTP客户端)
- Spring WebSocket
- Jsoup (HTML解析)
- Jackson (JSON处理)
- Lombok

### 前端
- Vue 3 (Composition API)
- Pinia (状态管理)
- ECharts (图表)
- Vite (构建工具)

## 📊 API文档

### REST API

#### 获取快照数据
```
GET /api/hot/snapshot
```

#### 获取单个平台数据
```
GET /api/hot/board/{platform}
```

#### 获取趋势数据
```
GET /api/hot/trend?platform={platform}&title={title}
```

#### 获取聚合排行
```
GET /api/hot/aggregate
```

#### 手动刷新
```
POST /api/hot/refresh
```

### WebSocket

连接地址: `ws://localhost:8080/ws/hot`

消息格式:
```json
{
  "type": "update",
  "platform": "zhihu",
  "data": { ... }
}
```

## 🐛 故障排查

### 后端无法启动
- 检查8080端口是否被占用: `lsof -i:8080`
- 检查Java版本: `java -version` (需要17+)

### 前端无法连接后端
- 确认后端已启动
- 检查CORS配置
- 查看浏览器控制台错误

### 数据源无数据
- 查看后端日志中的错误信息
- 某些网站有反爬限制，可能需要代理
- 网络问题可能导致超时

## 📝 开发指南

### 添加新数据源

1. 在 `backend/src/main/java/com/hotdata/source/` 创建新类
2. 实现 `HotSource` 接口
3. 添加 `@Component` 注解
4. 实现 `fetch()` 方法

示例:
```java
@Component
public class NewSource implements HotSource {
    @Override
    public String platform() { return "new"; }
    
    @Override
    public String platformName() { return "新平台"; }
    
    @Override
    public String category() { return "tech"; }
    
    @Override
    public List<HotItem> fetch() throws Exception {
        // 实现数据抓取逻辑
    }
}
```

### 添加平台Logo

在 `frontend/src/components/HotBoard.vue` 的 `platformLogos` 对象中添加:

```javascript
const platformLogos = {
  new: 'https://example.com/favicon.ico',
  // ...
}
```

## 📄 许可证

仅供学习用途

## 🙏 致谢

感谢所有数据源平台提供的公开数据。

## 📮 联系方式

如有问题或建议，欢迎提Issue。

---

**注意**: 本项目仅用于学习交流，请遵守各平台的robots.txt和使用条款。
