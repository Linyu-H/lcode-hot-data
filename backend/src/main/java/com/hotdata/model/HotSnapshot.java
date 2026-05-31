package com.hotdata.model;

import java.util.List;

public record HotSnapshot(long timestamp, List<HotBoard> boards) {
}
