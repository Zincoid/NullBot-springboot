package org.bot.nullbot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.info.SettingInfo;
import org.bot.nullbot.enums.Scope;
import org.bot.nullbot.exception.NullBotLogException;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.SettingService;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"GroupSet", "群设置"})
@Component
@RequiredArgsConstructor
@Slf4j
public class GroupSetCommand implements Command
{
    private final DeepSeekClient deepSeekClient;
    private final SettingService settingService;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            Long groupId = groupMessageEvent.getGroupId();
            Long userId = groupMessageEvent.getUserId();
            try {
                if (params.isEmpty()) throw new NullBotMsgException("[群设置] ❌参数不足");
                String option = params.get(0);

                if ("-view".equals(option)) {
                    SettingInfo setting = settingService.getSetting(groupId);
                    bot.sendGroupMsg(groupId, "[群设置] ℹ️已获取！\n" + setting, false);
                    log.info("\t\t\t\t├─[GroupSet] 已获取群设置 - {}", groupId);
                    return;
                }

                if ("-ai".equals(option)) {
                    if (params.size() < 2) throw new NullBotMsgException("[群设置] ❌AI设置参数不足");
                    String setting = params.get(1);
                    if ("scp".equals(setting)) {
                        Scope scope = settingService.switchScope(groupId);
                        bot.sendGroupMsg(groupId, "[AI] \uD83D\uDD04已切换: " + scope, false);
                        log.info("\t\t\t\t├─[GroupSet] 已更改群 {} 设置 {} -> {}", groupId, setting, scope);
                        return;
                    }
                    if ("frq".equals(setting)) {
                        if(params.size() < 3) throw new NullBotMsgException("[群设置] ❌AI设置参数不足");
                        double freq = Double.parseDouble(event.getCommandParameters().get(2));
                        settingService.setReplyFrequency(groupId, freq);
                        bot.sendGroupMsg(groupId, "[AI] ✅发言频率已更新", false);
                        log.info("\t\t\t\t├─[GroupSet] 已更改群 {} AI自动发言频率 -> {}", groupId, freq);
                        return;
                    }
                    boolean isEnabled = switch (setting) {
                        case "ati" -> settingService.switchAntiInjection(groupId);
                        case "tkn" -> settingService.switchThinking(groupId);
                        case "voi" -> {
                            deepSeekClient.clearHistory(groupId, userId, settingService.getChatOption(groupId));
                            yield settingService.switchVoice(groupId);
                        }
                        case "ebd" -> {
                            deepSeekClient.clearHistory(groupId, userId, settingService.getChatOption(groupId));
                            yield settingService.switchEmbedding(groupId);
                        }
                        case "eau" -> settingService.switchEmbeddingAuth(groupId);
                        case "cus" -> {
                            deepSeekClient.clearHistory(groupId, userId, settingService.getChatOption(groupId));
                            yield settingService.switchCustom(groupId);
                        }
                        case "aur" -> settingService.switchAutoReply(groupId);
                        default -> throw new NullBotMsgException("[群设置] ❌无此AI设置");
                    };
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[AI] \uD83D\uDD04已切换: " + (isEnabled ? "ON" : "OFF"), false);
                    log.info("\t\t\t\t├─[GroupSet] 已更改群 {} 设置 {} -> {}", groupId, setting, isEnabled ? "ON" : "OFF");
                    return;
                }

                if ("-monitor".equals(option)) {
                    if (params.size() < 2) throw new NullBotMsgException("[群设置] ❌监听设置参数不足");
                    String setting = params.get(1);
                    boolean isEnabled = switch (setting) {
                        case "img" -> settingService.switchImageCollect(groupId);
                        case "msg" -> settingService.switchMessageCollect(groupId);
                        case "key" -> settingService.switchKeywordDetect(groupId);
                        case "pok" -> settingService.switchPokeDetect(groupId);
                        case "rcl" -> settingService.switchRecallDetect(groupId);
                        default -> throw new NullBotMsgException("[群设置] ❌无此监听设置");
                    };
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[监听] \uD83D\uDD04已切换: " + (isEnabled ? "ON" : "OFF"), false);
                    log.info("\t\t\t\t├─[GroupSet] 已更改群 {} 设置 {} -> {}", groupId, setting, isEnabled ? "ON" : "OFF");
                    return;
                }

                if ("-guess".equals(option)) {
                    if(params.size() < 3) throw new NullBotMsgException("[群设置] ❌Guess设置参数不足");
                    double ratio = Double.parseDouble(event.getCommandParameters().get(1));
                    int padding = Integer.parseInt(event.getCommandParameters().get(2));
                    if(settingService.setGuessParams(groupId, ratio, padding)) {
                        bot.sendGroupMsg(groupId, "[游戏] ✅参数已更新", false);
                        log.info("\t\t\t\t├─[GroupSet] 已更改群 {} 设置 -> Guess游戏参数", groupId);
                        return;
                    }
                    throw new NullBotMsgException("[群设置] ❌Guess参数更新失败");
                }

                throw new NullBotMsgException("[群设置] ❌无此操作类型");
            } catch (NumberFormatException e) {
                throw new NullBotMsgException("[群设置] ❌参数格式错误");
            }
        }else
            throw new NullBotLogException("[群设置] ❌未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ GroupSet 命令
                功能: 设置群功能
                限权: %d 级
                格式: GroupSet [操作] [参数]
                
                操作与参数:
                • [-view]
                   获取群设置
                
                • [-ai] [模式选项|其他]
                   模式选项:
                   scp - 会话范围
                   ati - 防注模式
                   tkn - 思考模式
                   voi - 语音模式
                   ebd - 指令模式
                   eau - 指令验证
                   cus - 自定模式
                   aur - 自动发言
                   其他:
                   frq [0~1] - 发言频率
                
                • [-monitor] [监测类型]
                   监测类型:
                   img - 图片收集
                   msg - 消息收集
                   key - 词语检测
                   pok - 戳戳检测
                   rcl - 撤回检测
                
                • [-guess] [切割比例] [内边距]
                   设置 Guess 游戏难度
                
                注意:
                - 切换AI语音/指令/自定模式时会清空历史
                
                别名: 群设置""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return """
                ◉ GroupSet 命令
                功能: 设置群功能
                格式: GroupSet [操作] [可选: 参数]
                
                操作与参数:
                • [-view]
                   获取群设置
                
                • [-monitor] [监测类型]
                   监测类型:
                   img - 图片收集
                   msg - 消息收集
                   key - 词语检测
                   pok - 戳戳检测
                   rcl - 撤回检测
                
                • [-guess] [切割比例] [内边距]
                   设置 Guess 游戏难度
                
                示例:
                GroupSet -view
                GroupSet -monitor img
                GroupSet -guess 0.1 250
                
                注意:
                你不可执行 [-ai] 相关设置指令！
                针对Guess游戏 - 切割比例越小越难 内边距越小越难""";
    }
}
