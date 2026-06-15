package com.zincoid.nullbot.core.module.game.impl.tictactoe;

import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.module.game.framework.GameHandler;
import com.zincoid.nullbot.core.module.game.runtime.MatchManager;
import com.zincoid.nullbot.core.module.game.runtime.PlayerManager;
import com.zincoid.nullbot.core.model.result.GameResult;
import com.zincoid.nullbot.core.module.game.model.Match;
import com.zincoid.nullbot.core.module.game.model.Player;
import com.zincoid.nullbot.core.module.system.BotOperator;
import com.zincoid.nullbot.core.service.base.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class TicTacToeHandler extends GameHandler<TicTacToeState, TicTacToeLogic, TicTacToeRenderer> {

    private final UserService userService;

    public TicTacToeHandler(
            BotOperator botContainer,
            MatchManager matchManager,
            PlayerManager playerManager,
            UserService userService,
            TicTacToeLogic gameLogic,
            TicTacToeRenderer renderer
    ) {
        super(botContainer, matchManager, playerManager, gameLogic, renderer);
        this.userService = userService;
    }

    @Override
    public String getType() {
        return "井字棋";
    }

    @Override
    public String getPattern() {
        return "\\d+ \\d+";
    }

    @Override
    public void onStart(Match match, TicTacToeState state) {
        Player p1 = match.getP1();
        Player p2 = match.getP2();
        String message = renderer.render(state);
        if (!Objects.equals(p1.getInProgressGroupId(), p2.getInProgressGroupId()))
            botOperator.sendGroupMsg(p1.getInProgressGroupId(), message);
        botOperator.sendGroupMsg(p2.getInProgressGroupId(), message);
    }

    @Override
    public void onEnd(Match match, TicTacToeState state) {
        if (state.getWin() != null) {
            userService.plusExperience(state.getWin(), 100);
            userService.increaseDrawTimes(state.getWin(), 30);
        } else {
            userService.plusExperience(state.getO(), 50);
            userService.plusExperience(state.getX(), 50);
            userService.increaseDrawTimes(state.getO(), 15);
            userService.increaseDrawTimes(state.getX(), 15);
        }
    }

    @Override
    public GameResult onAction(TicTacToeState state, Player self, Player opp, CmdArgs args) {
        int r = args.nextInt() - 1;
        int c = args.nextInt() - 1;
        Character symbol = symbolOf(state, self);
        if (symbol == null) return fail("非对局玩家");
        if (state.getCurrent() != symbol) return fail("没轮到你");
        if (!gameLogic.place(state, r, c)) return fail("非法落子");
        log.info("☑ [TicTacToe] 玩家 {} 落子 [{}, {}]", self.getId(), r + 1, c + 1);
        String board = renderer.render(state);
        Character winner = gameLogic.check(state);
        if (winner != null) {
            state.setFinished(true);
            state.setWin(winner == 'X' ? state.getX() : state.getO());
            return finish(false, board + renderer.resultMessage(winner), null);
        }
        if (gameLogic.draw(state)) {
            state.setFinished(true);
            return finish(false, board + renderer.drawMessage(), null);
        }
        return success(false, board, null);
    }

    private Character symbolOf(TicTacToeState state, Player self) {
        if (self.getId().equals(state.getX())) return 'X';
        if (self.getId().equals(state.getO())) return 'O';
        return null;
    }
}
