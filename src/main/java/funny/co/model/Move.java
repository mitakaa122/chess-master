package funny.co.model;

public final class Move {
    private Position from;
    private Position to;
    private PieceType piece;
    private MoveType type;
    private String notation;
    private boolean isWhite;

    private Move(Position from, Position to, PieceType piece, MoveType type, String notation, boolean isWhite) {
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.type = type;
        this.notation = notation;
        this.isWhite = isWhite;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public PieceType getPiece() {
        return piece;
    }

    public MoveType getType() {
        return type;
    }

    public String getNotation() {
        return notation;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Position from;
        private Position to;
        private PieceType piece;
        private MoveType type;
        private String notation;
        private boolean isWhite;

        public Builder from(Position from) {
            this.from = from;
            return this;
        }

        public Builder to(Position to) {
            this.to = to;
            return this;
        }

        public Builder piece(PieceType piece) {
            this.piece = piece;
            return this;
        }

        public Builder type(MoveType type) {
            this.type = type;
            return this;
        }

        public Builder notation(String notation) {
            this.notation = notation;
            return this;
        }

        public Builder white(boolean white) {
            isWhite = white;
            return this;
        }

        public Move build() {
            return new Move(from, to, piece, type, notation, isWhite);
        }
    }
}
