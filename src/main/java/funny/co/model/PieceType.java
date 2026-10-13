package funny.co.model;

import funny.co.core.Movements;
import funny.co.core.PieceMovement;

import java.util.function.Supplier;

public enum PieceType {
    KING(() -> Movements.kingMovement, "K"),
    KNIGHT(() -> Movements.knightMovement, "Kh"),
    PAWN(() -> Movements.pawnMovement, "P"),
    QUEEN(() -> Movements.queenMovement, "Q"),
    ROCK(() -> Movements.rockMovement, "R"),
    BISHOP(() -> Movements.bishopMovement, "B");

    private Supplier<PieceMovement> movementSupplier;
    private String name;

    PieceType(Supplier<PieceMovement> movementSupplier, String name) {
        this.movementSupplier = movementSupplier;
        this.name = name;
    }

    public Supplier<PieceMovement> movement() {
        return movementSupplier;
    }

    public String getName() {
        return name;
    }
}
