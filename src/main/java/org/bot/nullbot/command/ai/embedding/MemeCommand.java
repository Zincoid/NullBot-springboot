package org.bot.nullbot.command.ai.embedding;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.storage.ChatStorage;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.FileUtil;
import org.springframework.stereotype.Component;

@CommandMapping({"65275d24"})  // 加密 仅供AI嵌入调用
@Component
@RequiredArgsConstructor
@Slf4j
public class MemeCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;
    private final ChatStorage chatStorage;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        Long groupId;

        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent)
            groupId = groupMessageEvent.getGroupId();
        else if (event.getEvent() instanceof PokeNoticeEvent pokeNoticeEvent)
            groupId = pokeNoticeEvent.getGroupId();
        else
            throw new NullBotLogException("[表情] ❌未设计 - 非群消息/戳一戳事件响应方式");

        if(event.getCommandParameters().isEmpty()) throw new NullBotMsgException("[表情] ❌参数不足");

        String memeFolderPath = fileStorageProperties.getResourcePath() + "/ai/meme";
        String memeName = event.getCommandParameters().getFirst();
        String memePath = FileUtil.getFilePathByName(memeFolderPath, memeName);

        if (memePath == null) {
            chatStorage.recordError("表情文件 " + memeName + " 不存在，不要再使用了");  // 自动记录表情错误使用
            throw new NullBotLogException("[表情] ❌" + memeName + " 不存在");
        }

        String response = MsgUtils.builder()
                .img(memePath)
                .build();
        bot.sendGroupMsg(groupId, response, false);
        log.info("\t\t\t\t├─[Meme] 已发送表情: {}", memeName);
    }

    @Override
    public Integer getAccess() { return 2; }

    // 加密命令 无用户帮助

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ 65275d24 命令
                功能: 发送表情包图片
                限权: %d 级
                格式: 65275d24 [表情文件名]
                可使用表情文件列表(文件名指代为 表情主体人物_表达的文字内容或情绪.文件扩展名)：
                %s
                示例: 65275d24 女孩_干嘛.jpg
                注意: 你可以发送表情包图片以表达自己的情绪，要经常发表情！
                (重要！)你只能用提供给你的完整表情文件名！不要用下划线把不同文件名的主体人物和表达内容情绪的文本拼接起来使用，这种文件不存在！""",
                getAccess(),
                FileUtil.getFileListAsString(fileStorageProperties.getResourcePath() + "/ai/meme", ", ", true)
        );
    }
}
