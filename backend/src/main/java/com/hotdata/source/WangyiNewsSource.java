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
public class WangyiNewsSource implements HotSource {

    private final HttpClientFactory http;

    public WangyiNewsSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "wangyi_news";
    }

    @Override
    public String platformName() {
        return "网易新闻";
    }

    @Override
    public String category() {
        return "news";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://news.163.com/").get()
                .uri("https://news.163.com/special/0001386F/rank_whole.html")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());

        if (body == null || body.isBlank()) {
            return List.of();
        }

        Document doc = Jsoup.parse(body);
        Elements items = doc.select("table tr td a");

        List<HotItem> list = new ArrayList<>();
        int rank = 1;

        for (Element item : items) {
            if (rank > 30) break;

            String title = item.text().trim();
            if (title.isEmpty() || title.length() < 5) continue;

            String url = item.attr("abs:href");
            if (url.isEmpty() || !url.startsWith("http")) continue;

            list.add(new HotItem(rank++, title, url, null, null, null, null));
        }

        return list;
    }
}
