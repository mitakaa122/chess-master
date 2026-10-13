package funny.co.ui;

import funny.co.model.ChessSquare;
import funny.co.model.Position;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.ImagePattern;

import java.util.Map;

import static funny.co.ui.ChessboardBuilder.MAX_DIM;
import static funny.co.ui.ChessboardBuilder.MIN_DIM;
import static funny.co.ui.ChessboardBuilder.pieces;

public class ChessboardPane extends GridPane {
    private final Map<Position, ChessSquare> squares;

    public ChessboardPane(Map<Position, ChessSquare> squares) {
        this.squares = squares;
        squares.forEach((key, value) -> this.add(value, key.getCol(), 7 - key.getRow()));
    }

    public void refresh() {
        squares.values().forEach(square -> {
            if (square.getBackground() != ChessboardBuilder.check) {
                square.setBackground(square.getFill());
            }
            square.setSelected(false);
            square.setOpacity(1.0);
        });
    }

    public void disable() {
        squares.values().forEach(square -> {
            square.setEnable(false);
        });
    }

    public Map<Position, ChessSquare> getSquares() {
        return squares;
    }

    public void enable() {
        squares.values().forEach(square -> {
            square.setEnable(true);
        });
    }

    public void minimize() {
        redraw(MIN_DIM, "/piece/min");
    }

    public void maximize() {
        redraw(MAX_DIM, "/piece/");
    }

    private void redraw(double dimension, String path) {
        ChessboardBuilder.loadImages(path);
        for (ChessSquare square : squares.values()) {
            square.setPrefSize(dimension, dimension);
            var piece = square.getPiece();
            if (piece != null) {
                Image image = pieces.get(piece.getUrl());
                piece.getFigure().setHeight(dimension);
                piece.getFigure().setWidth(dimension);
                piece.getFigure().setFill(new ImagePattern(image));
            }
        }
    }
}
