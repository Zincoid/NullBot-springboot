package com.zincoid.nullbot.core.module.ai.chat.model.openai;

import com.zincoid.nullbot.core.exception.CoreException;
import com.zincoid.nullbot.core.module.ai.chat.manage.AiCostManager;
import com.zincoid.nullbot.core.module.ai.chat.model.Model;
import com.zincoid.nullbot.core.module.ai.chat.model.ModelReq;
import com.zincoid.nullbot.core.module.ai.chat.model.ModelRes;
import com.zincoid.nullbot.core.properties.ai.OpenAiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class OpenAiModel implements Model {

    private final OpenAiProperties openAiProperties;
    private final AiCostManager aiCostManager;
    private final Map<String, RestClient> restClients = new ConcurrentHashMap<>();

    public OpenAiModel(OpenAiProperties openAiProperties, AiCostManager aiCostManager) {
        this.openAiProperties = openAiProperties;
        this.aiCostManager = aiCostManager;
        if (openAiProperties.getProviders() != null)
            openAiProperties.getProviders().forEach(p ->
                    log.info("▽ [OpenAiModel] 供应商已注册 - Name: {}, Model: {}",
                            p.getName(), p.getModel()));
        log.info("▽ [OpenAiModel] 当前供应商: {}", openAiProperties.current().getName());
    }

    @Override
    public ModelRes invoke(ModelReq req) {
        OpenAiProperties.Provider provider = openAiProperties.current();
        OpenAiReq apiReq = OpenAiReq.from(req, provider.getModel());
        OpenAiRes apiRes;
        try {
            apiRes = getClient(provider).post()
                    .uri(resolveUrl(provider))
                    .body(apiReq)
                    .retrieve()
                    .body(OpenAiRes.class);
        } catch (RestClientResponseException e) {
            String body = e.getResponseBodyAsString();
            boolean insufficient = e.getStatusCode().is4xxClientError() && (
                    body.contains("Insufficient Balance")
                            || body.contains("InsufficientBalance")
                            || body.contains("insufficient_balance")
                            || body.contains("insufficient_quota")
                            || body.contains("quota")
                            || body.contains("余额"));
            if (insufficient) {
                aiCostManager.markOutOf(provider);
                log.warn("▽ [OpenAiModel] API 欠费 ({}) - {}: {}", provider.getName(), e.getStatusCode().value(), body);
                throw new CoreException("模型 API 已欠费 (OpenAI: " + provider.getName() + ")");
            }
            throw e;
        }
        if (apiRes == null) throw new RuntimeException("OpenAI API返回空响应");
        return apiRes.toModelRes();
    }

    private RestClient getClient(OpenAiProperties.Provider provider) {
        return restClients.computeIfAbsent(provider.getName(), name ->
                RestClient.builder()
                        .defaultHeader("Authorization", "Bearer " + provider.getApiKey())
                        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                        .build());
    }

    private String resolveUrl(OpenAiProperties.Provider provider) {
        String url = provider.getApiUrl();
        if (url.endsWith("/chat/completions")) return url;
        return url.endsWith("/") ? url + "chat/completions" : url + "/chat/completions";
    }
}
