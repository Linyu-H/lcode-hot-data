package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DoubanSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public DoubanSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "douban";
    }

    @Override
    public String platformName() {
        return "豆瓣电影";
    }

    @Override
    public String category() {
        return "entertainment";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://m.douban.com/").get()
                .uri("https://m.douban.com/rexxar/api/v2/subject_collection/movie_real_time_hotest/items?count=20")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) {
            return List.of();
        }
        JsonNode root = mapper.readTree(body);
        JsonNode arr = root.path("subject_collection_items");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode node : arr) {
            String title = node.path("title").asText();
            if (title.isBlank()) continue;
            String url = node.path("url").asText();
            JsonNode rating = node.path("rating");
            double score = rating.path("value").asDouble(0);
            long count = rating.path("count").asLong(0);
            String hotValue = score > 0 ? score + "分 · " + formatNum(count) + "人" : "";
            String info = node.path("card_subtitle").asText(null);
            list.add(new HotItem(++rank, title, url, hotValue, count, info, null));
        }
        return list;
    }

    private String formatNum(long n) {
        if (n >= 10000) return String.format("%.1f万", n / 10000.0);
        return String.valueOf(n);
    }
}
