package com.hotdata.source;

import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

@Component
public class DevToSource extends RssHotSource {
    public DevToSource(HttpClientFactory http) { super(http); }
    @Override public String platform() { return "devto"; }
    @Override public String platformName() { return "DEV.to"; }
    @Override public String category() { return "dev"; }
    @Override protected String feedUrl() { return "https://dev.to/feed"; }
    @Override protected String referer() { return "https://dev.to/"; }
}
