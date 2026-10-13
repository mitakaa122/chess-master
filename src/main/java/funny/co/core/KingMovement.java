package funny.co.core;

import funny.co.model.ChessSquare;
import funny.co.model.Chessboard;
import funny.co.model.PieceType;
import funny.co.model.Position;
import funny.co.ui.ChessboardBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KingMovement extends AbstractPieceMovement {
    private static final Logger log = Logger.getLogger(KingMovement.class.getName());
    private final List<Position> dirs = List.of(
            Position.of(-1, 0),
            Position.of(0, 1),
            Position.of(1, 0),
            Position.of(0, -1),
            Position.of(1, 1),
            Position.of(-1, 1),
            Position.of(-1, -1),
            Position.of(1, -1)
    );

    @Override
    public List<Position> find(Chessboard chessboard, ChessSquare square) {
        var squares = chessboard.getSquares();
        List<Position> possible = new ArrayList<>(8);
        var current = square.getPosition();
        for (Position dir : dirs) {
            var pos = current.add(dir);
            if (!pos.validPosition()) {
                continue;
            }
            var cs = squares.get(pos);
            if (cs.isFree()) {
                append(possible, pos);
            } else {
                if (cs.isEnemyPiece(square.getPiece().isWhite())) {
                    append(possible, pos);
                }
            }
        }
        // check if castling is possible
        if (square.getPiece().noMoves()) {
            isCastling(square, chessboard, possible);
        }

        return possible;
    }

    private void isCastling(ChessSquare square, Chessboard chessboard, List<Position> possible) {
        var squares = chessboard.getSquares();
        boolean white = square.getPiece().isWhite();
        int rank = white ? 0 : 7;

        var leftRock = squares.get(Position.of(rank, 0));
        var rightRock = squares.get(Position.of(rank, 7));
        var pos = square.getPosition();

        if (!leftRock.pieceIsPresent() || !rightRock.pieceIsPresent()) {
            return;
        }

        // make sure that king is not under check
        if (square.getBackground() == ChessboardBuilder.check) {
            log.log(Level.INFO,
                    String.format("castling not possible for %s king, because of the check",
                            white ? "white" : "black"));
            return;
        }
        if (leftRock.getPiece().noMoves()) {
            iterate(square, chessboard, pos, possible, leftRock, -1);
        }
        if (rightRock.getPiece().noMoves()) {
            iterate(square, chessboard, pos, possible, rightRock, 1);
        }
    }

    private void iterate(ChessSquare king,
                         Chessboard chessboard,
                         Position pos,
                         List<Position> possible,
                         ChessSquare rock,
                         int dir) {
        var squares = chessboard.getSquares();
        pos = pos.add(Position.of(0, dir));
        while (pos.validPosition()) {
            var sq = squares.get(pos);
            if (sq == null || !sq.isFree() && sq.getPiece().getType() != PieceType.ROCK) {
                return;
            }
            if (underCheck(chessboard, king, pos)) {
                return;
            }
            pos = pos.add(Position.of(0, dir));
        }
        possible.add(rock.getPosition().sub(Position.of(0, dir)));
    }

    @Override
    public boolean canMove(ChessSquare square, Chessboard chessboard, Position position) {
        if (!allMoves(square, chessboard).contains(position)) {
            return false;
        }
        return !underCheck(chessboard, square, position);
    }

    @Override
    public void move(ChessSquare square, Chessboard chessboard, Position position) {
        var squares = chessboard.getSquares();
        var piece = square.getPiece();
        boolean white = square.getPiece().isWhite();
        if (piece.noMoves() && isCastling(position, white)) {
            boolean left = position.getCol() == 1;
            position = position.add(Position.of(0, left ? -1 : 1));
            // TODO check the rock is not promoted from a pawn
            var rock = squares.get(position);
            var kingPos = square.getPosition();
//            var king = squares.get(kingPos);

            if (left) {
                // moves king 2 squares
                var newKingPlace = squares.get(Position.of(kingPos.getRow(), kingPos.getCol() - 2));
//                newKingPlace.add(king.getPiece());
//                square = newKingPlace;

                var newKingPlacePos = newKingPlace.getPosition();
                position = newKingPlacePos;
                var newRockPlace = squares.get(Position.of(newKingPlacePos.getRow(), newKingPlacePos.getCol() + 1));
                newRockPlace.add(rock.getPiece());
            } else {
                var newKingPlace = squares.get(Position.of(kingPos.getRow(), kingPos.getCol() + 2));
//                newKingPlace.add(king.getPiece());
//                square = newKingPlace;

                var newKingPlacePos = newKingPlace.getPosition();
                position = newKingPlacePos;

                var newRockPlace = squares.get(Position.of(newKingPlacePos.getRow(), newKingPlacePos.getCol() - 1));
                newRockPlace.add(rock.getPiece());
            }
            rock.remove();
        }
        super.move(square, chessboard, position);
    }

    private boolean isCastling(Position position, boolean white) {
        int rank = white ? 0 : 7;
        return position.getRow() == rank && (position.getCol() == 1 || position.getCol() == 6);
    }
}
