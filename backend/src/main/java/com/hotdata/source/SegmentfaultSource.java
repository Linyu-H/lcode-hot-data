package com.hotdata.source;

import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

@Component
public class SegmentfaultSource extends RssHotSource {

    public SegmentfaultSource(HttpClientFactory http) {
        super(http);
    }

    @Override
    public String platform() {
        return "segmentfault";
    }

    @Override
    public String platformName() {
        return "SegmentFault";
    }

    @Override
    public String category() {
        return "dev";
    }

    @Override
    protected String feedUrl() {
        return "https://segmentfault.com/feeds";
    }

    @Override
    protected String referer() {
        return "https://segmentfault.com/";
    }
}
