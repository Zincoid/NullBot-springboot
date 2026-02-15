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
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.FileService;
import org.bot.nullbot.util.FileUtil;
import org.bot.nullbot.util.MessageParseUtil;
import org.bot.nullbot.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@CommandMapping({"ImageDel", "删除图片"})
@Component
@RequiredArgsConstructor
@Slf4j
public class ImageDeleteCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;
    private final FileService fileService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            String directory = fileStorageProperties.getImagePath() + "/collect";
            ArrayMsg reply = groupMessageEvent.getArrayMsg().getFirst();
            if (reply.getType() == MsgTypeEnum.reply) {
                GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
                Map<String, String> imageMap = MessageParseUtil.parseGroupRawMessageAsImageMap(replyMsg.getRawMessage());
                if (imageMap.isEmpty()) throw new NullBotMsgException("[删除图片] ❌未引用图片");
                for (Map.Entry<String, String> entry : imageMap.entrySet()) {
                    String originName = entry.getKey();
                    // QQ获取文件名后缀全是jpg只能模式匹配...
                    String fileName = originName.substring(0, originName.lastIndexOf("."));
                    List<String> realFileNames;
                    try {
                        realFileNames = FileUtil.deleteFilesByPattern(directory, fileName + ".*");
                    } catch (Exception e) {
                        throw new NullBotMsgException("[删除图片] ❌" + e.getMessage());
                    }
                    if (realFileNames.size() != 1)
                        throw new NullBotMsgException("[删除图片] ❌删除异常");
                    if(!fileService.deleteFileRecordForBot(directory, realFileNames.getFirst()))
                        throw new NullBotMsgException("[删除图片] ❌数据库更新失败");
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[删除图片] ⚠️已删除\n- " +
                            StringUtil.truncateFileName(realFileNames.getFirst(), 12), false);
                    log.info("\t\t\t\t├─[ImageDelete] 图片已删除 - {}.*", fileName);
                }
            } else if (!event.getCommandParameters().isEmpty()) {
                String fileName = event.getCommandParameters().getFirst();
                try {
                    FileUtil.deleteFileByName(directory, fileName);
                } catch (Exception e) {
                    throw new NullBotMsgException("[删除图片] ❌" + e.getMessage());
                }
                if(!fileService.deleteFileRecordForBot(directory, fileName))
                    throw new NullBotMsgException("[删除图片] ❌数据库更新失败");
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[删除图片] ⚠️已删除\n- " +
                        StringUtil.truncateFileName(fileName, 12), false);
                log.info("\t\t\t\t├─[ImageDelete] 图片已删除 - {}", fileName);
            } else
                throw new NullBotMsgException("[删除图片] ❌无文件名或引用");
        } else
            throw new NullBotLogException("[删除图片] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ ImageDel 命令
                功能: 删除保存的图片
                限权: %d 级
                格式:
                1. ImageDel [文件名]
                2. [引用图片] ImageDel
                别名: 删除图片""", getAccess()
        );
    }
}
