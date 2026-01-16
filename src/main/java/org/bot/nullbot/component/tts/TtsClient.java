package org.bot.nullbot.component.tts;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.config.TtsConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Data
@Component
@Slf4j
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
     * 调用 TTS API 合成语音 (BASE64 编码)
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

        // 打印完整请求内容到日志
        // logCompleteRequest(apiUrl, headers, requestBody, text);

        // 发送请求
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, Map.class);

        // 解析响应
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && "合成成功".equals(responseBody.get("msg"))) {
            return (String) responseBody.get("audio_url");
        }

        throw new RuntimeException("TTS失败: " + responseBody);
    }

    private String downloadAndConvertToBase64(String audioUrl) {
        // 下载音频文件
        byte[] audioBytes = restTemplate.getForObject(audioUrl, byte[].class);

        if (audioBytes == null || audioBytes.length == 0)
            throw new RuntimeException("下载音频文件失败");

        // 转换为base64编码
        return Base64.getEncoder().encodeToString(audioBytes);
    }

    private void logCompleteRequest(String url, HttpHeaders headers, Map<String, Object> requestBody, String originalText) {
        try {
            // 创建安全headers副本 隐藏敏感信息
            HttpHeaders safeHeaders = new HttpHeaders();
            safeHeaders.putAll(headers);

            // 隐藏敏感信息: API Key
            if (safeHeaders.containsHeader("Authorization")) {
                String authHeader = safeHeaders.getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    // 只显示前4位和后4位
                    if (token.length() > 8) {
                        String maskedToken = token.substring(0, 4) + "****" + token.substring(token.length() - 4);
                        safeHeaders.set("Authorization", "Bearer " + maskedToken);
                    } else {
                        safeHeaders.set("Authorization", "Bearer ****");
                    }
                }
            }

            // 创建日志输出
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("\n");
            logMessage.append("========== TTS API 请求详情 ==========\n");
            logMessage.append("请求URL: ").append(url).append("\n");
            logMessage.append("原始文本: ").append(originalText).append("\n");
            logMessage.append("文本长度: ").append(originalText.length()).append("\n");
            logMessage.append("\n");
            logMessage.append("请求头:\n");
            safeHeaders.forEach((key, values) -> {
                logMessage.append("  ").append(key).append(": ");
                values.forEach(logMessage::append);
                logMessage.append("\n");
            });
            logMessage.append("\n");
            logMessage.append("请求体 (JSON):\n");

            // 格式化请求体，确保文本内容显示完整
            Map<String, Object> logRequestBody = new HashMap<>(requestBody);
            String text = (String) logRequestBody.get("text");
            if (text != null && text.length() > 200) {
                logRequestBody.put("text_preview", text.substring(0, 200) + "... [总长度: " + text.length() + " 字符]");
            }

            // 使用JSON格式输出
            logMessage.append("{\n");
            logRequestBody.forEach((key, value) -> {
                if ("text".equals(key) && text != null && text.length() > 200) {
                    // 已经添加了预览，跳过原始长文本
                    return;
                }
                String valueStr;
                if (value instanceof String strValue && strValue.length() > 100) {
                    valueStr = "\"" + strValue.substring(0, 100) + "... [总长度: " + strValue.length() + " 字符]\"";
                } else {
                    valueStr = value != null ? value.toString() : "null";
                    if (value instanceof String) {
                        valueStr = "\"" + valueStr + "\"";
                    }
                }
                logMessage.append("  \"").append(key).append("\": ").append(valueStr).append(",\n");
            });
            logMessage.append("}\n");
            logMessage.append("======================================\n");

            log.info("TTS请求详情:\n{}", logMessage);
        } catch (Exception e) {
            log.warn("记录请求日志时发生异常: {}", e.getMessage());
        }
    }
}
