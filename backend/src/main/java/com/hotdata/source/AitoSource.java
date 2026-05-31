package com.hotdata.source;

import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AitoSource implements HotSource {

    private final HttpClientFactory http;

    public AitoSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "aito";
    }

    @Override
    public String platformName() {
        return "AITO论坛";
    }

    @Override
    public String category() {
        return "forum";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String html = http.newClient("https://aito.do/").get()
                .uri("https://aito.do/")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (html == null || html.isBlank()) {
            return List.of();
        }
        Document doc = Jsoup.parse(html);
        Elements items = doc.select(".topic-list-item, .topic-item");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (Element item : items) {
            Element link = item.selectFirst("a.title, a.topic-title");
            if (link == null) continue;
            String title = link.text().trim();
            if (title.isBlank()) continue;
            String url = link.absUrl("href");
            if (!url.startsWith("http")) {
                url = "https://aito.do" + url;
            }

            list.add(new HotItem(++rank, title, url, "", null, null, null));
            if (rank >= 30) break;
        }
        return list;
    }
}
