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
public class SspaiSource implements HotSource {

    private final HttpClientFactory http;

    public SspaiSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "sspai";
    }

    @Override
    public String platformName() {
        return "少数派";
    }

    @Override
    public String category() {
        return "tech";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://sspai.com/").get()
                .uri("https://sspai.com/feed")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) {
            return List.of();
        }
        Document doc = Jsoup.parse(body, "", org.jsoup.parser.Parser.xmlParser());
        Elements items = doc.select("item");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (Element item : items) {
            String title = item.selectFirst("title") != null ? item.selectFirst("title").text().trim() : "";
            String url = item.selectFirst("link") != null ? item.selectFirst("link").text().trim() : "";
            String desc = item.selectFirst("description") != null ? Jsoup.parse(item.selectFirst("description").text()).text() : null;
            String pubDate = item.selectFirst("pubDate") != null ? item.selectFirst("pubDate").text().trim() : "最新";
            if (title.isBlank() || url.isBlank()) continue;
            list.add(new HotItem(++rank, title, url, pubDate, (long) rank, trim(desc, 120), null));
            if (rank >= 30) break;
        }
        return list;
    }

    private String trim(String s, int max) {
        if (s == null || s.isBlank()) return null;
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }
}
