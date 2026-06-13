package com.zincoid.nullbot.core.module.ai.tts;

import com.zincoid.nullbot.core.properties.ai.TtsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
public class TtsClient {

    private final TtsProperties ttsProperties;
    private final RestClient restClient;

    public TtsClient(TtsProperties ttsProperties) {
        this.ttsProperties = ttsProperties;
        this.restClient = RestClient.builder()
                .defaultHeader("Authorization", "Bearer " + ttsProperties.getApiKey())
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .build();
        log.info("▽ [TtsClient] TTS客户端已初始化 - ModelName: {}", ttsProperties.getModelName());
    }

    // ================== 用户方法 ==================

    public String synthesize(String text) {
        if (!isOnline()) throw new RuntimeException("TTS服务已离线");
        TtsReq req = TtsReq.forSynthesize(text, ttsProperties);
        return postSynthesize(req, "/infer_single");
    }

    public String clone(String refAudioPath, String refText, String text) {
        if (!isOnline()) throw new RuntimeException("TTS服务已离线");
        TtsReq req = TtsReq.forClone(refAudioPath, refText, text);
        return postSynthesize(req, "/infer_classic");
    }

    public String upload(String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile())
            throw new RuntimeException("文件不存在: " + filePath);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        TtsRes res = restClient.post()
                .uri(resolveUrl("/upload"))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(TtsRes.class);
        if (res == null) throw new RuntimeException("TTS 上传返回空响应");
        res.ensureSuccess();
        return res.filePath();
    }

    public boolean isOnline() {
        try {
            Map<?, ?> res = restClient.get()
                    .uri(resolveUrl("/api"))
                    .retrieve()
                    .body(Map.class);
            return res != null && res.containsKey("message");
        } catch (Exception e) {
            log.warn("TTS 服务不在线: {}", e.getMessage());
            return false;
        }
    }

    // ================== 工具方法 ==================

    private String resolveUrl(String path) {
        String url = ttsProperties.getApiUrl();
        return url.endsWith("/") ? url + path.substring(1) : url + path;
    }

    private String postSynthesize(TtsReq req, String endpoint) {
        TtsRes res = restClient.post()
                .uri(resolveUrl(endpoint))
                .body(req)
                .retrieve()
                .body(TtsRes.class);
        if (res == null) throw new RuntimeException("TTS API 返回空响应");
        res.ensureSuccess();
        return downloadAsBase64(res.audioUrl());
    }

    private String downloadAsBase64(String audioUrl) {
        byte[] audioBytes = restClient.get()
                .uri(audioUrl)
                .retrieve()
                .body(byte[].class);
        if (audioBytes == null || audioBytes.length == 0)
            throw new RuntimeException("下载音频文件失败 URL: " + audioUrl);
        return Base64.getEncoder().encodeToString(audioBytes);
    }
}
