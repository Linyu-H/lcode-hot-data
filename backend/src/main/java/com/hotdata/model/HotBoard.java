package com.hotdata.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HotBoard(
        String platform,
        String platformName,
        String category,
        long updatedAt,
        boolean fromCache,
        String error,
        List<HotItem> items
) {
}
