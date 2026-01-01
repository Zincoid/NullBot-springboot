package org.bot.nullbot.command.ai.embedding;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.DeepSeekConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.util.FileUtil;
import org.springframework.stereotype.Component;

@CommandMapping({"65275d24-ada6-49de-8a40-01c05ad279ea"})  // 加密 仅供AI嵌入调用
@Component
@RequiredArgsConstructor
@Slf4j
public class MemeCommand implements Command
{
    private final DeepSeekConfig deepSeekConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            if(event.getCommandParameters().isEmpty()) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[表情] ❌参数不足", false);
                log.info("\t\t\t\t├─[Meme] 参数不足");
                return;
            }
            String memeFolderPath = deepSeekConfig.getMemePath();
            String memePath = FileUtil.getFilePathByName(memeFolderPath, event.getCommandParameters().getFirst());
            if (memePath != null) {
                String response = MsgUtils.builder()
                        .img(memePath)
                        .build();
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                log.info("\t\t\t\t├─[Meme] 已发送表情: {}", memePath);
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[表情] ❌无此图片", false);
                log.info("\t\t\t\t├─[Meme] 表情不存在");
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
                ◉ 65275d24-ada6-49de-8a40-01c05ad279ea 命令
                功能: 发送表情包图片
                限权: %d
                格式: 65275d24-ada6-49de-8a40-01c05ad279ea
                可使用表情(名称指 图中主体人物_表达的文字内容或情绪.文件扩展名)：
                %s
                示例: 65275d24-ada6-49de-8a40-01c05ad279ea 猫娘_卖萌.jpg
                注意: 你可以根据自己的想法发送表情包图片以表达自己的情绪！！！""",
                getAccess(),
                FileUtil.getFileListAsString(deepSeekConfig.getMemePath())
        );
    }
}
