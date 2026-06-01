package com.hotdata.source;

import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

@Component
public class InfoqSource extends RssHotSource {

    public InfoqSource(HttpClientFactory http) {
        super(http);
    }

    @Override
    public String platform() {
        return "infoq";
    }

    @Override
    public String platformName() {
        return "InfoQ中文";
    }

    @Override
    public String category() {
        return "dev";
    }

    @Override
    protected String feedUrl() {
        return "https://www.infoq.cn/feed";
    }

    @Override
    protected String referer() {
        return "https://www.infoq.cn/";
    }
}
