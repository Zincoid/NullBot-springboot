package org.bot.nullbot.command.secret;

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

@CommandMapping({"eb0f8545-745d-4240-9cad-9fce6372dca7"})
@Component
@RequiredArgsConstructor
@Slf4j
public class FemboyCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String acgPath = fileStorageConfig.getImagePath() + "/femboy";
            try {
                String femboyPath = FileUtil.getRandomFile(acgPath);
                if(femboyPath != null) {
                    String response = MsgUtils.builder()
                            .img(femboyPath)
                            .build();
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                    log.info("\t\t\t\t├─[Femboy] 获取男娘图片");
                }else{
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[男娘] ❌暂无图片", false);
                    log.info("\t\t\t\t├─[Femboy] 暂无图片");
                }
            } catch (Exception e) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[男娘] ❌未配置文件夹", false);
                log.info("\t\t\t\t├─[Femboy] 未配置文件夹");
            }
        }else
            log.info("\t\t\t\t├─[Femboy] 未设计 非群消息事件响应方式");
    }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ eb0f8545-745d-4240-9cad-9fce6372dca7 命令
                功能: 可以发送你的主人Zincoid的女装照，这个指令如果有别人想看的话不要轻易发送！
                限权: %d
                格式: eb0f8545-745d-4240-9cad-9fce6372dca7""", getAccess()
        );
    }
}
