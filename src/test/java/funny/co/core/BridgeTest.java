package funny.co.core;

import funny.co.model.Position;
import funny.co.ui.ChessboardBuilder;
import funny.co.ui.ChessboardPane;
import javafx.application.Platform;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.util.Properties;
import java.util.concurrent.*;

public final class BridgeTest {
    private static GameplayHandler game;
    private static ChessboardPane pane;
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static String base, token;
    private static void fx(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null); Platform.runLater(task); task.get(8, TimeUnit.SECONDS);
    }
    private static HttpResponse<String> request(String path, String body, String credential) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(base + path)).header("Authorization", "Bearer " + credential).timeout(java.time.Duration.ofSeconds(5));
        if (body == null) builder.GET(); else builder.POST(HttpRequest.BodyPublishers.ofString(body));
        return CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
    private static void check(boolean valid, String message) { if (!valid) throw new AssertionError(message); }
    private static void click(String square) {
        int sq = Rules.square(square);
        pane.getSquares().get(Position.of(sq / 8, sq % 8)).getOnMouseClicked().handle(null);
    }
    public static void main(String[] args) throws Exception {
        System.setProperty("chess.home", "target/bridge-smoke");
        Platform.startup(() -> { });
        try {
            fx(() -> {
                pane = ChessboardBuilder.build(512, 640);
                game = new GameplayHandler(pane, true); game.bind();
            });
            Properties session = new Properties();
            try (var reader = Files.newBufferedReader(Paths.get("target/bridge-smoke/target/codex-session.properties"))) { session.load(reader); }
            base = "http://127.0.0.1:" + session.getProperty("port"); token = session.getProperty("token");
            check(request("/state", null, "wrong").statusCode() == 401, "Missing authentication accepted");
            check(request("/state", null, token).body().contains("\"turn\":\"white\""), "Initial white turn");
            check(request("/move", "move=e7e5&version=0", token).statusCode() == 409, "Black moved out of turn");
            fx(() -> { click("e2"); click("e4"); });
            check(request("/state", null, token).body().contains("\"moves\":[\"e2e4\"]"), "GUI move did not reach bridge");
            fx(() -> { click("e7"); click("e5"); });
            check(request("/state", null, token).body().contains("\"version\":1"), "GUI moved Codex's pieces");
            check(request("/move", "move=e7e4&version=1", token).statusCode() == 409, "Illegal move accepted");
            var response = request("/move", "move=e7e5&version=1", token);
            check(response.statusCode() == 200 && response.body().contains("\"turn\":\"white\""), "Codex reply failed");
            fx(() -> check(pane.getSquares().get(Position.of(4, 4)).getPiece() != null && pane.getSquares().get(Position.of(6, 4)).isFree(), "Bridge move did not update GUI"));
            check(request("/move", "move=g8f6&version=1", token).statusCode() == 409, "Stale action accepted");
            fx(game::back);
            check(request("/state", null, token).body().contains(Rules.START), "Undo did not restore full state");
            fx(game::forward);
            check(request("/state", null, token).body().contains("\"moves\":[\"e2e4\",\"e7e5\"]"), "Redo failed");
            System.out.println("PASS: real JavaFX clicks and authenticated HTTP moves, turn protection, stale moves, undo and redo.");
        } finally { if (game != null) fx(game::close); Platform.exit(); }
    }
}
