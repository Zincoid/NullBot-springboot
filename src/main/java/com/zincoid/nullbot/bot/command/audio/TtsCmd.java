package com.zincoid.nullbot.bot.command.audio;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotErrorException;
import com.zincoid.nullbot.bot.exception.BotInfoException;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.enums.Emoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.core.module.ai.tts.TtsClient;
import com.zincoid.nullbot.core.model.information.FileMeta;
import com.zincoid.nullbot.core.model.data.po.TtsTemplatePO;
import com.zincoid.nullbot.core.service.tts.TtsTemplateService;
import com.zincoid.nullbot.core.utils.SaveUtil;
import com.zincoid.nullbot.core.utils.MsgUtil;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@CmdMapping({"Tts", "语音合成"})
@Component
@RequiredArgsConstructor
public class TtsCmd implements Cmd {

    private static final DateTimeFormatter LIST_DATE_FORMAT = DateTimeFormatter.ofPattern("yy/MM/dd");
    private static final Set<String> AUDIO_EXTENSIONS = Set.of("mp3", "wav", "aac", "flac", "m4a", "wma", "ogg", "aiff", "alac", "opus");

    private final TtsTemplateService ttsTemplateService;
    private final TtsClient ttsClient;

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();
        switch (args.next()) {
            case "synth" -> {
                String message = handleSynthesize(args.rest());
                bot.sendGroupMsg(groupId, message, false);
            }
            case "clone" -> {
                if (args.hasOpt("save", "s")) handleCloneSave(bot, event, args, groupId, userId, userName);
                else if (args.hasOpt("delete", "d")) handleCloneDelete(bot, args, groupId);
                else if (args.hasOpt("use", "u")) handleCloneUse(bot, args, groupId);
                else if (args.hasOpt("list", "l")) handleCloneList(bot, groupId);
                else throw new BotWarnException("无此操作");
            }
            default -> throw new BotWarnException("无此子命令");
        }
    }

    @Override
    public void run(Bot bot, PrivateMessageEvent event, CmdArgs args) {
        if ("synth".equals(args.next())) {
            String message = handleSynthesize(args.rest());
            bot.sendPrivateMsg(event.getUserId(), message, false);
            return;
        }
        throw new BotWarnException("无此子命令");
    }

    // ================== synth 子命令 ==================

    private String handleSynthesize(String text) {
        String base64 = ttsClient.synthesize(text);
        log.info("☑ [Tts] 合成语音已回复: {}", text);
        return MsgUtils.builder().voice("base64://" + base64).build();
    }

    // ================== clone 子命令 ==================

    private void handleCloneSave(Bot bot, GroupMessageEvent event, CmdArgs args,
                                  long groupId, long userId, String userName) {
        ArrayMsg reply = event.getArrayMsg().getFirst();
        if (reply.getType() != MsgTypeEnum.reply)
            throw new BotWarnException("未引用模板音频");
        MsgResp replyMsg = bot.getMsg((int) reply.getLongData("id")).getData();
        Map<String, String> fileMap = MsgUtil.extractFileMap(replyMsg.getArrayMsg());
        if (fileMap.isEmpty())
            throw new BotWarnException("引用未包含音频");
        if (fileMap.size() > 1)
            throw new BotErrorException("引用音频过多");
        Map.Entry<String, String> audio = fileMap.entrySet().iterator().next();
        if (!isAudioFile(audio.getKey()))
            throw new BotWarnException("引用文件非音频");
        String templateName = args.next();
        String templateText = args.next();
        FileMeta fileMeta = SaveUtil.save(audio.getValue());
        String uploadedPath = ttsClient.upload(fileMeta.getPath());
        if (!ttsTemplateService.add(templateName, uploadedPath, templateText, userId, userName))
            throw new BotWarnException("存在重名模板");
        bot.sendGroupMsg(groupId, "💾模板已保存: %s".formatted(uploadedPath), false);
        log.info("☑ [语音合成] 模板已保存 - {}: {} -> {}", templateName, templateText, uploadedPath);
    }

    private void handleCloneDelete(Bot bot, CmdArgs args, long groupId) {
        String templateName = args.next();
        if (!ttsTemplateService.delete(templateName))
            throw new BotInfoException(Emoji.INFO, "模板不存在");
        bot.sendGroupMsg(groupId, "⚠️模板已删除", false);
        log.info("☑ [Tts] 模板已删除 -> {}", templateName);
    }

    private void handleCloneUse(Bot bot, CmdArgs args, long groupId) {
        String templateName = args.next();
        String text = args.rest();
        TtsTemplatePO template = ttsTemplateService.get(templateName);
        if (template == null)
            throw new BotInfoException(Emoji.INFO, "模板不存在");
        incrementUsedSafe(template.getId());
        String base64 = ttsClient.clone(template.getPath(), template.getText(), text);
        String response = MsgUtils.builder().voice("base64://" + base64).build();
        bot.sendGroupMsg(groupId, response, false);
        log.info("☑ [Tts] 克隆语音已回复: {}", text);
    }

    private void handleCloneList(Bot bot, long groupId) {
        List<TtsTemplatePO> templates = ttsTemplateService.list();
        StringBuilder sb = new StringBuilder("[名称 | 作者 - 创建日期 | 用量]");
        for (TtsTemplatePO template : templates)
            sb.append("\n").append(template.getName())
                    .append(" | ").append(template.getOwnerName())
                    .append(" - ").append(template.getCreatedTime().format(LIST_DATE_FORMAT))
                    .append(" | ").append(template.getUsed());
        bot.sendGroupMsg(groupId, "[语音合成模板列表] ✅已获取\n" + sb, false);
        log.info("☑ [Tts] 模板列表已获取");
    }

    // ================== 工具方法 ==================

    private void incrementUsedSafe(int templateId) {
        try {
            ttsTemplateService.increaseUsed(templateId);
        } catch (Exception e) {
            log.warn("[Tts] 模板用量更新失败 id={}: {}", templateId, e.getMessage());
        }
    }

    private static boolean isAudioFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) return false;
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) return false;
        return AUDIO_EXTENSIONS.contains(fileName.substring(dotIndex + 1).toLowerCase());
    }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ Tts 命令
                功能: 文本转换语音
                限权: %d 级
                用法: Tts [子命令] [参数...]

                子命令:
                synth [文本]  一般合成
                clone [选项]  克隆合成

                克隆选项:
                -l,--list              模板列表
                -s,--save [模板] [文本]  保存模板 (引用音频)
                -d,--delete [模板]      删除模板
                -u,--use [模板] [文本]   音频克隆

                注意:
                - 一般合成使用固定人物模型
                - 模板音频时长要求 3~10秒
                - 保存模板需音频的内容文本

                别名: 语音合成""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ Tts 命令
                功能: 文本转语音并发送到群中
                用法: Tts synth [文本]
                注意: 需发送语音替代文字回复时使用""";
    }
}
