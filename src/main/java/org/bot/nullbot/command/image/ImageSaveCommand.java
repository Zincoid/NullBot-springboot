package org.bot.nullbot.command.image;

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
import org.bot.nullbot.util.DownloadUtil;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.stereotype.Component;

import java.util.Map;


@CommandMapping({"ImageSave", "保存图片"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ImageSaveCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            ArrayMsg reply = groupMessageEvent.getArrayMsg().getFirst();
            if (reply.getType() == MsgTypeEnum.reply) {
                GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
                Map<String, String> imageMap = MessageParseUtil.parseGroupRawMessageAsImageMap(replyMsg.getRawMessage());
                if(!imageMap.isEmpty()){
                    for (Map.Entry<String, String> entry : imageMap.entrySet()) {
                        String originName = entry.getKey();
                        String url = entry.getValue();
                        String fileName = originName.substring(0, originName.lastIndexOf("."));
                        String info = DownloadUtil.downloadFile(url, fileStorageConfig.getImagePath() + "/collect", fileName);
                        // if(event.getCommandParameters().isEmpty() || !"-noInfo".equals(event.getCommandParameters().getFirst())){
                        //     bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图片] \uD83D\uDCBE已保存！\n" + info, false);
                        // }
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图片] \uD83D\uDCBE已保存！", false);
                        // bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图片] \uD83D\uDCBE已保存！\n" + info, false);
                        log.info("\t\t\t\t├─[Image.Save] 已保存为: {}", info);
                    }
                }else{
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图片] ❌未包含可保存图片", false);
                    log.info("\t\t\t\t├─[Image.Save] 未包含可保存图片");
                }
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图片] ❌需回复要保存的图片", false);
                log.info("\t\t\t\t├─[Image.Save] 未指定消息");
            }
        }else
            log.info("\t\t\t\t├─[Image.Save] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return "◉ ImageSave 命令\n功能: 保存图片至本地\n限权: " + getAccess() + "\n格式: [引用图片]ImageSave\n中文命令: 保存图片";
    }
}
