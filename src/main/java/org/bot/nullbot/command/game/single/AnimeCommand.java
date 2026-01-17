package org.bot.nullbot.command.game.single;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.FileUtil;
import org.springframework.stereotype.Component;

@CommandMapping({"Anime", "anime", "二次元", "色图", "涩图"})
@Component
@RequiredArgsConstructor
@Slf4j
public class AnimeCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String acgPath = fileStorageConfig.getImagePath() + "/acg/二次元";

            String animePath;
            try {
                animePath = FileUtil.getRandomFile(acgPath);
            } catch (Exception e) {
                throw new NullBotMsgException("[二次元] ❌目录异常");
            }
            if(animePath == null)
                throw new NullBotMsgException("[二次元] ❌暂无图片");

            String response = MsgUtils.builder()
                    .img(animePath)
                    .build();
            bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
            log.info("\t\t\t\t├─[Anime] 获取二次元图片");
        }else
            throw new NullBotLogException("[二次元] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Anime 命令
                功能: 随机二/三次元图
                限权: %d 级
                格式: Anime
                别名: anime/二次元/色图/涩图""", getAccess()
        );
    }
}
