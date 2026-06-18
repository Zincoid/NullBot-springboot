package com.zincoid.nullbot.core.module.game.impl.reversi;

import com.zincoid.nullbot.bot.command.CmdArgs;
import com.zincoid.nullbot.core.module.game.framework.handler.DualHandler;
import com.zincoid.nullbot.core.module.game.runtime.MatchManager;
import com.zincoid.nullbot.core.module.game.runtime.PlayerManager;
import com.zincoid.nullbot.core.module.game.model.match.DualMatch;
import com.zincoid.nullbot.core.module.game.model.Result;
import com.zincoid.nullbot.core.module.game.model.Player;
import com.zincoid.nullbot.core.module.system.BotOperator;
import com.zincoid.nullbot.core.service.base.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
public class ReversiHandler extends DualHandler<ReversiState, ReversiLogic, ReversiRenderer> {

    private final UserService userService;

    public ReversiHandler(
            ReversiLogic logic,
            ReversiRenderer renderer,
            BotOperator botContainer,
            MatchManager matchManager,
            PlayerManager playerManager,
            UserService userService
    ) {
        super(logic, renderer, botContainer, matchManager, playerManager);
        this.userService = userService;
    }

    @Override
    public String getType() {
        return "黑白棋";
    }

    @Override
    public String getPattern() {
        return "^[A-Ha-h][1-8]$";
    }

    @Override
    public void onStart(DualMatch match, ReversiState state) {
        Player p1 = match.getP1();
        Player p2 = match.getP2();
        String message = renderer.render(state);
        if (!Objects.equals(p1.getInProgressGroupId(), p2.getInProgressGroupId()))
            botOperator.sendGroupMsg(p1.getInProgressGroupId(), message);
        botOperator.sendGroupMsg(p2.getInProgressGroupId(), message);
    }

    @Override
    public void onEnd(DualMatch match, ReversiState state) {
        if (state.getWinner() != null) {
            userService.plusExperience(state.getWinner(), 200);
            userService.increaseDrawTimes(state.getWinner(), 50);
        } else {
            userService.plusExperience(state.getBlack(), 100);
            userService.plusExperience(state.getWhite(), 100);
            userService.increaseDrawTimes(state.getBlack(), 25);
            userService.increaseDrawTimes(state.getWhite(), 25);
        }
    }

    @Override
    public Result onAction(DualMatch match, ReversiState state, Player self, CmdArgs args) {
        String pos = args.next().toUpperCase();
        if (!pos.matches("^[A-H][1-8]$")) return fail("坐标错误 范围: A1~H8");
        Character sym = symbolOf(state, self);
        if (sym == null) return fail("非对局玩家");
        if (state.getCurrent() != sym) return fail("没轮到你");
        if (state.isFinished()) return fail("对局已结束");
        int col = pos.charAt(0) - 'A';
        int row = pos.charAt(1) - '1';
        if (!logic.place(state, row, col)) return fail("非法落子");
        log.info("☑ [Reversi] 玩家 {} 落子 [{}]", self.getId(), pos);
        String board = renderer.render(state);
        if (logic.noMoves(state, 'B') && logic.noMoves(state, 'W'))
            return finishGame(state, board);
        return success(false, board, null);
    }

    private Result finishGame(ReversiState state, String board) {
        state.setFinished(true);
        int b = 0, w = 0;
        for (char[] row : state.getBoard())
            for (char c : row)
                if (c == 'B') b++;
                else if (c == 'W') w++;
        if (b > w) state.setWinner(state.getBlack());
        else if (w > b) state.setWinner(state.getWhite());
        return finish(false, board + renderer.resultMessage(b, w, state.getBlack(), state.getWhite()), null);
    }

    private Character symbolOf(ReversiState state, Player self) {
        if (self.getId().equals(state.getBlack())) return 'B';
        if (self.getId().equals(state.getWhite())) return 'W';
        return null;
    }
}
