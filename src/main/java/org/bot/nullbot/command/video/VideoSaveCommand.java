package org.bot.nullbot.command.video;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GetMsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.plugin.util.DownloadUtil;
import org.bot.nullbot.plugin.util.MessageParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@CommandMapping({"VideoSave", "保存视频"})
@Component
@RequiredArgsConstructor
public class VideoSaveCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(VideoSaveCommand.class);
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            ArrayMsg reply = groupMessageEvent.getArrayMsg().get(0);
            if (reply.getType() == MsgTypeEnum.reply) {
                GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
                // logger.info(replyMsg.getRawMessage());
                Map<String, String> videoMap = MessageParseUtil.parseGroupRawMessageAsVideoMap(replyMsg.getRawMessage());  // 可优化为单个键值对
                if(!videoMap.isEmpty()){
                    for (Map.Entry<String, String> entry : videoMap.entrySet()) {
                        String fileName = entry.getKey();
                        String url = entry.getValue();
                        String info = DownloadUtil.downloadFile(url, fileStorageConfig.getVideoPath(), fileName);
                        // if(event.getCommandParameters().isEmpty() || !"-noInfo".equals(event.getCommandParameters().get(0))){
                        //     bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[保存视频] 已保存为: " + info, false);
                        // }
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[视频] \uD83D\uDCBE已保存！\n" + info, false);
                        logger.info("\t\t\t\t├─[Video.Save] 已保存为: {}", info);
                    }
                }else{
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[视频] ❌未包含可保存视频", false);
                    logger.info("\t\t\t\t├─[Video.Save] 未包含可保存图片");
                }
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[视频] ❌需回复要保存的视频", false);
                logger.info("\t\t\t\t├─[Video.Save] 未指定消息");
            }
        }else
            logger.info("\t\t\t\t├─[Video.Save] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return 1;
    }

    @Override
    public String getHelp() {
        return "◉ VideoSave 命令\n功能: 保存视频至本地\n限权: " + getAccess() + "\n格式: [引用视频]VideoSave\n中文命令: 保存视频";
    }
}
