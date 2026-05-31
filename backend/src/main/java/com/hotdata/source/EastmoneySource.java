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
        // 东方财富热搜榜
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://searchapi.eastmoney.com/api/suggest/get?input=&type=14&token=D43BF722C8E33BDC906FB84D85E326E8&count=30"))
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                .header("Accept", "application/json, text/javascript, */*; q=0.01")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Referer", "https://so.eastmoney.com/")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200 || resp.body() == null) {
            throw new IllegalStateException("HTTP " + resp.statusCode());
        }

        JsonNode root = mapper.readTree(resp.body());
        JsonNode data = root.path("QuotationCodeTable").path("Data");
        List<HotItem> list = new ArrayList<>();
        int rank = 0;

        for (JsonNode node : data) {
            String code = node.path("Code").asText();
            String name = node.path("Name").asText();
            if (name.isBlank()) continue;

            String title = name + " (" + code + ")";
            String marketCode = node.path("MktNum").asText();
            String url = "https://quote.eastmoney.com/" + marketCode + code + ".html";

            String hotValue = "热搜";
            String type = node.path("TypeName").asText("");

            list.add(new HotItem(++rank, title, url, hotValue, (long) rank, null, type));
            if (rank >= 30) break;
        }

        return list;
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
