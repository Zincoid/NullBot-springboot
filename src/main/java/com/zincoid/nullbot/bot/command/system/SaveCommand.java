package com.zincoid.nullbot.bot.command.system;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.command.image.ImageSaveCommand;
import com.zincoid.nullbot.bot.command.saying.SayingSaveCommand;
import com.zincoid.nullbot.bot.command.video.VideoSaveCommand;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.core.utils.MsgParseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@CommandMapping({"Save", "save", "保存"})
@Component
@RequiredArgsConstructor
public class SaveCommand implements Command {

    private final VideoSaveCommand videoSaveCommand;
    private final ImageSaveCommand imageSaveCommand;
    private final SayingSaveCommand sayingSaveCommand;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (reply.getType() != MsgTypeEnum.reply) throw new BotWarnException("缺少引用");
        MsgResp replyMsg = bot.getMsg((int) reply.getLongData("id")).getData();

        if (!MsgParseUtil.extractVidMap(replyMsg.getArrayMsg()).isEmpty()) {
            videoSaveCommand.execute(bot, event, args);
        } else if (!MsgParseUtil.extractImgMap(replyMsg.getArrayMsg()).isEmpty()) {
            imageSaveCommand.execute(bot, event, args);
        } else sayingSaveCommand.execute(bot, event, args);
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
