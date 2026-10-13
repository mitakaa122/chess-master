package funny.co.core;

import funny.co.ui.ChessboardPane;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChessGame {
    private static final Logger log = Logger.getLogger(ChessGame.class.getName());
    private final String gameMatch = DigestUtils.sha1Hex(String.valueOf(System.currentTimeMillis()));
    private int move;
    private List<String> moves;
    private ChessboardPane board;

    public void play() {
        log.log(Level.INFO, "game started {0}", gameMatch);
        boolean whiteMoves = move % 2 == 0;
        log.log(Level.INFO, "move #{0}, {1} moves", whiteMoves ? "white" : "black");
        board.getSquares().values().stream()
                .filter(square -> square.getPiece() != null)
                .filter(square -> whiteMoves == square.getPiece().isWhite())
                .forEach(square -> square.setEnable(true));
    }

}
