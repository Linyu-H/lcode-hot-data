package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JuejinSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public JuejinSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "juejin";
    }

    @Override
    public String platformName() {
        return "掘金";
    }

    @Override
    public String category() {
        return "dev";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String payload = "{\"id_type\":2,\"sort_type\":200,\"cursor\":\"0\",\"limit\":30}";
        String body = http.newClient("https://juejin.cn/").post()
                .uri("https://api.juejin.cn/recommend_api/v1/article/recommend_all_feed?aid=2608&spider=0")
                .header("Content-Type", "application/json")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) {
            return List.of();
        }
        JsonNode root = mapper.readTree(body);
        JsonNode data = root.path("data");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode item : data) {
            int itemType = item.path("item_type").asInt(0);
            if (itemType != 2) continue;
            JsonNode ai = item.path("item_info").path("article_info");
            String title = ai.path("title").asText();
            if (title.isBlank()) continue;
            String articleId = ai.path("article_id").asText();
            String url = "https://juejin.cn/post/" + articleId;
            long views = ai.path("view_count").asLong(0);
            long diggs = ai.path("digg_count").asLong(0);
            String hotValue = formatNum(views) + " · " + diggs + "赞";
            list.add(new HotItem(++rank, title, url, hotValue, views, null, null));
            if (rank >= 30) break;
        }
        return list;
    }

    private String formatNum(long n) {
        if (n >= 10000) return String.format("%.1f万", n / 10000.0);
        return String.valueOf(n);
    }
}
