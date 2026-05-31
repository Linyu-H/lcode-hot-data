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
public class DouyinSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public DouyinSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "douyin";
    }

    @Override
    public String platformName() {
        return "抖音热榜";
    }

    @Override
    public String category() {
        return "entertainment";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://www.douyin.com/").get()
                .uri("https://www.douyin.com/aweme/v1/web/hot/search/list/")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) {
            return List.of();
        }
        JsonNode root = mapper.readTree(body);
        JsonNode arr = root.path("data").path("word_list");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode node : arr) {
            String word = node.path("word").asText();
            if (word.isBlank()) continue;
            String url = "https://www.douyin.com/search/" + URLEncoder.encode(word, StandardCharsets.UTF_8);
            long hotValue = node.path("hot_value").asLong(0);
            String hot = hotValue > 0 ? formatNum(hotValue) : "";
            list.add(new HotItem(++rank, word, url, hot, hotValue, null, null));
            if (rank >= 30) break;
        }
        return list;
    }

    private String formatNum(long n) {
        if (n >= 100000000) return String.format("%.1f亿", n / 100000000.0);
        if (n >= 10000) return String.format("%.1f万", n / 10000.0);
        return String.valueOf(n);
    }
}
