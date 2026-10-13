package funny.co.design;

import funny.co.model.ChessSquareState;

public interface CareTaker {
    void hitSave(ChessSquareState state);

    ChessSquareState hitUndo();
}
