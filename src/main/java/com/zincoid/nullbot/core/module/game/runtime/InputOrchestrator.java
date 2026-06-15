package com.zincoid.nullbot.core.module.game.runtime;

import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.enums.BniMode;
import com.zincoid.nullbot.core.module.control.BotInputManager;
import com.zincoid.nullbot.core.module.game.framework.GameHandler;
import com.zincoid.nullbot.core.module.game.model.Match;
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
public class InputOrchestrator {

    private static final long INPUT_TIMEOUT_SECONDS = 300;

    private final BotInputManager botInputManager;
    private final BotOperator botOperator;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @PostConstruct
    public void init() {
        log.info("▽ [InputOrchestrator] 游戏监听器已启动");
    }

    @PreDestroy
    public void destroy() {
        if (!executor.isShutdown()) executor.shutdownNow();
        log.info("▽ [InputOrchestrator] 游戏监听器已关闭");
    }

    public void listen(Match match, GameHandler<?, ?, ?> handler) {
        executor.submit(() -> loop(match, handler, match.getP1().getId()));
        executor.submit(() -> loop(match, handler, match.getP2().getId()));
    }

    private void loop(Match match, GameHandler<?, ?, ?> handler, Long playerId) {
        log.info("▽ [InputOrchestrator] 游戏监听开始 - PlayerID: {}, MatchID: {}, Type: {}", playerId, match.getId(), match.getType());
        try {
            while (handler.isActive(match.getId())) {
                List<Pair<Long, String>> inputs = botInputManager.request(BniMode.PS, playerId, handler.getPattern(), INPUT_TIMEOUT_SECONDS);
                if (inputs.isEmpty() || !handler.isActive(match.getId())) break;
                String input = inputs.getFirst().getValue().trim();
                log.info("[InputOrchestrator] 游戏监听输入 - PlayerID: {}, MatchID: {}, Input: {}", playerId, match.getId(), input);
                CmdArgs args = CmdArgs.of(List.of(input.split("\\s+")));
                handler.act(playerId, args).send(botOperator.getBot());
            }
        } catch (Exception e) {
            log.error("▽ [InputOrchestrator] 游戏监听异常 - PlayerID: {}, MatchID: {}", playerId, match.getId(), e);
        }
        log.info("▽ [InputOrchestrator] 游戏监听结束 - PlayerID: {}, MatchID: {}", playerId, match.getId());
    }
}
