package com.zincoid.nullbot.core.module.ai.voice;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.properties.ai.TtsProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Data
public class TtsClient {

    private String apiUrl;
    private String apiKey;
    private String version;
    private String modelName;
    private String promptTextLang;
    private String textLang;
    private String emotion;

    private final RestTemplate restTemplate;

    public TtsClient(TtsProperties ttsProperties) {
        apiUrl = ttsProperties.getApiUrl();
        apiKey = ttsProperties.getApiKey();
        version = ttsProperties.getVersion();
        modelName = ttsProperties.getModelName();
        promptTextLang = ttsProperties.getPromptTextLang();
        textLang = ttsProperties.getTextLang();
        emotion = ttsProperties.getEmotion();
        restTemplate = new RestTemplate();
    }

    // ================== 用户方法 ==================

    /**
     * 调用TTS API合成语音 (BASE64 编码)
     */
    public String synthesize(String text) {
        // 调用TTS API获取音频URL
        String audioUrl = callTtsApiInferSingle(text);
        // 下载音频文件并转换为base64
        return downloadAndConvertToBase64(audioUrl);
    }

    /**
     * 调用TTS API克隆语音 (BASE64 编码)
     */
    public String synthesize_clone(String refAudioPath, String refText, String text) {
        // 调用TTS API获取音频URL
        String audioUrl = callTtsApiInferClassic(refAudioPath, refText, text);
        // 下载音频文件并转换为base64
        return downloadAndConvertToBase64(audioUrl);
    }

    // ================== 请求方法 ==================

    /**
     * 合成音频请求
     * @param text 合成文本
     * @return 音频 URL
     */
    private String callTtsApiInferSingle(String text) {
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
        ResponseEntity<Map> response = restTemplate.exchange(apiUrl + "/infer_single", HttpMethod.POST, request, Map.class);

        // 解析响应
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && "合成成功".equals(responseBody.get("msg"))) {
            return (String) responseBody.get("audio_url");
        }

        throw new RuntimeException("TTS失败: " + responseBody);
    }

    /**
     * 克隆音频请求
     * @param refAudioPath TTS API端音频模板路径
     * @param refText TTS API端音频模板路径文本
     * @param text 合成文本
     * @return 音频 URL
     */
    private String callTtsApiInferClassic(String refAudioPath, String refText, String text) {
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("accept", "application/json");

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("version", "v4");
        requestBody.put("gpt_model_name", "【GSVI】羽毛笔-e10");
        requestBody.put("sovits_model_name", "【GSVI】羽毛笔_e10_s150_I32");
        requestBody.put("ref_audio_path", refAudioPath);
        requestBody.put("prompt_text", refText);
        requestBody.put("prompt_text_lang", "中文");
        requestBody.put("text", text);
        requestBody.put("text_lang", "中文");

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
        ResponseEntity<Map> response = restTemplate.exchange(apiUrl + "/infer_classic", HttpMethod.POST, request, Map.class);

        // 解析响应
        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && "合成成功".equals(responseBody.get("msg"))) {
            return (String) responseBody.get("audio_url");
        }

        throw new RuntimeException("TTS失败: " + responseBody);
    }

    /**
     * 上传音频文件到TTS API服务器
     * @param filePath 本地文件路径
     * @return 服务器上的文件路径
     */
    public String upload(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists() || !file.isFile()) {
                throw new RuntimeException("文件不存在: " + filePath);
            }

            // 构建 multipart/form-data 请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("accept", "application/json");

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(file));

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            // 发送上传请求
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl + "/upload",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            // 解析响应
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && "上传成功".equals(responseBody.get("msg"))) {
                return (String) responseBody.get("file_path");
            } else {
                String errorMsg = responseBody != null ?
                        responseBody.toString() : "未知错误";
                throw new RuntimeException("文件上传失败: " + errorMsg);
            }

        } catch (Exception e) {
            log.error("上传文件失败: {}", filePath, e);
            throw new RuntimeException("上传文件失败: " + e.getMessage(), e);
        }
    }

    // ================== 工具方法 ==================

    private String downloadAndConvertToBase64(String audioUrl) {
        // 下载音频文件
        byte[] audioBytes = restTemplate.getForObject(audioUrl, byte[].class);

        if (audioBytes == null || audioBytes.length == 0)
            throw new RuntimeException("下载音频文件失败 URL: " + audioUrl);

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
