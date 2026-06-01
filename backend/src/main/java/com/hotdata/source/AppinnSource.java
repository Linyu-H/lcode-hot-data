package com.hotdata.source;

import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

@Component
public class AppinnSource extends RssHotSource {
    public AppinnSource(HttpClientFactory http) { super(http); }
    @Override public String platform() { return "appinn"; }
    @Override public String platformName() { return "小众软件"; }
    @Override public String category() { return "tech"; }
    @Override protected String feedUrl() { return "https://www.appinn.com/feed/"; }
    @Override protected String referer() { return "https://www.appinn.com/"; }
}
