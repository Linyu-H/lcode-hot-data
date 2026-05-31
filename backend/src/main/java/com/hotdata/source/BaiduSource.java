package com.hotdata.source;

import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class BaiduSource implements HotSource {

    private final HttpClientFactory http;

    public BaiduSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "baidu";
    }

    @Override
    public String platformName() {
        return "百度热搜";
    }

    @Override
    public String category() {
        return "comprehensive";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String html = http.newClient("https://www.baidu.com/").get()
                .uri("https://top.baidu.com/board?tab=realtime")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (html == null || html.isBlank()) {
            return List.of();
        }
        Document doc = Jsoup.parse(html);
        Elements wraps = doc.select("div[class*=category-wrap_]");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (Element wrap : wraps) {
            Element titleEl = wrap.selectFirst("div[class*=c-single-text-ellipsis]");
            if (titleEl == null) continue;
            String title = titleEl.text().trim();
            if (title.isBlank()) continue;

            Element hotEl = wrap.selectFirst("div[class*=hot-index_]");
            String hot = hotEl != null ? hotEl.text().trim() : "";

            Element link = wrap.selectFirst("a[class*=title_]");
            if (link == null) link = wrap.selectFirst("a[href]");
            String url = link != null ? link.absUrl("href") : "";
            if (url.isBlank()) {
                url = "https://www.baidu.com/s?wd=" + URLEncoder.encode(title, StandardCharsets.UTF_8);
            }

            Element descEl = wrap.selectFirst("div[class*=desc_]");
            String desc = descEl != null ? descEl.text().trim() : null;

            Long score = parseScore(hot);
            list.add(new HotItem(++rank, title, url, hot, score, desc, null));
            if (rank >= 50) break;
        }
        return list;
    }

    private Long parseScore(String hot) {
        if (hot == null || hot.isBlank()) return null;
        StringBuilder digits = new StringBuilder();
        for (char c : hot.toCharArray()) {
            if (Character.isDigit(c)) digits.append(c);
            else if (!digits.isEmpty()) break;
        }
        try {
            return digits.isEmpty() ? null : Long.parseLong(digits.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
