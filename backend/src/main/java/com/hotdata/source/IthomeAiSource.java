package com.hotdata.source;

import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

@Component
public class IthomeAiSource extends IthomeChannelSource {
    public IthomeAiSource(HttpClientFactory http) { super(http); }
    @Override public String platform() { return "ithome_ai"; }
    @Override public String platformName() { return "IT之家AI"; }
    @Override public String category() { return "tech"; }
    @Override protected String endpoint() { return "https://api.ithome.com/json/newslist/ai"; }
}
