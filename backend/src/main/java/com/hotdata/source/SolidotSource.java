package com.hotdata.source;

import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

@Component
public class SolidotSource extends RssHotSource {

    public SolidotSource(HttpClientFactory http) {
        super(http);
    }

    @Override
    public String platform() {
        return "solidot";
    }

    @Override
    public String platformName() {
        return "Solidot";
    }

    @Override
    public String category() {
        return "tech";
    }

    @Override
    protected String feedUrl() {
        return "https://www.solidot.org/index.rss";
    }

    @Override
    protected String referer() {
        return "https://www.solidot.org/";
    }
}
