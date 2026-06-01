package com.hotdata.source;

import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public abstract class HtmlListSource implements HotSource {

    private final HttpClientFactory http;

    protected HtmlListSource(HttpClientFactory http) {
        this.http = http;
    }

    protected abstract String pageUrl();

    protected String referer() {
        return pageUrl();
    }

    protected abstract String linkSelector();

    protected boolean accept(Element link, String title, String url) {
        return title.length() >= 6 && url.startsWith("http");
    }

    protected String hotValue(Element link, int rank) {
        return "热门";
    }

    protected String extra(Element link) {
        return null;
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient(referer()).get()
                .uri(pageUrl())
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) return List.of();
        Document doc = Jsoup.parse(body, pageUrl());
        Elements links = doc.select(linkSelector());
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (Element link : links) {
            String title = link.text().trim().replaceAll("\\s+", " ");
            String url = link.absUrl("href");
            if (!accept(link, title, url)) continue;
            if (list.stream().anyMatch(i -> i.title().equals(title))) continue;
            list.add(new HotItem(++rank, title, url, hotValue(link, rank), (long) (30 - rank + 1), null, extra(link)));
            if (rank >= 30) break;
        }
        return list;
    }
}
