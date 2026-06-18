package com.zincoid.nullbot.core.module.game.runtime;

import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.enums.BniMode;
import com.zincoid.nullbot.core.module.control.BotInputManager;
import com.zincoid.nullbot.core.module.game.framework.Handler;
import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.model.Player;
import com.zincoid.nullbot.core.module.system.BotOperator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class InputListener {

    private static final long INPUT_TIMEOUT_SECONDS = 300;

    private final BotInputManager botInputManager;
    private final BotOperator botOperator;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @PostConstruct
    public void init() {
        log.info("▽ [InputListener] 游戏监听器已启动");
    }

    @PreDestroy
    public void destroy() {
        if (!executor.isShutdown()) executor.shutdownNow();
        log.info("▽ [InputListener] 游戏监听器已关闭");
    }

    public void listen(Match match, Handler<?, ?, ?, ?> handler) {
        for (Player p : match.getPlayers())
            executor.submit(() -> loop(match, handler, p.getId()));
    }

    private void loop(Match match, Handler<?, ?, ?, ?> handler, Long playerId) {
        String matchId = match.getId();
        log.info("▽ [InputListener] 监听开始 - MID: {}, PID: {}", matchId, playerId);
        try {
            while (handler.isActive(matchId)) {
                List<Pair<Long, String>> inputs = botInputManager
                        .request(BniMode.PS, playerId, handler.getPattern(), INPUT_TIMEOUT_SECONDS);
                if (inputs.isEmpty() || !handler.isActive(matchId)) break;
                String input = inputs.getFirst().getValue().trim();
                log.info("[InputListener] 监听输入 - MID: {}, PID: {}, Input: {}", matchId, playerId, input);
                CmdArgs args = CmdArgs.of(List.of(input.split("\\s+")));
                handler.act(playerId, args).send(botOperator.getBot());
            }
        } catch (Exception e) {
            log.error("▽ [InputListener] 监听异常 - MID: {}, PID: {}", matchId, playerId, e);
        }
        log.info("▽ [InputListener] 监听结束 - MID: {}, PID: {}", matchId, playerId);
    }
}
