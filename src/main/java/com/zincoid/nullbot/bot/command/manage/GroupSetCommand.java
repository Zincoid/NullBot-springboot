package com.zincoid.nullbot.bot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.exception.NullBotException;
import com.zincoid.nullbot.core.component.ai.chat.client.QQAiClient;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CommandMapping;
import com.zincoid.nullbot.bot.command.Command;
import com.zincoid.nullbot.core.component.control.CommandRateLimiter;
import com.zincoid.nullbot.core.model.data.po.SettingPO;
import com.zincoid.nullbot.core.component.ai.chat.enums.ChatScope;
import com.zincoid.nullbot.core.component.ai.chat.enums.ChatStrategy;
import com.zincoid.nullbot.core.enums.LimitScope;
import com.zincoid.nullbot.core.service.SettingService;
import com.zincoid.nullbot.core.util.BotCtxUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@CommandMapping({"GroupSet", "群设置"})
@Component
@Slf4j
public class GroupSetCommand implements Command {

    private final QQAiClient qqAiClient;
    private final SettingService settingService;
    private final CommandRateLimiter commandRateLimiter;

    public GroupSetCommand(@Lazy QQAiClient qqAiClient, SettingService settingService, CommandRateLimiter commandRateLimiter) {
        this.qqAiClient = qqAiClient;
        this.settingService = settingService;
        this.commandRateLimiter = commandRateLimiter;
    }

