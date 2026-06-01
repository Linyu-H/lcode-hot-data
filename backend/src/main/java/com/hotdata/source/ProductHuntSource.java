package com.hotdata.source;

import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

@Component
public class ProductHuntSource extends RssHotSource {

    public ProductHuntSource(HttpClientFactory http) {
        super(http);
    }

    @Override
    public String platform() {
        return "producthunt";
    }

    @Override
    public String platformName() {
        return "Product Hunt";
    }

    @Override
    public String category() {
        return "dev";
    }

    @Override
    protected String feedUrl() {
        return "https://www.producthunt.com/feed";
    }

    @Override
    protected String referer() {
        return "https://www.producthunt.com/";
    }
}
