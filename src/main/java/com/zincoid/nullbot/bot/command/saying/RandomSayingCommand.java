package com.zincoid.nullbot.bot.command.saying;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.model.data.po.SayingPO;
import com.zincoid.nullbot.core.service.SayingService;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"RandomSaying", "Saying", "saying", "say", "随机语录", "语录"})
@Component
@RequiredArgsConstructor
@Slf4j
public class RandomSayingCommand implements Command {

    private final SayingService sayingService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        SayingPO saying;
        if (params.isEmpty()) {
            saying = sayingService.getRand();
        } else {
            try {
                long qqNumber = Long.parseLong(params.getFirst());
                saying = sayingService.getRandByUserId(qqNumber);
            } catch (NumberFormatException e) {
                throw new NullBotException("[随机语录] ❌参数格式错误");
            }
        }
        if (saying == null)
            throw new NullBotException("[随机语录] ❌暂无用户记录");
        bot.sendGroupMsg(event.getGroupId(), saying.toString(), false);
        log.info("├─[RandomSaying] 已发送语录 - No.{}", saying.getId());
    }

    @Override
    public Integer getAccess() { return -1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ RandomSaying 命令
                功能: 随机语录 (可指定发言人)
                限权: %d 级
                格式: RandomSaying [可选: QQ号]
                别名: saying/say/随机语录/语录""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ RandomSaying 命令
                功能: 随机语录 (可指定发言人)
                格式: RandomSaying [可选: QQ号]""";
    }
}
