package funny.co.core;

import funny.co.model.ChessSquare;
import funny.co.model.Chessboard;
import funny.co.model.Piece;
import funny.co.model.PieceType;
import funny.co.model.Position;
import funny.co.ui.ChessboardBuilder;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PawnMovement extends AbstractPieceMovement {
    private static final Logger log = Logger.getLogger(PawnMovement.class.getName());
    private static final ButtonType queen = new ButtonType("Queen");
    private static final ButtonType rock = new ButtonType("Rock");
    private static final ButtonType bishop = new ButtonType("Bishop");
    private static final ButtonType knight = new ButtonType("Knight");
    private static final Alert promotion = getAlert();

    private static Alert getAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Pawn promotion");
        alert.setHeaderText("Choose piece type");

        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(queen, rock, bishop, knight);
        return alert;
    }

    public List<Position> find(Chessboard chessboard, ChessSquare square) {
        var squares = chessboard.getSquares();
        List<Position> moves = new ArrayList<>(4);
        List<Position> possible = new ArrayList<>(2);
        var pos = square.getPosition();
        var piece = square.getPiece();
        Objects.requireNonNull(piece);
        var dir = piece.getDirection();
        boolean white = piece.isWhite();
        if (pos.getRow() == 7 && white) {
            pawnPromotion(chessboard, square);
        }
        if (pos.getRow() == 0 && !white) {
            pawnPromotion(chessboard, square);
        }
        if (piece.noMoves() && (pos.getRow() == 1 || pos.getRow() == 6)) {
            append(possible, pos.add(Position.of(2, 0).mult(dir)));
        }
        if (pos.getRow() == 4 && white) {
            enpassant(possible, chessboard, pos, piece);
        }
        if (pos.getRow() == 3 && !white) {
            enpassant(possible, chessboard, pos, piece);
        }

        append(possible, pos.add(Position.of(1, 0).mult(dir)));

        moves.addAll(filterFree(possible, squares));
        possible.clear();

        append(possible, pos.add(Position.of(1, -1).mult(dir)));
        append(possible, pos.add(Position.of(1, 1).mult(dir)));

        moves.addAll(filterAttack(possible, squares, piece.isWhite()));

        return moves;
    }

    private void enpassant(List<Position> possible, Chessboard chessboard, Position pos, Piece piece) {
        var squares = chessboard.getSquares();
        Queue<Piece> moves = chessboard.getMoves();
        Piece lastMove = moves.peek();
        if (lastMove == null || lastMove.getType() != PieceType.PAWN || !lastMove.wasFirstMove()) {
            return;
        }

        var dir = piece.getDirection();
        var leftPos = pos.add(Position.of(0, 1).mult(dir));
        var left = squares.get(leftPos);
        if (left != null) {
            appendIfPresent(left, leftPos.add(Position.of(1, 0).mult(dir)), possible, lastMove);
        }

        var rightPos = pos.add(Position.of(0, -1).mult(dir));
        var right = squares.get(rightPos);
        if (right != null) {
            appendIfPresent(right, rightPos.add(Position.of(1, 0).mult(dir)), possible, lastMove);
        }
    }

    private void appendIfPresent(@NotNull ChessSquare square,
                                 @NotNull Position position,
                                 List<Position> moves,
                                 Piece lastMove) {
        var piece = square.getPiece();
        if (piece != null && piece.getType() == PieceType.PAWN && piece == lastMove) {
            append(moves, position);
        }
    }

    private void pawnPromotion(Chessboard chessboard, ChessSquare square) {
        chessboard.disable();
        Optional<ButtonType> option = promotion.showAndWait();
        if (option.isPresent()) {
            ButtonType type = option.get();
            PieceType pieceType;
            if (type == queen) {
                pieceType = PieceType.QUEEN;
            } else if (type == rock) {
                pieceType = PieceType.ROCK;
            } else if (type == bishop) {
                pieceType = PieceType.BISHOP;
            } else if (type == knight) {
                pieceType = PieceType.KNIGHT;
            } else {
                pieceType = PieceType.QUEEN;
            }
            boolean colour = square.getPiece().isWhite();
            var piece = ChessboardBuilder.createPiece(pieceType, colour);

            square.remove();
            square.add(piece);
            var pos = square.getPosition();
            log.log(Level.INFO, String.format("pawn promoted %s%d", pos.getRank(), pos.getFile()));
        }

        chessboard.enable();
    }

    @Override
    public void move(ChessSquare square, Chessboard chessboard, Position position) {
        var squares = chessboard.getSquares();
        var piece = square.getPiece();
        Objects.requireNonNull(piece);
        var to = squares.get(position);
        if (isEpassant(square, to, piece.isWhite())) {
            var underneath = to.getPosition().sub(Position.of(1, 0).mult(piece.getDirection()));
            var underneathPawn = squares.get(underneath);
            var pawn = underneathPawn.getPiece();
            if (!underneathPawn.isFree() && pawn.getType() == PieceType.PAWN) {
                chessboard.addRemoved(underneathPawn.remove());
            }
        }
        super.move(square, chessboard, position);
    }

    private boolean isEpassant(ChessSquare from, ChessSquare to, boolean white) {
        return to.isFree() && isDiagonal(from.getPosition(), to.getPosition(), white);
    }

    private boolean isDiagonal(Position from, Position to, boolean white) {
        return to.getRow() == from.getRow() + (white ? 1 : -1)
                && (to.getCol() == from.getCol() + 1
                || to.getCol() == from.getCol() - 1);
    }
}
