package com.hotdata.source;

import com.hotdata.util.HttpClientFactory;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class ChiphellSource extends HtmlListSource {
    public ChiphellSource(HttpClientFactory http) { super(http); }
    @Override public String platform() { return "chiphell"; }
    @Override public String platformName() { return "Chiphell"; }
    @Override public String category() { return "forum"; }
    @Override protected String pageUrl() { return "https://www.chiphell.com/forum.php?mod=guide&view=hot"; }
    @Override protected String linkSelector() { return "a[href*=thread-]"; }
    @Override protected boolean accept(Element link, String title, String url) {
        return title.length() >= 5 && url.contains("chiphell.com") && url.contains("thread-");
    }
}
