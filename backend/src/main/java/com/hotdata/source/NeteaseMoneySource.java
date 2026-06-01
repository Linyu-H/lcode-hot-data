package com.hotdata.source;

import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NeteaseMoneySource implements HotSource {
    private final HttpClientFactory http;
    public NeteaseMoneySource(HttpClientFactory http) { this.http = http; }
    @Override public String platform() { return "netease_money"; }
    @Override public String platformName() { return "网易财经"; }
    @Override public String category() { return "finance"; }

    @Override
    public List<HotItem> fetch() throws Exception {
        String body = http.newClient("https://money.163.com/").get()
                .uri("https://money.163.com/special/00259BVP/news_flow_index.js")
                .retrieve()
                .bodyToMono(String.class)
                .block(http.timeout());
        if (body == null || body.isBlank()) return List.of();
        Pattern p = Pattern.compile("\\{[^{}]*?\\\"title\\\"\\s*:\\s*\\\"(.*?)\\\"[^{}]*?\\\"docurl\\\"\\s*:\\s*\\\"(.*?)\\\"", Pattern.DOTALL);
        Matcher m = p.matcher(body);
        List<HotItem> list = new ArrayList<>();
        int rank = 0;
        while (m.find() && rank < 30) {
            String title = unescape(m.group(1));
            String url = unescape(m.group(2));
            if (title.isBlank() || url.isBlank()) continue;
            list.add(new HotItem(++rank, title, url, "最新", (long) (31 - rank), null, null));
        }
        return list;
    }

    private String unescape(String s) {
        return s.replace("\\/", "/").replace("\\\"", "\"").trim();
    }
}
