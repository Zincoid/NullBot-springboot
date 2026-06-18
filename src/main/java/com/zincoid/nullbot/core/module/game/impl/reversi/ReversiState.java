package com.zincoid.nullbot.core.module.game.impl.reversi;

import com.zincoid.nullbot.core.module.game.framework.State;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Arrays;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReversiState extends State {

    private char[][] board = new char[8][8];

    private Long black;
    private Long white;
    private Long winner;

    private char current;
    private boolean finished;

    public ReversiState() {
        for (char[] row : board) Arrays.fill(row, '.');
        board[3][3] = 'W';
        board[3][4] = 'B';
        board[4][3] = 'B';
        board[4][4] = 'W';
        current = 'B';
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
