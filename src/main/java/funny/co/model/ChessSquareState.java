package funny.co.model;

import javafx.scene.Node;

import java.util.List;

public class ChessSquareState {
    private ChessSquare square;
    private List<Node> nodes;

    public ChessSquareState(ChessSquare square, List<Node> list) {
        this.square = square.copy();
        this.nodes = List.copyOf(list);
    }

    public ChessSquare getSquare() {
        return square;
    }

    public List<Node> getNodes() {
        return nodes;
    }
}
