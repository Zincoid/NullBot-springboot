package com.zincoid.nullbot.bot.command.ai.inner;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.tool.OssUrlBuilder;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.service.FileService;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@CommandMapping({"65275d24"})  // 加密 仅供AI调用
@Component
@RequiredArgsConstructor
public class MemeCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;
    private final OssUrlBuilder ossUrlBuilder;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs params) {
        meme(bot, event.getGroupId(), false, params.nextString());
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, CommandArgs params) {
        meme(bot, event.getUserId(), true, params.nextString());
    }

    @Override
    public void execute(Bot bot, PokeNoticeEvent event, CommandArgs params) {
        if (event.getGroupId() != null) {
            meme(bot, event.getGroupId(), false, params.nextString());
        } else {
            meme(bot, event.getUserId(), true, params.nextString());
        }
    }

    private void meme(Bot bot, Long resourceId, boolean isPrivate, String memeName) {
        String memePath = fileStorageProperties.getResourcePath() + "/ai/meme";
        List<FilePO> memes = fileService.search(memeName, memePath);
        String response = MsgUtils.builder()
                .img(ossUrlBuilder.from(memes.getFirst().getId())).build();
        if (isPrivate) bot.sendPrivateMsg(resourceId, response, false);
        else bot.sendGroupMsg(resourceId, response, false);
        log.info("☑ [Meme] 已发送表情: {}", memeName);
    }

    @Override
    public Integer getAccess() { return 2; }

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
                fileService.search("", fileStorageProperties.getResourcePath() + "/ai/meme").stream()
                        .map(FilePO::getFileName)
                        .toList()
        );
    }
}
