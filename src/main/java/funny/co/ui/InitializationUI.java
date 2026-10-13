package funny.co.ui;

import funny.co.core.Caller;
import funny.co.core.GameController;
import funny.co.core.KeyboardHandler;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class InitializationUI {
    private static final String CHESS = "Chess";
    private static final String PIECE_MIN_BLACK_QUEEN_PNG = "/piece/min/black_queen.png";

    public static void init(Stage stage, Caller caller) {
        var image = new Image(InitializationUI.class.getResourceAsStream(PIECE_MIN_BLACK_QUEEN_PNG));
        stage.getIcons().addAll(image);

        stage.setTitle(CHESS);
        AnchorPane pane = new AnchorPane();
        Scene scene = new Scene(pane);

        StackPane chessboardHolder = new StackPane();
        GameController controller = new GameController(chessboardHolder, stage);

        MenuPanel panel = new MenuPanel(controller);
        VBox vBox = new VBox();
        vBox.getChildren().addAll(panel.getBar(), chessboardHolder);
        pane.getChildren().addAll(vBox);

        scene.setOnKeyReleased(new KeyboardHandler(caller, controller));
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setOnCloseRequest(event -> { controller.close(); caller.close(); });

        controller.newGame();
        stage.show();
        controller.screenCapture();
    }

}
