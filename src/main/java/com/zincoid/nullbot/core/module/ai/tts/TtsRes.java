package com.zincoid.nullbot.core.module.ai.tts;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TtsRes(
        String msg,
        @JsonProperty("audio_url") String audioUrl,
        @JsonProperty("file_path") String filePath
) {
    public boolean isSuccess() {
        return "合成成功".equals(msg) || "上传成功".equals(msg);
    }

    public void ensureSuccess() {
        if (!isSuccess())
            throw new RuntimeException("TTS失败: " + this);
    }
}
