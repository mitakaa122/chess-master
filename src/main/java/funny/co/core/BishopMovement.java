package funny.co.core;

import funny.co.model.ChessSquare;
import funny.co.model.Chessboard;
import funny.co.model.Position;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BishopMovement extends AbstractPieceMovement {
    @Override
    public List<Position> find(Chessboard chessboard, ChessSquare square) {
        return Stream.of(
                versatile(chessboard.getSquares(), square, Position.of(1, 1)),
                versatile(chessboard.getSquares(), square, Position.of(-1, 1)),
                versatile(chessboard.getSquares(), square, Position.of(-1, -1)),
                versatile(chessboard.getSquares(), square, Position.of(1, -1)))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
