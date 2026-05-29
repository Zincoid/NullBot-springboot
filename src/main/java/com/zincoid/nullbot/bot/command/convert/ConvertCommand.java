package com.zincoid.nullbot.bot.command.convert;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.service.RenderingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.information.FileInfo;
import com.zincoid.nullbot.core.util.DownloadUtil;
import com.zincoid.nullbot.core.util.MsgParseUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Slf4j
@CommandMapping({"Convert", "图像处理"})
@Component
@RequiredArgsConstructor
public class ConvertCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final RenderingService renderingService;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        ArrayMsg reply = event.getArrayMsg().getFirst();
        String method = args.nextString();
        List<String> urls = new ArrayList<>();

        if (reply.getType() == MsgTypeEnum.reply) {
            // 引用收集
            MsgResp replyMsg = bot.getMsg(reply.getData().get("id").asInt()).getData();
            Map<String, String> imageMap = MsgParseUtil.extractImgMap(replyMsg.getRawMessage());
            urls.addAll(imageMap.values());
        }
        if (args.hasNext()) {
            // ID 收集
            long qqNumber = args.nextLong();
            urls.add(ShiroUtils.getUserAvatar(qqNumber, 5));
        } else {
            // AT 收集
            List<Long> qqNumbers = MsgParseUtil.extractAtNumbers(event.getRawMessage());
            for (Long qqNumber : qqNumbers) urls.add(ShiroUtils.getUserAvatar(qqNumber, 5));
        }

        if (urls.isEmpty()) throw new BotWarnException("缺少图片引用或ID参数或AT用户");

        String tempPath = fileStorageProperties.getTempPath();
        for (String url : urls) {
            String tempName = UUID.randomUUID().toString();
            FileInfo fileInfo = DownloadUtil.downloadFile(url, tempPath, tempName);
            String downloadedName = fileInfo.getFileName();
            String imagePath = tempPath + "/" + downloadedName;
            String base64;
            try {
                base64 = switch (method) {
                    case "RIP" -> renderingService.rip(imagePath);
                    case "PRTS" -> renderingService.prts(imagePath, false);
                    case "InvsPRTS" -> renderingService.prts(imagePath, true);
                    default -> throw new BotWarnException("无此操作");
                };
            } finally {
                FileUtils.deleteQuietly(new File(imagePath));
            }
            String response = MsgUtils.builder().img("base64://" + base64).build();
            bot.sendGroupMsg(groupId, response, false);
            log.info("☑ [Convert] 图像处理已完成");
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
                2. Convert [方式] [@用户|QQ号]
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
