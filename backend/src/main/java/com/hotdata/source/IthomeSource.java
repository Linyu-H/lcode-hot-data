package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class IthomeSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public IthomeSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "ithome";
    }

    @Override
    public String platformName() {
        return "IT之家";
    }

    @Override
    public String category() {
        return "tech";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://www.ithome.com/").get()
                .uri("https://api.ithome.com/json/newslist/rank")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) {
            return List.of();
        }
        JsonNode root = mapper.readTree(body);
        JsonNode arr = root.path("channel48rank");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode node : arr) {
            String title = node.path("title").asText();
            if (title.isBlank()) continue;
            String urlPath = node.path("url").asText();
            String url = "https://www.ithome.com" + urlPath;
            long hits = node.path("hitcount").asLong(0);
            long comments = node.path("commentcount").asLong(0);
            String hotValue = formatNum(hits) + " · " + comments + "评";
            list.add(new HotItem(++rank, title, url, hotValue, hits, null, null));
        }
        return list;
    }

    private String formatNum(long n) {
        if (n >= 10000) return String.format("%.1f万", n / 10000.0);
        return String.valueOf(n);
    }
}
