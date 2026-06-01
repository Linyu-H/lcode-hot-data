package com.hotdata.source;

import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

@Component
public class RuanyifengSource extends RssHotSource {

    public RuanyifengSource(HttpClientFactory http) {
        super(http);
    }

    @Override
    public String platform() {
        return "ruanyifeng";
    }

    @Override
    public String platformName() {
        return "阮一峰博客";
    }

    @Override
    public String category() {
        return "dev";
    }

    @Override
    protected String feedUrl() {
        return "https://www.ruanyifeng.com/blog/atom.xml";
    }

    @Override
    protected String referer() {
        return "https://www.ruanyifeng.com/blog/";
    }
}
