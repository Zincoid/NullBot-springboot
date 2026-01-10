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
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.service.FileService;
import org.bot.nullbot.util.DownloadUtil;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.stereotype.Component;

import java.util.Map;

@CommandMapping({"VideoSave", "保存视频"})
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoSaveCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;
    private final FileService fileService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            ArrayMsg reply = groupMessageEvent.getArrayMsg().getFirst();
            if (reply.getType() != MsgTypeEnum.reply) {
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[视频] ❌需回复要保存的视频", false);
                log.info("\t\t\t\t├─[Video.Save] 未指定消息");
                return;
            }

            GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
            Map<String, String> videoMap = MessageParseUtil.parseGroupRawMessageAsVideoMap(replyMsg.getRawMessage());  // 可优化为单个键值对
            if(videoMap.isEmpty()){
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[视频] ❌未包含可保存视频", false);
                log.info("\t\t\t\t├─[Video.Save] 未包含可保存图片");
                return;
            }

            for (Map.Entry<String, String> entry : videoMap.entrySet()) {
                String fileName = entry.getKey();
                String url = entry.getValue();
                try {
                    DownloadUtil.DownloadInfo downloadInfo = DownloadUtil.downloadFile(url, fileStorageConfig.getVideoPath(), fileName);
                    if(!fileService.addFileRecordForBot(
                            fileStorageConfig.getVideoPath(),
                            downloadInfo.getFileName(),
                            downloadInfo.getFileSize())
                    ) {
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[视频] ❌数据库更新失败", false);
                        log.info("\t\t\t\t├─[Video.Save] 数据库更新失败");
                        return;
                    }
                    // if(event.getCommandParameters().isEmpty() || !"-noInfo".equals(event.getCommandParameters().get(0))){
                    //     bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[视频] \uD83D\uDCBE已保存！\n" + info, false);
                    // }
                    // bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[视频] \uD83D\uDCBE已保存！", false);
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[视频] \uD83D\uDCBE已保存！", false);
                    log.info("\t\t\t\t├─[Video.Save] 已保存为: {}", downloadInfo.getFileName());
                } catch (Exception e) {
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[视频] ❌保存失败:\n" + e.getMessage(), false);
                    log.info("\t\t\t\t├─[Video.Save] 保存失败", e);
                }
            }
        }else
            log.info("\t\t\t\t├─[Video.Save] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ VideoSave 命令
                功能: 保存视频至视频库
                限权: %d 级
                格式: [引用视频] VideoSave
                中文命令: 保存视频""", getAccess()
        );
    }
}
