package chess;
//referred to as chess.ChessMove
import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
//public class named ChessMove
public class ChessMove {
    private final ChessPosition startPosition; //where piece starts
    private final ChessPosition endPosition; //where piece ends
    private final ChessPiece.PieceType promotionPiece; //what piece pawn becomes on promotion, if none = null
    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    } //Constructor; assigns  fields

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition(){return startPosition;}
    // Getter; returns the start position
    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPosition;
    }
    // Getter; returns the end position
    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }
    //Getter; returns promotion piece or null depending

    //returns the hashcode for the move which combines the three into a hash value; for tests
    @Override
    public int hashCode() {
        return Objects.hash(startPosition, endPosition, promotionPiece);
    }

    // compares chess moves objects
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // if they are the same object return true
        if (obj == null || getClass() != obj.getClass()) return false; // if obj is null or not the same class return false
        ChessMove other = (ChessMove) obj;//casts to ChessMove to be compared
        return Objects.equals(startPosition, other.startPosition) && Objects.equals(endPosition,other.endPosition) && Objects.equals(promotionPiece, other.promotionPiece);
    } // compares the startPosition, endPosition, promotionPiece; If equal true otherwise false
}
