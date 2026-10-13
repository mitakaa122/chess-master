package funny.co.model;

import funny.co.design.Prototype;
import funny.co.ui.ChessboardPane;
import javafx.scene.control.Alert;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Chessboard implements Prototype< Map<Position, ChessSquareState>> {
    private final Map<Position, ChessSquare> squares;
    private final List<Piece> removed = new LinkedList<>();
    private final Deque<Piece> moves = new LinkedList<>();
    private final ChessboardPane pane;
    public ChessSquare selected;
    public boolean whiteMoves = true;

    public Chessboard(ChessboardPane pane) {
        this.squares = pane.getSquares();
        this.pane = pane;
    }

    public Map<Position, ChessSquare> getSquares() {
        return squares;
    }

    public void addRemoved(Piece piece) {
        removed.add(piece);
    }

    public List<Piece> getRemoved() {
        return removed;
    }

    public ChessboardPane getPane() {
        return pane;
    }

    public void refresh() {
        this.pane.refresh();
    }

    public void disable() {
        pane.setOpacity(0.8);
        pane.disable();
    }

    public ChessSquare findKing(boolean white) {
        return squares.values()
                .stream()
                .filter(square -> square.getPiece() != null)
                .filter(square -> square.getPiece().getType() == PieceType.KING)
                .filter(square -> square.getPiece().isWhite() == white)
                .findAny()
                .get();
    }

    public List<ChessSquare> getPieces(boolean white) {
        return squares.values()
                .stream()
                .filter(square -> square.getPiece() != null)
                .filter(square -> square.getPiece().isWhite() == white)
                .collect(Collectors.toList());
    }

    public void enable() {
        pane.setOpacity(1.0);
        pane.enable();
    }

    public Deque<Piece> getMoves() {
        return moves;
    }

    public void checkmate(boolean whiteWin) {
        this.refresh();
        this.disable();

        String winner = whiteWin ? "white" : "black";
        Alert checkmateAlert = new Alert(Alert.AlertType.INFORMATION);
        checkmateAlert.setTitle("Checkmate");
        checkmateAlert.setContentText(String.format("Checkmate, %s wins", winner));
        checkmateAlert.showAndWait();
    }

    @Override
    public Map<Position, ChessSquareState> copy() {
        Map<Position, ChessSquareState> squareStateMap = new HashMap<>();
        for (Map.Entry<Position, ChessSquare> e : squares.entrySet()) {
            ChessSquareState clone = e.getValue().save();
            Position key = e.getKey().copy();
            squareStateMap.put(key, clone);
        }
        return squareStateMap;
    }

    public void restore(Map<Position, ChessSquareState> squares) {
        for (Map.Entry<Position, ChessSquare> e : this.squares.entrySet()) {
            var square = e.getValue();
            square.remove();
            var state = squares.get(e.getKey());
            square.restore(state);
        }
        this.refresh();
    }
}
