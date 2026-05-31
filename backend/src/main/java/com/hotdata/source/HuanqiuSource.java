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
public class HuanqiuSource implements HotSource {

    private final HttpClientFactory http;

    public HuanqiuSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "huanqiu";
    }

    @Override
    public String platformName() {
        return "环球网";
    }

    @Override
    public String category() {
        return "news";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String html = http.newClient("https://www.huanqiu.com/").get()
                .uri("https://www.huanqiu.com/")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (html == null || html.isBlank()) {
            return List.of();
        }
        Document doc = Jsoup.parse(html, "https://www.huanqiu.com/");

        // 查找所有文章链接
        Elements links = doc.select("a[href*=/article/]");

        List<HotItem> list = new ArrayList<>();
        int rank = 0;

        for (Element link : links) {
            String url = link.absUrl("href");
            if (url.isBlank() || !url.contains("huanqiu.com/article/")) continue;

            String title = link.text().trim();
            if (title.isBlank() || title.length() < 5) continue;

            // 去重
            boolean exists = list.stream().anyMatch(item -> item.title().equals(title));
            if (exists) continue;

            list.add(new HotItem(++rank, title, url, "", null, null, null));
            if (rank >= 30) break;
        }

        return list;
    }
}
