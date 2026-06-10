package com.zincoid.nullbot.core.model.bot.event;

import com.mikuac.shiro.dto.event.Event;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.zincoid.nullbot.core.enums.EventScope;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor(staticName = "of")
public class CommandEvent<T extends Event> {

    private final T event;
    private final String commandType;
    private final List<String> commandParameters;
    private final boolean authRequired;
    private final boolean rateLimit;

    // =================== 静态工厂方法 ===================

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

    // =================== 信息获取方法 ===================

    public EventScope getEventScope() {
        if (event instanceof GroupMessageEvent) return EventScope.GROUP;
        if (event instanceof PokeNoticeEvent e) return e.getGroupId() == null ? EventScope.PRIVATE : EventScope.GROUP;
        if (event instanceof GroupMsgDeleteNoticeEvent) return EventScope.GROUP;
        if (event instanceof PrivateMessageEvent) return EventScope.PRIVATE;
        return EventScope.UNKNOWN;
    }

    public Long getGroupId() {
        if (event instanceof GroupMessageEvent e) return e.getGroupId();
        if (event instanceof PokeNoticeEvent e) return e.getGroupId() == null ? 0L : e.getGroupId();
        if (event instanceof GroupMsgDeleteNoticeEvent e) return e.getGroupId();
        if (event instanceof PrivateMessageEvent) return 0L;
        throw new IllegalArgumentException("未知事件类型");
    }

    public Long getUserId() {
        if (event instanceof GroupMessageEvent e) return e.getUserId();
        if (event instanceof PokeNoticeEvent e) return e.getUserId();
        if (event instanceof GroupMsgDeleteNoticeEvent e) return e.getUserId();
        if (event instanceof PrivateMessageEvent e) return e.getUserId();
        throw new IllegalArgumentException("未知事件类型");
    }
}
