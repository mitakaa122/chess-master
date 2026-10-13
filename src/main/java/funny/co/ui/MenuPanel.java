package funny.co.ui;

import funny.co.core.GameController;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.StackPane;

public class MenuPanel {
    private MenuBar bar;

    private Alert alertInfo = new Alert(Alert.AlertType.INFORMATION);
    private Alert alertGithubInfo = new Alert(Alert.AlertType.INFORMATION);

    public MenuPanel(final GameController controller) {
        init(controller);
    }

    public MenuBar getBar() {
        return bar;
    }

    public void init(final GameController controller) {
        bar = new MenuBar();
        Menu game = new Menu("Game");
        Menu settings = new Menu("Settings");
        Menu about = new Menu("About");
        bar.getMenus().addAll(game, settings, about);

        MenuItem newGame = new MenuItem("New game vs Codex");
        MenuItem localGame = new MenuItem("New local game");
        MenuItem undo = new MenuItem("Undo turn");
        MenuItem redo = new MenuItem("Redo turn");
        MenuItem claim = new MenuItem("Claim draw");
        localGame.setOnAction(event -> controller.localGame());
        undo.setOnAction(event -> controller.back());
        redo.setOnAction(event -> controller.forward());
        claim.setOnAction(event -> controller.claimDraw());
        MenuItem loadGame = new MenuItem("Load game");

        // disable right now
        loadGame.disableProperty().setValue(true);
        game.getItems().addAll(newGame, localGame, undo, redo, claim, loadGame);

        newGame.setOnAction(event -> {
            controller.codexGame();
        });

        MenuItem minimize = new MenuItem("Min");
        MenuItem maximize = new MenuItem("Max");
        settings.getItems().addAll(minimize, maximize);
        minimize.setDisable(true);
        maximize.setDisable(true);

        minimize.setOnAction(event -> {
            controller.minimize();
        });

        maximize.setOnAction(event -> {
            controller.maximize();
        });

        MenuItem info = new MenuItem("About");
        MenuItem githubInfo = new MenuItem("Github");
        about.getItems().addAll(info, githubInfo);

        alertInfo.setTitle("About game");
        alertInfo.setHeaderText("version 0.1");
        alertInfo.setContentText("Classic chess with local two-player play and a Codex opponent controlled through the active conversation.");

        info.setOnAction(event -> alertInfo.showAndWait());

        alertGithubInfo.setTitle("Source code");
        alertGithubInfo.setHeaderText("Github repository");

        Hyperlink repoLink = new Hyperlink("https://github.com/kurisumakise2011/chess");
        repoLink.setText("GIT https://github.com/kurisumakise2011/chess");
        StackPane pane = new StackPane();
        pane.getChildren().addAll(repoLink);
        alertGithubInfo.getDialogPane().setContent(pane);

        githubInfo.setOnAction(event -> alertGithubInfo.showAndWait());
    }

}
