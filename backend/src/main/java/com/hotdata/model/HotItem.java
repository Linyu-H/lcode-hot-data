package com.hotdata.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record HotItem(
        int rank,
        String title,
        String url,
        String hotValue,
        Long hotScore,
        String description,
        String extra
) {
}
