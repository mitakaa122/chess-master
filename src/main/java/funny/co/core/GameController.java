package funny.co.core;

import funny.co.ui.ChessboardBuilder;
import funny.co.ui.ChessboardPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.awt.Dimension;
import java.awt.Toolkit;

public class GameController {
    private static final int DEFAULT_WIDTH = 512;
    private static final int DEFAULT_HEIGHT = 640;
    private double width;
    private double height;

    private StackPane chessboardHolder;
    private Runnable back;
    private Runnable forward;
    private Runnable maximize;
    private Runnable minimize;
    private Stage stage;
    private GameplayHandler gameplay;
    private boolean codexMode = true;

    public GameController(StackPane chessboardHolder, Stage stage) {
        this.chessboardHolder = chessboardHolder;
        this.stage = stage;
    }

    public void newGame() {
        if (gameplay != null) gameplay.close();
        if (!chessboardHolder.getChildren().isEmpty()) {
            chessboardHolder.getChildren().clear();
        }
        // creates new chessboard
        ChessboardPane chessboard = ChessboardBuilder.build(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        gameplay = new GameplayHandler(chessboard, codexMode);
        BorderPane framed = new BorderPane(chessboard);
        HBox files = new HBox();
        for (char file = 'a'; file <= 'h'; file++) {
            Label label = new Label(String.valueOf(file)); label.setMinSize(64, 24); label.setAlignment(Pos.CENTER); files.getChildren().add(label);
        }
        VBox ranks = new VBox();
        for (int rank = 8; rank >= 1; rank--) {
            Label label = new Label(String.valueOf(rank)); label.setMinSize(24, 64); label.setAlignment(Pos.CENTER); ranks.getChildren().add(label);
        }
        framed.setBottom(files); BorderPane.setMargin(files, new javafx.geometry.Insets(0, 0, 0, 24)); framed.setLeft(ranks);
        chessboardHolder.getChildren().add(new VBox(gameplay.statusLabel(), framed, gameplay.historyLabel()));
        stage.setTitle(codexMode ? "Chess - Vlad (White) vs Codex (Black)" : "Chess - Local two-player game");
        back = gameplay::back;
        forward = gameplay::forward;
        maximize = chessboard::maximize;
        minimize = chessboard::minimize;
        gameplay.bind();
        stage.sizeToScene();
    }

    public void localGame() { codexMode = false; newGame(); }
    public void codexGame() { codexMode = true; newGame(); }
    public void claimDraw() { if (gameplay != null) gameplay.claimDraw(); }
    public void close() { if (gameplay != null) gameplay.close(); }

    public void back() {
        if (back != null) {
            back.run();
        }
    }

    public void forward() {
        if (forward != null) {
            forward.run();
        }
    }

    public void minimize() {
        if (minimize != null) {
            stage.setHeight(height);
            stage.setWidth(width);
            minimize.run();
        }
    }

    public void maximize() {
        if (maximize != null) {
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            stage.setWidth(size.getWidth());
            stage.setHeight(size.getHeight());
            maximize.run();
        }
    }

    public void screenCapture() {
        this.height = stage.getHeight() + 10;
        this.width = stage.getWidth() - 2;
    }
}
