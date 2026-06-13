package com.zincoid.nullbot.core.module.ai.tts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zincoid.nullbot.core.properties.ai.TtsProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TtsReq {

    // --------- 通用字段 ---------

    private String version;
    private String text;
    @JsonProperty("text_lang")
    private String textLang;
    @JsonProperty("prompt_text_lang")
    private String promptTextLang;

    // ---- infer_single 专属 ----

    @JsonProperty("model_name")
    private String modelName;
    private String emotion;

    // ---- infer_classic 专属 ----

    @JsonProperty("gpt_model_name")
    private String gptModelName;
    @JsonProperty("sovits_model_name")
    private String sovitsModelName;
    @JsonProperty("ref_audio_path")
    private String refAudioPath;
    @JsonProperty("prompt_text")
    private String promptText;

    // ------ 合成参数 (共用) ------

    @JsonProperty("top_k")
    private int topK;
    @JsonProperty("top_p")
    private int topP;
    private double temperature;
    @JsonProperty("text_split_method")
    private String textSplitMethod;
    @JsonProperty("batch_size")
    private int batchSize;
    @JsonProperty("batch_threshold")
    private double batchThreshold;
    @JsonProperty("split_bucket")
    private boolean splitBucket;
    @JsonProperty("speed_facter")
    private double speedFacter;
    @JsonProperty("fragment_interval")
    private double fragmentInterval;
    @JsonProperty("media_type")
    private String mediaType;
    @JsonProperty("parallel_infer")
    private boolean parallelInfer;
    @JsonProperty("repetition_penalty")
    private double repetitionPenalty;
    private int seed;
    @JsonProperty("sample_steps")
    private int sampleSteps;
    @JsonProperty("if_sr")
    private boolean ifSr;

    public static TtsReq forSynthesize(String text, TtsProperties props) {
        return defaults()
                .version(props.getVersion())
                .modelName(props.getModelName())
                .promptTextLang(props.getPromptTextLang())
                .emotion(props.getEmotion())
                .text(text)
                .textLang(props.getTextLang())
                .build();
    }

    public static TtsReq forClone(String refAudioPath, String refText, String text) {
        return defaults()
                .version("v4")
                .gptModelName("【GSVI】羽毛笔-e10")
                .sovitsModelName("【GSVI】羽毛笔_e10_s150_I32")
                .refAudioPath(refAudioPath)
                .promptText(refText)
                .promptTextLang("中文")
                .text(text)
                .textLang("中文")
                .build();
    }

    private static TtsReqBuilder defaults() {
        return TtsReq.builder()
                .topK(10)
                .topP(1)
                .temperature(1)
                .textSplitMethod("不切")
                .batchSize(1)
                .batchThreshold(0.75)
                .splitBucket(true)
                .speedFacter(1)
                .fragmentInterval(0.3)
                .mediaType("mp3")
                .parallelInfer(true)
                .repetitionPenalty(1.35)
                .seed(-1)
                .sampleSteps(16)
                .ifSr(false);
    }
}
