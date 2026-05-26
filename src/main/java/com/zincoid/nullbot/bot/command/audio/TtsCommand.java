package com.zincoid.nullbot.bot.command.audio;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import com.zincoid.nullbot.bot.command.CommandArgs;
import com.zincoid.nullbot.bot.exception.NullBotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.ai.voice.TtsClient;
import com.zincoid.nullbot.core.properties.FileStorageProperties;
import com.zincoid.nullbot.core.model.information.FileInfo;
import com.zincoid.nullbot.core.model.data.po.TtsTemplatePO;
import com.zincoid.nullbot.core.service.TtsTemplateService;
import com.zincoid.nullbot.core.util.DownloadUtil;
import com.zincoid.nullbot.core.util.MsgParseUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@CommandMapping({"Tts", "tts", "语音合成"})
@Component
@RequiredArgsConstructor
public class TtsCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final TtsTemplateService ttsTemplateService;
    private final TtsClient ttsClient;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, CommandArgs args) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();

        String option = args.nextString();
        if ("-clone".equals(option)) {
            switch (args.nextString()) {
                case "save" -> {
                    ArrayMsg reply = event.getArrayMsg().getFirst();
                    if (reply.getType() != MsgTypeEnum.reply)
                        throw new NullBotException("需引用模板音频");
                    MsgResp replyMsg = bot.getMsg(reply.getData().get("id").asInt()).getData();
                    // 暂不支持 AMR 格式音频
                    // Map<String, String> recordMap = MsgParseUtil.parseGroupRawMessageAsRecordMap(replyMsg.getRawMessage());
                    Map<String, String> fileMap = MsgParseUtil.extractFileMap(replyMsg.getRawMessage());
                    Map<String, String> voiceMap = new HashMap<>();
                    // voiceMap.putAll(recordMap);
                    voiceMap.putAll(fileMap);
                    if (voiceMap.isEmpty())
                        throw new NullBotException("引用未包含音频");
                    for (Map.Entry<String, String> entry : voiceMap.entrySet())
                        if (!isAudioFile(entry.getKey()))
                            throw new NullBotException("引用非音频文件");

                    String tempPath = fileStorageProperties.getTempPath();
                    String templateName = args.nextString();
                    String templateText = args.nextString();

                    for (Map.Entry<String, String> entry : voiceMap.entrySet()) {
                        String tempName = entry.getKey();
                        String url = entry.getValue();
                        FileInfo fileInfo = DownloadUtil.downloadFile(url, tempPath, tempName);
                        String downloadedName = fileInfo.getFileName();
                        String uploadedPath;
                        try {
                            uploadedPath = ttsClient.upload(tempPath + "/" + downloadedName);
                        } finally {
                            FileUtils.deleteQuietly(new File(tempPath + "/" + downloadedName));
                        }
                        if (!ttsTemplateService.add(templateName, uploadedPath, templateText, userId, userName))
                            throw new NullBotException("存在重名冲突");
                        bot.sendGroupMsg(groupId, """
                                [语音合成] \uD83D\uDCBE模板已保存！
                                %s : %s -> %s"""
                                .formatted(templateName, templateText, uploadedPath), false);
                        log.info("☑ [语音合成] 模板已保存 - {}:{} -> {}", templateName, templateText, uploadedPath);
                    }
                }

                case "delete" -> {
                    String templateName = args.nextString();
                    if (!ttsTemplateService.deleteByName(templateName))
                        throw new NullBotException("该模板不存在");
                    bot.sendGroupMsg(event.getGroupId(), "[语音合成] ⚠️模板已删除", false);
                    log.info("☑ [Tts] 已删除模板 -> {}", templateName);
                }

                case "use" -> {
                    String templateName = args.nextString();
                    String targetText = args.nextString();
                    TtsTemplatePO template = ttsTemplateService.getByName(templateName);
                    if (template == null)
                        throw new NullBotException("模板不存在");
                    String base64 = ttsClient.synthesize_clone(template.getPath(), template.getText(), targetText);
                    ttsTemplateService.increaseUsed(template.getId());
                    String response = MsgUtils.builder()
                            .voice("base64://" + base64)
                            .build();
                    bot.sendGroupMsg(event.getGroupId(), response, false);
                    log.info("☑ [Tts] 已回复克隆语音: {}", targetText);
                }

                case "list" -> {
                    List<TtsTemplatePO> templates = ttsTemplateService.getList();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd");
                    StringBuilder sb = new StringBuilder("\n[名称 | 作者 - 创建日期 | 用量]");
                    for (TtsTemplatePO template : templates) {
                        sb.append("\n").append(template.getName())
                                .append(" | ").append(template.getOwnerName())
                                .append(" - ").append(template.getCreatedTime().format(formatter))
                                .append(" | ").append(template.getUsed());
                    }
                    bot.sendGroupMsg(event.getGroupId(), "[语音合成] ✅已获取模板列表" + sb, false);
                    log.info("☑ [Tts] 已获取模板列表");
                }

                default -> throw new NullBotException("无此克隆选项");
            }
            return;
        }

        if ("-synth".equals(option)) {
            String targetText = args.nextFullString();
            String base64 = ttsClient.synthesize(targetText);
            String response = MsgUtils.builder().voice("base64://" + base64).build();
            bot.sendGroupMsg(event.getGroupId(), response, false);
            log.info("☑ [Tts] 合成语音已回复");
            return;
        }

        throw new NullBotException("无此操作");
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, CommandArgs args) {
        String option = args.nextString();
        if ("-synth".equals(option)) {
            String targetText = args.nextFullString();
            String base64 = ttsClient.synthesize(targetText);
            String response = MsgUtils.builder().voice("base64://" + base64).build();
            bot.sendPrivateMsg(event.getUserId(), response, false);
            log.info("☑ [Tts] 合成语音已回复: {}", targetText);
            return;
        }

        throw new NullBotException("无此操作");
    }

    public static boolean isAudioFile(String fileName) {
        if (fileName == null || fileName.isEmpty()) return false;
        String lowerFileName = fileName.toLowerCase();
        int dotIndex = lowerFileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == lowerFileName.length() - 1) return false;
        String extension = lowerFileName.substring(dotIndex + 1);
        return switch (extension) {
            case "mp3", "wav", "aac", "flac", "m4a", "wma", "ogg", "aiff", "alac", "opus" -> true;
            default -> false;
        };
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
                注意: 需要发送语音替代文字回复时使用该命令""";
    }
}
