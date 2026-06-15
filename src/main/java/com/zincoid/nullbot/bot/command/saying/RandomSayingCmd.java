package com.zincoid.nullbot.bot.command.saying;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.core.enums.Emoji;
import com.zincoid.nullbot.core.utils.MsgParseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.model.data.po.SayingPO;
import com.zincoid.nullbot.core.service.base.SayingService;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@CmdMapping({"RandomSaying", "Saying", "saying", "say", "随机语录", "语录"})
@Component
@RequiredArgsConstructor
public class RandomSayingCmd implements Cmd {

    private final SayingService sayingService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        List<Long> atNumbers = MsgParseUtil.extractAtNumbers(event.getArrayMsg());
        SayingPO saying = atNumbers.isEmpty()
                ? args.hasNext()
                ? sayingService.getRandByUserId(args.nextLong())
                : sayingService.getRand()
                : sayingService.getRandByUserId(atNumbers.getFirst());
        if (saying == null) throw new BotInfoException(Emoji.INFO, "暂无用户记录");
        bot.sendGroupMsg(event.getGroupId(), saying.toString(), false);
        log.info("☑ [RandomSaying] 语录已发送 -> No.{}", saying.getId());
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
