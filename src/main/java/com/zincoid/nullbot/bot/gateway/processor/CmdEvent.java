package com.zincoid.nullbot.bot.gateway.processor;

import com.mikuac.shiro.dto.event.Event;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.zincoid.nullbot.core.context.BotCtx;
import com.zincoid.nullbot.core.enums.EventScope;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
public class CmdEvent<T extends Event> {

    private final T event;
    private final String cmdType;
    private final List<String> cmdParams;
    private final boolean authRequired;
    private final boolean rateLimit;

    private final EventScope eventScope;
    private final Long userId;
    private final Long groupId;

    // =================== 全参指令工厂方法 ===================

    public static <T extends Event> CmdEvent<T> of(T event, String cmdType, List<String> cmdParams, boolean authRequired, boolean rateLimit) {
        return of(event, cmdType, cmdParams, authRequired, rateLimit, BotCtx.getScope(), BotCtx.getUserId(), BotCtx.getGroupId());
    }

    // =================== 便捷指令工厂方法 ===================

    public static CmdEvent<GroupMessageEvent> of(GroupMessageEvent event) {
        int i = event.getArrayMsg().getFirst().getType() == MsgTypeEnum.reply ? 1 : 0;
        String cmd = event.getArrayMsg().get(i).getStringData("text").substring(1);
        List<String> information = List.of(cmd.split("\\s+"));
        return of(event, information.getFirst(), information.subList(1, information.size()), true, true);
    }

    public static CmdEvent<PrivateMessageEvent> of(PrivateMessageEvent event) {
        String cmd = event.getMessage().substring(1);
        List<String> information = List.of(cmd.split("\\s+"));
        return of(event, information.getFirst(), information.subList(1, information.size()), true, true);
    }

    public static CmdEvent<PokeNoticeEvent> of(PokeNoticeEvent event) {
        return of(event, "PokeReact", List.of(), true, true);
    }

    public static CmdEvent<GroupMsgDeleteNoticeEvent> of(GroupMsgDeleteNoticeEvent event) {
        return of(event, "RecallReact", List.of(), false, false);
    }

    // ================== 内部指令工厂方法 ===================

    public static CmdEvent<?> of(String cmd, boolean authRequired) {
        List<String> information = List.of(cmd.split("\\s+"));
        return of(BotCtx.getEvent(), information.getFirst(), information.subList(1, information.size()), authRequired, false);
    }

    public static CmdEvent<?> of(String cmd) {
        return of(cmd, BotCtx.getScope() == EventScope.GROUP && BotCtx.getSetting().isInnerCmdAuth());
    }
}
