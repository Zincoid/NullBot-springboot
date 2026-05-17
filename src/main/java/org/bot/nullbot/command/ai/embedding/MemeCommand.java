package org.bot.nullbot.command.ai.embedding;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.storage.ChatStorage;
import org.bot.nullbot.component.tool.OssUrlBuilder;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.po.FilePO;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.FileService;
import org.bot.nullbot.util.FileUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"65275d24"})  // 加密 仅供AI嵌入调用
@Component
@RequiredArgsConstructor
@Slf4j
public class MemeCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final ChatStorage chatStorage;
    private final FileService fileService;
    private final OssUrlBuilder ossUrlBuilder;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        meme(bot, params, event.getGroupId(), false);
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, List<String> params) {
        meme(bot, params, event.getUserId(), true);
    }

    @Override
    public void execute(Bot bot, PokeNoticeEvent event, List<String> params) {
        if (event.getGroupId() != null) {
            meme(bot, params, event.getGroupId(), false);
        } else {
            meme(bot, params, event.getUserId(), true);
        }
    }

    private void meme(Bot bot, List<String> params, Long targetId, boolean isPrivate) {
        if (params.isEmpty())
            throw new NullBotMsgException("[表情] ❌参数不足");

        String memePath = fileStorageProperties.getResourcePath() + "/ai/meme";
        String memeName = params.getFirst();
        List<FilePO> memes = fileService.search(memeName, memePath);

        // 自动记录表情错误使用
        if (memes.isEmpty()) {
            chatStorage.recordError("表情文件 " + memeName + " 不存在，不要再使用了");
            throw new NullBotLogException("[表情] ❌" + memeName + " 不存在");
        }

        String response = MsgUtils.builder()
                .img(ossUrlBuilder.from(memes.getFirst().getId()))
                .build();

        if (isPrivate) {
            bot.sendPrivateMsg(targetId, response, false);
        } else {
            bot.sendGroupMsg(targetId, response, false);
        }

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
                        格式: 65275d24 [表情文件名]
                        可用表情文件列表 (文件名为 表情主体人物_表达的文字内容或情绪.文件扩展名):
                        %s
                        示例: 65275d24 女孩_干嘛.jpg
                        注意: 你可以发送表情包图片以表达自己的情绪，要经常发表情
                        (重要！) 你只能用提供给你的完整表情文件名，不要用下划线把不同文件名的主体人物和表达内容情绪的文本拼接起来使用，这种文件不存在""",
                FileUtil.getFileListAsString(fileStorageProperties.getResourcePath() + "/ai/meme", ", ", true)
        );
    }
}
