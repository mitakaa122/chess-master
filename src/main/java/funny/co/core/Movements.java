package funny.co.core;

public final class Movements {
    private Movements() {}
    public static final KingMovement kingMovement = new KingMovement();
    public static final KnightMovement knightMovement = new KnightMovement();
    public static final PawnMovement pawnMovement = new PawnMovement();
    public static final QueenMovement queenMovement = new QueenMovement();
    public static final RockMovement rockMovement = new RockMovement();
    public static final BishopMovement bishopMovement = new BishopMovement();
}
