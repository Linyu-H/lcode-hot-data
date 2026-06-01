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
public class OschinaSource implements HotSource {

    private final HttpClientFactory http;

    public OschinaSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "oschina";
    }

    @Override
    public String platformName() {
        return "开源中国";
    }

    @Override
    public String category() {
        return "dev";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://www.oschina.net/news").get()
                .uri("https://www.oschina.net/news/widgets/_news_index_project_list?type=ajax&p=1")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) return List.of();
        Document doc = Jsoup.parse(body, "https://www.oschina.net");
        Elements links = doc.select("a[href]");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (Element a : links) {
            String title = a.text().trim();
            String url = a.absUrl("href");
            if (title.length() < 8 || !url.contains("oschina.net/news")) continue;
            if (list.stream().anyMatch(i -> i.title().equals(title))) continue;
            list.add(new HotItem(++rank, title, url, "最新", (long) (31 - rank), null, null));
            if (rank >= 30) break;
        }
        return list;
    }
}
