package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LinuxDoWelfareSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public LinuxDoWelfareSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "linuxdo_welfare";
    }

    @Override
    public String platformName() {
        return "Linux.do公益站";
    }

    @Override
    public String category() {
        return "forum";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        // 使用WebFlux客户端，更稳定
        String body = http.newClient("https://linux.do/")
                .get()
                .uri("https://linux.do/c/welfare/36.json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "application/json")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .header("Referer", "https://linux.do/c/welfare")
                .header("sec-ch-ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\"")
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-ch-ua-platform", "\"Windows\"")
                .header("sec-fetch-dest", "empty")
                .header("sec-fetch-mode", "cors")
                .header("sec-fetch-site", "same-origin")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());

        if (body == null || body.isBlank()) {
            return List.of();
        }

        JsonNode root = mapper.readTree(body);
        JsonNode topics = root.path("topic_list").path("topics");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode node : topics) {
            if (node.path("pinned").asBoolean(false)) continue;
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
                tag = tags.get(0).path("name").asText();
            }

            String desc = posts > 0 ? (posts + " 回复") : null;
            list.add(new HotItem(++rank, title, url, hotValue, views, desc, tag));
            if (rank >= 30) break;
        }
        return list;
    }

    private String formatNum(long n) {
        if (n >= 10000) return String.format("%.1f万", n / 10000.0);
        return String.valueOf(n);
    }
}
