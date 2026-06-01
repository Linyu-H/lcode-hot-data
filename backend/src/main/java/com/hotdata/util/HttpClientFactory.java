package com.hotdata.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.net.InetSocketAddress;
import java.time.Duration;

@Component
public class HttpClientFactory {

    @Value("${hotdata.fetch.user-agent}")
    private String userAgent;

    @Value("${hotdata.fetch.timeout-ms}")
    private int timeoutMs;

    @Value("${hotdata.proxy.enabled:false}")
    private boolean proxyEnabled;

    @Value("${hotdata.proxy.host:127.0.0.1}")
    private String proxyHost;

    @Value("${hotdata.proxy.port:7897}")
    private int proxyPort;

    public WebClient newClient() {
        return newClient(null);
    }

    public WebClient newClient(String referer) {
        HttpClient http = HttpClient.create()
                .responseTimeout(Duration.ofMillis(timeoutMs))
                .followRedirect(true);

        // 配置代理
        if (proxyEnabled) {
            http = http.proxy(proxy -> proxy
                    .type(ProxyProvider.Proxy.HTTP)
                    .address(new InetSocketAddress(proxyHost, proxyPort)));
        }

        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
        WebClient.Builder builder = WebClient.builder()
                .exchangeStrategies(strategies)
                .clientConnector(new ReactorClientHttpConnector(http))
                .defaultHeader("User-Agent", userAgent)
                .defaultHeader("Accept", "application/json,text/html,*/*")
                .defaultHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .defaultHeader("Cache-Control", "no-cache")
                .defaultHeader("Pragma", "no-cache");
        if (referer != null && !referer.isBlank()) {
            builder.defaultHeader("Referer", referer);
        }
        return builder.build();
    }

    public Duration timeout() {
        return Duration.ofMillis(timeoutMs);
    }

    public String userAgent() {
        return userAgent;
    }
}
