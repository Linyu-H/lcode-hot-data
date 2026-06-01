package com.hotdata.source;

import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

@Component
public class IthomeDigiSource extends IthomeChannelSource {
    public IthomeDigiSource(HttpClientFactory http) { super(http); }
    @Override public String platform() { return "ithome_digi"; }
    @Override public String platformName() { return "IT之家数码"; }
    @Override public String category() { return "tech"; }
    @Override protected String endpoint() { return "https://api.ithome.com/json/newslist/digi"; }
}
