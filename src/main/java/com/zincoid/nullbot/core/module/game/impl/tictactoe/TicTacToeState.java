package com.zincoid.nullbot.core.module.game.impl.tictactoe;

import com.zincoid.nullbot.core.module.game.framework.State;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicTacToeState extends State {

    private char[][] board = {
        {'.', '.', '.'},
        {'.', '.', '.'},
        {'.', '.', '.'}
    };

    private Long X;
    private Long O;
    private Long win;

    private char current = 'X';
    private boolean finished;

    @Override
    public boolean isFinished() {
        return finished;
    }
}
