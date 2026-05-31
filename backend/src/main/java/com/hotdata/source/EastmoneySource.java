package com.hotdata.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class EastmoneySource implements HotSource {

    private final HttpClientFactory http;
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = buildClient();

    public EastmoneySource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "eastmoney";
    }

    @Override
    public String platformName() {
        return "东方财富";
    }

    @Override
    public String category() {
        return "finance";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        // 东方财富涨幅榜 - 使用WebFlux客户端
        try {
            String body = http.newClient("https://push2.eastmoney.com/")
                    .get()
                    .uri("https://push2.eastmoney.com/api/qt/clist/get?pn=1&pz=30&po=1&np=1&ut=bd1d9ddb04089700cf9c27f6f7426281&fltt=2&invt=2&fid=f3&fs=m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23&fields=f1,f2,f3,f4,f12,f13,f14")
                    .header("Referer", "https://quote.eastmoney.com/")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(http.timeout());

            if (body == null || body.isBlank()) {
                return List.of();
            }

            JsonNode root = mapper.readTree(body);
            JsonNode data = root.path("data").path("diff");

            if (data.isMissingNode() || !data.isArray()) {
                return List.of();
            }

            List<HotItem> list = new ArrayList<>();
            int rank = 0;

            for (JsonNode node : data) {
                String code = node.path("f12").asText();
                String name = node.path("f14").asText();
                if (name.isBlank() || code.isBlank()) continue;

                double price = node.path("f2").asDouble(0);
                double change = node.path("f3").asDouble(0);

                String title = name + " (" + code + ")";
                String marketCode = code.startsWith("6") ? "sh" : "sz";
                String url = "https://quote.eastmoney.com/" + marketCode + code + ".html";

                String changeStr = (change > 0 ? "+" : "") + String.format("%.2f%%", change);
                String hotValue = changeStr + " · ¥" + String.format("%.2f", price);

                list.add(new HotItem(++rank, title, url, hotValue, (long) Math.abs(change * 100), null, changeStr));
                if (rank >= 30) break;
            }

            return list;
        } catch (Exception e) {
            return List.of();
        }
    }

    private String formatAmount(long amount) {
        if (amount >= 100000000) {
            return String.format("%.2f亿", amount / 100000000.0);
        } else if (amount >= 10000) {
            return String.format("%.2f万", amount / 10000.0);
        }
        return String.valueOf(amount);
    }

    private static HttpClient buildClient() {
        HttpClient.Builder b = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL);
        InetSocketAddress proxy = detectProxy();
        if (proxy != null) {
            b.proxy(ProxySelector.of(proxy));
        }
        return b.build();
    }

    private static InetSocketAddress detectProxy() {
        String url = firstNonBlank(
                System.getenv("HTTPS_PROXY"),
                System.getenv("https_proxy"),
                System.getenv("HTTP_PROXY"),
                System.getenv("http_proxy"));
        if (url != null && !url.isBlank()) {
            try {
                URI u = URI.create(url);
                if (u.getHost() != null) {
                    int port = u.getPort() > 0 ? u.getPort() : 80;
                    return new InetSocketAddress(u.getHost(), port);
                }
            } catch (Exception ignored) {}
        }
        String host = firstNonBlank(
                System.getProperty("https.proxyHost"),
                System.getProperty("http.proxyHost"));
        String port = firstNonBlank(
                System.getProperty("https.proxyPort"),
                System.getProperty("http.proxyPort"));
        if (host != null && port != null) {
            try {
                return new InetSocketAddress(host, Integer.parseInt(port));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return null;
    }
}
