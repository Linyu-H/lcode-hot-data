package com.hotdata.source;

import com.hotdata.util.HttpClientFactory;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class JandanSource extends HtmlListSource {
    public JandanSource(HttpClientFactory http) { super(http); }
    @Override public String platform() { return "jandan"; }
    @Override public String platformName() { return "煎蛋热榜"; }
    @Override public String category() { return "entertainment"; }
    @Override protected String pageUrl() { return "https://jandan.net/top"; }
    @Override protected String linkSelector() { return "a[href*=/t/] , a[href*=/p/] , a[href*=/ooxx/]"; }
    @Override protected boolean accept(Element link, String title, String url) {
        return title.length() >= 3 && url.contains("jandan.net") && !title.equals("煎蛋");
    }
}
