package com.zincoid.nullbot.bot.command.manage;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.Cmd;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.exception.BotWarnException;
import com.zincoid.nullbot.core.module.ai.chat.client.impl.QQChatClient;
import lombok.extern.slf4j.Slf4j;
import com.zincoid.nullbot.core.annotation.CmdMapping;
import com.zincoid.nullbot.core.module.control.CmdRateLimiter;
import com.zincoid.nullbot.core.model.data.po.SettingPO;
import com.zincoid.nullbot.core.enums.ChatScope;
import com.zincoid.nullbot.core.enums.ChatStrategy;
import com.zincoid.nullbot.core.enums.LimitScope;
import com.zincoid.nullbot.core.service.system.SettingService;
import com.zincoid.nullbot.core.context.BotCtx;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@CmdMapping({"GroupSet", "群设置"})
@Component
public class GroupSetCmd implements Cmd {

    private final QQChatClient qqChatClient;
    private final SettingService settingService;
    private final CmdRateLimiter cmdRateLimiter;

    public GroupSetCmd(@Lazy QQChatClient qqChatClient, SettingService settingService, CmdRateLimiter cmdRateLimiter) {
        this.qqChatClient = qqChatClient;
        this.settingService = settingService;
        this.cmdRateLimiter = cmdRateLimiter;
    }

    @Override
    public void run(Bot bot, GroupMessageEvent event, CmdArgs args) {
        Long groupId = event.getGroupId();
        String option = args.nextString();
        SettingPO setting = BotCtx.getSetting();

        if ("-view".equals(option)) {
            bot.sendGroupMsg(groupId, setting.toString(), false);
            log.info("☑ [GroupSet] 群设置已获取 - GroupId: {}", groupId);
            return;
        }

        if ("-limit".equals(option)) {
            String name = args.nextString();
            String msg;
            switch (name) {
                case "scp" -> {
                    LimitScope newLimitScope = setting.switchLimitScope();
                    msg = "限速范围 -> %s".formatted(newLimitScope);
                }
                case "cap" -> {
                    int capacity = args.nextInt();
                    setting.setLimitCapacity(capacity);
                    msg = "限速容量 -> %s".formatted(capacity);
                }
                case "ref" -> {
                    int refill = args.nextInt();
                    setting.setLimitRefill(refill);
                    msg = "补充数量 -> %s".formatted(refill);
                }
                case "itv" -> {
                    int interval = args.nextInt();
                    setting.setLimitInterval(interval);
                    msg = "补充间隔 -> %s".formatted(interval);
                }
                default -> throw new BotWarnException("无此Limit设置");
            }

            settingService.set(setting);
            cmdRateLimiter.reset(groupId);
            bot.sendGroupMsg(groupId, "✅限速已更新: %s".formatted(msg), false);
            log.info("☑ [GroupSet] 限速设置已更新 - GroupId: {}, {}", groupId, msg);
            return;
        }

        if ("-ai".equals(option)) {
            String name = args.nextString();
            String msg;

            switch (name) {
                case "scp" -> {
                    ChatScope newScope = setting.switchChatScope();
                    msg = "会话范围 -> %s".formatted(newScope);
                }
                case "stg" -> {
                    qqChatClient.clear(BotCtx.getChatId());
                    ChatStrategy strategy = setting.switchChatStrategy();
                    msg = "对话策略 -> %s".formatted(strategy);
                }
                case "frq" -> {
                    double freq = args.nextDouble();
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
                    qqChatClient.clear(BotCtx.getChatId());
                    boolean enabled = setting.switchCustom();
                    msg = "允许自定 -> %s".formatted(enabled ? "ON" : "OFF");
                }
                case "aur" -> {
                    boolean enabled = setting.switchAutoReply();
                    msg = "自动发言 -> %s".formatted(enabled ? "ON" : "OFF");
                }
                default -> throw new BotWarnException("无此AI设置");
            }

            settingService.set(setting);
            bot.sendGroupMsg(groupId, "✅AI已更新: %s".formatted(msg), false);
            log.info("☑ [GroupSet] AI设置已更新 - GroupId: {}, {}", groupId, msg);
            return;
        }

        if ("-monitor".equals(option)) {
            String name = args.nextString();
            boolean enabled = switch (name) {
                case "img" -> setting.switchImageCollect();
                case "msg" -> setting.switchMessageCollect();
                case "key" -> setting.switchKeywordDetect();
                case "pok" -> setting.switchPokeDetect();
                case "rcl" -> setting.switchRecallDetect();
                default -> throw new BotWarnException("无此Monitor设置");
            };
            settingService.set(setting);
            bot.sendGroupMsg(event.getGroupId(), "✅监听已切换: %s".formatted(enabled ? "ON" : "OFF"), false);
            log.info("☑ [GroupSet] 监听设置已切换 - GroupId: {}, {} -> {}", groupId, name, enabled);
            return;
        }

        if ("-guess".equals(option)) {
            double cropRatio = args.nextDouble();
            double transparentRatio = args.nextDouble();
            int padding = args.nextInt();
            setting.setGuessCropRatio(cropRatio);
            setting.setGuessTransparentRatio(transparentRatio);
            setting.setGuessPadding(padding);
            settingService.set(setting);
            bot.sendGroupMsg(groupId, "✅猜图参数已更新", false);
            log.info("☑ [GroupSet] 猜图参数已更新 - GroupId: {}, Params: {} {} {}", groupId, cropRatio, transparentRatio, padding);
            return;
        }

        throw new BotWarnException("无此操作");
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
                   tkn - 思考模式
                   voi - 语音模式
                   ati - 注入保护
                   ica - 内令鉴权
                   cus - 允许自定
                   aur - 自主发言
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
                - 切换AI语音/策略/自定时会清空历史
                
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
