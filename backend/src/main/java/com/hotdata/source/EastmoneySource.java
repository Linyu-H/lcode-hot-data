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
public class EastmoneySource implements HotSource {
    private final HttpClientFactory http;
    public EastmoneySource(HttpClientFactory http) { this.http = http; }
    @Override public String platform() { return "eastmoney"; }
    @Override public String platformName() { return "东方财富"; }
    @Override public String category() { return "finance"; }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://finance.eastmoney.com/").get()
                .uri("https://finance.eastmoney.com/a/czqyw.html")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) return List.of();
        Document doc = Jsoup.parse(body, "https://finance.eastmoney.com");
        Elements links = doc.select("a[href*=/a/]");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (Element a : links) {
            String title = a.text().trim().replaceAll("\\s+", " ");
            String url = a.absUrl("href");
            if (title.length() < 8 || !url.contains("eastmoney.com/a/")) continue;
            if (list.stream().anyMatch(i -> i.title().equals(title))) continue;
            list.add(new HotItem(++rank, title, url, "要闻", (long) (31 - rank), null, null));
            if (rank >= 30) break;
        }
        return list;
    }
}
