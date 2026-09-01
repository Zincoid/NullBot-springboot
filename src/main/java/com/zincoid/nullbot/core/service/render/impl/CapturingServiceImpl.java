package com.zincoid.nullbot.core.service.render.impl;

import com.zincoid.nullbot.core.module.render.browser.WebCapturer;
import com.zincoid.nullbot.core.service.render.CapturingService;
import com.zincoid.nullbot.web.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CapturingServiceImpl implements CapturingService {

    private final WebCapturer webCapturer;

    @Override
    public String prtsAny(String keyword) {
        return webCapturer.load("https://prts.wiki/w/" + keyword)
                .size(1024, 5120)
                // .click("input[onchange*='switchDisplay第一天赋算法']")
                .click("input[onchange*='switchDisplay第一天赋潜能']")
                // .click("input[onchange*='switchDisplay第二天赋算法']")
                .click("input[onchange*='switchDisplay第二天赋潜能']")
                .hide(".backToTop", "#toc", "#rightToc", ".music-btn", "#calc", "#equip-selector",
                        "#spine-root", "#catlinks", "#注释与链接", "#干员模型", "#敌人模型")
                .target("#bodyContent")
                .capture();
    }

    @Override
    public String prtsOpt(String option, String keyword) {
        return switch (option) {
            case "语音" -> webCapturer.load("https://prts.wiki/w/" + keyword)
                    .size(1024, 5120)
                    .click("a[class*='z-1 float-right select-none']")
                    .hide(".backToTop", "#rightToc", ".z-1.float-right.select-none")
                    .target("#voice-table-root")
                    .capture();
            case "档案" -> webCapturer.load("https://prts.wiki/w/" + keyword)
                    .size(1024, 5120)
                    .click("//table[.//th//b[contains(.,'人员档案')]]//button[contains(@class,'mw-collapsible-toggle')]")
                    .hide(".backToTop", "#rightToc", ".mw-collapsible-toggle")
                    .target("//table[.//th//b[contains(text(),'人员档案')]]")
                    .capture();
            case "密录" -> webCapturer.load("https://prts.wiki/w/" + keyword)
                    .size(1024, 5120)
                    .click("//table[.//th//b[contains(.,'干员密录')]]//button[contains(@class,'mw-collapsible-toggle')]")
                    .hide(".backToTop", "#rightToc", ".mw-collapsible-toggle")
                    .target("//table[.//th//b[contains(text(),'干员密录')]]")
                    .capture();
            case "悖论" -> webCapturer.load("https://prts.wiki/w/" + keyword)
                    .size(1024, 5120)
                    .click("//table[.//th//b[contains(.,'悖论模拟')]]//button[contains(@class,'mw-collapsible-toggle')]")
                    .hide(".backToTop", "#rightToc", ".mw-collapsible-toggle")
                    .target("//table[.//th//b[contains(text(),'悖论模拟')]]")
                    .capture();
            default -> throw new CommonException("无此查询项");
        };
    }

    @Override
    public String ai(String option) {
        String anchor = switch (option) {
            case "智能" -> "artificial-analysis-intelligence-index";
            case "模型对比" -> "artificial-analysis-intelligence-index-by-open-weights-proprietary";
            case "历史" -> "frontier-language-model-intelligence-over-time";
            case "成本" -> "cost-per-intelligence-index-task";
            case "性价比" -> "intelligence-index-vs-cost-per-intelligence-index-task";
            case "算力成本" -> "cost-to-run-artificial-analysis-intelligence-index";
            case "定价" -> "pricing-cache-hit-input-and-output";
            case "编码" -> "artificial-analysis-coding-agent-index";
            case "智能体" -> "artificial-analysis-agentic-index";
            case "开放性" -> "artificial-analysis-openness-index-components";
            case "Token" -> "output-tokens-per-intelligence-index-task";
            case "速度" -> "output-speed";
            case "耗时" -> "time-per-intelligence-index-task";
            case "供应商" -> "endpoint-accuracy-index-gpt-oss-120b-high";
            default -> throw new CommonException("无此查询项");
        };
        return webCapturer.load("https://artificialanalysis.ai/")
                .size(1536, 2400)
                .scrollTo("#" + anchor)
                .pause(2000)
                .target("//*[@id='" + anchor + "']/..")
                .capture();
    }
}
