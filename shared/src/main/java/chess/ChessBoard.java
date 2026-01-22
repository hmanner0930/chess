package chess;
import java.util.Arrays;
/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private final ChessPiece[][] board;
    public ChessBoard() {
        board = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        int rowIndex = position.getRow() -1;
        int colIndex = position.getColumn()-1;
        board[rowIndex][colIndex] = piece;

    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        int rowIndex = position.getRow() -1;
        int colIndex = position.getColumn()-1;
        return board[rowIndex][colIndex];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for(int r=0; r<8; r++){
            for (int c=0; c<8;c++){
                board[r][c] = null;
            }
        }
        ChessGame.TeamColor WHITE = ChessGame.TeamColor.WHITE;
        ChessGame.TeamColor BLACK = ChessGame.TeamColor.BLACK;
        addPiece(new ChessPosition(1,1), new ChessPiece(WHITE,ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(1,2), new ChessPiece(WHITE,ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1,3), new ChessPiece(WHITE,ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1,4), new ChessPiece(WHITE,ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(1,5), new ChessPiece(WHITE,ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(1,6), new ChessPiece(WHITE,ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1,7), new ChessPiece(WHITE,ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1,8), new ChessPiece(WHITE,ChessPiece.PieceType.ROOK));
        // for pawnss
        for(int c = 1; c<=8; c++){
            addPiece(new ChessPosition(2,c), new ChessPiece(WHITE, ChessPiece.PieceType.PAWN));
        }

        addPiece(new ChessPosition(8,1), new ChessPiece(BLACK,ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(8,2), new ChessPiece(BLACK,ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8,3), new ChessPiece(BLACK,ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8,4), new ChessPiece(BLACK,ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(8,5), new ChessPiece(BLACK,ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(8,6), new ChessPiece(BLACK,ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8,7), new ChessPiece(BLACK,ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8,8), new ChessPiece(BLACK,ChessPiece.PieceType.ROOK));
        // for pawns
        for(int c = 1; c<=8; c++){
            addPiece(new ChessPosition(7,c), new ChessPiece(BLACK, ChessPiece.PieceType.PAWN));
        }
    }
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessBoard that = (ChessBoard) o;
        return Arrays.deepEquals(this.board, that.board);
    }
}
