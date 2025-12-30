package org.bot.nullbot.entity;

import com.mikuac.shiro.dto.event.Event;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import com.mikuac.shiro.dto.event.notice.PokeNoticeEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class CommandEvent<T extends Event>
{
    private String commandType;
    private List<String> commandParameters;
    private T event;
    private boolean authRequired = true;

    public CommandEvent(T event) {  // 基础 创建事件
        this.event = event;
        if(event instanceof GroupMessageEvent groupMessageEvent)
            parseGroupMessageEvent(groupMessageEvent, groupMessageEvent.getArrayMsg().getFirst().getType() == MsgTypeEnum.reply ? 1 : 0);
        else if(event instanceof PokeNoticeEvent)
            parseGroupPokeNoticeEvent();
        else if (event instanceof GroupMsgDeleteNoticeEvent)
            parseGroupMsgDeleteNoticeEvent();
    }

    public CommandEvent(String commandType, T event) {  // AT触发聊天 创建事件
        this.event = event;
        this.commandType = commandType;
        commandParameters = new ArrayList<>();
    }

    public CommandEvent(T event, String command, boolean authRequired) {  // 嵌入调用指令 创建事件
        this.event = event;
        this.authRequired = authRequired;
        List<String> information = List.of(command.split(" "));
        commandType = information.getFirst();
        commandParameters = information.subList(1, information.size());
    }

    private void parseGroupMessageEvent(GroupMessageEvent event, int i)
    {
        String command = event.getArrayMsg().get(i).getData().get("text").substring(1);
        List<String> information = List.of(command.split(" "));
        commandType = information.getFirst();
        commandParameters = information.subList(1, information.size());
    }

    private void parseGroupPokeNoticeEvent() {
        commandType = "PokeReact";
        commandParameters = new ArrayList<>();
    }

    private void parseGroupMsgDeleteNoticeEvent() {
        commandType = "RecallReact";
        commandParameters = new ArrayList<>();
    }

    @Override
    public String toString()
    {
        return "CommandEvent{" +
                ", commandType='" + commandType + '\'' +
                ", commandParameters=" + commandParameters +
                ", event=" + event +
                '}';
    }
}
