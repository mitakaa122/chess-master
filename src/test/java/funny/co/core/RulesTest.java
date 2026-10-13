package funny.co.core;

public final class RulesTest {
    private static int checks;
    private static void check(boolean value, String name) {
        checks++;
        if (!value) throw new AssertionError(name);
    }
    public static void main(String[] args) {
        Rules game = new Rules();
        check(game.fen().equals(Rules.START), "Starting FEN");
        check(game.perft(1) == 20, "Initial perft 1");
        check(game.perft(2) == 400, "Initial perft 2");
        check(game.perft(3) == 8902, "Initial perft 3");
        check(game.perft(4) == 197281, "Initial perft 4");
        Rules kiwipete = new Rules("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
        check(kiwipete.perft(1) == 48, "Kiwipete perft 1");
        check(kiwipete.perft(2) == 2039, "Kiwipete perft 2");
        check(kiwipete.perft(3) == 97862, "Kiwipete perft 3");
        Rules endgame = new Rules("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1");
        check(endgame.perft(3) == 2812, "Rook and en-passant perft");
        check(!new Rules("4k3/8/8/8/8/4N3/4P3/4K3 w - - 0 1").legalMoves().contains("e2e4"), "Pawn cannot jump blocker");
        check(!new Rules("8/8/8/8/8/4k3/8/4K3 w - - 0 1").legalMoves().contains("e1e2"), "Kings cannot touch");
        check(!new Rules("k7/8/8/4KPpr/8/8/8/8 w - g6 0 1").legalMoves().contains("f5g6"), "En passant exposing king");
        game = new Rules();
        for (String move : new String[] {"e2e4", "a7a6", "e4e5", "d7d5", "e5d6"}) game.play(move);
        check(game.at(Rules.square("d5")) == '.' && game.at(Rules.square("d6")) == 'P', "En passant removes captured pawn");
        game = new Rules("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        game.play("e1c1"); game.play("e8g8");
        check(game.at(2) == 'K' && game.at(3) == 'R' && game.at(62) == 'k' && game.at(61) == 'r', "Both castling directions");
        check(!new Rules("4kr2/8/8/8/8/8/8/4K2R w K - 0 1").legalMoves().contains("e1g1"), "No castle through attack");
        check(!new Rules("4k3/8/8/8/8/8/8/4K3 w K - 0 1").legalMoves().contains("e1g1"), "No castle without rook");
        game = new Rules("4k3/P7/8/8/8/8/8/4K3 w - - 0 1");
        check(game.legalMoves().stream().filter(m -> m.startsWith("a7a8")).count() == 4, "Four promotion choices");
        game.play("a7a8n"); check(game.at(56) == 'N', "Underpromotion");
        game = new Rules();
        for (String move : new String[] {"f2f3", "e7e5", "g2g4", "d8h4"}) game.play(move);
        check(game.outcome().equals("Black wins by checkmate"), "Fool's mate");
        check(new Rules("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1").outcome().equals("Draw by stalemate"), "Stalemate");
        check(new Rules("7k/8/6K1/8/8/8/8/8 w - - 0 1").outcome().equals("Draw by insufficient material"), "Bare kings");
        check(new Rules("7k/8/6K1/8/8/8/8/R7 w - - 150 1").outcome().equals("Draw by 75-move rule"), "Automatic move-count draw");
        game = new Rules(); String initial = game.repetitionKey();
        for (String move : new String[] {"g1f3", "g8f6", "f3g1", "f6g8"}) game.play(move);
        check(initial.equals(game.repetitionKey()), "Repetition ignores move counters");
        game = new Rules(); String before = game.fen();
        try { game.play("e2e5"); throw new AssertionError("Illegal move accepted"); } catch (IllegalArgumentException expected) { }
        check(game.fen().equals(before), "Illegal move does not mutate position");
        System.out.println("PASS: " + checks + " rules checks, including reference perft counts through depth 4.");
    }
}