    @Override
    public void execute(Bot bot, GroupMessageEvent event, List<String> params) {
        Long groupId = event.getGroupId();
        Long userId = event.getUserId();
        try {
            if (params.isEmpty()) throw new NullBotException("[群设置] ❌参数不足");
            String option = params.getFirst();
            SettingPO setting = BotCtxUtil.getSetting();

            if ("-view".equals(option)) {
                bot.sendGroupMsg(groupId, "[群设置] ℹ️已获取！\n" + setting, false);
                log.info("├─[GroupSet] 已获取群设置 - {}", groupId);
                return;
            }

            if ("-limit".equals(option)) {
                if (params.size() < 2) throw new NullBotException("[群设置] ❌Limit设置参数不足");
                String name = params.get(1);
                String msg;

                switch (name) {
                    case "scp" -> {
                        LimitScope newLimitScope = setting.switchLimitScope();
                        msg = "限速范围 -> %s".formatted(newLimitScope);
                    }
                    case "cap" -> {
                        if (params.size() < 3) throw new NullBotException("[群设置] ❌Limit设置参数不足");
                        int capacity = Integer.parseInt(params.get(2));
                        setting.setLimitCapacity(capacity);
                        msg = "限速容量 -> %s".formatted(capacity);
                    }
                    case "ref" -> {
                        if (params.size() < 3) throw new NullBotException("[群设置] ❌Limit设置参数不足");
                        int refill = Integer.parseInt(params.get(2));
                        setting.setLimitRefill(refill);
                        msg = "补充数量 -> %s".formatted(refill);
                    }
                    case "itv" -> {
                        if (params.size() < 3) throw new NullBotException("[群设置] ❌Limit设置参数不足");
                        int interval = Integer.parseInt(params.get(2));
                        setting.setLimitInterval(interval);
                        msg = "补充间隔 -> %s".formatted(interval);
                    }
                    default -> throw new NullBotException("[群设置] ❌无此Limit设置");
                }

                settingService.set(setting);
                commandRateLimiter.reset(groupId);
                bot.sendGroupMsg(groupId, """
                        [限速] ✅设置已更新
                        %s""".formatted(msg), false);
                log.info("├─[GroupSet] 已更改群 {} 限速设置 - {}", groupId, msg);
                return;
            }

            if ("-ai".equals(option)) {
                if (params.size() < 2) throw new NullBotException("[群设置] ❌AI设置参数不足");
                String name = params.get(1);
                String msg;

                switch (name) {
                    case "scp" -> {
                        ChatScope newScope = setting.switchChatScope();
                        msg = "会话范围 -> %s".formatted(newScope);
                    }
                    case "stg" -> {
                        qqAiClient.clear(BotCtxUtil.getChatId());
                        ChatStrategy strategy = setting.switchChatStrategy();
                        msg = "对话策略 -> %s".formatted(strategy);
                    }
                    case "frq" -> {
                        if (params.size() < 3) throw new NullBotException("[群设置] ❌AI设置参数不足");
                        double freq = Double.parseDouble(params.get(2));
                        setting.setReplyFrequency(freq);
                        msg = "发言频率 -> %s".formatted(freq);
                    }
                    case "ati" -> {
                        boolean enabled = setting.switchAntiInjection();
                        msg = "防注模式 -> %s".formatted(enabled ? "ON" : "OFF");
                    }
                    case "tkn" -> {
                        boolean enabled = setting.switchThinking();
                        msg = "思考模式 -> %s".formatted(enabled ? "ON" : "OFF");
                    }
                    case "voi" -> {
                        boolean enabled = setting.switchVoice();
                        msg = "语音模式 -> %s".formatted(enabled ? "ON" : "OFF");
                    }
                    case "ica" -> {
                        boolean enabled = setting.switchInnerCmdAuth();
                        msg = "内令鉴权 -> %s".formatted(enabled ? "ON" : "OFF");
                    }
                    case "cus" -> {
                        qqAiClient.clear(BotCtxUtil.getChatId());
                        boolean enabled = setting.switchCustom();
                        msg = "自定模式 -> %s".formatted(enabled ? "ON" : "OFF");
                    }
                    case "aur" -> {
                        boolean enabled = setting.switchAutoReply();
                        msg = "自动发言 -> %s".formatted(enabled ? "ON" : "OFF");
                    }
                    default -> throw new NullBotException("[群设置] ❌无此AI设置");
                }

                settingService.set(setting);
                bot.sendGroupMsg(groupId, """
                        [AI] ✅设置已更新
                        %s""".formatted(msg), false);
                log.info("├─[GroupSet] 已更改群 {} AI设置 - {}", groupId, msg);
                return;
            }

            if ("-monitor".equals(option)) {
                if (params.size() < 2) throw new NullBotException("[群设置] ❌Monitor设置参数不足");
                String name = params.get(1);
                boolean enabled = switch (name) {
                    case "img" -> setting.switchImageCollect();
                    case "msg" -> setting.switchMessageCollect();
                    case "key" -> setting.switchKeywordDetect();
                    case "pok" -> setting.switchPokeDetect();
                    case "rcl" -> setting.switchRecallDetect();
                    default -> throw new NullBotException("[群设置] ❌无此Monitor设置");
                };
                settingService.set(setting);
                bot.sendGroupMsg(event.getGroupId(), "[监听] ✅已切换: %s".formatted(enabled ? "ON" : "OFF"), false);
                log.info("├─[GroupSet] 已更改群 {} 监听设置 - {} -> {}", groupId, name, enabled ? "ON" : "OFF");
                return;
            }

            if ("-guess".equals(option)) {
                if (params.size() < 4) throw new NullBotException("[群设置] ❌Guess设置参数不足");
                double cropRatio = Double.parseDouble(params.get(1));
                double transparentRatio = Double.parseDouble(params.get(2));
                int padding = Integer.parseInt(params.get(3));
                setting.setGuessCropRatio(cropRatio);
                setting.setGuessTransparentRatio(transparentRatio);
                setting.setGuessPadding(padding);
                settingService.set(setting);
                bot.sendGroupMsg(groupId, "[猜角色] ✅参数已更新", false);
                log.info("├─[GroupSet] 已更改群 {} Guess参数 -> {} {} {}", groupId, cropRatio, transparentRatio, padding);
                return;
            }

            throw new NullBotException("[群设置] ❌无此操作类型");
        } catch (NumberFormatException e) {
            throw new NullBotException("[群设置] ❌参数格式错误");
        }
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
                   stg - 对话策略
                   ati - 防注模式
                   tkn - 思考模式
                   voi - 语音模式
                   ica - 内令鉴权
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
