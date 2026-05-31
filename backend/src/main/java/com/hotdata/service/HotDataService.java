package com.hotdata.service;

import com.hotdata.model.HotBoard;
import com.hotdata.model.HotItem;
import com.hotdata.model.HotSnapshot;
import com.hotdata.model.TrendPoint;
import com.hotdata.source.HotSource;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
public class HotDataService {

    private static final Logger log = LoggerFactory.getLogger(HotDataService.class);

    private final List<HotSource> sources;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final Map<String, HotBoard> latestBoards = new ConcurrentHashMap<>();
    private final Deque<HotSnapshot> history = new ArrayDeque<>();
    private final Map<String, Map<String, Deque<TrendPoint>>> trends = new ConcurrentHashMap<>();
    private final List<Consumer<HotSnapshot>> listeners = new ArrayList<>();

    @Value("${hotdata.history.max-snapshots}")
    private int maxSnapshots;

    public HotDataService(List<HotSource> sources) {
        this.sources = sources;
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    @Scheduled(fixedDelayString = "${hotdata.fetch.interval-ms}")
    public void scheduledRefresh() {
        refresh();
    }

    public synchronized HotSnapshot refresh() {
        long now = System.currentTimeMillis();
        List<CompletableFuture<HotBoard>> futures = sources.stream()
                .map(src -> CompletableFuture.supplyAsync(() -> fetchOne(src, now), executor))
                .toList();

        List<HotBoard> boards = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            try {
                boards.add(futures.get(i).get(15, TimeUnit.SECONDS));
            } catch (Exception e) {
                HotSource src = sources.get(i);
                HotBoard cached = latestBoards.get(src.platform());
                boards.add(new HotBoard(src.platform(), src.platformName(), src.category(), now, true,
                        e.getMessage(), cached == null ? List.of() : cached.items()));
            }
        }

        boards.forEach(b -> latestBoards.put(b.platform(), b));
        HotSnapshot snapshot = new HotSnapshot(now, boards);
        recordHistory(snapshot);
        recordTrend(snapshot);
        broadcast(snapshot);
        return snapshot;
    }

    private HotBoard fetchOne(HotSource src, long timestamp) {
        try {
            List<HotItem> items = src.fetch();
            return new HotBoard(src.platform(), src.platformName(), src.category(), timestamp, false, null, items);
        } catch (Exception e) {
            log.warn("fetch {} failed: {}", src.platform(), e.toString());
            HotBoard cached = latestBoards.get(src.platform());
            List<HotItem> items = cached == null ? List.of() : cached.items();
            return new HotBoard(src.platform(), src.platformName(), src.category(), timestamp, true, e.getMessage(), items);
        }
    }

    private void recordHistory(HotSnapshot snapshot) {
        synchronized (history) {
            history.addLast(snapshot);
            while (history.size() > maxSnapshots) {
                history.removeFirst();
            }
        }
    }

    private void recordTrend(HotSnapshot snapshot) {
        for (HotBoard board : snapshot.boards()) {
            Map<String, Deque<TrendPoint>> platformTrends =
                    trends.computeIfAbsent(board.platform(), k -> new ConcurrentHashMap<>());
            for (HotItem item : board.items()) {
                Deque<TrendPoint> series = platformTrends.computeIfAbsent(
                        item.title(), k -> new ArrayDeque<>());
                synchronized (series) {
                    series.addLast(new TrendPoint(snapshot.timestamp(), item.rank(),
                            item.hotScore(), item.hotValue()));
                    while (series.size() > maxSnapshots) {
                        series.removeFirst();
                    }
                }
            }
        }
    }

    public HotSnapshot latest() {
        long timestamp = history.isEmpty() ? System.currentTimeMillis()
                : history.peekLast().timestamp();
        List<HotBoard> ordered = new ArrayList<>();
        for (HotSource src : sources) {
            HotBoard board = latestBoards.get(src.platform());
            if (board != null) ordered.add(board);
        }
        return new HotSnapshot(timestamp, ordered);
    }

    public HotBoard board(String platform) {
        return latestBoards.get(platform);
    }

    public List<TrendPoint> trend(String platform, String title) {
        Map<String, Deque<TrendPoint>> platformTrends = trends.get(platform);
        if (platformTrends == null) return List.of();
        Deque<TrendPoint> series = platformTrends.get(title);
        if (series == null) return List.of();
        synchronized (series) {
            return new ArrayList<>(series);
        }
    }

    public List<Map<String, Object>> aggregateRanking(int topN) {
        Map<String, Map<String, Object>> agg = new HashMap<>();
        HotSnapshot snap = latest();
        for (HotBoard board : snap.boards()) {
            int total = board.items().size();
            for (HotItem item : board.items()) {
                Map<String, Object> entry = agg.computeIfAbsent(item.title(), k -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("title", k);
                    m.put("score", 0.0);
                    m.put("platforms", new ArrayList<Map<String, Object>>());
                    return m;
                });
                double score = (double) entry.get("score") + Math.max(0, total - item.rank() + 1);
                entry.put("score", score);
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> plats = (List<Map<String, Object>>) entry.get("platforms");
                Map<String, Object> p = new LinkedHashMap<>();
                p.put("platform", board.platform());
                p.put("platformName", board.platformName());
                p.put("rank", item.rank());
                p.put("url", item.url());
                p.put("hotValue", item.hotValue());
                plats.add(p);
            }
        }
        return agg.values().stream()
                .sorted(Comparator.comparingDouble((Map<String, Object> m) -> (double) m.get("score")).reversed())
                .limit(topN)
                .toList();
    }

    public void addListener(Consumer<HotSnapshot> listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(Consumer<HotSnapshot> listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private void broadcast(HotSnapshot snapshot) {
        List<Consumer<HotSnapshot>> snap;
        synchronized (listeners) {
            snap = new ArrayList<>(listeners);
        }
        for (Consumer<HotSnapshot> l : snap) {
            try {
                l.accept(snapshot);
            } catch (Exception e) {
                log.warn("listener failed: {}", e.toString());
            }
        }
    }
}
