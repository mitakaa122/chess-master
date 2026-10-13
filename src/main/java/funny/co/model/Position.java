package funny.co.model;

import funny.co.design.Prototype;

import java.util.Objects;

public class Position implements Prototype<Position> {
    public static final Position WHITE_DIRECTION = Position.of(1, 1);
    public static final Position BLACK_DIRECTION = Position.of(-1, 1);
    private static final String[] ranks = {"a", "b", "c", "d", "e", "f", "g", "h"};

    private int i;
    private int j;

    private Position(int i, int j) {
        this.i = i;
        this.j = j;
    }

    public static Position of(int i, int j) {
        return new Position(i, j);
    }

    public Position add(Position position) {
        return new Position(this.i + position.i, this.j + position.j);
    }

    public Position sub(Position position) {
        return new Position(this.i - position.i, this.j - position.j);
    }

    public Position mult(Position n) {
        return new Position(this.i * n.i, this.j * n.j);
    }

    public int getRow() {
        return i;
    }

    public int getCol() {
        return j;
    }

    public int getFile() {
        return j;
    }

    public String getRank() {
        return ranks[i];
    }

    public boolean validPosition() {
        return i >= 0 && i <= 7 && j >= 0 && j <= 7;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return i == position.i && j == position.j;
    }

    @Override
    public int hashCode() {
        return Objects.hash(i, j);
    }

    @Override
    public Position copy() {
        return Position.of(this.i, this.j);
    }
}
