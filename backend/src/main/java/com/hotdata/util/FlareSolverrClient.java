package com.hotdata.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * FlareSolverr客户端 - 用于绕过Cloudflare保护
 *
 * FlareSolverr是一个代理服务器，用于绕过Cloudflare和DDoS-GUARD保护
 * 项目地址: https://github.com/FlareSolverr/FlareSolverr
 */
@Component
public class FlareSolverrClient {

    private static final Logger log = LoggerFactory.getLogger(FlareSolverrClient.class);

    @Value("${flaresolverr.enabled:false}")
    private boolean enabled;

    @Value("${flaresolverr.url:http://localhost:8191}")
    private String flareSolverrUrl;

    @Value("${flaresolverr.timeout:60000}")
    private int timeout;

    private final ObjectMapper mapper = new ObjectMapper();
    private WebClient webClient;

    /**
     * 通过FlareSolverr获取URL内容
     *
     * @param url 目标URL
     * @return 页面内容
     * @throws Exception 如果请求失败
     */
    public String get(String url) throws Exception {
        if (!enabled) {
            throw new IllegalStateException("FlareSolverr is not enabled. Set flaresolverr.enabled=true in application.properties");
        }

        if (webClient == null) {
            webClient = WebClient.builder()
                    .baseUrl(flareSolverrUrl)
                    .build();
        }

        log.debug("Requesting URL via FlareSolverr: {}", url);

        // 构建请求体
        ObjectNode request = mapper.createObjectNode();
        request.put("cmd", "request.get");
        request.put("url", url);
        request.put("maxTimeout", timeout);

        try {
            String response = webClient.post()
                    .uri("/v1")
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeout + 5000))
                    .block();

            if (response == null || response.isBlank()) {
                throw new Exception("Empty response from FlareSolverr");
            }

            // 解析响应
            JsonNode root = mapper.readTree(response);
            String status = root.path("status").asText();

            if (!"ok".equals(status)) {
                String message = root.path("message").asText("Unknown error");
                throw new Exception("FlareSolverr error: " + message);
            }

            // 提取页面内容
            String solution = root.path("solution").path("response").asText();
            if (solution.isBlank()) {
                throw new Exception("No content in FlareSolverr response");
            }

            // FlareSolverr返回的JSON可能被包裹在HTML中，需要提取
            String content = extractJsonFromHtml(solution);

            log.debug("Successfully fetched URL via FlareSolverr: {}", url);
            return content;

        } catch (Exception e) {
            log.error("FlareSolverr request failed for URL: {}", url, e);
            throw new Exception("Failed to fetch via FlareSolverr: " + e.getMessage(), e);
        }
    }

    /**
     * 检查FlareSolverr服务是否可用
     *
     * @return true如果服务可用
     */
    public boolean isAvailable() {
        if (!enabled) {
            return false;
        }

        try {
            if (webClient == null) {
                webClient = WebClient.builder()
                        .baseUrl(flareSolverrUrl)
                        .build();
            }

            String response = webClient.get()
                    .uri("/")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            return response != null && response.contains("FlareSolverr");
        } catch (Exception e) {
            log.warn("FlareSolverr service not available at {}: {}", flareSolverrUrl, e.getMessage());
            return false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 从HTML中提取JSON内容
     * FlareSolverr有时会返回包裹在<pre>标签中的JSON
     */
    private String extractJsonFromHtml(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }

        // 如果内容以 < 开头，说明是HTML
        if (html.trim().startsWith("<")) {
            // 尝试提取<pre>标签中的内容
            int preStart = html.indexOf("<pre>");
            int preEnd = html.indexOf("</pre>");

            if (preStart != -1 && preEnd != -1 && preEnd > preStart) {
                String extracted = html.substring(preStart + 5, preEnd).trim();
                log.debug("Extracted JSON from HTML <pre> tag");
                return extracted;
            }

            // 如果没有<pre>标签，尝试提取<body>中的内容
            int bodyStart = html.indexOf("<body>");
            int bodyEnd = html.indexOf("</body>");

            if (bodyStart != -1 && bodyEnd != -1 && bodyEnd > bodyStart) {
                String bodyContent = html.substring(bodyStart + 6, bodyEnd).trim();
                // 移除可能的<pre>标签
                bodyContent = bodyContent.replaceAll("</?pre>", "").trim();
                log.debug("Extracted JSON from HTML <body> tag");
                return bodyContent;
            }
        }

        // 如果不是HTML或无法提取，返回原内容
        return html;
    }
}
