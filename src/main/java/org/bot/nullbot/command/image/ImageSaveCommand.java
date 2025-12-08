package org.bot.nullbot.command.image;

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


@CommandMapping({"ImageSave"})
@Component
@RequiredArgsConstructor
public class ImageSaveCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(ImageSaveCommand.class);
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            ArrayMsg reply = groupMessageEvent.getArrayMsg().get(0);
            if (reply.getType() == MsgTypeEnum.reply) {
                GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
                Map<String, String> imageMap = MessageParseUtil.parseGroupRawMessageAsImageMap(replyMsg.getRawMessage());
                if(!imageMap.isEmpty()){
                    for (Map.Entry<String, String> entry : imageMap.entrySet()) {
                        String originName = entry.getKey();
                        String url = entry.getValue();
                        String fileName = originName.substring(0, originName.lastIndexOf("."));
                        String info = DownloadUtil.downloadImage(url, fileStorageConfig.getImagePath() + "/collect", fileName);
                        // if(event.getCommandParameters().isEmpty() || !"-noInfo".equals(event.getCommandParameters().get(0))){
                        //     bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Image.Save] 已保存至: " + info, false);
                        // }
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Image.Save] 已保存至: " + info, false);
                        logger.info("\t\t\t\t├─[Image.Save] 已保存至: {}", info);
                    }
                }else{
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Image.Save] 无图片", false);
                    logger.info("\t\t\t\t├─[Image.Save] 无图片");
                }
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[Image.Save] 该命令需回复要保存的图片", false);
                logger.info("\t\t\t\t├─[Image.Save] 未指定消息");
            }
        }else
            logger.info("\t\t\t\t├─[Image.Save] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "/ImageSave 命令\n功能: 保存图片至本地\n限权: 0\n格式: [引用图片]/ImageSave";
    }
}
