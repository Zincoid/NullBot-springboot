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
import org.bot.nullbot.plugin.util.FileUtil;
import org.bot.nullbot.plugin.util.MessageParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@CommandMapping({"ImageDelete", "删除图片"})
@Component
@RequiredArgsConstructor
public class ImageDeleteCommand implements Command
{
    private static final Logger logger = LoggerFactory.getLogger(ImageDeleteCommand.class);
    private final FileStorageConfig fileStorageConfig;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            ArrayMsg reply = groupMessageEvent.getArrayMsg().get(0);
            if(reply.getType() == MsgTypeEnum.reply){
                GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
                Map<String, String> imageMap = MessageParseUtil.parseGroupRawMessageAsImageMap(replyMsg.getRawMessage());
                if(!imageMap.isEmpty()) {
                    for (Map.Entry<String, String> entry : imageMap.entrySet()) {
                        String originName = entry.getKey();
                        String url = entry.getValue();
                        String fileName = originName.substring(0, originName.lastIndexOf("."));
                        String response = FileUtil.deleteFilesByPattern(fileStorageConfig.getImagePath() + "/collect", fileName + ".*");
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图片] \uD83D\uDDD1\uFE0F" + response, false);
                        logger.info("\t\t\t\t├─[Image.Delete] {}", response);
                    }
                }else{
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图片] ❌未包含可删除图片", false);
                    logger.info("\t\t\t\t├─[Image.Delete] 未包含可删除图片");
                }
            }else if(!event.getCommandParameters().isEmpty()){
                String fileName = event.getCommandParameters().get(0);
                String response = FileUtil.deleteFileByName(fileStorageConfig.getImagePath() + "/collect", fileName);
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图片] \uD83D\uDDD1\uFE0F" + response, false);
                logger.info("\t\t\t\t├─[Image.Delete] {}", response);
            }else{
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图片] ❌无删除参数或引用", false);
                logger.info("\t\t\t\t├─[Image.Delete] 无删除参数或引用");
            }
        }else
            logger.info("\t\t\t\t├─[Image.Delete] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() {
        return 2;
    }

    @Override
    public String getHelp() {
        return "◉ ImageDelete 命令\n功能: 删除保存的图片\n限权: " + getAccess() + "\n格式: ImageDelete [文件名] 或 [引用图片]ImageDelete\n中文命令: 删除图片";
    }
}
