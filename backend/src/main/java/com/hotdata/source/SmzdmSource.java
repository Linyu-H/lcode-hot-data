package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SmzdmSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public SmzdmSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "smzdm";
    }

    @Override
    public String platformName() {
        return "什么值得买";
    }

    @Override
    public String category() {
        return "welfare";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://post.smzdm.com/").get()
                .uri("https://post.smzdm.com/rank/json_more?unit=day")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) {
            return List.of();
        }
        JsonNode arr = mapper.readTree(body).path("data");
        if (!arr.isArray()) {
            return List.of();
        }
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode node : arr) {
            String title = firstText(node, "article_title", "title");
            String url = firstText(node, "article_url", "url", "channel_url");
            String desc = Jsoup.parse(firstText(node, "article_excerpt", "article_content", "description")).text();
            long up = node.path("up_count").asLong(0);
            long comments = node.path("comment_count").asLong(0);
            long collections = node.path("collection_count").asLong(0);
            long score = up + comments + collections;
            String hotValue = score > 0 ? up + "赞 · " + comments + "评" : "热门";
            String tag = firstText(node, "channel_name", "channel_cn", "article_type", "article_channel_name");
            if (title.isBlank() || url.isBlank()) continue;
            list.add(new HotItem(++rank, title, url, hotValue, score > 0 ? score : (long) rank, trim(desc, 100), tag.isBlank() ? null : tag));
            if (rank >= 30) break;
        }
        return list;
    }

    private String firstText(JsonNode node, String... keys) {
        for (String key : keys) {
            String value = node.path(key).asText("").trim();
            if (!value.isBlank()) return value;
        }
        return "";
    }

    private String trim(String s, int max) {
        if (s == null || s.isBlank()) return null;
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }
}
