package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ThepaperSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public ThepaperSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "thepaper";
    }

    @Override
    public String platformName() {
        return "澎湃新闻";
    }

    @Override
    public String category() {
        return "news";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://www.thepaper.cn/").get()
                .uri("https://cache.thepaper.cn/contentapi/wwwIndex/rightSidebar")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) {
            return List.of();
        }
        JsonNode root = mapper.readTree(body);
        JsonNode arr = root.path("data").path("hotNews");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode node : arr) {
            String title = node.path("name").asText();
            if (title.isBlank()) continue;
            String contId = node.path("contId").asText();
            String url = "https://www.thepaper.cn/newsDetail_forward_" + contId;
            list.add(new HotItem(++rank, title, url, "", null, null, null));
        }
        return list;
    }
}
