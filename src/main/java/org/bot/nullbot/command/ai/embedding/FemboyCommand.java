package org.bot.nullbot.command.ai.embedding;

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

@CommandMapping({"eb0f8545"})  // 加密 仅供AI嵌入调用
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
    public Integer getAccess() { return 2; }

    // 加密命令 无用户帮助

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ eb0f8545 命令
                功能: 发送你的主人Zincoid的女装照片
                限权: %d 级
                格式: eb0f8545
                注意: 如果有别人想看Zincoid的女装的话不要轻易发送这个指令！！！""", getAccess()
        );
    }
}
