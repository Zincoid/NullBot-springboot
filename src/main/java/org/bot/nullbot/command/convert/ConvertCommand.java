package org.bot.nullbot.command.convert;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
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
import org.bot.nullbot.entity.info.FileInfo;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.DownloadUtil;
import org.bot.nullbot.util.FileUtil;
import org.bot.nullbot.util.MessageParseUtil;
import org.bot.nullbot.component.render.ImageConverter;
import org.springframework.stereotype.Component;

import java.util.*;

@CommandMapping({"Convert", "图像处理"})
@Component
@Slf4j
@RequiredArgsConstructor
public class ConvertCommand implements Command
{
    private final FileStorageProperties fileStorageProperties;
    private final ImageConverter imageConverter;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            Long groupId = groupMessageEvent.getGroupId();

            if (event.getCommandParameters().isEmpty())
                throw new NullBotMsgException("[图像处理] ❌无方法参数");

            String method = event.getCommandParameters().getFirst();
            if (!List.of("RIP", "PRTS", "InversePRTS").contains(method))
                throw new NullBotMsgException("[图像处理] ❌方法不存在");

            List<String> urls = new ArrayList<>();

            // 引用收集
            ArrayMsg reply = groupMessageEvent.getArrayMsg().getFirst();
            if (reply.getType() == MsgTypeEnum.reply) {
                GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
                Map<String, String> imageMap = MessageParseUtil.parseGroupRawMessageAsImageMap(replyMsg.getRawMessage());
                urls.addAll(imageMap.values());
            }

            //  ID参数收集 或 AT收集
            if (event.getCommandParameters().size() > 1) {
                long qqNumber;
                try {
                    qqNumber = Long.parseLong(event.getCommandParameters().get(1));
                } catch (NumberFormatException e) {
                    throw new NullBotMsgException("[图像处理] ❌参数格式错误");
                }
                urls.add(ShiroUtils.getUserAvatar(qqNumber, 5));
            }else{
                List<Long> qqNumbers = MessageParseUtil.extractAtQQNumbers(groupMessageEvent.getRawMessage());
                for (Long qqNumber : qqNumbers) urls.add(ShiroUtils.getUserAvatar(qqNumber, 5));
            }

            if (urls.isEmpty())
                throw new NullBotMsgException("[图像处理] ❌无引用图片或ID参数或At消息");

            // 开始处理
            String tempFilePath = fileStorageProperties.getTempPath();
            for (String url : urls) {
                String tempFileName = UUID.randomUUID().toString();
                String downloadedFileName;
                try {
                    FileInfo fileInfo = DownloadUtil.downloadFile(url, tempFilePath, tempFileName, "\t\t\t\t├─ ");
                    downloadedFileName = fileInfo.getFileName();
                } catch (Exception e) {
                    throw new NullBotMsgException("[图像处理] ❌下载时出错: " + e.getMessage());
                }
                String imagePath = tempFilePath + "/" + downloadedFileName;
                String base64;
                try {
                    base64 = switch (method){
                        case "RIP" -> imageConverter.RIP(imagePath);
                        case "PRTS" -> imageConverter.PRTS(imagePath);
                        case "InvsPRTS" -> imageConverter.inversePRTS(imagePath);
                        default -> throw new NullBotMsgException("[图像处理] ❌方法不存在");
                    };
                } catch (NullBotMsgException e) {
                    throw e;
                } catch (Exception e) {
                    throw new NullBotMsgException("[图像处理] ❌处理时出错: " + e.getMessage());
                } finally {
                    FileUtil.deleteFileByName(tempFilePath, downloadedFileName);
                }
                String response = MsgUtils.builder().img("base64://" + base64).build();
                bot.sendGroupMsg(groupId, response, false);
                log.info("\t\t\t\t├─[Convert] 处理完成 - {}", downloadedFileName);
            }
            // bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[图像处理] ✅全部处理完成！", false);
        }else
            throw new NullBotLogException("[图像处理] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Convert 命令
                功能: P图
                限权: %d 级
                格式:
                1. [引用] Convert [方式]
                2. Convert [方式] [@任何人|QQ号]
                方式: RIP/PRTS/InvsPRTS
                别名: 图像处理""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ Convert 命令
                功能: 用户头像P图
                格式: Convert [方式] [QQ号]
                方式: RIP(安息)/PRTS(封锁)/InvsPRTS(封锁反色)
                示例: Convert RIP 2660181154""";
    }
}
