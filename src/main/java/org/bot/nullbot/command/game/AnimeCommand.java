package org.bot.nullbot.command.game;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.util.FileUtil;
import org.springframework.stereotype.Component;

@CommandMapping({"Anime", "二次元", "色图", "涩图"})
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
            try {
                String animePath = FileUtil.getRandomFile(acgPath);
                if(animePath != null) {
                    String response = MsgUtils.builder()
                            .img(animePath)
                            .build();
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                    log.info("\t\t\t\t├─[Anime] 获取二次元图片");
                }else{
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[二次元] ❌暂无图片", false);
                    log.info("\t\t\t\t├─[Anime] 暂无图片");
                }
            } catch (Exception e) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[二次元] ❌未配置文件夹", false);
                log.info("\t\t\t\t├─[Anime] 未配置文件夹");
            }
        }else
            log.info("\t\t\t\t├─[Anime] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ Anime 命令\n功能: 随机二/三次元图\n限权: " + getAccess() + "\n格式: Anime\n中文命令: 二次元/色图/涩图";
    }
}
