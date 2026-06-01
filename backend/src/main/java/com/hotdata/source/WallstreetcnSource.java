package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WallstreetcnSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public WallstreetcnSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override public String platform() { return "wallstreetcn"; }
    @Override public String platformName() { return "华尔街见闻"; }
    @Override public String category() { return "finance"; }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://wallstreetcn.com/").get()
                .uri("https://api-one-wscn.awtmt.com/apiv1/content/information-flow?channel=global-channel&accept=article&cursor=&limit=20&action=upglide")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) return List.of();
        JsonNode arr = mapper.readTree(body).path("data").path("items");
        if (!arr.isArray()) return List.of();
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode item : arr) {
            JsonNode r = item.path("resource");
            String title = r.path("title").asText("");
            String url = r.path("uri").asText("");
            if (url.isBlank()) url = r.path("share_url").asText("");
            if (!url.startsWith("http") && !url.isBlank()) url = "https://wallstreetcn.com" + url;
            String desc = r.path("content_short").asText(r.path("description").asText(null));
            long score = r.path("display_time").asLong(rank + 1);
            if (title.isBlank() || url.isBlank()) continue;
            list.add(new HotItem(++rank, title, url, "最新", score, desc, null));
            if (rank >= 30) break;
        }
        return list;
    }
}
