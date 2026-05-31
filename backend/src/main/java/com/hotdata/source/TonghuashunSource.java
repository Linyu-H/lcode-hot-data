package com.hotdata.source;

import com.hotdata.model.HotItem;
import com.hotdata.util.HttpClientFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
public class TonghuashunSource implements HotSource {

    private final HttpClientFactory http;
    private final HttpClient client = buildClient();

    public TonghuashunSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "tonghuashun";
    }

    @Override
    public String platformName() {
        return "同花顺";
    }

    @Override
    public String category() {
        return "finance";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://data.10jqka.com.cn/rank/cxg/"))
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200 || resp.body() == null) {
            throw new IllegalStateException("HTTP " + resp.statusCode());
        }

        Document doc = Jsoup.parse(resp.body());
        Elements rows = doc.select("table tbody tr");

        List<HotItem> list = new ArrayList<>();
        int rank = 0;

        for (Element row : rows) {
            Elements tds = row.select("td");
            if (tds.size() < 4) continue;

            Element nameElem = tds.get(1).selectFirst("a");
            if (nameElem == null) continue;

            String name = nameElem.text();
            String code = tds.get(0).text();
            if (name.isBlank() || code.isBlank()) continue;

            String title = name + " (" + code + ")";
            String url = "http://stockpage.10jqka.com.cn/" + code + "/";

            String priceText = tds.get(2).text();
            String changeText = tds.get(3).text();

            String hotValue = changeText + " · " + priceText;

            String desc = tds.size() > 4 ? tds.get(4).text() : null;

            list.add(new HotItem(++rank, title, url, hotValue, (long) rank, desc, changeText));
            if (rank >= 30) break;
        }

        return list;
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
