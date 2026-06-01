package com.hotdata.source;

import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

@Component
public class ReadhubSource extends RssHotSource {
    public ReadhubSource(HttpClientFactory http) { super(http); }
    @Override public String platform() { return "readhub"; }
    @Override public String platformName() { return "Readhub"; }
    @Override public String category() { return "tech"; }
    @Override protected String feedUrl() { return "https://readhub.cn/rss"; }
    @Override protected String referer() { return "https://readhub.cn/"; }
}
