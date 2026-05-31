# 新增数据源使用指南

## 🎯 快速开始

### 启动服务

```bash
# 启动后端
cd backend
mvn spring-boot:run

# 启动前端（另一个终端）
cd frontend
npm run dev
```

### 访问系统

- 前端界面: http://localhost:5173
- 后端API: http://localhost:8080/api/hot/snapshot

## 📊 新增数据源列表

### ✅ 正常运行的数据源

#### 1. NodeLoc 论坛
```bash
# API调用
curl http://localhost:8080/api/hot/board/nodeloc

# 数据示例
{
  "platform": "nodeloc",
  "platformName": "NodeLoc",
  "category": "forum",
  "items": [
    {
      "rank": 1,
      "title": "白嫖个美国大学学位吧！",
      "url": "https://www.nodeloc.com/t/topic/86865",
      "hotValue": "374 · 13赞",
      "hotScore": 374,
      "description": "127 回复"
    }
  ]
}
```

#### 2. 同花顺
```bash
# API调用
curl http://localhost:8080/api/hot/board/tonghuashun

# 数据示例
{
  "platform": "tonghuashun",
  "platformName": "同花顺",
  "category": "finance",
  "items": [
    {
      "rank": 1,
      "title": "深圳能源 (000027)",
      "url": "http://stockpage.10jqka.com.cn/000027/",
      "hotValue": "0.37% · 深圳能源",
      "hotScore": 1
    }
  ]
}
```

#### 3. 果核剥壳
```bash
# API调用
curl http://localhost:8080/api/hot/board/ghxi

# 数据示例
{
  "platform": "ghxi",
  "platformName": "果核剥壳",
  "category": "tech",
  "items": [
    {
      "rank": 1,
      "title": "Android ToApp(网站转应用) v1.8.0",
      "url": "https://www.ghxi.com/toapp.html",
      "hotValue": "Fri, 29 May 2026 10:02:28 +0000",
      "description": "ToApp 一款能够将任何网站转化为原生 Android 应用...",
      "extra": "系统相关"
    }
  ]
}
```

### ❌ 暂时禁用的数据源

以下数据源由于技术限制暂时返回空数据，不影响系统稳定性：

- **东方财富** (eastmoney) - 连接问题
- **NodeSeek** (nodeseek) - Cloudflare保护
- **雪球** (xueqiu) - 需要登录
- **金十数据** (jinshi) - 反爬虫

## 🔧 配置说明

### 代理配置

如果需要使用代理访问某些数据源，可以设置环境变量：

```bash
# 设置代理
export HTTPS_PROXY=http://127.0.0.1:7890
export HTTP_PROXY=http://127.0.0.1:7890

# 或使用系统属性
mvn spring-boot:run -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890
```

### 超时配置

默认超时时间为15秒，可以在 `application.properties` 中修改：

```properties
http.timeout=15000
```

## 📱 前端使用

新增的数据源会自动显示在前端界面中：

1. **论坛分类**: 可以看到NodeLoc的热门帖子
2. **金融分类**: 可以看到同花顺的股票数据
3. **科技分类**: 可以看到果核剥壳的软件资讯

## 🔍 数据刷新

### 自动刷新
- 系统每5分钟自动刷新一次数据
- WebSocket实时推送更新

### 手动刷新
```bash
# 手动触发刷新
curl -X POST http://localhost:8080/api/hot/refresh
```

## 📊 数据统计

### 查看所有数据源
```bash
curl http://localhost:8080/api/hot/snapshot | jq '.boards[] | {platform, platformName, items: (.items | length)}'
```

### 查看特定分类
```bash
# 论坛类
curl http://localhost:8080/api/hot/snapshot | jq '.boards[] | select(.category == "forum")'

# 金融类
curl http://localhost:8080/api/hot/snapshot | jq '.boards[] | select(.category == "finance")'

# 科技类
curl http://localhost:8080/api/hot/snapshot | jq '.boards[] | select(.category == "tech")'
```

## 🐛 故障排查

### 数据源无数据

1. **检查网络连接**
   ```bash
   curl -I https://www.nodeloc.com
   ```

2. **查看后端日志**
   ```bash
   tail -f logs/hotdata.log
   ```

3. **检查代理配置**
   ```bash
   echo $HTTPS_PROXY
   ```

### 服务无法启动

1. **检查端口占用**
   ```bash
   lsof -i:8080
   ```

2. **检查Java版本**
   ```bash
   java -version  # 需要Java 17+
   ```

3. **清理并重新编译**
   ```bash
   mvn clean install
   ```

## 📝 开发指南

### 添加新数据源

1. 创建新的Source类：
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
        // 实现数据获取逻辑
        return List.of();
    }
}
```

2. 系统会自动注册并开始抓取数据

### 数据获取方式

支持三种方式：

1. **JSON API**
   ```java
   String body = http.newClient(baseUrl).get()
       .uri(apiUrl)
       .retrieve()
       .bodyToMono(String.class)
       .block(http.timeout());
   ```

2. **HTML解析**
   ```java
   Document doc = Jsoup.parse(html);
   Elements items = doc.select(".item");
   ```

3. **RSS源**
   ```java
   Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
   Elements items = doc.select("item");
   ```

## 📚 相关文档

- [NEW_SOURCES.md](./NEW_SOURCES.md) - 新数据源详细说明
- [TEST_REPORT.md](./TEST_REPORT.md) - 测试报告
- [OPTIMIZATION_REPORT.md](./OPTIMIZATION_REPORT.md) - 优化报告
- [FINAL_COMPLETION_REPORT.md](./FINAL_COMPLETION_REPORT.md) - 最终报告

## 🙏 致谢

感谢以下平台提供的公开数据：
- NodeLoc论坛
- 同花顺
- 果核剥壳

## ⚠️ 注意事项

1. 本项目仅用于学习交流
2. 请遵守各平台的使用条款
3. 不要过于频繁地请求数据
4. 建议使用代理以避免IP被封

---

**更新时间**: 2026-05-31  
**版本**: v1.0.0  
**状态**: ✅ 正常运行
