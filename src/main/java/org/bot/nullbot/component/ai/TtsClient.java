package org.bot.nullbot.component.ai;

import lombok.Data;
import org.bot.nullbot.config.TtsConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Data
@Component
public class TtsClient
{
    private String apiUrl;
    private String apiKey;
    private String version;
    private String modelName;
    private String promptTextLang;
    private String textLang;
    private String emotion;

    private final RestTemplate restTemplate;

    public TtsClient(TtsConfig config) {
        apiUrl = config.getApiUrl();
        apiKey = config.getApiKey();
        version = config.getVersion();
        modelName = config.getModelName();
        promptTextLang = config.getPromptTextLang();
        textLang = config.getTextLang();
        emotion = config.getEmotion();
        restTemplate = new RestTemplate();
    }

    /**
     * 调用TTS API合成语音 返回base64编码的音频数据
     */
    public String synthesize(String text) {
        // 1. 调用TTS API获取音频URL
        String audioUrl = callTtsApi(text);

        // 2. 下载音频文件并转换为base64
        return downloadAndConvertToBase64(audioUrl);
    }

    private String callTtsApi(String text) {
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("accept", "application/json");

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("version", version);
        requestBody.put("model_name", modelName);
        requestBody.put("prompt_text_lang", promptTextLang);
        requestBody.put("emotion", emotion);
        requestBody.put("text", text);
        requestBody.put("text_lang", textLang);
        requestBody.put("top_k", 10);
        requestBody.put("top_p", 1);
        requestBody.put("temperature", 1);
        requestBody.put("text_split_method", "不切");
        requestBody.put("batch_size", 1);
        requestBody.put("batch_threshold", 0.75);
        requestBody.put("split_bucket", true);
        requestBody.put("speed_facter", 1);
        requestBody.put("fragment_interval", 0.3);
        requestBody.put("media_type", "mp3");
        requestBody.put("parallel_infer", true);
        requestBody.put("repetition_penalty", 1.35);
        requestBody.put("seed", -1);
        requestBody.put("sample_steps", 16);
        requestBody.put("if_sr", false);

        // 发送请求
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, Map.class);

        // 解析响应
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && "合成成功".equals(responseBody.get("msg"))) {
            return (String) responseBody.get("audio_url");
        }

        throw new RuntimeException("TTS合成失败: " + responseBody);
    }

    private String downloadAndConvertToBase64(String audioUrl) {
        // 下载音频文件
        byte[] audioBytes = restTemplate.getForObject(audioUrl, byte[].class);

        if (audioBytes == null || audioBytes.length == 0)
            throw new RuntimeException("下载音频文件失败");

        // 转换为base64编码
        return Base64.getEncoder().encodeToString(audioBytes);
    }
}
