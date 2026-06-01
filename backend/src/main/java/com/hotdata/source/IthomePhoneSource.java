package com.hotdata.source;

import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

@Component
public class IthomePhoneSource extends IthomeChannelSource {
    public IthomePhoneSource(HttpClientFactory http) { super(http); }
    @Override public String platform() { return "ithome_phone"; }
    @Override public String platformName() { return "IT之家手机"; }
    @Override public String category() { return "tech"; }
    @Override protected String endpoint() { return "https://api.ithome.com/json/newslist/phone"; }
}
