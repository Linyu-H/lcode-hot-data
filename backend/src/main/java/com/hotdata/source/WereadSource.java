package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WereadSource implements HotSource {
    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();
    public WereadSource(HttpClientFactory http) { this.http = http; }
    @Override public String platform() { return "weread"; }
    @Override public String platformName() { return "微信读书"; }
    @Override public String category() { return "comprehensive"; }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://weread.qq.com/").get()
                .uri("https://weread.qq.com/web/bookListInCategory/all?rank=1")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) return List.of();
        JsonNode arr = mapper.readTree(body).path("books");
        if (!arr.isArray()) return List.of();
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (JsonNode b : arr) {
            JsonNode info = b.path("bookInfo");
            String title = info.path("title").asText("");
            String author = info.path("author").asText("");
            String bookId = info.path("bookId").asText("");
            long reading = b.path("readingCount").asLong(0);
            if (title.isBlank() || bookId.isBlank()) continue;
            String url = "https://weread.qq.com/web/bookDetail/" + bookId;
            list.add(new HotItem(++rank, title, url, reading > 0 ? reading + "人在读" : "热门", reading > 0 ? reading : (long) rank, null, author.isBlank() ? null : author));
            if (rank >= 30) break;
        }
        return list;
    }
}
