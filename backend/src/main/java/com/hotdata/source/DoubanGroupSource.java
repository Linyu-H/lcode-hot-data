package com.hotdata.source;

import com.hotdata.util.HttpClientFactory;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class DoubanGroupSource extends HtmlListSource {
    public DoubanGroupSource(HttpClientFactory http) { super(http); }
    @Override public String platform() { return "douban_group"; }
    @Override public String platformName() { return "豆瓣小组"; }
    @Override public String category() { return "forum"; }
    @Override protected String pageUrl() { return "https://www.douban.com/group/explore"; }
    @Override protected String linkSelector() { return "a[href*=/group/topic/]"; }
    @Override protected boolean accept(Element link, String title, String url) {
        return super.accept(link, title, url) && url.contains("/group/topic/");
    }
}
