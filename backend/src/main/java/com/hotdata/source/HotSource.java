package com.hotdata.source;

import com.hotdata.model.HotItem;

import java.util.List;

public interface HotSource {
    String platform();

    String platformName();

    String category();

    List<HotItem> fetch() throws Exception;
}
