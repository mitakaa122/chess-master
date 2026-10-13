package funny.co.model;

public class PieceMeta {
    private String url;
    private PieceType type;

    private PieceMeta(String url, PieceType type) {
        this.url = url;
        this.type = type;
    }

    public static PieceMeta of(String url, PieceType type) {
        return new PieceMeta(url, type);
    }

    public String getUrl() {
        return url;
    }

    public PieceType getType() {
        return type;
    }
}
