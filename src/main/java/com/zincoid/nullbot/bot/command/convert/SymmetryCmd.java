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
import com.zincoid.nullbot.bot.exception.BotErrorException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.service.render.RenderingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.model.information.FileInfo;
import com.zincoid.nullbot.core.utils.SaveUtil;
import com.zincoid.nullbot.core.utils.MsgUtil;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@CmdMapping({"Symmetry", "对称"})
@Component
@RequiredArgsConstructor
public class SymmetryCmd implements Cmd {

    private final RenderingService renderingService;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) throws Exception {
        Long groupId = event.getGroupId();
        ArrayMsg reply = event.getArrayMsg().getFirst();
        List<String> urls = new ArrayList<>();
        String mode = "left";

        if (reply.getType() == MsgTypeEnum.reply) {
            // 引用收集
            MsgResp replyMsg = bot.getMsg((int) reply.getLongData("id")).getData();
            Map<String, String> imageMap = MsgUtil.extractImgMap(replyMsg.getArrayMsg());
            urls.addAll(imageMap.values());
        }
        if (args.hasNext()) {
            // ID 收集
            String modeStr = args.next();
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
                    List<Long> qqNumbers = MsgUtil.extractAtNumbers(event.getArrayMsg());
                    for (Long number : qqNumbers) urls.add(ShiroUtils.getUserAvatar(number, 5));
                }
            } else {
                long qqNumber = args.getLong(0);
                urls.add(ShiroUtils.getUserAvatar(qqNumber, 5));
            }
        } else {
            // AT 收集
            List<Long> qqNumbers = MsgUtil.extractAtNumbers(event.getArrayMsg());
            for (Long number : qqNumbers) urls.add(ShiroUtils.getUserAvatar(number, 5));
        }

        if (urls.isEmpty()) throw new BotWarnException("缺少引用图片或ID参数或AT用户");

        for (String url : urls) {
            FileInfo fileInfo = SaveUtil.save(url);
            String base64 = renderingService.symmetry(fileInfo.getPath(), mode);
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
