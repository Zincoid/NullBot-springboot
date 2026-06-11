package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.command.image.ImageSaveCmd;
import com.zincoid.nullbot.bot.command.saying.SayingSaveCmd;
import com.zincoid.nullbot.bot.command.video.VideoSaveCmd;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.utils.MsgParseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"Save", "save", "保存"})
@Component
@RequiredArgsConstructor
public class SaveCmd implements Cmd {

    private final VideoSaveCmd videoSaveCmd;
    private final ImageSaveCmd imageSaveCmd;
    private final SayingSaveCmd sayingSaveCmd;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (reply.getType() != MsgTypeEnum.reply) throw new BotWarnException("缺少引用");
        MsgResp replyMsg = bot.getMsg((int) reply.getLongData("id")).getData();

        if (!MsgParseUtil.extractVidMap(replyMsg.getArrayMsg()).isEmpty()) {
            videoSaveCmd.run(bot, event, args);
        } else if (!MsgParseUtil.extractImgMap(replyMsg.getArrayMsg()).isEmpty()) {
            imageSaveCmd.run(bot, event, args);
        } else sayingSaveCmd.run(bot, event, args);
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Save 命令
                功能: 保存视频图片或语录
                限权: %d 级
                格式: [引用] Save
                别名: 保存/save
                优先: 视频>图片>语录""", getAccess()
        );
    }
}
