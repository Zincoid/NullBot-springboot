package org.bot.nullbot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot.nullbot.annotation.CommandMapping;
import org.bot.nullbot.command.Command;
import org.bot.nullbot.component.ai.DeepSeekClient;
import org.bot.nullbot.component.control.CommandRateLimiter;
import org.bot.nullbot.entity.setting.*;
import org.bot.nullbot.enums.ChatScope;
import org.bot.nullbot.enums.LimitScope;
import org.bot.nullbot.exception.NullBotMsgException;
import org.bot.nullbot.service.SettingService;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"GroupSet", "群设置"})
@Component
@RequiredArgsConstructor
@Slf4j
public class GroupSetCommand implements Command {

    private final DeepSeekClient deepSeekClient;
    private final SettingService settingService;
    private final CommandRateLimiter commandRateLimiter;

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        try {
            if (params.isEmpty()) throw new NullBotMsgException("[群设置] ❌参数不足");
            String option = params.get(0);

            if ("-view".equals(option)) {
                Setting setting = settingService.get(groupId);
                bot.sendGroupMsg(groupId, "[群设置] ℹ️已获取！\n" + setting, false);
                log.info("\t\t\t\t├─[GroupSet] 已获取群设置 - {}", groupId);
                return;
            }

            if ("-limit".equals(option)) {
                if (params.size() < 2) throw new NullBotMsgException("[群设置] ❌Limit设置参数不足");
                LimitOption limitOption = settingService.getLimitOption(groupId);
                String setting = params.get(1);
                String msg;

                switch (setting) {
                    case "scp" -> {
                        LimitScope newLimitScope = limitOption.switchLimitScope();
                        msg = "限速范围 -> %s".formatted(newLimitScope);
                    }
                    case "cap" -> {
                        if (params.size() < 3) throw new NullBotMsgException("[群设置] ❌Limit设置参数不足");
                        int capacity = Integer.parseInt(params.get(2));
                        limitOption.setLimitCapacity(capacity);
                        msg = "限速容量 -> %s".formatted(capacity);
                    }
                    case "ref" -> {
                        if (params.size() < 3) throw new NullBotMsgException("[群设置] ❌Limit设置参数不足");
                        int refill = Integer.parseInt(params.get(2));
                        limitOption.setLimitRefill(refill);
                        msg = "补充数量 -> %s".formatted(refill);
                    }
                    case "itv" -> {
                        if (params.size() < 3) throw new NullBotMsgException("[群设置] ❌Limit设置参数不足");
                        int interval = Integer.parseInt(params.get(2));
                        limitOption.setLimitInterval(interval);
                        msg = "补充间隔 -> %s".formatted(interval);
                    }
                    default -> throw new NullBotMsgException("[群设置] ❌无此Limit设置");
                }

                settingService.setLimitOption(groupId, limitOption);
                commandRateLimiter.reset(groupId);
                bot.sendGroupMsg(groupId, """
                        [限速] ✅设置已更新
                        - %s""".formatted(msg), false);
                log.info("\t\t\t\t├─[GroupSet] 已更改群 {} 限速设置 - {}", groupId, msg);
                return;
            }

            if ("-ai".equals(option)) {
                if (params.size() < 2) throw new NullBotMsgException("[群设置] ❌AI设置参数不足");
                String setting = params.get(1);
                ChatOption chatOption = settingService.getChatOption(groupId);
                String msg;

                switch (setting) {
                    case "scp" -> {
                        ChatScope newScope = chatOption.switchChatScope();
                        msg = "会话范围 -> %s".formatted(newScope);
                    }
                    case "frq" -> {
                        if (params.size() < 3) throw new NullBotMsgException("[群设置] ❌AI设置参数不足");
                        double freq = Double.parseDouble(params.get(2));
                        chatOption.setReplyFrequency(freq);
                        msg = "发言频率 -> %s".formatted(freq);
                    }
                    case "ati" -> {
                        boolean enabled = chatOption.switchAntiInjection();
                        msg = "防注模式 -> %s".formatted(enabled ? "ON" : "OFF");
                    }
                    case "tkn" -> {
                        boolean enabled = chatOption.switchThinking();
                        msg = "思考模式 -> %s".formatted(enabled ? "ON" : "OFF");
                    }
                    case "voi" -> {
                        boolean enabled = chatOption.switchVoice();
                        msg = "语音模式 -> %s".formatted(enabled ? "ON" : "OFF");
                    }
                    case "ebd" -> {
                        deepSeekClient.clearGroupHistory(groupId, userId);
                        boolean enabled = chatOption.switchEmbedding();
                        msg = "指令模式 -> %s".formatted(enabled ? "ON" : "OFF");
                    }
                    case "eau" -> {
                        boolean enabled = chatOption.switchEmbeddingAuth();
                        msg = "指令校验 -> %s".formatted(enabled ? "ON" : "OFF");
                    }
                    case "cus" -> {
                        deepSeekClient.clearGroupHistory(groupId, userId);
                        boolean enabled = chatOption.switchCustom();
                        msg = "自定模式 -> %s".formatted(enabled ? "ON" : "OFF");
                    }
                    case "aur" -> {
                        boolean enabled = chatOption.switchAutoReply();
                        msg = "自动发言 -> %s".formatted(enabled ? "ON" : "OFF");
                    }
                    default -> throw new NullBotMsgException("[群设置] ❌无此AI设置");
                }

                settingService.setChatOption(groupId, chatOption);
                bot.sendGroupMsg(groupId, """
                        [AI] ✅设置已更新
                        - %s""".formatted(msg), false);
                log.info("\t\t\t\t├─[GroupSet] 已更改群 {} AI设置 - {}", groupId, msg);
                return;
            }

            if ("-monitor".equals(option)) {
                if (params.size() < 2) throw new NullBotMsgException("[群设置] ❌Monitor设置参数不足");
                String setting = params.get(1);
                MonitorOption monitorOption = settingService.getMonitorOption(groupId);
                boolean enabled = switch (setting) {
                    case "img" -> monitorOption.switchImageCollect();
                    case "msg" -> monitorOption.switchMessageCollect();
                    case "key" -> monitorOption.switchKeywordDetect();
                    case "pok" -> monitorOption.switchPokeDetect();
                    case "rcl" -> monitorOption.switchRecallDetect();
                    default -> throw new NullBotMsgException("[群设置] ❌无此Monitor设置");
                };
                settingService.setMonitorOption(groupId, monitorOption);
                bot.sendGroupMsg(event.getGroupId(), "[监听] ✅已切换: %s".formatted(enabled ? "ON" : "OFF"), false);
                log.info("\t\t\t\t├─[GroupSet] 已更改群 {} 监听设置 - {} -> {}", groupId, setting, enabled ? "ON" : "OFF");
                return;
            }

            if ("-guess".equals(option)) {
                if (params.size() < 4) throw new NullBotMsgException("[群设置] ❌Guess设置参数不足");
                double cropRatio = Double.parseDouble(params.get(1));
                double transparentRatio = Double.parseDouble(params.get(2));
                int padding = Integer.parseInt(params.get(3));
                GuessOption guessOption = settingService.getGuessOption(groupId);
                guessOption.setGuessCropRatio(cropRatio);
                guessOption.setGuessTransparentRatio(transparentRatio);
                guessOption.setGuessPadding(padding);
                settingService.setGuessOption(groupId, guessOption);
                bot.sendGroupMsg(groupId, "[猜角色] ✅参数已更新", false);
                log.info("\t\t\t\t├─[GroupSet] 已更改群 {} Guess参数 -> {} {} {}", groupId, cropRatio, transparentRatio, padding);
                return;
            }

            throw new NullBotMsgException("[群设置] ❌无此操作类型");
        } catch (NumberFormatException e) {
            throw new NullBotMsgException("[群设置] ❌参数格式错误");
        }
    }

    @Override
    public Integer getAccess() {
        return 1;
    }

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
                
                • [-limit] [选项|其他]
                   选项:
                   scp - 限速范围
                   其他:
                   cap [上限量] - 限速容量
                   ref [补充量] - 限速补充
                   itv [分钟数] - 补充间隔
                
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
                
                • [-guess] [切割比例] [透明比例] [切割边距]
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
                
                • [-guess] [切割比例] [透明比例] [切割边距]
                   设置 Guess 游戏难度
                
                示例:
                GroupSet -view
                GroupSet -guess 0.1 0.75 250
                
                注意:
                你不可执行 [-limit] 和 [-ai] 和 [-monitor] 相关设置指令
                针对Guess游戏 - 切割比例和切割边距越小越难 透明比例越大越难""";
    }
}
