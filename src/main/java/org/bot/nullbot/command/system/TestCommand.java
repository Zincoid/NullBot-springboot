package org.bot.nullbot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.control.BotNextInputer;
import org.bot.nullbot.enums.BniMode;
import org.bot.nullbot.exception.NullBotMsgException;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Test", "test", "测试"})
@Component
@Slf4j
@RequiredArgsConstructor
public class TestCommand implements Command
{
    private final BotNextInputer botNextInputer;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        if (params.isEmpty())
            throw new NullBotMsgException("[测试] ❌参数不足");
        BniMode mode = switch (params.getFirst()) {
            case "PS" -> BniMode.PS;
            case "GS" -> BniMode.GS;
            case "GM" -> BniMode.GM;
            default -> throw new NullBotMsgException("[测试] ❌无此模式");
        };
        bot.sendGroupMsg(groupId, "[测试] ⏳等待输入中...", false);
        List<Pair<Long, String>> inputs;
        try {
            inputs = botNextInputer.request(mode, mode == BniMode.PS ? userId : groupId, 10, ".*");
        } catch (Exception e) {
            throw new NullBotMsgException("[测试] ❌" + e.getMessage());
        }
        if (mode != BniMode.GM && inputs.isEmpty()) {
            bot.sendGroupMsg(groupId, "[测试] ⚠️输入超时", false);
            return;
        }
        bot.sendGroupMsg(groupId, "[测试] ✅输入结束\n" + inputs, false);
    }

    @Override
    public Integer getAccess() { return 2; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Test 命令
                功能: 测试
                限权: %d 级
                格式: 不固定
                别名: test/测试""", getAccess()
        );
    }
}
