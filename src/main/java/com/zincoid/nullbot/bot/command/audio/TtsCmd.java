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
import com.zincoid.nullbot.core.model.information.FileInfo;
import com.zincoid.nullbot.core.model.data.po.TtsTemplatePO;
import com.zincoid.nullbot.core.service.tts.TtsTemplateService;
import com.zincoid.nullbot.core.utils.DownloadUtil;
import com.zincoid.nullbot.core.utils.MsgParseUtil;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@CmdMapping({"Tts", "tts", "语音合成"})
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
        String option = args.nextString();
        if ("-clone".equals(option)) {
            switch (args.nextString()) {
                case "save"  -> handleCloneSave(bot, event, args, groupId, userId, userName);
                case "delete" -> handleCloneDelete(bot, args, groupId);
                case "use"   -> handleCloneUse(bot, args, groupId);
                case "list"  -> handleCloneList(bot, groupId);
                default -> throw new BotWarnException("无此操作");
            }
            return;
        }
        if ("-synth".equals(option)) {
            String voiceMsg = handleSynthesize(args.nextFullString());
            bot.sendGroupMsg(groupId, voiceMsg, false);
            return;
        }
        throw new BotWarnException("无此操作");
    }

    @Override
    public void run(Bot bot, PrivateMessageEvent event, CmdArgs args) {
        String option = args.nextString();
        if ("-synth".equals(option)) {
            String voiceMsg = handleSynthesize(args.nextFullString());
            bot.sendPrivateMsg(event.getUserId(), voiceMsg, false);
            return;
        }
        throw new BotWarnException("无此操作");
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
        Map<String, String> fileMap = MsgParseUtil.extractFileMap(replyMsg.getArrayMsg());
        if (fileMap.isEmpty())
            throw new BotWarnException("引用未包含音频");
        if (fileMap.size() > 1)
            throw new BotErrorException("引用音频过多");
        Map.Entry<String, String> audio = fileMap.entrySet().iterator().next();
        if (!isAudioFile(audio.getKey()))
            throw new BotWarnException("引用文件非音频");
        String templateName = args.nextString();
        String templateText = args.nextString();
        FileInfo fileInfo = DownloadUtil.save(audio.getValue());
        String uploadedPath = ttsClient.upload(fileInfo.getPath());
        if (!ttsTemplateService.add(templateName, uploadedPath, templateText, userId, userName))
            throw new BotWarnException("存在重名模板");
        bot.sendGroupMsg(groupId, "💾模板已保存: %s".formatted(uploadedPath), false);
        log.info("☑ [语音合成] 模板已保存 - {}: {} -> {}", templateName, templateText, uploadedPath);
    }

    private void handleCloneDelete(Bot bot, CmdArgs args, long groupId) {
        String templateName = args.nextString();
        if (!ttsTemplateService.delete(templateName))
            throw new BotInfoException(Emoji.INFO, "模板不存在");
        bot.sendGroupMsg(groupId, "⚠️模板已删除", false);
        log.info("☑ [Tts] 模板已删除 -> {}", templateName);
    }

    private void handleCloneUse(Bot bot, CmdArgs args, long groupId) {
        String templateName = args.nextString();
        String text = args.nextString();
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
                功能: 文本转语音
                限权: %d 级
                格式: Tts [操作方式] [参数...]

                操作方式与参数:
                • [-synth] [目标文本]
                   一般合成
                • [-clone] [克隆选项] [参数...]
                   克隆合成

                克隆选项与参数:
                - [list]
                   模板列表
                - [save] [模板名] [音频文本]
                   保存模板 (需引用音频文件)
                - [delete] [模板名]
                   删除模板
                - [use] [模板名] [目标文本]
                   音频合成 (使用模板)

                注意:
                - 一般合成使用固定人物模型
                - 模板音频时长要求 3~10秒
                - 保存模板需音频的内容文本

                别名: tts/语音合成""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ Tts 命令
                功能: 文本转语音并发送到群中
                格式: Tts -synth [文本]
                注意: 需发送语音替代文字回复时使用""";
    }
}
