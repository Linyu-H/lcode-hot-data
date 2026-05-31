package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class V2exSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public V2exSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "v2ex";
    }

    @Override
    public String platformName() {
        return "V2EX";
    }

    @Override
    public String category() {
        return "dev";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://www.v2ex.com/").get()
                .uri("https://www.v2ex.com/api/topics/hot.json")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) {
            return List.of();
        }
        JsonNode root = mapper.readTree(body);
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode node : root) {
            String title = node.path("title").asText();
            if (title.isBlank()) continue;
            String url = node.path("url").asText();
            int replies = node.path("replies").asInt(0);
            String hotValue = replies + " 回复";
            String nodeName = node.path("node").path("title").asText(null);
            list.add(new HotItem(++rank, title, url, hotValue, (long) replies, null, nodeName));
        }
        return list;
    }
}
