package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Kr36Source implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public Kr36Source(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "36kr";
    }

    @Override
    public String platformName() {
        return "36氪";
    }

    @Override
    public String category() {
        return "finance";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://36kr.com/").post()
                .uri("https://gateway.36kr.com/api/mis/nav/home/nav/rank/hot")
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .bodyValue("{\"partner_id\":\"wap\",\"param\":{\"siteId\":1,\"platformId\":2}}")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) {
            return List.of();
        }
        JsonNode root = mapper.readTree(body);
        JsonNode arr = root.path("data").path("hotRankList");
        if (arr.isMissingNode() || !arr.isArray()) {
            return List.of();
        }
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode node : arr) {
            JsonNode tm = node.path("templateMaterial");
            String title = tm.path("widgetTitle").asText();
            if (title.isBlank()) continue;
            String route = node.path("route").asText();
            if (route.isBlank()) continue;
            String url = "https://www.36kr.com/" + route;
            long reads = tm.path("statRead").asLong(0);
            String hotValue = formatNum(reads) + "阅读";
            list.add(new HotItem(++rank, title, url, hotValue, reads, null, null));
            if (rank >= 30) break;
        }
        return list;
    }

    private String formatNum(long n) {
        if (n >= 10000) return String.format("%.1f万", n / 10000.0);
        return String.valueOf(n);
    }
}
