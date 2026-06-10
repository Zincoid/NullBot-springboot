package com.zincoid.nullbot.core.model.bot.event;

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
public class CommandEvent<T extends Event> {

    private final T event;
    private final String commandType;
    private final List<String> commandParameters;
    private final boolean authRequired;
    private final boolean rateLimit;

    private final EventScope eventScope;
    private final Long userId;
    private final Long groupId;

    // =================== 全参工厂方法 ===================

    public static <T extends Event> CommandEvent<T> of(T event, String commandType,
            List<String> commandParameters, boolean authRequired, boolean rateLimit) {
        return CommandEvent.of(event, commandType, commandParameters, authRequired, rateLimit,
                BotCtx.getScope(), BotCtx.getUserId(), BotCtx.getGroupId());
    }

    // =================== 便捷工厂方法 ===================

    public static CommandEvent<GroupMessageEvent> of(GroupMessageEvent event) {
        int i = event.getArrayMsg().getFirst().getType() == MsgTypeEnum.reply ? 1 : 0;
        String command = event.getArrayMsg().get(i).getStringData("text").substring(1);
        List<String> information = List.of(command.split(" "));
        return CommandEvent.of(event, information.getFirst(), information.subList(1, information.size()), true, true);
    }

    public static CommandEvent<PrivateMessageEvent> of(PrivateMessageEvent event) {
        String command = event.getMessage().substring(1);
        List<String> information = List.of(command.split(" "));
        return CommandEvent.of(event, information.getFirst(), information.subList(1, information.size()), true, true);
    }

    public static CommandEvent<PokeNoticeEvent> of(PokeNoticeEvent event) {
        return CommandEvent.of(event, "PokeReact", List.of(), true, true);
    }

    public static CommandEvent<GroupMsgDeleteNoticeEvent> of(GroupMsgDeleteNoticeEvent event) {
        return CommandEvent.of(event, "RecallReact", List.of(), false, false);
    }
}
