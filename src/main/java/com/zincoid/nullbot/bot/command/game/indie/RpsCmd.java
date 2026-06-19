package com.zincoid.nullbot.bot.command.game.indie;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.enums.BniMode;
import com.zincoid.nullbot.core.enums.Rps;
import com.zincoid.nullbot.core.module.control.BotInputManager;
import com.zincoid.nullbot.core.utils.MsgUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@CmdMapping({"Rps", "rps", "猜拳"})
@Component
@RequiredArgsConstructor
public class RpsCmd implements Cmd {

    private static final int WAIT_TIMEOUT_SECONDS = 30;  // 等待时间

    private final BotInputManager botInputManager;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        Rps botRps = Rps.random();
        log.info("☑ [Rps] 猜拳开始 - UserID: {}, BotRps: {}", userId, botRps);
        bot.sendGroupMsg(groupId, "猜拳开始，我出%s！".formatted(botRps.getDesc()), false);
        List<Pair<Long, String>> inputs = botInputManager.request(BniMode.PS, userId,
                "\\[CQ:rps,result=(\\d+)]", WAIT_TIMEOUT_SECONDS);
        if (inputs.isEmpty()) {
            bot.sendGroupMsg(groupId, "猜拳超时！", false);
            return;
        }
        Rps usrRps = MsgUtil.extractRps(inputs.getFirst().getRight());
        log.info("☑ [Rps] 猜拳响应 - UserID: {}, UsrRps: {}", userId, usrRps);
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (usrRps == botRps) {
            bot.sendGroupMsg(groupId, "平局了！", false);
            return;
        }
        boolean win = usrRps.judge(botRps);
        if (win) bot.sendGroupMsg(groupId, "你赢了！", false);
        else {
            if (args.hasOpt("ban", "b"))
                bot.setGroupBan(groupId, userId, 60 * args.optInt("ban", "b", 1));
            bot.sendGroupMsg(groupId, "你输了！", false);
        }
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Rps 命令
                功能: 猜拳 (使用互动表情)
                限权: %d 级
                格式: Rps [选项]
                
                选项:
                  -b, --ban=[分钟]  启用禁言处罚
                
                别名: rps/猜拳""", getAccess());
    }
}
