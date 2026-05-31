package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BiliSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public BiliSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "bilibili";
    }

    @Override
    public String platformName() {
        return "B站热榜";
    }

    @Override
    public String category() {
        return "entertainment";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://www.bilibili.com/").get()
                .uri("https://api.bilibili.com/x/web-interface/popular?ps=50&pn=1")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null) {
            return List.of();
        }
        JsonNode root = mapper.readTree(body);
        JsonNode arr = root.path("data").path("list");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode node : arr) {
            String title = node.path("title").asText();
            if (title.isBlank()) continue;
            String bvid = node.path("bvid").asText();
            String url = "https://www.bilibili.com/video/" + bvid;
            long view = node.path("stat").path("view").asLong(0);
            String hotValue = formatNum(view);
            String reason = node.path("rcmd_reason").path("content").asText(null);
            String upName = node.path("owner").path("name").asText(null);
            list.add(new HotItem(++rank, title, url, hotValue, view, reason, upName));
        }
        return list;
    }

    private String formatNum(long num) {
        if (num >= 10000) {
            return String.format("%.1f万播放", num / 10000.0);
        }
        return num + "播放";
    }
}
