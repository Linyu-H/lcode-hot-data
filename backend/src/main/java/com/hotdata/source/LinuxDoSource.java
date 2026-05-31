package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import com.hotdata.util.FlareSolverrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LinuxDoSource implements HotSource {

    private static final Logger log = LoggerFactory.getLogger(LinuxDoSource.class);
    private final HttpClientFactory http;
    private final FlareSolverrClient flareSolverr;
    private final ObjectMapper mapper = new ObjectMapper();

    public LinuxDoSource(HttpClientFactory http, FlareSolverrClient flareSolverr) {
        this.http = http;
        this.flareSolverr = flareSolverr;
    }

    @Override
    public String platform() {
        return "linuxdo";
    }

    @Override
    public String platformName() {
        return "Linux.do";
    }

    @Override
    public String category() {
        return "forum";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body;

        // 优先使用FlareSolverr绕过Cloudflare
        if (flareSolverr.isEnabled() && flareSolverr.isAvailable()) {
            log.info("Using FlareSolverr to bypass Cloudflare for LinuxDo");
            try {
                body = flareSolverr.get("https://linux.do/latest.json");
            } catch (Exception e) {
                log.warn("FlareSolverr failed, falling back to direct request: {}", e.getMessage());
                body = fetchDirect();
            }
        } else {
            // 回退到直接请求
            log.debug("FlareSolverr not available, using direct request");
            body = fetchDirect();
        }

        if (body == null || body.isBlank()) {
            return List.of();
        }

        JsonNode root = mapper.readTree(body);
        JsonNode topics = root.path("topic_list").path("topics");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode node : topics) {
            String title = node.path("title").asText();
            if (title.isBlank()) continue;
            long id = node.path("id").asLong();
            String slug = node.path("slug").asText();
            String url = "https://linux.do/t/" + slug + "/" + id;

            long views = node.path("views").asLong(0);
            long likes = node.path("like_count").asLong(0);
            long posts = node.path("posts_count").asLong(0);
            String hotValue = formatNum(views) + " · " + likes + "赞";

            String tag = null;
            JsonNode tags = node.path("tags");
            if (tags.isArray() && tags.size() > 0) {
                tag = tags.get(0).asText();
            }

            String desc = posts > 0 ? (posts + " 回复") : null;
            list.add(new HotItem(++rank, title, url, hotValue, views, desc, tag));
            if (rank >= 50) break;
        }
        return list;
    }

    /**
     * 直接请求（不使用FlareSolverr）
     */
    private String fetchDirect() throws Exception {
        return http.newClient("https://linux.do/")
                .get()
                .uri("https://linux.do/latest.json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "application/json")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Referer", "https://linux.do/")
                .header("sec-ch-ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\"")
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-ch-ua-platform", "\"Windows\"")
                .header("sec-fetch-dest", "empty")
                .header("sec-fetch-mode", "cors")
                .header("sec-fetch-site", "same-origin")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
    }

    private String formatNum(long n) {
        if (n >= 10000) return String.format("%.1f万", n / 10000.0);
        return String.valueOf(n);
    }
}
