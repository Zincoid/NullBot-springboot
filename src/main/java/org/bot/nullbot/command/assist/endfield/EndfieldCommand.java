package org.bot.nullbot.command.assist.endfield;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.FileUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"Endfield", "endfield", "end", "终末地"})
@Component
@Slf4j
@RequiredArgsConstructor
public class EndfieldCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            if(params.isEmpty()) throw new NullBotMsgException("[终末地] ❌未指定内容");
            List<String> helpPaths = FileUtil.getFilesByPattern(fileStorageProperties.getImagePath() + "/assist", params.getFirst());
            if (helpPaths.isEmpty()) throw new NullBotMsgException("[终末地] ❌未找到内容");
            String response = MsgUtils.builder().img(helpPaths.getFirst()).build();  // 只取第一个查询结果
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
            log.info("\t\t\t\t├─[Endfield] 已获取资源");
        }else
            throw new NullBotLogException("[Endfield] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Endfield 命令
                功能: 获取终末地攻略
                限权: %d 级
                格式: Endfield [-list|查询内容]
                别名: endfield/end/终末地""", getAccess()
        );
    }
}
