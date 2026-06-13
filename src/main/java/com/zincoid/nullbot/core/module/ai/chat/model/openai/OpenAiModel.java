package com.zincoid.nullbot.core.module.ai.chat.model.openai;

import com.zincoid.nullbot.core.module.ai.chat.model.Model;
import com.zincoid.nullbot.core.module.ai.chat.model.ModelReq;
import com.zincoid.nullbot.core.module.ai.chat.model.ModelRes;
import com.zincoid.nullbot.core.properties.ai.OpenAiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class OpenAiModel implements Model {

    private final OpenAiProperties openAiProperties;
    private final RestClient restClient;

    public OpenAiModel(OpenAiProperties openAiProperties) {
        this.openAiProperties = openAiProperties;
        this.restClient = RestClient.builder()
                .defaultHeader("Authorization", "Bearer " + openAiProperties.getApiKey())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
        log.info("▽ [OpenAiModel] 模型已初始化 - ModelName: {}", openAiProperties.getModel());
    }

    @Override
    public ModelRes invoke(ModelReq req) {
        OpenAiReq apiReq = OpenAiReq.from(req, openAiProperties.getModel());
        OpenAiRes apiRes = restClient.post()
                .uri(resolveUrl())
                .body(apiReq)
                .retrieve()
                .body(OpenAiRes.class);
        if (apiRes == null) throw new RuntimeException("OpenAI API返回空响应");
        return apiRes.toModelRes();
    }

    private String resolveUrl() {
        String url = openAiProperties.getApiUrl();
        if (url.endsWith("/chat/completions")) return url;
        return url.endsWith("/") ? url + "chat/completions" : url + "/chat/completions";
    }
}
