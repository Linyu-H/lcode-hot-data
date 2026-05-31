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
public class TencentVideoSource implements HotSource {

    private final HttpClientFactory http;

    public TencentVideoSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "tencentvideo";
    }

    @Override
    public String platformName() {
        return "腾讯视频";
    }

    @Override
    public String category() {
        return "entertainment";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        String html = http.newClient("https://v.qq.com/").get()
                .uri("https://v.qq.com/x/bu/pagesheet/list?append=1&channel=cartoon&iarea=1&listpage=1&offset=0&pagesize=30")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (html == null || html.isBlank()) {
            return List.of();
        }
        Document doc = Jsoup.parse(html);
        Elements items = doc.select(".list_item");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        for (Element item : items) {
            Element link = item.selectFirst("a");
            if (link == null) continue;
            String title = link.attr("title");
            if (title.isBlank()) continue;
            String url = link.absUrl("href");

            list.add(new HotItem(++rank, title, url, "", null, null, null));
            if (rank >= 30) break;
        }
        return list;
    }
}
