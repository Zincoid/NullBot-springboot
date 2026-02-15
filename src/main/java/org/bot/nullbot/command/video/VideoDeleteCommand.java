package org.bot.nullbot.command.video;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GetMsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.FileUtil;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.stereotype.Component;

import java.util.Map;

@CommandMapping({"VideoDel", "删除视频"})
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoDeleteCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            ArrayMsg reply = groupMessageEvent.getArrayMsg().getFirst();
            if (reply.getType() == MsgTypeEnum.reply) {
                GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
                // 可优化为单个键值对?
                Map<String, String> videoMap = MessageParseUtil.parseGroupRawMessageAsVideoMap(replyMsg.getRawMessage());
                if (videoMap.isEmpty()) throw new NullBotMsgException("[删除视频] ❌未引用视频");
                for (Map.Entry<String, String> entry : videoMap.entrySet()) {
                    String fileName = entry.getKey();
                    FileUtil.deleteFileByName(fileStorageProperties.getVideoPath(), fileName);
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[删除视频] ⚠️已删除\n- " + fileName, false);
                    log.info("\t\t\t\t├─[VideoDelete] 视频已删除 - {}", fileName);
                }
            } else if (!event.getCommandParameters().isEmpty()) {
                String fileName = event.getCommandParameters().getFirst();
                FileUtil.deleteFileByName(fileStorageProperties.getVideoPath(), fileName);
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[删除视频] ⚠️已删除\n- " + fileName, false);
                log.info("\t\t\t\t├─[VideoDelete] 视频已删除 - {}", fileName);
            } else {
                throw new NullBotMsgException("[删除视频] ❌无文件名或引用");
            }
        }else
            throw new NullBotLogException("[删除视频] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ VideoDel 命令
                功能: 删除保存的视频
                限权: %d 级
                格式:
                1. VideoDel [文件名]
                2. [引用视频] VideoDel
                别名: 删除视频""", getAccess()
        );
    }
}
