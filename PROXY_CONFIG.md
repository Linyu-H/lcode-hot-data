# 🌐 代理配置指南

## 已配置的代理

当前系统已配置使用本地代理：
- **代理地址**: 127.0.0.1:7897
- **代理类型**: HTTP代理
- **状态**: ✅ 已启用

## 配置文件

### application.yml
```yaml
hotdata:
  proxy:
    enabled: true      # 启用代理
    host: "127.0.0.1"  # 代理主机
    port: 7897         # 代理端口
```

### HttpClientFactory.java
已修改为支持代理配置，所有HTTP请求都会通过代理服务器。

## 如何修改代理

### 1. 修改配置文件
编辑 `backend/src/main/resources/application.yml`:

```yaml
hotdata:
  proxy:
    enabled: true           # 设置为false可禁用代理
    host: "your-proxy-ip"   # 修改为你的代理IP
    port: 7890              # 修改为你的代理端口
```

### 2. 重启后端
```bash
./stop.sh
./start.sh
```

## 支持的代理类型

当前配置支持：
- ✅ HTTP代理
- ✅ HTTPS代理

如需SOCKS5代理，需要修改 `HttpClientFactory.java` 中的代理类型：
```java
.type(ProxyProvider.Proxy.SOCKS5)  // 改为SOCKS5
```

## 测试代理

### 检查代理是否工作
```bash
# 查看后端日志
tail -f logs/backend.log | grep -i "fetch"

# 测试API
curl http://localhost:8080/api/hot/snapshot | jq '.boards[] | select(.items | length > 0) | .platformName'
```

### 预期结果
如果代理配置正确，之前DNS解析失败的数据源应该能够正常获取数据：
- GitHub Trending
- 果核剥壳
- 爱奇艺
- 什么值得买
- 淘宝热搜
- 环球网
- 全球主机交流

## 常见问题

### 1. 代理连接失败
**症状**: 日志显示 "Connection refused" 或 "Connection timeout"

**解决方案**:
- 确认代理服务正在运行
- 检查代理地址和端口是否正确
- 确认防火墙允许连接

### 2. 部分网站仍然无法访问
**症状**: 某些数据源仍然返回错误

**可能原因**:
- 网站有更严格的反爬虫机制
- 需要更复杂的请求头
- 网站需要JavaScript渲染

**解决方案**:
- 增加请求延迟
- 使用更真实的浏览器指纹
- 考虑使用Puppeteer/Selenium

### 3. 代理速度慢
**症状**: 数据获取超时

**解决方案**:
- 增加超时时间（application.yml中的timeout-ms）
- 使用更快的代理服务器
- 减少并发请求数量

## 环境变量方式（备选）

也可以通过环境变量配置代理：

```bash
export HTTP_PROXY=http://127.0.0.1:7897
export HTTPS_PROXY=http://127.0.0.1:7897
export NO_PROXY=localhost,127.0.0.1

# 启动后端
java -jar target/hotdata-backend-1.0.0.jar
```

## 禁用代理

如果不需要代理，修改 `application.yml`:

```yaml
hotdata:
  proxy:
    enabled: false  # 禁用代理
```

## 代理服务器推荐

### 本地代理工具
- **Clash**: 支持多种协议，规则丰富
- **V2Ray**: 功能强大，配置灵活
- **Shadowsocks**: 轻量级，简单易用

### 香港服务器
如果你有香港服务器，可以：
1. 在服务器上搭建代理服务（Squid, Shadowsocks等）
2. 配置本地客户端连接
3. 修改application.yml指向本地代理端口

## 验证代理配置

启动后端后，检查日志：

```bash
# 查看启动日志
tail -100 logs/backend.log | grep -i proxy

# 查看数据获取日志
tail -f logs/backend.log | grep -E "(fetch|failed|success)"
```

如果看到数据源成功获取数据，说明代理配置正确！

---

**当前状态**: ✅ 代理已配置，等待后端启动验证
