package com.hotdata.source;

import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public abstract class RssHotSource implements HotSource {

    private final HttpClientFactory http;

    protected RssHotSource(HttpClientFactory http) {
        this.http = http;
    }

    protected abstract String feedUrl();

    protected String referer() {
        return feedUrl();
    }

    protected int limit() {
        return 30;
    }

    protected String defaultHotValue(Element item) {
        String date = text(item, "pubDate");
        if (date.isBlank()) date = text(item, "updated");
        if (date.isBlank()) date = text(item, "published");
        return date.isBlank() ? "最新" : date;
    }

    protected String normalizeBody(String body) {
        return body;
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient(referer()).get()
                .uri(feedUrl())
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) {
            return List.of();
        }
        body = normalizeBody(body);
        Document doc = Jsoup.parse(body, "", org.jsoup.parser.Parser.xmlParser());
        Elements entries = doc.select("item");
        if (entries.isEmpty()) entries = doc.select("entry");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (Element item : entries) {
            String title = text(item, "title");
            String url = text(item, "link");
            Element link = item.selectFirst("link[href]");
            if ((url.isBlank() || url.length() > 500) && link != null) {
                url = link.attr("href");
            }
            String desc = text(item, "description");
            if (desc.isBlank()) desc = text(item, "summary");
            if (desc.isBlank()) desc = text(item, "content");
            desc = Jsoup.parse(desc).text();
            if (title.isBlank() || url.isBlank()) continue;
            list.add(new HotItem(++rank, title, url, defaultHotValue(item), (long) (limit() - rank + 1), trim(desc, 120), null));
            if (rank >= limit()) break;
        }
        return list;
    }

    protected String text(Element root, String selector) {
        Element el = root.selectFirst(selector);
        return el == null ? "" : el.text().trim();
    }

    protected String trim(String s, int max) {
        if (s == null || s.isBlank()) return null;
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }
}
