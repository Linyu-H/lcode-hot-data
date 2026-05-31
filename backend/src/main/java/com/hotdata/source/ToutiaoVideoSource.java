package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ToutiaoVideoSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public ToutiaoVideoSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "toutiao_video";
    }

    @Override
    public String platformName() {
        return "头条视频";
    }

    @Override
    public String category() {
        return "entertainment";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://www.toutiao.com/").get()
                .uri("https://www.toutiao.com/hot-event/hot-board/?origin=toutiao_pc")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());

        if (body == null || body.isBlank()) {
            return List.of();
        }

        JsonNode root = mapper.readTree(body);
        JsonNode data = root.path("data");

        List<HotItem> list = new ArrayList<>();
        int rank = 1;

        for (JsonNode item : data) {
            if (rank > 30) break;

            String title = item.path("Title").asText();
            if (title.isEmpty()) continue;

            String url = item.path("Url").asText();
            if (url.isEmpty()) {
                url = "https://www.toutiao.com/";
            }

            String hotValue = item.path("HotValue").asText();

            list.add(new HotItem(rank++, title, url, hotValue, null, null, null));
        }

        return list;
    }
}
