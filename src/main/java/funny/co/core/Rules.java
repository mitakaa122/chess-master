package funny.co.core;

import java.util.*;
import java.util.stream.Collectors;

/** UI-independent chess position. Squares run from a1=0 to h8=63. */
public final class Rules {
    public static final String START = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private char[] board = new char[64];
    private boolean white = true;
    private String rights = "KQkq";
    private int ep = -1, halfmove, fullmove = 1;

    public Rules() { this(START); }
    public Rules(String fen) {
        Arrays.fill(board, '.');
        String[] fields = fen.split(" ");
        String[] rows = fields[0].split("/");
        if (rows.length != 8 || fields.length != 6) throw new IllegalArgumentException("Invalid FEN");
        for (int r = 0; r < 8; r++) {
            int file = 0;
            for (char c : rows[r].toCharArray()) {
                if (Character.isDigit(c)) file += c - '0';
                else board[(7 - r) * 8 + file++] = c;
            }
            if (file != 8) throw new IllegalArgumentException("Invalid FEN rank");
        }
        white = fields[1].equals("w");
        rights = fields[2].equals("-") ? "" : fields[2];
        ep = fields[3].equals("-") ? -1 : square(fields[3]);
        halfmove = Integer.parseInt(fields[4]); fullmove = Integer.parseInt(fields[5]);
    }
    private Rules(Rules other) {
        board = other.board.clone(); white = other.white; rights = other.rights;
        ep = other.ep; halfmove = other.halfmove; fullmove = other.fullmove;
    }
    public Rules copy() { return new Rules(this); }
    public boolean whiteToMove() { return white; }
    public int halfmoveClock() { return halfmove; }
    public char at(int square) { return board[square]; }
    public static String name(int square) { return "" + (char)('a' + square % 8) + (1 + square / 8); }
    public static int square(String text) {
        if (!text.matches("[a-h][1-8]")) throw new IllegalArgumentException("Invalid square");
        return (text.charAt(1) - '1') * 8 + text.charAt(0) - 'a';
    }
    private boolean friendly(char p) { return p != '.' && Character.isUpperCase(p) == white; }
    private static boolean inside(int r, int f) { return r >= 0 && r < 8 && f >= 0 && f < 8; }
    public boolean inCheck() { return inCheck(white); }
    private boolean inCheck(boolean side) {
        char king = side ? 'K' : 'k';
        for (int i = 0; i < 64; i++) if (board[i] == king) return attacked(i, !side);
        throw new IllegalStateException("Missing king");
    }
    private boolean attacked(int target, boolean byWhite) {
        int tr = target / 8, tf = target % 8;
        for (int from = 0; from < 64; from++) {
            char p = board[from];
            if (p == '.' || Character.isUpperCase(p) != byWhite) continue;
            int dr = tr - from / 8, df = tf - from % 8;
            char kind = Character.toLowerCase(p);
            if (kind == 'p' && dr == (byWhite ? 1 : -1) && Math.abs(df) == 1) return true;
            if (kind == 'n' && Math.abs(dr) * Math.abs(df) == 2) return true;
            if (kind == 'k' && Math.max(Math.abs(dr), Math.abs(df)) == 1) return true;
            boolean diagonal = Math.abs(dr) == Math.abs(df) && dr != 0;
            boolean straight = (dr == 0) != (df == 0);
            if (!((kind == 'b' || kind == 'q') && diagonal || (kind == 'r' || kind == 'q') && straight)) continue;
            int step = Integer.signum(dr) * 8 + Integer.signum(df), sq = from + step;
            while (sq != target && board[sq] == '.') sq += step;
            if (sq == target) return true;
        }
        return false;
    }
    private void add(List<String> moves, int from, int to) {
        if (Character.toLowerCase(board[from]) == 'p' && (to / 8 == 0 || to / 8 == 7)) {
            for (char p : "qrbn".toCharArray()) moves.add(name(from) + name(to) + p);
        } else moves.add(name(from) + name(to));
    }
    public List<String> legalMoves() {
        List<String> pseudo = new ArrayList<>(), legal = new ArrayList<>();
        for (int from = 0; from < 64; from++) {
            if (!friendly(board[from])) continue;
            char p = Character.toLowerCase(board[from]);
            int r = from / 8, f = from % 8;
            if (p == 'p') {
                int dir = white ? 1 : -1, next = r + dir;
                if (!inside(next, f)) continue;
                if (board[next * 8 + f] == '.') {
                    add(pseudo, from, next * 8 + f);
                    if (r == (white ? 1 : 6) && board[(r + 2 * dir) * 8 + f] == '.') add(pseudo, from, (r + 2 * dir) * 8 + f);
                }
                for (int df : new int[] {-1, 1}) if (inside(next, f + df)) {
                    int to = next * 8 + f + df;
                    if (board[to] != '.' && !friendly(board[to]) && Character.toLowerCase(board[to]) != 'k'
                            || to == ep && board[to] == '.' && board[to - dir * 8] == (white ? 'p' : 'P')) add(pseudo, from, to);
                }
                continue;
            }
            int[][] directions = p == 'n' ? new int[][] {{1,2},{2,1},{-1,2},{-2,1},{1,-2},{2,-1},{-1,-2},{-2,-1}}
                    : p == 'b' ? new int[][] {{1,1},{1,-1},{-1,1},{-1,-1}}
                    : p == 'r' ? new int[][] {{1,0},{-1,0},{0,1},{0,-1}}
                    : new int[][] {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
            for (int[] d : directions) {
                for (int step = 1; step <= (p == 'n' || p == 'k' ? 1 : 7); step++) {
                    int nr = r + d[0] * step, nf = f + d[1] * step;
                    if (!inside(nr, nf)) break;
                    int to = nr * 8 + nf;
                    if (friendly(board[to]) || Character.toLowerCase(board[to]) == 'k') break;
                    add(pseudo, from, to);
                    if (board[to] != '.') break;
                }
            }
            if (p == 'k' && from == (white ? 4 : 60) && !inCheck()) {
                int base = white ? 0 : 56;
                char rook = white ? 'R' : 'r';
                if (rights.indexOf(white ? 'K' : 'k') >= 0 && board[base + 7] == rook && board[base + 5] == '.' && board[base + 6] == '.'
                        && !attacked(base + 5, !white) && !attacked(base + 6, !white)) add(pseudo, from, base + 6);
                if (rights.indexOf(white ? 'Q' : 'q') >= 0 && board[base] == rook && board[base + 1] == '.' && board[base + 2] == '.' && board[base + 3] == '.'
                        && !attacked(base + 3, !white) && !attacked(base + 2, !white)) add(pseudo, from, base + 2);
            }
        }
        for (String move : pseudo) {
            Rules next = copy(); next.apply(move);
            if (!next.inCheck(white)) legal.add(move);
        }
        return legal;
    }
    public void play(String move) {
        if (!legalMoves().contains(move)) throw new IllegalArgumentException("Illegal move: " + move);
        apply(move);
    }
    private void apply(String move) {
        int from = square(move.substring(0, 2)), to = square(move.substring(2, 4));
        char piece = board[from], captured = board[to];
        boolean pawn = Character.toLowerCase(piece) == 'p';
        if (pawn && to == ep && captured == '.' && from % 8 != to % 8) board[to + (white ? -8 : 8)] = '.';
        board[from] = '.'; board[to] = piece;
        if (Character.toLowerCase(piece) == 'k') {
            rights = rights.replace(white ? "K" : "k", "").replace(white ? "Q" : "q", "");
            if (Math.abs(to - from) == 2) {
                int rookFrom = to > from ? from + 3 : from - 4, rookTo = to > from ? from + 1 : from - 1;
                board[rookTo] = board[rookFrom]; board[rookFrom] = '.';
            }
        }
        int[] corners = {0,7,56,63}; String flags = "QKqk";
        for (int i = 0; i < 4; i++) if (from == corners[i] || to == corners[i]) rights = rights.replace(flags.substring(i, i + 1), "");
        if (move.length() == 5) board[to] = white ? Character.toUpperCase(move.charAt(4)) : move.charAt(4);
        ep = pawn && Math.abs(to - from) == 16 ? (to + from) / 2 : -1;
        halfmove = pawn || captured != '.' ? 0 : halfmove + 1;
        if (!white) fullmove++;
        white = !white;
    }
    public String fen() {
        StringBuilder fen = new StringBuilder();
        for (int r = 7; r >= 0; r--) {
            int empty = 0;
            for (int f = 0; f < 8; f++) {
                char p = board[r * 8 + f];
                if (p == '.') empty++;
                else { if (empty > 0) fen.append(empty); empty = 0; fen.append(p); }
            }
            if (empty > 0) fen.append(empty);
            if (r > 0) fen.append('/');
        }
        return fen + (white ? " w " : " b ") + (rights.isEmpty() ? "-" : rights) + " " + (ep < 0 ? "-" : name(ep)) + " " + halfmove + " " + fullmove;
    }
    public String repetitionKey() {
        String[] f = fen().split(" ");
        if (ep >= 0 && legalMoves().stream().noneMatch(m -> square(m.substring(2, 4)) == ep && Character.toLowerCase(board[square(m.substring(0, 2))]) == 'p')) f[3] = "-";
        return String.join(" ", Arrays.copyOf(f, 4));
    }
    public String outcome() {
        if (legalMoves().isEmpty()) return inCheck() ? (white ? "Black" : "White") + " wins by checkmate" : "Draw by stalemate";
        if (halfmove >= 150) return "Draw by 75-move rule";
        List<Integer> minor = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            char p = Character.toLowerCase(board[i]);
            if (p == '.' || p == 'k') continue;
            if (p != 'b' && p != 'n') return "";
            minor.add(i);
        }
        if (minor.size() <= 1) return "Draw by insufficient material";
        if (minor.stream().allMatch(i -> Character.toLowerCase(board[i]) == 'b') && minor.stream().map(i -> (i / 8 + i % 8) % 2).distinct().count() == 1)
            return "Draw by insufficient material";
        return "";
    }
    public long perft(int depth) {
        if (depth == 0) return 1;
        long nodes = 0;
        for (String move : legalMoves()) { Rules next = copy(); next.apply(move); nodes += next.perft(depth - 1); }
        return nodes;
    }
}
