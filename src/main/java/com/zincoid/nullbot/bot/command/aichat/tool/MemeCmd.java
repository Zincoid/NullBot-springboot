package com.zincoid.nullbot.bot.command.aichat.tool;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotOmitException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.module.resource.builder.ResourceUrlBuilder;
import com.zincoid.nullbot.core.properties.file.StorageProperties;
import com.zincoid.nullbot.core.model.data.po.FilePO;
import com.zincoid.nullbot.core.service.file.FileService;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@CmdMapping({"65275d24"})
@Component
@RequiredArgsConstructor
public class MemeCmd implements Cmd {

    private final StorageProperties storageProperties;
    private final FileService fileService;
    private final ResourceUrlBuilder resourceUrlBuilder;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        meme(bot, event.getGroupId(), false, args.nextString());
    }
    @Override
    public void run(Bot bot, PrivateMessageEvent event, CmdArgs args) {
        meme(bot, event.getUserId(), true, args.nextString());
    }
    @Override
    public void run(Bot bot, PokeNoticeEvent event, CmdArgs args) {
        if (event.getGroupId() != null) {
            meme(bot, event.getGroupId(), false, args.nextString());
        } else {
            meme(bot, event.getUserId(), true, args.nextString());
        }
    }

    private void meme(Bot bot, Long resourceId, boolean isPrivate, String memeName) {
        String memePath = storageProperties.getResourcePath() + "/ai/meme";
        List<FilePO> memes = fileService.search(memeName, memePath);
        if (memes.isEmpty()) throw new BotOmitException("表情未找到");
        String response = MsgUtils.builder()
                .img(resourceUrlBuilder.from(memes.getFirst().getId())).build();
        if (isPrivate) bot.sendPrivateMsg(resourceId, response, false);
        else bot.sendGroupMsg(resourceId, response, false);
        log.info("☑ [Meme] 表情已发送: {}", memeName);
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
                fileService.list(storageProperties.getResourcePath() + "/ai/meme").stream()
                        .map(FilePO::getFileName)
                        .toList()
        );
    }
}
