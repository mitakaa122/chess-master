package funny.co.ui;

import funny.co.exception.ChessLogicException;
import funny.co.model.ChessSquare;
import funny.co.model.Piece;
import funny.co.model.PieceMeta;
import funny.co.model.PieceType;
import funny.co.model.Position;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;

public class ChessboardBuilder {
    public static final int MIN_DIM = 64;
    public static final int MAX_DIM = 128;

    // vertical
    public static final int FILES = 8;
    // horizontal
    public static final int RANKS = 8;

    public static final Background whiteSquare = new Background(new BackgroundFill(Color.WHEAT, CornerRadii.EMPTY, Insets.EMPTY));
    public static final Background blackSquare = new Background(new BackgroundFill(Color.ROSYBROWN, CornerRadii.EMPTY, Insets.EMPTY));
    public static final Background selected = new Background(new BackgroundFill(Color.DARKOLIVEGREEN, CornerRadii.EMPTY, Insets.EMPTY));
    public static final Background check = new Background(new BackgroundFill(Color.INDIANRED, CornerRadii.EMPTY, Insets.EMPTY));
    public static final BackgroundFill transparent = new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY);
    public static final CornerRadii radius = new CornerRadii(40f);
    public static final Insets insets = new Insets(25, 25, 25, 25);
    public static final BackgroundFill circle = new BackgroundFill(Color.DARKOLIVEGREEN, radius, insets);
    public static final Map<String, Image> pieces = new HashMap<>();
    private static final Map<Position, PieceMeta> positions = new HashMap<>();
    public static BackgroundFill corners = new BackgroundFill(Color.DARKOLIVEGREEN, CornerRadii.EMPTY, Insets.EMPTY);

    static {
        loadImages("/piece/min");
    }

    public static void loadImages(String path) {
        String base = path.endsWith("/") ? path : path + "/";
        for (String color : new String[]{"white", "black"}) {
            for (String type : new String[]{"king", "queen", "rock", "bishop", "knight", "pawn"}) {
                String name = color + "_" + type + ".png";
                String resource = base + name;
                try (InputStream in = ChessboardBuilder.class.getResourceAsStream(resource)) {
                    if (in == null) {
                        throw new IllegalStateException("Resource not found: " + resource);
                    }
                    Image image = new Image(in);
                    if (image.isError()) {
                        throw new IllegalStateException("Failed to load image: " + resource, image.getException());
                    }
                    pieces.put(name, image);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read resource: " + resource, e);
                }
            }
        }
        putPos(0, false);
        putPos(7, true);
    }

    private static void putPos(int rank, boolean black) {
        String colour = black ? "black" : "white";
        positions.put(Position.of(rank, 0), PieceMeta.of(colour + "_rock.png", PieceType.ROCK));
        positions.put(Position.of(rank, 1), PieceMeta.of(colour + "_knight.png", PieceType.KNIGHT));
        positions.put(Position.of(rank, 2), PieceMeta.of(colour + "_bishop.png", PieceType.BISHOP));
        positions.put(Position.of(rank, 3), PieceMeta.of(colour + "_queen.png", PieceType.QUEEN));
        positions.put(Position.of(rank, 4), PieceMeta.of(colour + "_king.png", PieceType.KING));
        positions.put(Position.of(rank, 5), PieceMeta.of(colour + "_bishop.png", PieceType.BISHOP));
        positions.put(Position.of(rank, 6), PieceMeta.of(colour + "_knight.png", PieceType.KNIGHT));
        positions.put(Position.of(rank, 7), PieceMeta.of(colour + "_rock.png", PieceType.ROCK));
        for (int j = 0; j < RANKS; j++) {
            positions.put(Position.of(black ? rank - 1 : rank + 1, j), PieceMeta.of(colour + "_pawn.png", PieceType.PAWN));
        }
    }

    public static ChessboardPane build(double width, double height) {
        return draw(width, height);
    }

    private static ChessboardPane draw(double width, double height) {
        Map<Position, ChessSquare> squares = new HashMap<>();
        for (int i = 0; i < FILES; i++) {
            for (int j = 0; j < RANKS; j++) {
                var col = colour(i, j);
                ChessSquare cell = new ChessSquare();
                cell.setPrefSize(MIN_DIM, MIN_DIM);
                cell.setBackground(col);
                cell.setFill(col);
                cell.setEnable(true);
                Piece piece = getPiece(i, j, MIN_DIM, MIN_DIM);
                if (piece != null) {
                    cell.add(piece);
                }
                Position position = Position.of(i, j);
                cell.setPosition(position);
                squares.put(position, cell);
            }
        }
        return new ChessboardPane(squares);
    }

    private static Piece getPiece(int i, int j, double w, double h) {
        PieceMeta meta = positions.get(Position.of(i, j));
        if (meta == null) {
            return null;
        }
        Image image = pieces.get(meta.getUrl());
        Rectangle rect = new Rectangle();
        rect.setWidth(w);
        rect.setHeight(h);
        rect.setFill(new ImagePattern(image));
        return Piece.builder()
                .url(meta.getUrl())
                .figure(rect)
                .type(meta.getType())
                .white(i == 0 || i == 1)
                .moves(0)
                .build();
    }

    public static Piece createPiece(@NotNull PieceType type, boolean white) {
        var pos = define(type, white);
        var meta = positions.get(pos);
        Image image = pieces.get(meta.getUrl());
        Rectangle rect = new Rectangle();
        // TODO fix hardcoded sizes
        rect.setWidth(64);
        rect.setHeight(64);
        rect.setFill(new ImagePattern(image));
        return Piece.builder()
                .url(meta.getUrl())
                .figure(rect)
                .type(meta.getType())
                .white(white)
                .moves(0)
                .build();
    }

    private static Position define(PieceType type, boolean white) {
        int rank = white ? 0 : 7;
        switch (type) {
            case KING:
                return Position.of(rank, 4);
            case PAWN:
                return Position.of(white ? 1 : 6, 0);
            case QUEEN:
                return Position.of(rank, 3);
            case BISHOP:
                return Position.of(rank, 2);
            case KNIGHT:
                return Position.of(rank, 1);
            case ROCK:
                return Position.of(rank, 0);
            default:
                throw new ChessLogicException("unknown pawn promotion");
        }
    }

    private static Background colour(int i, int j) {
        return (i + j) % 2 == 0 ? blackSquare : whiteSquare;
    }
}
