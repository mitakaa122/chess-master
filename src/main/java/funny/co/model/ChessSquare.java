package funny.co.model;

import funny.co.design.Originator;
import funny.co.design.Prototype;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.Nullable;

public class ChessSquare extends StackPane
        implements Originator<ChessSquareState>, Prototype<ChessSquare> {
    private boolean selected;
    private Piece current;
    private Background fill;
    private Position position;
    private boolean enable;

    public boolean isFree() {
        return current == null;
    }

    public boolean isEnemyPiece(boolean isWhite) {
        return !(current == null || isWhite == current.isWhite());
    }

    public boolean add(Piece piece) {
        int size = getChildren().size();
        if (size != 0) {
            return false;
        }
        this.current = piece;
        getChildren().add(piece.getFigure());
        return true;
    }

    public Piece remove() {
        var current = this.current;
        getChildren().clear();
        this.current = null;
        return current;
    }

    @Nullable
    public Piece getPiece() {
        return current;
    }

    public boolean pieceIsPresent() {
        return current != null;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Background getFill() {
        return fill;
    }

    public void setFill(Background fill) {
        this.fill = fill;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public ChessSquareState save() {
        return new ChessSquareState(this, getChildren());
    }

    @Override
    public void restore(ChessSquareState chessSquareState) {
        var state = chessSquareState.getSquare();
        this.selected = state.selected;
        this.current = state.current;
        this.position = state.position;
        this.enable = state.enable;
        this.getChildren().addAll(chessSquareState.getNodes());
    }

    @Override
    public ChessSquare copy() {
        ChessSquare clone = new ChessSquare();
        clone.selected = this.selected;
        clone.current = this.current;
//        clone.position = this.position.copy();
        clone.position = this.position;
        clone.enable = this.enable;

        return clone;
    }
}
