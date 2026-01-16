package org.bot.nullbot.command.audio;

import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GetMsgResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.TtsClient;
import org.bot.nullbot.config.FileStorageConfig;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.info.FileInfo;
import org.bot.nullbot.entity.po.TtsTemplatePO;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.TtsTemplateService;
import org.bot.nullbot.util.DownloadUtil;
import org.bot.nullbot.util.FileUtil;
import org.bot.nullbot.util.MessageParseUtil;
import org.springframework.stereotype.Component;

import java.util.*;

@CommandMapping({"Tts", "语音合成"})
@Component
@Slf4j
@RequiredArgsConstructor
public class TtsCommand implements Command
{
    private final FileStorageConfig fileStorageConfig;
    private final TtsTemplateService ttsTemplateService;
    private final TtsClient ttsClient;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            Long groupId = groupMessageEvent.getGroupId();
            Long userId = groupMessageEvent.getSender().getUserId();
            String userName = bot.getStrangerInfo(userId, true).getData().getNickname();

            if (params.size() < 2) throw new NullBotMsgException("[语音合成] ❌参数不足");

            String option = params.getFirst();
            if ("-clone".equals(option)) {
                switch (params.get(1)) {
                    case "save" -> {
                        ArrayMsg reply = groupMessageEvent.getArrayMsg().getFirst();
                        if (reply.getType() != MsgTypeEnum.reply)
                            throw new NullBotMsgException("[语音合成] ❌需引用模板音频");
                        if (params.size() < 4)
                            throw new NullBotMsgException("[语音合成] ❌新模板参数不足");

                        GetMsgResp replyMsg = bot.getMsg(Integer.parseInt(reply.getData().get("id"))).getData();
                        // Map<String, String> recordMap = MessageParseUtil.parseGroupRawMessageAsRecordMap(replyMsg.getRawMessage());  // 暂不支持 AMR 格式音频
                        Map<String, String> fileMap = MessageParseUtil.parseGroupRawMessageAsFileMap(replyMsg.getRawMessage());

                        Map<String, String> voiceMap = new HashMap<>();
                        // voiceMap.putAll(recordMap);
                        voiceMap.putAll(fileMap);

                        if(voiceMap.isEmpty())
                            throw new NullBotMsgException("[语音合成] ❌引用未包含音频");
                        for (Map.Entry<String, String> entry : voiceMap.entrySet())
                            if(!isAudioFile(entry.getKey()))
                                throw new NullBotMsgException("[语音合成] ❌引用非音频文件");

                        String tempFilePath = fileStorageConfig.getTempPath();
                        String templateName = params.get(2);
                        String templateText = params.get(3);

                        for (Map.Entry<String, String> entry : voiceMap.entrySet()) {
                            String tempFileName = entry.getKey();
                            String url = entry.getValue();
                            String downloadedFileName;

                            try {
                                FileInfo fileInfo = DownloadUtil.downloadFile(url, tempFilePath, tempFileName, "\t\t\t\t├─ ");
                                downloadedFileName = fileInfo.getFileName();
                            } catch (Exception e) {
                                throw new NullBotMsgException("[语音合成] ❌模板临时文件下载失败: " + e.getMessage());
                            }

                            String uploadedPath;
                            try {
                                uploadedPath = ttsClient.upload(tempFilePath + "/" + downloadedFileName);
                            } catch (Exception e) {
                                throw new NullBotMsgException("[语音合成] ❌模板临时文件上传失败: " + e.getMessage());
                            } finally {
                                FileUtil.deleteFileByName(tempFilePath, downloadedFileName);
                            }

                            if (!ttsTemplateService.addTemplate(templateName, uploadedPath, templateText, userId, userName))
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
                        if(!ttsTemplateService.deleteTemplate(templateName))
                            throw new NullBotMsgException("[语音合成] ❌该模板不存在");
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[语音合成] ⚠️模板已删除", false);
                        log.info("\t\t\t\t├─[Tts] 已删除模板 - {}", templateName);
                    }

                    case "use" -> {
                        if (params.size() < 4)
                            throw new NullBotMsgException("[语音合成] ❌克隆参数不足");
                        String templateName = params.get(2);
                        String targetText = params.get(3);
                        TtsTemplatePO template = ttsTemplateService.getTemplate(templateName);
                        if (template == null)
                            throw new NullBotMsgException("[语音合成] ❌模板不存在");
                        String base64;
                        try {
                            base64 = ttsClient.synthesize_clone(template.getPath(), template.getText(), targetText);
                        } catch (Exception e) {
                            throw new NullBotMsgException("[语音合成] ❌克隆时出错: " + e.getMessage());
                        }
                        String response = MsgUtils.builder()
                                .voice("base64://" + base64)
                                .build();
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                        log.info("\t\t\t\t├─[Tts] 已回复克隆语音: {}", targetText.replaceAll("\\R", " "));
                    }

                    case "list" -> {
                        List<TtsTemplatePO> templates = ttsTemplateService.getTemplateList();
                        StringBuilder sb = new StringBuilder("\n[  模板名 ======= 创建者  ]");
                        for (TtsTemplatePO template : templates) {
                            sb.append("\n").append(template.getName()).append(" - ")
                                    .append(template.getOwnerName()).append("(").append(template.getOwnerId()).append(")");
                        }
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[语音合成] ✅已获取模板列表" + sb, false);
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
                bot.sendGroupMsg(groupMessageEvent.getGroupId(), response, false);
                log.info("\t\t\t\t├─[Tts] 已回复合成语音: {}", targetText.replaceAll("\\R", " "));
                return;
            }

            throw new NullBotMsgException("[语音合成] ❌无此操作");
        }else
            throw new NullBotLogException("[语音合成] ❌未设计 - 非群消息事件响应方式");
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
                功能: 文字转语音(一般合成/克隆)
                限权: %d 级
                格式: Tts [操作] [参数...]
                
                操作与参数:
                • [-synth] [文本]
                   一般合成
                
                • [-clone] [选项] [选项参数]
                   选项与参数:
                   list
                   - 查看模板列表
                   save [模板名] [音频文本]
                   - 保存模板 (需引用音频文件)
                   delete [模板名]
                   - 删除模板
                   use [模板名] [目标文本]
                   - 使用模板合成音频
                
                中文命令: 语音合成""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ Tts 命令
                功能: 文字转语音并发送到群中
                限权: %d 级
                格式: Tts -synth [文本]
                注意: 当你想要发送语音代替文字回复时使用该命令！""", getAccess()
        );
    }
}
