package com.zincoid.nullbot.bot.command.saying;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.exception.NullBotMsgException;
import com.zincoid.nullbot.core.service.SayingService;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"SayingDel", "删除语录"})
@Component
@RequiredArgsConstructor
@Slf4j
public class SayingDeleteCommand implements Command {

    private final SayingService sayingService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        if (params.isEmpty())
            throw new NullBotMsgException("[删除语录] ❌参数不足");
        try {
            int id = Integer.parseInt(params.getFirst());
            boolean deleted = sayingService.deleteById(id);
            bot.sendGroupMsg(event.getGroupId(), "[删除语录] ⚠️No.%s %s".formatted(id, deleted ? "已删除" : "不存在"), false);
            log.info("├─[SayingDelete] 执行语录删除 - No.{} -> {}", id, deleted ? "已删除" : "不存在");
        } catch (NumberFormatException e) {
            throw new NullBotMsgException("[删除语录] ❌参数格式错误");
        }
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ SayingDelete 命令
                功能: 删除语录
                限权: %d 级
                格式: SayingDelete [语录ID]
                别名: 删除语录""", getAccess()
        );
    }
}
