package com.hotdata.source;

import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

@Component
public class GcoresSource extends RssHotSource {

    public GcoresSource(HttpClientFactory http) {
        super(http);
    }

    @Override
    public String platform() {
        return "gcores";
    }

    @Override
    public String platformName() {
        return "机核";
    }

    @Override
    public String category() {
        return "game";
    }

    @Override
    protected String feedUrl() {
        return "https://www.gcores.com/rss";
    }

    @Override
    protected String referer() {
        return "https://www.gcores.com/";
    }
}
