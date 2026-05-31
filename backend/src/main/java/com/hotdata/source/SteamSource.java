package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SteamSource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();

    public SteamSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "steam";
    }

    @Override
    public String platformName() {
        return "Steam";
    }

    @Override
    public String category() {
        return "game";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://store.steampowered.com/").get()
                .uri("https://store.steampowered.com/api/featuredcategories")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) {
            return List.of();
        }
        JsonNode root = mapper.readTree(body);
        JsonNode specials = root.path("specials").path("items");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode node : specials) {
            String name = node.path("name").asText();
            if (name.isBlank()) continue;
            int appid = node.path("id").asInt();
            String url = "https://store.steampowered.com/app/" + appid;

            list.add(new HotItem(++rank, name, url, "", null, null, null));
            if (rank >= 20) break;
        }
        return list;
    }
}
