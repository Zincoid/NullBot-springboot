package com.zincoid.nullbot.core.model.bot.event;

import com.mikuac.shiro.dto.event.Event;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CommandEvent<T extends Event> {

    private T event;
    private String commandType;
    private List<String> commandParameters;
    private boolean authRequired = true;
    private boolean rateLimit = true;

    // =================== 构造方法 ===================

    // 基础 创建事件 (根据事件类型自动获取指令名和参数)
    public CommandEvent(T event) {
        this.event = event;
        if (event instanceof GroupMessageEvent _event)
            parseGroupMessageEvent(_event);
        else if (event instanceof PokeNoticeEvent)
            parsePokeNoticeEvent();
        else if (event instanceof GroupMsgDeleteNoticeEvent)
            parseGroupMsgDeleteNoticeEvent();
        else if (event instanceof PrivateMessageEvent _event)
            parsePrivateMessageEvent(_event);
    }

    // 自定 创建事件 (嵌入调用 关键词等使用) (可优化?)
    public CommandEvent(T event, String command, boolean authRequired, boolean rateLimit) {
        this.event = event;
        this.authRequired = authRequired;
        this.rateLimit = rateLimit;
        List<String> information = List.of(command.split(" "));
        commandType = information.getFirst();
        commandParameters = information.subList(1, information.size());
    }

    // =================== 工具方法 ===================

    private void parseGroupMessageEvent(GroupMessageEvent event) {
        int i = event.getArrayMsg().getFirst().getType() == MsgTypeEnum.reply ? 1 : 0;
        String command = event.getArrayMsg().get(i).getData().get("text").asString().substring(1);
        List<String> information = List.of(command.split(" "));
        commandType = information.getFirst();
        commandParameters = information.subList(1, information.size());
    }

    private void parsePrivateMessageEvent(PrivateMessageEvent event) {
        String command = event.getMessage().substring(1);
        List<String> information = List.of(command.split(" "));
        commandType = information.getFirst();
        commandParameters = information.subList(1, information.size());
    }

    private void parsePokeNoticeEvent() {
        commandType = "PokeReact";
        commandParameters = List.of();
    }

    private void parseGroupMsgDeleteNoticeEvent() {
        authRequired = false;
        rateLimit = false;
        commandType = "RecallReact";
        commandParameters = List.of();
    }

    @Override
    public String toString() {
        return "CommandEvent{" +
                ", commandType='" + commandType + '\'' +
                ", commandParameters=" + commandParameters +
                ", event=" + event +
                '}';
    }
}
