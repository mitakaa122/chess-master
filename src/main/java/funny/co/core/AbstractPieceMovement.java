package funny.co.core;

import funny.co.model.ChessSquare;
import funny.co.model.Chessboard;
import funny.co.model.PieceType;
import funny.co.model.Position;
import funny.co.ui.ChessboardBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class AbstractPieceMovement implements PieceMovement {
    private static final Logger log = Logger.getLogger(AbstractPieceMovement.class.getName());
    public List<Position> allMoves(ChessSquare square, Chessboard chessboard) {
        Position position = square.getPosition();
        if (!position.validPosition()) {
            return Collections.emptyList();
        }
        return find(chessboard, square);
    }

    public abstract List<Position> find(Chessboard chessboard, ChessSquare square);

    public boolean canMove(ChessSquare square, Chessboard chessboard, Position position) {
        var moves = allMoves(square, chessboard);
        boolean hasPosition = moves.contains(position);
        if (!hasPosition) {
            return false;
        }
        return !underCheck(chessboard, square, position);
    }

    protected boolean underCheck(Chessboard chessboard, ChessSquare square, Position position) {
        return fakeMove(chessboard, square, position);
    }

    private boolean fakeMove(Chessboard chessboard, ChessSquare square, Position position) {
        var squares = chessboard.getSquares();
        var target = squares.get(position);
        var piece = square.getPiece();

        var checkpointSq = square.save();
        var checkpointT = target.save();

        square.remove();
        if (!target.isFree()) {
            // not necessary adding piece to removed list, because of the fake movement
            target.remove();
        }
        target.add(piece);
        boolean underCheck = isCheck(chessboard, piece.isWhite());
        square.restore(checkpointSq);
        target.restore(checkpointT);

        return underCheck;
    }

    private boolean isCheck(Chessboard chessboard, boolean whiteMoves) {
        return chessboard.getSquares()
                .values()
                .stream()
                .filter(square -> square.getPiece() != null && square.getPiece().isWhite() != whiteMoves)
                .filter(square -> square.getPiece().getType() != PieceType.KING)
                .map(square -> square.getPiece().getMovement().allMoves(square, chessboard))
                .map(position -> filterAttackOnKing(position, chessboard.getSquares(), whiteMoves))
                .mapToLong(Collection::size)
                .sum() > 0;
    }

    public void move(ChessSquare square, Chessboard chessboard, Position position) {
        var piece = square.getPiece();
        piece.hasMoved();
        // removes from previous square
//        var squareCopy = square.save();
        square.remove();

        Map<Position, ChessSquare> squares = chessboard.getSquares();
        ChessSquare target = squares.get(position);
        if (!target.isFree()) {
//            var targetCopy = target.save();
//            chessboard.hitSave(targetCopy);
            chessboard.addRemoved(target.remove());
        }
        target.add(piece);
        var moves = allMoves(target, chessboard);
        var colour = target.getPiece().isWhite();
        var enemyKing = chessboard.findKing(!colour);
        var pos = enemyKing.getPosition();
        if (moves.contains(pos)) {
            // will make check
//            log.log(Level.INFO, "");
            enemyKing.setBackground(ChessboardBuilder.check);
            List<ChessSquare> pieces = chessboard.getPieces(!colour);
            for (ChessSquare sq : pieces) {
                var pieceOnSquare = sq.getPiece();
                var poses = pieceOnSquare.getMovement().allMoves(sq, chessboard);
                for (Position possiblePosition : poses) {
                    if (!underCheck(chessboard, sq, possiblePosition)) {
                        return;
                    }
                }
            }
            // checkmate
            chessboard.checkmate(colour);
            return;
        }
        chessboard.refresh();
    }

    protected void append(List<Position> moves, Position position) {
        if (position != null && position.validPosition()) {
            moves.add(position);
        }
    }

    protected List<Position> filterFree(List<Position> possible, Map<Position, ChessSquare> squares) {
        return possible
                .stream()
                .map(squares::get)
                .filter(ChessSquare::isFree)
                .map(ChessSquare::getPosition)
                .collect(Collectors.toList());
    }

    protected List<Position> filterAttackOnKing(@NotNull List<Position> possible, Map<Position, ChessSquare> squares, boolean isWhite) {
        return possible
                .stream()
                .map(squares::get)
                .filter(square -> square.isEnemyPiece(!isWhite) && square.getPiece().getType() == PieceType.KING)
                .map(ChessSquare::getPosition)
                .collect(Collectors.toList());
    }

    protected List<Position> filterAttack(List<Position> possible, Map<Position, ChessSquare> squares, boolean isWhite) {
        return possible
                .stream()
                .map(squares::get)
                .filter(square -> square.isEnemyPiece(isWhite))
                .map(ChessSquare::getPosition)
                .collect(Collectors.toList());
    }

    protected List<Position> versatile(Map<Position, ChessSquare> squares, ChessSquare square, Position vec) {
        List<Position> possible = new ArrayList<>(8);
        var pos = square.getPosition();
        while (pos.validPosition()) {
            pos = pos.add(vec);
            var cs = squares.get(pos);
            if (cs == null || !cs.isFree()) {
                break;
            }
            append(possible, pos);
        }
        // add last and check if it's under attack
        ChessSquare cs = squares.get(pos);
        if (cs != null && cs.isEnemyPiece(square.getPiece().isWhite())) {
            append(possible, pos);
        }

        return possible;
    }
}
