package com.zincoid.nullbot.bot.command.convert;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.service.render.RenderingService;
import com.zincoid.nullbot.core.utils.SaveUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.model.information.FileMeta;
import com.zincoid.nullbot.core.utils.MsgUtil;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@CmdMapping({"Convert", "图像处理"})
@Component
@RequiredArgsConstructor
public class ConvertCmd implements Cmd {

    private final RenderingService renderingService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        ArrayMsg reply = event.getArrayMsg().getFirst();
        String method = args.next();
        List<String> urls = new ArrayList<>();

        if (reply.getType() == MsgTypeEnum.reply) {
            // 引用收集
            MsgResp replyMsg = bot.getMsg((int) reply.getLongData("id")).getData();
            Map<String, String> imageMap = MsgUtil.extractImgMap(replyMsg.getArrayMsg());
            urls.addAll(imageMap.values());
        }
        if (args.hasNext()) {
            // ID 收集
            long qqNumber = args.nextLong();
            urls.add(ShiroUtils.getUserAvatar(qqNumber, 5));
        } else {
            // AT 收集
            List<Long> qqNumbers = MsgUtil.extractAtNumbers(event.getArrayMsg());
            for (Long qqNumber : qqNumbers) urls.add(ShiroUtils.getUserAvatar(qqNumber, 5));
        }

        if (urls.isEmpty()) throw new BotWarnException("缺少图片引用或ID参数或AT用户");

        for (String url : urls) {
            FileMeta fileMeta = SaveUtil.save(url);
            String imagePath = fileMeta.getPath();
            String base64 = switch (method) {
                case "RIP" -> renderingService.rip(imagePath);
                case "PRTS" -> renderingService.prts(imagePath, false);
                case "InvsPRTS" -> renderingService.prts(imagePath, true);
                default -> throw new BotWarnException("无此操作");
            };
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
