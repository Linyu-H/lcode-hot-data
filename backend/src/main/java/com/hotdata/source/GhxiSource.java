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
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://www.ghxi.com/"))
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
        Elements articles = doc.select("article.post, .post-item, .article-item");

        List<HotItem> list = new ArrayList<>();
        int rank = 0;

        for (Element article : articles) {
            Element titleElem = article.selectFirst("h2 a, .post-title a, .article-title a");
            if (titleElem == null) continue;

            String title = titleElem.text();
            if (title.isBlank()) continue;

            String url = titleElem.attr("abs:href");
            if (url.isBlank()) continue;

            // 尝试获取浏览量或评论数
            Element metaElem = article.selectFirst(".post-meta, .meta-info");
            String hotValue = "热门";
            if (metaElem != null) {
                String metaText = metaElem.text();
                if (metaText.contains("浏览") || metaText.contains("阅读")) {
                    hotValue = metaText;
                }
            }

            // 尝试获取摘要
            Element descElem = article.selectFirst(".post-excerpt, .excerpt, .summary");
            String desc = descElem != null ? descElem.text() : null;
            if (desc != null && desc.length() > 100) {
                desc = desc.substring(0, 100) + "...";
            }

            list.add(new HotItem(++rank, title, url, hotValue, (long) rank, desc, null));
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
