package org.bot.nullbot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.component.control.SettingManager;
import org.bot.nullbot.entity.CommandEvent;
import org.bot.nullbot.entity.info.SettingInfo;
import org.bot.nullbot.enums.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"GroupSet", "群设置"})
@Component
@RequiredArgsConstructor
@Slf4j
public class GroupSetCommand implements Command
{
    private final DeepSeekClient deepSeekClient;
    private final SettingManager settingManager;

    @Override
    public void execute(Bot bot, CommandEvent<?> event) {
        if (event.getEvent() instanceof GroupMessageEvent groupMessageEvent) {
            List<String> params = event.getCommandParameters();
            Long groupId = groupMessageEvent.getGroupId();
            Long userId = groupMessageEvent.getUserId();
            try {
                if (params.isEmpty()) throw new IllegalArgumentException("参数不足");
                String option = params.get(0);

                if ("-view".equals(option)) {
                    SettingInfo setting = settingManager.getSetting(groupId);
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[群设置] ℹ️已获取！\n" + setting, false);
                    log.info("\t\t\t\t├─[GroupSet] 已获取群设置 - {}", groupId);
                    return;
                }
                if ("-ai".equals(option)) {
                    if (params.size() < 2) throw new IllegalArgumentException("参数不足");
                    String setting = params.get(1);
                    if ("scp".equals(setting)) {
                        Scope scope = settingManager.switchScope(groupId);
                        bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[AI] \uD83D\uDD04已切换: " + scope, false);
                        log.info("\t\t\t\t├─[GroupSet] 已更改群 {} 设置 {} -> {}", groupId, setting, scope);
                        return;
                    }
                    boolean isEnabled = switch (setting) {
                        case "ati" -> settingManager.switchAntiInjection(groupId);
                        case "tkn" -> settingManager.switchThinking(groupId);
                        case "ebd" -> {
                            deepSeekClient.clearHistory(groupId, userId, settingManager.getChatOption(groupId));
                            yield settingManager.switchEmbedding(groupId);
                        }
                        case "eau" -> settingManager.switchEmbeddingAuth(groupId);
                        case "cus" -> {
                            deepSeekClient.clearHistory(groupId, userId, settingManager.getChatOption(groupId));
                            yield settingManager.switchCustom(groupId);
                        }
                        default -> throw new NoSuchMethodException("无此AI设置");
                    };
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[AI] \uD83D\uDD04已切换: " + (isEnabled ? "ON" : "OFF"), false);
                    log.info("\t\t\t\t├─[GroupSet] 已更改群 {} 设置 {} -> {}", groupId, setting, isEnabled ? "ON" : "OFF");
                    return;
                }
                if ("-monitor".equals(option)) {
                    if (params.size() < 2) throw new IllegalArgumentException("参数不足");
                    String setting = params.get(1);
                    boolean isEnabled = switch (setting) {
                        case "img" -> settingManager.switchImageCollect(groupId);
                        case "msg" -> settingManager.switchMessageCollect(groupId);
                        case "key" -> settingManager.switchKeywordDetect(groupId);
                        case "pok" -> settingManager.switchPokeDetect(groupId);
                        case "rcl" -> settingManager.switchRecallDetect(groupId);
                        default -> throw new NoSuchMethodException("无此监听设置");
                    };
                    bot.sendGroupMsg(groupMessageEvent.getGroupId(), "[监听] \uD83D\uDD04已切换: " + (isEnabled ? "ON" : "OFF"), false);
                    log.info("\t\t\t\t├─[GroupSet] 已更改群 {} 设置 {} -> {}", groupId, setting, isEnabled ? "ON" : "OFF");
                    return;
                }
                if ("-guess".equals(option)) {
                    if(params.size() < 3) throw new IllegalArgumentException("Guess游戏参数不足");
                    double ratio = Double.parseDouble(event.getCommandParameters().get(1));
                    int padding = Integer.parseInt(event.getCommandParameters().get(2));
                    if(settingManager.setGuessParams(groupId, ratio, padding)) {
                        bot.sendGroupMsg(groupId, "[游戏] ✅参数已更新", false);
                        log.info("\t\t\t\t├─[GroupSet] 已更群 {} 设置 -> Guess游戏参数", groupId);
                        return;
                    }
                    throw new Exception("Guess游戏参数更新失败");
                }

                throw new NoSuchMethodException("无此操作类型");
            } catch (NumberFormatException e) {
                bot.sendGroupMsg(groupId, "[游戏] ❌Guess游戏参数格式错误", false);
                log.info("\t\t\t\t├─[GroupSet] 群设置出错 - Guess游戏参数格式错误");
            } catch (Exception e) {
                bot.sendGroupMsg(groupId, "[群设置] ❌" + e.getMessage(), false);
                log.info("\t\t\t\t├─[GroupSet]群 {} 设置出错 - {}", groupId, e.getMessage());
            }
        }else
            log.info("\t\t\t\t├─[GroupSet] 未设计 - 非群消息事件响应方式");
    }

    @Override
    public Integer getAccess() { return 1; }

    @Override
    public String getHelp() {
        return String.format("""
                ◉ GroupSet 命令
                功能: 设置群功能
                限权: %d 级
                格式: GroupSet [操作] [可选: 参数]
                中文命令: 群设置
                
                操作与参数:
                • [-view]
                  获取群设置
                
                • [-ai] [模式选项]
                  模式选项:
                  scp - 聊天模式
                  ati - 防注入模式
                  tkn - 思考模式
                  ebd - 指令模式
                  eau - 指令验证
                  cus - 自定义提示词模式
                
                • [-monitor] [监测类型]
                  监测类型:
                  img - 图片收集
                  msg - 消息收集
                  key - 关键词检测
                  pok - 戳一戳检测
                  rcl - 撤回检测
                
                • [-guess] [切割比例] [内边距]
                  设置 Guess 游戏难度""", getAccess()
        );
    }

    @Override
    public String getHelpForAI() {
        return String.format("""
                ◉ GroupSet 命令
                功能: 设置群功能
                限权: %d 级
                格式: GroupSet [操作] [可选: 参数]
                中文命令: 群设置
                
                操作与参数:
                • [-view]
                  获取群设置
                
                • [-monitor] [监测类型]
                  监测类型:
                  img - 图片收集
                  msg - 消息收集
                  key - 关键词检测
                  pok - 戳一戳检测
                  rcl - 撤回检测
                
                • [-guess] [切割比例(范围 0.05-0.3)] [内边距(范围 150-300)]
                  设置 Guess 游戏难度
                
                示例:
                GroupSet -view
                GroupSet -monitor img
                GroupSet -guess 0.1 250
                
                注意:
                只有Zincoid可以调用！！！
                针对Guess游戏 - 切割比例越小越难 内边距越小越难""", getAccess()
        );
    }
}
