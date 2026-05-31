package com.hotdata.controller;

import com.hotdata.model.HotBoard;
import com.hotdata.model.HotSnapshot;
import com.hotdata.model.TrendPoint;
import com.hotdata.service.HotDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hot")
public class HotDataController {

    private final HotDataService service;

    public HotDataController(HotDataService service) {
        this.service = service;
    }

    @GetMapping("/snapshot")
    public HotSnapshot snapshot() {
        return service.latest();
    }

    @GetMapping("/board/{platform}")
    public ResponseEntity<HotBoard> board(@PathVariable String platform) {
        HotBoard board = service.board(platform);
        return board == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(board);
    }

    @GetMapping("/trend")
    public List<TrendPoint> trend(@RequestParam String platform, @RequestParam String title) {
        return service.trend(platform, title);
    }

    @GetMapping("/aggregate")
    public List<Map<String, Object>> aggregate(@RequestParam(defaultValue = "20") int top) {
        return service.aggregateRanking(top);
    }

    @PostMapping("/refresh")
    public HotSnapshot refresh() {
        return service.refresh();
    }
}
