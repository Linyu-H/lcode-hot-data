package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HackerNewsSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public HackerNewsSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "hackernews";
    }

    @Override
    public String platformName() {
        return "Hacker News";
    }

    @Override
    public String category() {
        return "dev";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient().get()
                .uri("https://hn.algolia.com/api/v1/search?tags=front_page&hitsPerPage=30")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) {
            return List.of();
        }
        JsonNode root = mapper.readTree(body);
        JsonNode hits = root.path("hits");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode node : hits) {
            String title = node.path("title").asText();
            if (title.isBlank()) continue;
            String url = node.path("url").asText();
            if (url.isBlank()) {
                String objectId = node.path("objectID").asText();
                url = "https://news.ycombinator.com/item?id=" + objectId;
            }
            int points = node.path("points").asInt(0);
            int comments = node.path("num_comments").asInt(0);
            String hotValue = points + " points · " + comments + " comments";
            list.add(new HotItem(++rank, title, url, hotValue, (long) points, null, null));
        }
        return list;
    }
}
