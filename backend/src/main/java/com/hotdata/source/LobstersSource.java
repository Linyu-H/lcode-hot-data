package com.hotdata.source;

import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

@Component
public class LobstersSource extends RssHotSource {
    public LobstersSource(HttpClientFactory http) { super(http); }
    @Override public String platform() { return "lobsters"; }
    @Override public String platformName() { return "Lobsters"; }
    @Override public String category() { return "dev"; }
    @Override protected String feedUrl() { return "https://lobste.rs/rss"; }
    @Override protected String referer() { return "https://lobste.rs/"; }
}
