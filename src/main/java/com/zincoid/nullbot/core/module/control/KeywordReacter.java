package com.zincoid.nullbot.core.module.control;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.bot.gateway.processor.CmdEvent;
import com.zincoid.nullbot.bot.gateway.processor.CmdProcessor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordReacter {

    private final CmdProcessor cmdProcessor;
    private final Map<String, Reaction> handlers = new LinkedHashMap<>();

    private record Reaction(String cmdType, CmdArgs args) {}

    @PostConstruct
    public void init() {
        register("男娘", "Reply 哪有男娘？");
        register("受着", "UserBan {userId} 1");
        // register("男娘", "Reply", List.of("哪有男娘？"));
        // register("受着", "UserBan", List.of("{userId}", "1"));
        log.info("▽ [KeywordReacter] 默认关键字已注册 - {}", handlers.keySet());
    }

    public void register(String keyword, String cmdType, List<String> args) {
        handlers.put(keyword, new Reaction(cmdType, CmdArgs.of(args)));
    }

    public void register(String keyword, String cmd) {
        List<String> split = List.of(cmd.split("\\s+"));
        register(keyword, split.getFirst(), split.subList(1, split.size()));
    }

    public boolean unregister(String keyword) {
        return handlers.remove(keyword) != null;
    }

    public boolean react(Bot bot, GroupMessageEvent event) throws Exception {
        int reacted = 0;
        for (Map.Entry<String, Reaction> entry : handlers.entrySet()) {
            if (!event.getMessage().contains(entry.getKey())) continue;
            Reaction reaction = entry.getValue();
            log.info("▽ [KeywordReacter] 群聊 {} - {}({}) -> \"{}\"", event.getGroupId(),
                    event.getSender().getNickname(), event.getUserId(), entry.getKey());
            CmdArgs resolved = reaction.args.resolve(Map.of(
                    "userId", event.getUserId().toString(),
                    "groupId", event.getGroupId().toString()
            ));
            cmdProcessor.processQQ(bot, CmdEvent.of(event, reaction.cmdType, resolved.getRaw(),
                    false, false));
            reacted++;
        }
        return reacted > 0;
    }
}
