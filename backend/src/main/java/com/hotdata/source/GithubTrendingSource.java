package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GithubTrendingSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public GithubTrendingSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "github";
    }

    @Override
    public String platformName() {
        return "GitHub Trending";
    }

    @Override
    public String category() {
        return "dev";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        // 使用GitHub API获取热门仓库
        String body = http.newClient("https://api.github.com/").get()
                .uri("https://api.github.com/search/repositories?q=stars:>1&sort=stars&order=desc&per_page=25")
                .header("Accept", "application/vnd.github.v3+json")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());

        if (body == null || body.isBlank()) {
            return List.of();
        }

        JsonNode root = mapper.readTree(body);
        JsonNode items = root.path("items");

        if (items.isMissingNode() || !items.isArray()) {
            return List.of();
        }

        List<HotItem> list = new ArrayList<>();
        int rank = 0;

        for (JsonNode item : items) {
            String fullName = item.path("full_name").asText();
            if (fullName.isBlank()) continue;

            String url = item.path("html_url").asText();
            String description = item.path("description").asText("");
            int stars = item.path("stargazers_count").asInt(0);
            String hotValue = formatStars(stars) + " stars";

            list.add(new HotItem(++rank, fullName, url, hotValue, (long)stars, description, null));
            if (rank >= 25) break;
        }

        return list;
    }

    private String formatStars(int stars) {
        if (stars >= 1000) {
            return String.format("%.1fk", stars / 1000.0);
        }
        return String.valueOf(stars);
    }
}
