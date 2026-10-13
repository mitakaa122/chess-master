package funny.co.core;

import funny.co.model.*;
import funny.co.ui.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import java.util.*;
import java.util.stream.Collectors;

/** Both mouse and bridge moves pass through the same legal-move check. */
public class GameplayHandler implements AutoCloseable {
    private final ChessboardPane pane;
    private final boolean codexMode;
    private Rules rules = new Rules();
    private final List<Rules> history = new ArrayList<>();
    private final List<String> moves = new ArrayList<>();
    private int index, version, selected = -1;
    private boolean closed, claimed;
    private LocalBridge bridge;
    private final Label status = new Label();
    private final Label moveLog = new Label();

    public GameplayHandler(ChessboardPane pane, boolean codexMode) {
        this.pane = pane;
        this.codexMode = codexMode;
        status.setPadding(new Insets(10));
        status.setWrapText(true);
        moveLog.setPadding(new Insets(10));
        moveLog.setWrapText(true);
        moveLog.setMaxWidth(550);
        history.add(rules.copy());
    }
    public Label statusLabel() { return status; }
    public Label historyLabel() { return moveLog; }
    public void bind() {
        pane.getSquares().forEach((pos, square) -> square.setOnMouseClicked(event -> click(pos.getRow() * 8 + pos.getCol())));
        render();
        if (codexMode) {
            try { bridge = new LocalBridge(this); }
            catch (Exception e) { status.setText("Codex connection unavailable: " + e.getMessage()); }
        }
    }
    private String result() {
        if (claimed) return "Draw claimed";
        String result = rules.outcome();
        if (!result.isEmpty()) return result;
        if (repetitions() >= 5) return "Draw by fivefold repetition";
        return "";
    }
    private long repetitions() {
        String key = rules.repetitionKey();
        return history.subList(0, index + 1).stream().filter(r -> r.repetitionKey().equals(key)).count();
    }
    private boolean claimable() { return rules.halfmoveClock() >= 100 || repetitions() >= 3; }
    public void claimDraw() {
        if (codexMode && !rules.whiteToMove()) return;
        if (result().isEmpty() && claimable()) { claimed = true; version++; render(); }
        else status.setText("No draw claim available in the current position.");
    }
    private void click(int sq) {
        if (closed || !result().isEmpty() || codexMode && !rules.whiteToMove()) return;
        char piece = rules.at(sq);
        if (piece != '.' && Character.isUpperCase(piece) == rules.whiteToMove()) {
            selected = sq; render();
            pane.getSquares().get(Position.of(sq / 8, sq % 8)).setBackground(ChessboardBuilder.selected);
            for (String move : rules.legalMoves()) if (move.startsWith(Rules.name(sq))) {
                int to = Rules.square(move.substring(2, 4));
                ChessSquare cell = pane.getSquares().get(Position.of(to / 8, to % 8));
                cell.setBackground(new Background(cell.getFill().getFills().get(0), ChessboardBuilder.circle));
            }
            return;
        }
        if (selected < 0) return;
        String prefix = Rules.name(selected) + Rules.name(sq);
        List<String> options = rules.legalMoves().stream().filter(m -> m.startsWith(prefix)).collect(Collectors.toList());
        if (options.isEmpty()) { selected = -1; render(); return; }
        String move = options.get(0);
        if (options.size() > 1) {
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Queen", "Queen", "Rook", "Bishop", "Knight");
            dialog.setTitle("Pawn promotion"); dialog.setHeaderText("Choose a piece");
            dialog.initOwner(pane.getScene().getWindow());
            Optional<String> choice = dialog.showAndWait();
            if (choice.isEmpty()) return;
            move = prefix + (choice.get().equals("Knight") ? "n" : choice.get().substring(0, 1).toLowerCase(Locale.ROOT));
        }
        play(move);
    }
    private void play(String move) {
        rules.play(move);
        while (history.size() > index + 1) history.remove(history.size() - 1);
        while (moves.size() > index) moves.remove(moves.size() - 1);
        moves.add(move); history.add(rules.copy()); index++; version++; selected = -1; claimed = false;
        render();
    }
    public String remoteMove(String move, int expectedVersion) {
        if (closed) throw new IllegalStateException("This game is closed.");
        if (!codexMode) throw new IllegalStateException("Codex mode is disabled.");
        if (expectedVersion != version) throw new IllegalArgumentException("Stale position. Read state again.");
        if (!result().isEmpty()) throw new IllegalArgumentException("Game over.");
        if (rules.whiteToMove()) throw new IllegalArgumentException("It is White's turn.");
        if (move.equals("claim")) {
            if (!claimable()) throw new IllegalArgumentException("No draw claim available.");
            claimed = true; version++; render();
        } else play(move);
        return stateJson();
    }
    public String stateJson() {
        String legal = rules.legalMoves().stream().map(m -> "\"" + m + "\"").collect(Collectors.joining(","));
        String log = moves.subList(0, index).stream().map(m -> "\"" + m + "\"").collect(Collectors.joining(","));
        return "{\"version\":" + version + ",\"side\":\"black\",\"turn\":\"" + (rules.whiteToMove() ? "white" : "black")
                + "\",\"fen\":\"" + rules.fen() + "\",\"check\":" + rules.inCheck() + ",\"result\":\"" + result()
                + "\",\"claimable\":" + claimable() + ",\"moves\":[" + log + "],\"legalMoves\":[" + legal + "]}";
    }
    private void render() {
        for (Map.Entry<Position, ChessSquare> entry : pane.getSquares().entrySet()) {
            Position pos = entry.getKey(); ChessSquare square = entry.getValue();
            char value = rules.at(pos.getRow() * 8 + pos.getCol());
            square.remove(); square.setBackground(square.getFill()); square.setOpacity(1);
            if (value != '.') {
                PieceType type;
                switch (Character.toLowerCase(value)) {
                    case 'k': type = PieceType.KING; break;
                    case 'q': type = PieceType.QUEEN; break;
                    case 'r': type = PieceType.ROCK; break;
                    case 'b': type = PieceType.BISHOP; break;
                    case 'n': type = PieceType.KNIGHT; break;
                    default: type = PieceType.PAWN;
                }
                Piece piece = ChessboardBuilder.createPiece(type, Character.isUpperCase(value));
                double size = square.getPrefWidth();
                piece.getFigure().setWidth(size); piece.getFigure().setHeight(size);
                square.add(piece);
                if (Character.toLowerCase(value) == 'k' && Character.isUpperCase(value) == rules.whiteToMove() && rules.inCheck()) square.setBackground(ChessboardBuilder.check);
            }
        }
        String result = result();
        String turn = rules.whiteToMove() ? (codexMode ? "Your turn (White)" : "White to move") : (codexMode ? "Codex to move (Black)" : "Black to move");
        status.setText(result.isEmpty() ? turn + (rules.inCheck() ? " - Check!" : "") : result);
        StringBuilder log = new StringBuilder();
        for (int i = Math.max(0, index - 8); i < index; i++) {
            if (i % 2 == 0) log.append(i / 2 + 1).append(". ");
            log.append(moves.get(i)).append(' ');
        }
        moveLog.setText(log.length() == 0 ? "Select a piece, then its destination. You play White." : log.toString());
    }
    public void back() {
        if (index == 0) return;
        index = Math.max(0, index - (codexMode && index % 2 == 0 ? 2 : 1));
        restore();
    }
    public void forward() {
        if (index == history.size() - 1) return;
        index = Math.min(history.size() - 1, index + (codexMode ? 2 : 1));
        restore();
    }
    private void restore() { rules = history.get(index).copy(); claimed = false; selected = -1; version++; render(); }
    @Override public void close() { closed = true; if (bridge != null) bridge.close(); }
}
