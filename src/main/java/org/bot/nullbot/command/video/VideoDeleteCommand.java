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
import org.bot.nullbot.util.FileUtil;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.stereotype.Component;

import java.util.Map;

@CommandMapping({"VideoDelete", "删除视频"})
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoDeleteCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            ArrayMsg reply = groupMessageEvent.getArrayMsg().getFirst();
            if(reply.getType() == MsgTypeEnum.reply){
                GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
                Map<String, String> videoMap = MessageParseUtil.parseGroupRawMessageAsVideoMap(replyMsg.getRawMessage());  // 可优化为单个键值对
                if(!videoMap.isEmpty()) {
                    for (Map.Entry<String, String> entry : videoMap.entrySet()) {
                        String fileName = entry.getKey();
                        String response = FileUtil.deleteFileByName(fileStorageConfig.getVideoPath(), fileName);
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[视频] \uD83D\uDDD1️" + response, false);
                        log.info("\t\t\t\t├─[Video.Delete] {}", response);
                    }
                }else{
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[视频] ❌未包含可删除视频", false);
                    log.info("\t\t\t\t├─[Video.Delete] 未包含可删除视频");
                }
            }else if(!event.getCommandParameters().isEmpty()){
                String fileName = event.getCommandParameters().getFirst();
                    String response = FileUtil.deleteFileByName(fileStorageConfig.getVideoPath(), fileName);
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[视频] \uD83D\uDDD1️" + response, false);
                log.info("\t\t\t\t├─[Video.Delete] {}", response);
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[视频] ❌无删除参数或引用", false);
                log.info("\t\t\t\t├─[Video.Delete] 无删除参数或引用");
            }
        }else
            log.info("\t\t\t\t├─[Video.Delete] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return 1;
    }

    @Override
    public String getHelp() {
        return "◉ VideoDelete 命令\n功能: 删除保存的视频\n限权: " + getAccess() + "\n格式: VideoDelete [文件名] 或 [引用视频]VideoDelete\n中文命令: 删除视频";
    }
}
