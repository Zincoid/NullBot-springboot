package com.zincoid.nullbot.core.service.render.impl;

import com.zincoid.nullbot.core.component.render.browser.WebCapturer;
import com.zincoid.nullbot.core.service.render.CapturingService;
import com.zincoid.nullbot.web.exception.CommonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CapturingServiceImpl implements CapturingService {

    private final WebCapturer webCapturer;

    @Override
    public String essence(String weapon) {
        return webCapturer.capture(
                "https://end.canmoe.com/", 1536, 5120,
                List.of("//section[contains(@class,'panel')][.//h2[contains(text(),'方案推荐列表')]]"),
                List.of(".ghost-button"),
                List.of(
                        "//*[@id=\"app\"]/div[2]/div/div[2]/div[2]/button",
                        "//*[@id=\"app\"]/div[3]/div/div[2]/div/button",
                        String.format("//span[@class='weapon-title-text' and text()='%s']", weapon),
                        "//button[contains(.,'收起其他方案')]"
                )
        );
    }

    @Override
    public String prtsAny(String keyword) {
        return webCapturer.capture(
                "https://prts.wiki/w/" + keyword, 1024, 5120,
                List.of("#bodyContent"),
                List.of(
                        ".backToTop", "#toc", "#rightToc",
                        ".music-btn", "#calc", "#equip-selector",
                        "#干员模型", "#敌人模型", "#spine-root",
                        "#注释与链接", "#catlinks"
                ),
                List.of(
                        // "input[onchange*='switchDisplay第一天赋算法']",
                        "input[onchange*='switchDisplay第一天赋潜能']",
                        // "input[onchange*='switchDisplay第二天赋算法']",
                        "input[onchange*='switchDisplay第二天赋潜能']"
                )
        );
    }

    @Override
    public String prtsOpt(String option, String keyword) {
        return switch (option) {
            case "语音" -> webCapturer.capture(
                    "https://prts.wiki/w/" + keyword, 1024, 5120,
                    List.of("#voice-table-root"),
                    List.of(".backToTop", "#rightToc", ".z-1.float-right.select-none"),
                    List.of("a[class*='z-1 float-right select-none']")
            );
            case "档案" -> webCapturer.capture(
                    "https://prts.wiki/w/" + keyword, 1024, 5120,
                    List.of("//table[.//th//b[contains(text(),'人员档案')]]"),
                    List.of(".backToTop", "#rightToc", ".mw-collapsible-toggle"),
                    List.of("//table[.//th//b[contains(.,'人员档案')]]//button[contains(@class,'mw-collapsible-toggle')]")
            );
            case "密录" -> webCapturer.capture(
                    "https://prts.wiki/w/" + keyword, 1024, 5120,
                    List.of("//table[.//th//b[contains(text(),'干员密录')]]"),
                    List.of(".backToTop", "#rightToc", ".mw-collapsible-toggle"),
                    List.of("//table[.//th//b[contains(.,'干员密录')]]//button[contains(@class,'mw-collapsible-toggle')]")
            );
            case "悖论" -> webCapturer.capture(
                    "https://prts.wiki/w/" + keyword, 1024, 5120,
                    List.of("//table[.//th//b[contains(text(),'悖论模拟')]]"),
                    List.of(".backToTop", "#rightToc", ".mw-collapsible-toggle"),
                    List.of("//table[.//th//b[contains(.,'悖论模拟')]]//button[contains(@class,'mw-collapsible-toggle')]")
            );
            default -> throw new CommonException("无此查询项");
        };
    }
}
