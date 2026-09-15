package com.zincoid.nullbot.bot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.properties.ai.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@CmdMapping({"Model", "模型"})
@Component
@RequiredArgsConstructor
public class ModelCmd implements Cmd {

    private final OpenAiProperties openAiProperties;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        if (args.isEmpty()) {
            OpenAiProperties.Provider cur = openAiProperties.current();
            List<OpenAiProperties.Provider> providers = openAiProperties.getProviders();
            StringBuilder sb = new StringBuilder("[模型配置] %s可用:".formatted(providers.size()));
            for (OpenAiProperties.Provider p : providers)
                sb.append('\n')
                        .append(p.getName().equalsIgnoreCase(cur.getName()) ? "[*] " : "[ ] ")
                        .append(p.getName());
            bot.sendGroupMsg(event.getGroupId(), sb.toString(), false);
            return;
        }
        OpenAiProperties.Provider p = openAiProperties.switchTo(args.next());
        bot.sendGroupMsg(event.getGroupId(), "✅已切换: " + p.getName(), false);
        log.info("☑ [Model] 供应商已切换 - Name: {}", p.getName());
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Model 命令
                功能: 查看或切换全局 AI 供应商
                限权: %d 级
                格式: Model [可选: 名称]
                别名: 模型""", getAccess()
        );
    }
}
