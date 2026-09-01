package com.zincoid.nullbot.bot.command.assist;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.service.render.CapturingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"AA", "模型查询"})
@Component
@RequiredArgsConstructor
public class AACmd implements Cmd {

    private final CapturingService capturingService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        String option = args.get(0, "智能");
        bot.sendGroupMsg(event.getGroupId(), "数据获取中，请稍候...", false);
        String base64 = capturingService.ai(option);
        String response = MsgUtils.builder().img("base64://" + base64).build();
        bot.sendGroupMsg(event.getGroupId(), response, false);
        log.info("☑ [AA] 图表已发送 - Option: {}", option);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ AA 命令
                功能: ArtificialAnalysis 模型图表查询
                限权: %d 级
                格式: AA [可选: 图表]
                图表: 智能/模型对比/历史/成本/性价比/算力成本/定价/编码/智能体/开放性/Token/速度/耗时/供应商
                默认: 智能
                别名: 模型查询""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ AA 命令
                功能: 通过 ArtificialAnalysis 网站查询 AI 模型榜单图表
                格式: AA [图表]
                图表可选值: 智能/模型对比/历史/成本/性价比/算力成本/定价/编码/智能体/开放性/Token/速度/耗时/供应商
                示例: AA 速度""";
    }
}
