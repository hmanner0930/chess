package chess;

import java.util.Objects;

public class ChessMove {
    private final ChessPosition startPosition;
    private final ChessPosition endPosition;
    private final ChessPiece.PieceType promotionPiece;
    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    public ChessPosition getStartPosition(){return startPosition;}

    public ChessPosition getEndPosition() {
        return endPosition;
    }

    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(startPosition).append(" -> ").append(endPosition);
        if (promotionPiece != null) {
            sb.append(" (Promote: ").append(promotionPiece).append(")");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPosition, endPosition, promotionPiece);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {return true;}
        if (object == null || getClass() != object.getClass()) {return false;}
        ChessMove other = (ChessMove) object;
        return Objects.equals(startPosition, other.startPosition)
                && Objects.equals(endPosition,other.endPosition)
                && Objects.equals(promotionPiece, other.promotionPiece);
    }
}
