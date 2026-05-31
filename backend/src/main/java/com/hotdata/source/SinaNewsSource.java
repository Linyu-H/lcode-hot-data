package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SinaNewsSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public SinaNewsSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "sina_news";
    }

    @Override
    public String platformName() {
        return "新浪新闻";
    }

    @Override
    public String category() {
        return "news";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://sina.cn/").get()
                .uri("https://newsapp.sina.cn/api/hotlist?newsId=HB-1-snhs%2Ftop_news_list-all")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) {
            return List.of();
        }
        JsonNode root = mapper.readTree(body);
        JsonNode arr = root.path("data").path("hotList");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode node : arr) {
            JsonNode info = node.path("info");
            String title = info.path("title").asText();
            if (title.isBlank()) continue;
            JsonNode base = node.path("base");
            String url = base.path("url").asText();
            String hotValue = info.path("hotValue").asText("");
            list.add(new HotItem(++rank, title, url, hotValue, null, null, null));
            if (rank >= 30) break;
        }
        return list;
    }
}
