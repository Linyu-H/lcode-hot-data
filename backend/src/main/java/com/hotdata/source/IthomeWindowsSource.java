package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class IthomeWindowsSource implements HotSource {
    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();
    public IthomeWindowsSource(HttpClientFactory http) { this.http = http; }
    @Override public String platform() { return "ithome_windows"; }
    @Override public String platformName() { return "IT之家Windows"; }
    @Override public String category() { return "tech"; }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://www.ithome.com/").get()
                .uri("https://api.ithome.com/json/newslist/windows")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) return List.of();
        JsonNode arr = mapper.readTree(body).path("newslist");
        if (!arr.isArray()) return List.of();
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode n : arr) {
            String title = n.path("title").asText("");
            String url = n.path("url").asText("");
            if (!url.startsWith("http") && !url.isBlank()) url = "https://www.ithome.com" + url;
            long comments = n.path("commentcount").asLong(0);
            if (title.isBlank() || url.isBlank()) continue;
            list.add(new HotItem(++rank, title, url, comments > 0 ? comments + "评" : "最新", comments > 0 ? comments : (long) rank, null, null));
            if (rank >= 30) break;
        }
        return list;
    }
}
