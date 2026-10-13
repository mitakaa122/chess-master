package funny.co.exception;

public class ChessLogicException extends RuntimeException {
    public ChessLogicException() {
    }

    public ChessLogicException(String message) {
        super(message);
    }

    public ChessLogicException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChessLogicException(Throwable cause) {
        super(cause);
    }
}
