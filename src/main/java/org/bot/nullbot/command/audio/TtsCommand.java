package org.bot.nullbot.command.audio;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.TtsClient;
import org.bot.nullbot.config.prop.FileStorageProperties;
import org.bot.nullbot.entity.info.FileInfo;
import org.bot.nullbot.entity.po.TtsTemplatePO;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.TtsTemplateService;
import org.bot.nullbot.util.DownloadUtil;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.*;

@CommandMapping({"Tts", "tts", "语音合成"})
@Component
@Slf4j
@RequiredArgsConstructor
public class TtsCommand implements Command {

    private final FileStorageProperties fileStorageProperties;
    private final TtsTemplateService ttsTemplateService;
    private final TtsClient ttsClient;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        String userName = event.getSender().getNickname();

        if (params.size() < 2)
            throw new NullBotMsgException("[语音合成] ❌参数不足");

        String option = params.getFirst();
        if ("-clone".equals(option)) {
            switch (params.get(1)) {
                case "save" -> {
                    ArrayMsg reply = event.getArrayMsg().getFirst();
                    if (reply.getType() != MsgTypeEnum.reply)
                        throw new NullBotMsgException("[语音合成] ❌需引用模板音频");
                    if (params.size() < 4)
                        throw new NullBotMsgException("[语音合成] ❌新模板参数不足");

                    MsgResp replyMsg = bot.getMsg(reply.getData().get("id").asInt()).getData();
                    // 暂不支持 AMR 格式音频
                    // Map<String, String> recordMap = MessageParseUtil.parseGroupRawMessageAsRecordMap(replyMsg.getRawMessage());
                    Map<String, String> fileMap = MessageParseUtil.parseGroupRawMessageAsFileMap(replyMsg.getRawMessage());

                    Map<String, String> voiceMap = new HashMap<>();
                    // voiceMap.putAll(recordMap);
                    voiceMap.putAll(fileMap);

                    if (voiceMap.isEmpty())
                        throw new NullBotMsgException("[语音合成] ❌引用未包含音频");
                    for (Map.Entry<String, String> entry : voiceMap.entrySet())
                        if (!isAudioFile(entry.getKey()))
                            throw new NullBotMsgException("[语音合成] ❌引用非音频文件");

                    String tempPath = fileStorageProperties.getTempPath();
                    String templateName = params.get(2);
                    String templateText = params.get(3);

                    for (Map.Entry<String, String> entry : voiceMap.entrySet()) {
                        String tempFileName = entry.getKey();
                        String url = entry.getValue();
                        String downloadedFileName;

                        try {
                            FileInfo fileInfo = DownloadUtil.downloadFile(url, tempPath, tempFileName, "\t\t\t\t├─ ");
                            downloadedFileName = fileInfo.getFileName();
                        } catch (Exception e) {
                            throw new NullBotMsgException("[语音合成] ❌模板临时文件下载失败: " + e.getMessage());
                        }

                        String uploadedPath;
                        try {
                            uploadedPath = ttsClient.upload(tempPath + "/" + downloadedFileName);
                        } catch (Exception e) {
                            throw new NullBotMsgException("[语音合成] ❌模板临时文件上传失败: " + e.getMessage());
                        } finally {
                            FileUtils.deleteQuietly(new File(tempPath + "/" + downloadedFileName));
                        }

                        if (!ttsTemplateService.add(templateName, uploadedPath, templateText, userId, userName))
                            throw new NullBotMsgException("[语音合成] ❌存在重名冲突");

                        bot.sendGroupMsg(groupId, "[语音合成] \uD83D\uDCBE模板已保存！\n" +
                                templateName + " : " + templateText + " -> " + uploadedPath, false);
                        log.info("\t\t\t\t├─[语音合成] 模板已保存 - {}:{} -> {}", templateName, templateText, uploadedPath);
                    }
                }

                case "delete" -> {
                    if (params.size() < 3)
                        throw new NullBotMsgException("[语音合成] ❌删除参数不足");
                    String templateName = params.get(2);
                    if (!ttsTemplateService.deleteByName(templateName))
                        throw new NullBotMsgException("[语音合成] ❌该模板不存在");
                    bot.sendGroupMsg(event.getGroupId(), "[语音合成] ⚠️模板已删除", false);
                    log.info("\t\t\t\t├─[Tts] 已删除模板 - {}", templateName);
                }

                case "use" -> {
                    if (params.size() < 4)
                        throw new NullBotMsgException("[语音合成] ❌克隆参数不足");
                    String templateName = params.get(2);
                    String targetText = params.get(3);
                    TtsTemplatePO template = ttsTemplateService.getByName(templateName);
                    if (template == null)
                        throw new NullBotMsgException("[语音合成] ❌模板不存在");
                    String base64;
                    try {
                        base64 = ttsClient.synthesize_clone(template.getPath(), template.getText(), targetText);
                    } catch (Exception e) {
                        throw new NullBotMsgException("[语音合成] ❌克隆时出错: " + e.getMessage());
                    }
                    ttsTemplateService.increaseUsed(template.getId());
                    String response = MsgUtils.builder()
                            .voice("base64://" + base64)
                            .build();
                    bot.sendGroupMsg(event.getGroupId(), response, false);
                    log.info("\t\t\t\t├─[Tts] 已回复克隆语音: {}", targetText.replaceAll("\\R", " "));
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
                    log.info("\t\t\t\t├─[Tts] 已获取模板列表");
                }

                default -> throw new NullBotMsgException("[语音合成] ❌无此克隆选项");
            }
            return;
        }

        if ("-synth".equals(option)) {
            String targetText = String.join(" ", params.subList(1, params.size()));
            String base64;
            try {
                base64 = ttsClient.synthesize(targetText);
            } catch (Exception e) {
                throw new NullBotMsgException("[语音合成] ❌合成时出错: " + e.getMessage());
            }
            String response = MsgUtils.builder()
                    .voice("base64://" + base64)
                    .build();
            bot.sendGroupMsg(event.getGroupId(), response, false);
            log.info("\t\t\t\t├─[Tts] 已回复合成语音");
            return;
        }

        throw new NullBotMsgException("[语音合成] ❌无此操作");
    }

    @Override
    public void execute(Bot bot, PrivateMessageEvent event, List<String> params) {
        if (params.size() < 2)
            throw new NullBotMsgException("[语音合成] ❌参数不足");

        String option = params.getFirst();
        if ("-synth".equals(option)) {
            String targetText = String.join(" ", params.subList(1, params.size()));
            String base64;
            try {
                base64 = ttsClient.synthesize(targetText);
            } catch (Exception e) {
                throw new NullBotMsgException("[语音合成] ❌合成时出错: " + e.getMessage());
            }
            String response = MsgUtils.builder()
                    .voice("base64://" + base64)
                    .build();
            bot.sendPrivateMsg(event.getUserId(), response, false);
            log.info("\t\t\t\t├─[Tts] 已私信合成语音: {}", targetText.replaceAll("\\R", " "));
            return;
        }

        throw new NullBotMsgException("[语音合成] ❌无此操作");
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
