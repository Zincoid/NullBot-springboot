package com.zincoid.nullbot.bot.command.convert;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.BotErrorException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.render.HtmlRenderer;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.information.FileInfo;
import com.zincoid.nullbot.core.util.DownloadUtil;
import com.zincoid.nullbot.core.util.MsgParseUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Slf4j
@CommandMapping({"Symmetry", "对称"})
@Component
@RequiredArgsConstructor
public class SymmetryCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final HtmlRenderer htmlRenderer;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) throws Exception {
        Long groupId = event.getGroupId();
        ArrayMsg reply = event.getArrayMsg().getFirst();
        List<String> urls = new ArrayList<>();
        String mode = "left";

        if (reply.getType() == MsgTypeEnum.reply) {
            // 引用收集
            MsgResp replyMsg = bot.getMsg(reply.getData().get("id").asInt()).getData();
            Map<String, String> imageMap = MsgParseUtil.extractImgMap(replyMsg.getRawMessage());
            urls.addAll(imageMap.values());
        }
        if (args.hasNext()) {
            // ID 收集
            String modeStr = args.nextString();
            if (List.of("左", "右", "上", "下").contains(modeStr)) {
                mode = switch (modeStr) {
                    case "左" -> "left";
                    case "右" -> "right";
                    case "上" -> "top";
                    case "下" -> "bottom";
                    default -> throw new BotErrorException("代码出错");
                };
                if (args.hasNext()) {
                    long qqNumber = args.nextLong();
                    urls.add(ShiroUtils.getUserAvatar(qqNumber, 5));
                } else {
                    List<Long> qqNumbers = MsgParseUtil.extractAtNumbers(event.getRawMessage());
                    for (Long number : qqNumbers) urls.add(ShiroUtils.getUserAvatar(number, 5));
                }
            } else {
                long qqNumber = args.getLong(0);
                urls.add(ShiroUtils.getUserAvatar(qqNumber, 5));
            }
        } else {
            // AT 收集
            List<Long> qqNumbers = MsgParseUtil.extractAtNumbers(event.getRawMessage());
            for (Long number : qqNumbers) urls.add(ShiroUtils.getUserAvatar(number, 5));
        }

        if (urls.isEmpty()) throw new BotWarnException("缺少引用图片或ID参数或AT用户");

        String tempPath = fileStorageProperties.getTempPath();
        for (String url : urls) {
            String tempName = UUID.randomUUID().toString();
            FileInfo fileInfo = DownloadUtil.downloadFile(url, tempPath, tempName);
            String downloadedName = fileInfo.getFileName();
            String imagePath = tempPath + "/" + downloadedName;
            String base64;
            try {
                base64 = htmlRenderer.load("static/html/symmetry.html")
                        .string("mode", mode)
                        .file("image", imagePath)
                        .render("#mirrorContainer");
            } finally {
                FileUtils.deleteQuietly(new File(imagePath));
            }
            String response = MsgUtils.builder().img("base64://" + base64).build();
            bot.sendGroupMsg(groupId, response, false);
            log.info("☑ [Symmetry] 图像处理已完成");
        }
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Symmetry 命令
                功能: 图片对称
                限权: %d 级
                格式:
                1. [引用] Symmetry [可选: 方式]
                2. Symmetry [可选: 方式] [@任何人|QQ号]
                方式: 上/下/左/右 (默认左)
                别名: 对称""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ Symmetry 命令
                功能: 头像图片对称处理
                格式: Symmetry [可选: 方式] [QQ号]
                方式: 上/下/左/右 (默认左)
                示例: Symmetry 右 2660181154""";
    }
}
