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
public class JinshiSource implements HotSource {

    private final HttpClientFactory http;
    private final HttpClient client = buildClient();

    public JinshiSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "jinshi";
    }

    @Override
    public String platformName() {
        return "金十数据";
    }

    @Override
    public String category() {
        return "finance";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://www.jin10.com/"))
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
        Elements items = doc.select(".jin-flash-item, .flash-item, .news-item");

        List<HotItem> list = new ArrayList<>();
        int rank = 0;

        for (Element item : items) {
            Element titleElem = item.selectFirst(".jin-flash-item-content, .flash-content, .news-content");
            if (titleElem == null) continue;

            String title = titleElem.text();
            if (title.isBlank()) continue;

            Element timeElem = item.selectFirst(".jin-flash-item-time, .flash-time, .news-time");
            String time = timeElem != null ? timeElem.text() : "";

            String url = "https://www.jin10.com/";
            Element linkElem = item.selectFirst("a");
            if (linkElem != null && linkElem.hasAttr("href")) {
                String href = linkElem.attr("href");
                if (!href.isBlank()) {
                    url = href.startsWith("http") ? href : "https://www.jin10.com" + href;
                }
            }

            String hotValue = time.isBlank() ? "实时" : time;

            list.add(new HotItem(++rank, title, url, hotValue, (long) rank, null, "快讯"));
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
