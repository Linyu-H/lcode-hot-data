package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class ToutiaoSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public ToutiaoSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "toutiao";
    }

    @Override
    public String platformName() {
        return "今日头条";
    }

    @Override
    public String category() {
        return "news";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://www.toutiao.com/").get()
                .uri("https://www.toutiao.com/hot-event/hot-board/?origin=toutiao_pc")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) {
            return List.of();
        }
        JsonNode root = mapper.readTree(body);
        JsonNode arr = root.path("data");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode node : arr) {
            String title = node.path("Title").asText();
            if (title.isBlank()) continue;
            String url = node.path("Url").asText();
            String hotValue = node.path("HotValue").asText("");
            Long score = parseLong(hotValue);
            list.add(new HotItem(++rank, title, url, hotValue, score, null, null));
            if (rank >= 30) break;
        }
        return list;
    }

    private Long parseLong(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Long.parseLong(s.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
