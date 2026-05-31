# 新增数据源说明

本次更新为热点监控系统添加了7个新的数据源，包括论坛、科技资讯和金融股市平台。

## 📋 新增数据源列表

### 论坛类 (Forum)

#### 1. NodeLoc (nodeloc)
- **平台名称**: NodeLoc
- **类别**: forum
- **数据源文件**: `NodeLocSource.java`
- **API**: https://www.nodeloc.com/latest.json
- **状态**: ✅ 成功 (29条数据)
- **说明**: 使用Discourse论坛API获取最新帖子，包含浏览量、点赞数、回复数等信息

#### 2. NodeSeek (nodeseek)
- **平台名称**: NodeSeek
- **类别**: forum
- **数据源文件**: `NodeSeekSource.java`
- **API**: https://www.nodeseek.com/latest.json
- **状态**: ⚠️ 需要验证 (可能需要特殊权限)
- **说明**: 同样使用Discourse论坛API，结构与NodeLoc类似

### 科技资讯类 (Tech)

#### 3. 果核剥壳 (ghxi)
- **平台名称**: 果核剥壳
- **类别**: tech
- **数据源文件**: `GhxiSource.java`
- **URL**: https://www.ghxi.com/
- **状态**: ⚠️ 需要调整 (HTML解析需要优化)
- **说明**: 使用Jsoup解析HTML页面，获取软件资讯和教程

### 金融股市类 (Finance)

#### 4. 东方财富 (eastmoney)
- **平台名称**: 东方财富
- **类别**: finance
- **数据源文件**: `EastmoneySource.java`
- **API**: https://searchapi.eastmoney.com/api/suggest/get
- **状态**: ⚠️ 需要验证
- **说明**: 获取热搜股票榜单，包含股票代码、名称等信息

#### 5. 雪球 (xueqiu)
- **平台名称**: 雪球
- **类别**: finance
- **数据源文件**: `XueqiuSource.java`
- **API**: https://xueqiu.com/statuses/hot/listV2.json
- **状态**: ⚠️ 需要Cookie (可能需要登录)
- **说明**: 获取雪球热帖，包含浏览量、点赞数、评论数

#### 6. 金十数据 (jinshi)
- **平台名称**: 金十数据
- **类别**: finance
- **数据源文件**: `JinshiSource.java`
- **URL**: https://www.jin10.com/
- **状态**: ⚠️ 需要调整 (HTML解析需要优化)
- **说明**: 获取实时财经快讯

#### 7. 同花顺 (tonghuashun)
- **平台名称**: 同花顺
- **类别**: finance
- **数据源文件**: `TonghuashunSource.java`
- **URL**: http://data.10jqka.com.cn/rank/cxg/
- **状态**: ✅ 成功 (30条数据)
- **说明**: 获取创新高股票榜单，包含涨跌幅、价格等信息

## 🔧 技术实现

### 共同特性
- 所有数据源都实现了 `HotSource` 接口
- 支持代理配置（通过环境变量或系统属性）
- 统一的错误处理和超时机制（15秒）
- 使用 `@Component` 注解自动注册到Spring容器

### API类型
1. **JSON API**: NodeLoc, NodeSeek, 东方财富, 雪球
2. **HTML解析**: 果核剥壳, 金十数据, 同花顺

### 代理支持
所有数据源都支持以下代理配置：
- 环境变量: `HTTPS_PROXY`, `https_proxy`, `HTTP_PROXY`, `http_proxy`
- 系统属性: `https.proxyHost`, `https.proxyPort`, `http.proxyHost`, `http.proxyPort`

## 📊 数据结构

每个数据源返回的 `HotItem` 包含：
- `rank`: 排名
- `title`: 标题
- `url`: 链接
- `hotValue`: 热度值显示（如"1.2万 · 100赞"）
- `hotScore`: 热度分数（用于排序）
- `description`: 描述信息
- `extra`: 额外信息（如标签、分类）

## 🚀 使用方法

### 启动后端
```bash
cd backend
mvn clean compile
mvn spring-boot:run
```

### 测试API
```bash
# 获取所有数据源快照
curl http://localhost:8080/api/hot/snapshot

# 获取特定平台数据
curl http://localhost:8080/api/hot/board/nodeloc
curl http://localhost:8080/api/hot/board/tonghuashun
```

## ⚠️ 注意事项

### 需要进一步优化的数据源

1. **NodeSeek**: API返回404，可能需要：
   - 检查API地址是否正确
   - 可能需要登录或特殊权限
   - 考虑使用HTML解析作为备选方案

2. **果核剥壳**: HTML解析未获取到数据，需要：
   - 检查网站HTML结构
   - 更新CSS选择器
   - 可能需要处理JavaScript渲染

3. **金十数据**: 同样需要优化HTML解析

4. **东方财富**: 连接问题，可能需要：
   - 更换API接口
   - 添加必要的请求头
   - 处理反爬虫机制

5. **雪球**: 返回400错误，需要：
   - 添加Cookie支持
   - 可能需要模拟登录
   - 或使用公开的热帖接口

### 反爬虫应对
部分网站有反爬虫机制，建议：
- 使用代理IP池
- 添加合理的请求间隔
- 模拟真实浏览器行为
- 使用Cookie和Session管理

## 📈 后续改进建议

1. **增加更多股市平台**:
   - 新浪财经
   - 腾讯财经
   - 网易财经
   - 财联社

2. **优化数据获取**:
   - 实现智能重试机制
   - 添加缓存层减少请求
   - 支持增量更新

3. **增强错误处理**:
   - 详细的错误日志
   - 失败告警机制
   - 自动降级策略

4. **性能优化**:
   - 并发请求优化
   - 连接池管理
   - 响应时间监控

## 📝 更新日志

### 2026-05-31
- ✅ 添加 NodeLoc 论坛数据源
- ✅ 添加 NodeSeek 论坛数据源
- ✅ 添加 果核剥壳 科技资讯源
- ✅ 添加 东方财富 股市数据源
- ✅ 添加 雪球 股市社区数据源
- ✅ 添加 金十数据 财经快讯源
- ✅ 添加 同花顺 股市数据源
- ✅ 所有数据源支持代理配置
- ✅ 统一错误处理和超时机制

## 🔗 相关文件

- 数据源实现: `backend/src/main/java/com/hotdata/source/`
- 数据模型: `backend/src/main/java/com/hotdata/model/HotItem.java`
- 服务层: `backend/src/main/java/com/hotdata/service/HotDataService.java`
- 配置文件: `backend/src/main/resources/application.properties`
