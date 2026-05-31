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
public class WeiboSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public WeiboSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "weibo";
    }

    @Override
    public String platformName() {
        return "微博热搜";
    }

    @Override
    public String category() {
        return "comprehensive";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://weibo.com/").get()
                .uri("https://weibo.com/ajax/side/hotSearch")
                .header("x-requested-with", "XMLHttpRequest")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null) {
            return List.of();
        }
        JsonNode root = mapper.readTree(body);
        JsonNode arr = root.path("data").path("realtime");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode node : arr) {
            String word = node.path("word").asText();
            if (word.isBlank()) continue;
            String url = "https://s.weibo.com/weibo?q=" +
                    URLEncoder.encode("#" + word + "#", StandardCharsets.UTF_8);
            long num = node.path("num").asLong(0);
            String hotValue = num > 0 ? formatNum(num) : node.path("raw_hot").asText("");
            String label = node.path("label_name").asText(null);
            list.add(new HotItem(++rank, word, url, hotValue, num == 0 ? null : num, null, label));
            if (rank >= 50) break;
        }
        return list;
    }

    private String formatNum(long num) {
        if (num >= 10000) {
            return String.format("%.1f万", num / 10000.0);
        }
        return String.valueOf(num);
    }
}
