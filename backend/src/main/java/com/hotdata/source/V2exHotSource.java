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
public class V2exHotSource implements HotSource {

    private final HttpClientFactory http;

    public V2exHotSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "v2ex_hot";
    }

    @Override
    public String platformName() {
        return "V2EX热议";
    }

    @Override
    public String category() {
        return "forum";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String html = http.newClient("https://www.v2ex.com/").get()
                .uri("https://www.v2ex.com/?tab=hot")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (html == null || html.isBlank()) {
            return List.of();
        }
        Document doc = Jsoup.parse(html, "https://www.v2ex.com/");
        Elements items = doc.select(".cell.item");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (Element item : items) {
            Element link = item.selectFirst("span.item_title a");
            if (link == null) continue;
            String title = link.text().trim();
            if (title.isBlank()) continue;
            String url = link.absUrl("href");

            Element nodeEl = item.selectFirst("a.node");
            Element countEl = item.selectFirst("a.count_livid, a.count_orange");
            String node = nodeEl != null ? nodeEl.text().trim() : null;
            String replies = countEl != null ? countEl.text().trim() : "";
            String hotValue = replies.isBlank() ? "" : replies + " 回复";

            list.add(new HotItem(++rank, title, url, hotValue, parseLong(replies), null, node));
            if (rank >= 30) break;
        }
        return list;
    }

    private Long parseLong(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return Long.parseLong(text.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
