package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ZhihuSource implements HotSource {

    private static final String MOBILE_UA =
            "Mozilla/5.0 (Linux; Android 10; Pixel 5) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36";

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public ZhihuSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "zhihu";
    }

    @Override
    public String platformName() {
        return "知乎热榜";
    }

    @Override
    public String category() {
        return "comprehensive";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://www.zhihu.com/").get()
                .uri("https://api.zhihu.com/topstory/hot-lists/total?limit=50")
                .headers(h -> h.set("User-Agent", MOBILE_UA))
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) {
            return List.of();
        }
        JsonNode root = mapper.readTree(body);
        JsonNode data = root.path("data");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode node : data) {
            JsonNode target = node.path("target");
            String title = target.path("title").asText();
            if (title.isBlank()) continue;

            String questionId = target.path("id").asText();
            String url = "https://www.zhihu.com/question/" + questionId;

            String hotValue = node.path("detail_text").asText("");
            Long score = parseScore(hotValue);

            String desc = target.path("excerpt").asText(null);
            if (desc != null && desc.length() > 80) desc = desc.substring(0, 80) + "...";

            list.add(new HotItem(++rank, title, url, hotValue, score, desc, null));
        }
        return list;
    }

    private Long parseScore(String hot) {
        if (hot == null || hot.isBlank()) return null;
        StringBuilder digits = new StringBuilder();
        for (char c : hot.toCharArray()) {
            if (Character.isDigit(c)) digits.append(c);
            else if (!digits.isEmpty()) break;
        }
        try {
            return digits.isEmpty() ? null : Long.parseLong(digits.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
