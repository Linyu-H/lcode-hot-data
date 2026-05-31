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
public class GhxiSource implements HotSource {

    private final HttpClientFactory http;
    private final HttpClient client = buildClient();

    public GhxiSource(HttpClientFactory http) {
        this.http = http;
    }

    @Override
    public String platform() {
        return "ghxi";
    }

    @Override
    public String platformName() {
        return "果核剥壳";
    }

    @Override
    public String category() {
        return "tech";
    }

    @Override
    public List<HotItem> fetch() throws Exception {
        // 使用RSS源获取数据
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://www.ghxi.com/feed"))
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                .header("Accept", "application/rss+xml, application/xml, text/xml")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200 || resp.body() == null) {
            throw new IllegalStateException("HTTP " + resp.statusCode());
        }

        Document doc = Jsoup.parse(resp.body(), "", org.jsoup.parser.Parser.xmlParser());
        Elements items = doc.select("item");

        List<HotItem> list = new ArrayList<>();
        int rank = 0;

        for (Element item : items) {
            Element titleElem = item.selectFirst("title");
            if (titleElem == null) continue;

            String title = titleElem.text();
            if (title.isBlank()) continue;

            Element linkElem = item.selectFirst("link");
            String url = linkElem != null ? linkElem.text() : "";
            if (url.isBlank()) continue;

            // 获取分类
            Element categoryElem = item.selectFirst("category");
            String category = categoryElem != null ? categoryElem.text() : null;

            // 获取描述
            Element descElem = item.selectFirst("description");
            String desc = descElem != null ? descElem.text() : null;
            if (desc != null && desc.length() > 100) {
                desc = desc.substring(0, 100) + "...";
            }

            // 获取发布日期
            Element pubDateElem = item.selectFirst("pubDate");
            String hotValue = pubDateElem != null ? pubDateElem.text() : "最新";

            list.add(new HotItem(++rank, title, url, hotValue, (long) rank, desc, category));
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
