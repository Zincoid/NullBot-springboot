package com.zincoid.nullbot.core.module.request;

import com.zincoid.nullbot.core.exception.CoreException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

import java.time.Duration;

@Slf4j
@Component
public class RequestClient {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    private final RestClient restClient;

    public RequestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
        log.info("▽ [RequestClient] 通用请求客户端已初始化 - ConnectTimeout: {}, ReadTimeout: {}",
                CONNECT_TIMEOUT, READ_TIMEOUT);
    }

    public <T> T get(String url, Class<T> responseType) {
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(responseType);
        } catch (RestClientResponseException e) {
            log.warn("▽ [RequestClient] 请求失败 ({}): {}", e.getStatusCode().value(), url);
            throw new CoreException("请求失败: " + e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            log.warn("▽ [RequestClient] 请求异常: {} - {}", url, e.getMessage());
            throw new CoreException("请求异常: " + url);
        }
    }
}
