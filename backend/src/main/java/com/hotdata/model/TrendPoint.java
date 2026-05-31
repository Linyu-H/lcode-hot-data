package com.hotdata.model;

public record TrendPoint(long timestamp, int rank, Long hotScore, String hotValue) {
}
