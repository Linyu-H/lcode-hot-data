package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CsdnSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public CsdnSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "csdn";
    }

    @Override
    public String platformName() {
        return "CSDN热榜";
    }

    @Override
    public String category() {
        return "dev";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://blog.csdn.net/").get()
                .uri("https://blog.csdn.net/phoenix/web/blog/hot-rank?page=0&pageSize=25&type=")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) {
            return List.of();
        }
        JsonNode arr = mapper.readTree(body).path("data");
        if (!arr.isArray()) {
            return List.of();
        }
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode node : arr) {
            String title = firstText(node, "articleTitle", "title", "blogTitle");
            String url = firstText(node, "articleDetailUrl", "url", "blogUrl");
            String author = firstText(node, "nickName", "userName", "authorName");
            long score = node.path("hotRankScore").asLong(node.path("viewCount").asLong(rank + 1));
            String hotValue = score > 0 ? formatNum(score) + "热度" : "热门";
            if (title.isBlank() || url.isBlank()) continue;
            list.add(new HotItem(++rank, title, url, hotValue, score > 0 ? score : (long) rank, null, author.isBlank() ? null : author));
            if (rank >= 30) break;
        }
        return list;
    }

    private String firstText(JsonNode node, String... keys) {
        for (String key : keys) {
            String value = node.path(key).asText("").trim();
            if (!value.isBlank()) return value;
        }
        return "";
    }

    private String formatNum(long n) {
        if (n >= 10000) return String.format("%.1f万", n / 10000.0);
        return String.valueOf(n);
    }
}
