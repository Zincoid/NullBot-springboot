package org.bot.nullbot.command.convert;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.info.FileInfo;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.util.DownloadUtil;
import org.bot.nullbot.util.MessageParseUtil;
import org.bot.nullbot.component.render.ImageConverter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@CommandMapping({"Convert", "图像处理"})
@Component
@Slf4j
@RequiredArgsConstructor
public class ConvertCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final ImageConverter imageConverter;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();

        if (params.isEmpty())
            throw new NullBotMsgException("[图像处理] ❌无方法参数");
        String method = params.getFirst();
        if (!List.of("RIP", "PRTS", "InvsPRTS").contains(method))  // 用于减少不必要的图像下载
            throw new NullBotMsgException("[图像处理] ❌方法不存在");

        List<String> urls = new ArrayList<>();

        // 引用收集
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (reply.getType() == MsgTypeEnum.reply) {
            MsgResp replyMsg = bot.getMsg(reply.getData().get("id").asInt()).getData();
            Map<String, String> imageMap = MessageParseUtil.parseGroupRawMsgAsImgMap(replyMsg.getRawMessage());
            urls.addAll(imageMap.values());
        }

        //  ID参数收集 或 AT收集
        if (params.size() > 1) {
            long qqNumber;
            try {
                qqNumber = Long.parseLong(params.get(1));
            } catch (NumberFormatException e) {
                throw new NullBotMsgException("[图像处理] ❌参数格式错误");
            }
            urls.add(ShiroUtils.getUserAvatar(qqNumber, 5));
        }else{
            List<Long> qqNumbers = MessageParseUtil.extractAtQQNumbers(event.getRawMessage());
            for (Long qqNumber : qqNumbers) urls.add(ShiroUtils.getUserAvatar(qqNumber, 5));
        }

        if (urls.isEmpty())
            throw new NullBotMsgException("[图像处理] ❌无引用图片或ID参数或At消息");

        // 开始处理
        String tempPath = fileStorageProperties.getTempPath();
        for (String url : urls) {
            String tempName = UUID.randomUUID().toString();
            String downloadedName;
            try {
                FileInfo fileInfo = DownloadUtil.downloadFile(url, tempPath, tempName, "\t\t\t\t├─ ");
                downloadedName = fileInfo.getFileName();
            } catch (Exception e) {
                throw new NullBotMsgException("[图像处理] ❌下载时出错: " + e.getMessage());
            }
            String imagePath = tempPath + "/" + downloadedName;
            String base64;
            try {
                base64 = switch (method){
                    case "RIP" -> imageConverter.RIP(imagePath);
                    case "PRTS" -> imageConverter.PRTS(imagePath);
                    case "InvsPRTS" -> imageConverter.invsPRTS(imagePath);
                    default -> throw new NullBotMsgException("[图像处理] ❌方法不存在");
                };
            } catch (NullBotMsgException e) {
                throw e;
            } catch (Exception e) {
                throw new NullBotMsgException("[图像处理] ❌处理时出错: " + e.getMessage());
            } finally {
                FileUtils.deleteQuietly(new File(tempPath + "/" + downloadedName));
            }
            String response = MsgUtils.builder().img("base64://" + base64).build();
            bot.sendGroupMsg(groupId, response, false);
            log.info("\t\t\t\t├─[Convert] 处理完成 - {}", downloadedName);
        }
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
