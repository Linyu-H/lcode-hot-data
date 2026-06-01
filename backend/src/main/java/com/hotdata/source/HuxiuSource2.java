package com.hotdata.source;

import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

@Component
public class HuxiuSource2 extends RssHotSource {
    public HuxiuSource2(HttpClientFactory http) { super(http); }
    @Override public String platform() { return "huxiu_rss"; }
    @Override public String platformName() { return "虎嗅"; }
    @Override public String category() { return "finance"; }
    @Override protected String feedUrl() { return "https://rss.huxiu.com/"; }
    @Override protected String referer() { return "https://www.huxiu.com/"; }
}
