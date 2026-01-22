package chess; // in chess package
import java.util.Arrays;
/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private final ChessPiece[][] board; //field that is named board
    public ChessBoard() {
        board = new ChessPiece[8][8];
    } //allocate the actual 8x8 object and assigns it to board field above. All null.

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

    } // places a piece on the board in the given position the rowIndex and colIndex covert it into our array index
    //lastly it stores the piece in that square and overwrites the piece that is there

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
    } //This fetches whatever piece is at that position by returning a chess piece.
    //Does the necessary conversion returns the piece at that position or null

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for(int r=0; r<8; r++){
            for (int c=0; c<8;c++){
                board[r][c] = null;
            }
        } //Iterates over every row and column and sets everything to null
        //Local variables for team color chessgame.teamcolor
        ChessGame.TeamColor White = ChessGame.TeamColor.WHITE;
        ChessGame.TeamColor Black = ChessGame.TeamColor.BLACK;
        //Places every white piece in the right place ChessPosition builds the coordinates and ChessPiece creates that specific piece.
        addPiece(new ChessPosition(1,1), new ChessPiece(White,ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(1,2), new ChessPiece(White,ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1,3), new ChessPiece(White,ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1,4), new ChessPiece(White,ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(1,5), new ChessPiece(White,ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(1,6), new ChessPiece(White,ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1,7), new ChessPiece(White,ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1,8), new ChessPiece(White,ChessPiece.PieceType.ROOK));
        // for pawns White
        //This places the white pawns all in row two
        for(int c = 1; c<=8; c++){
            addPiece(new ChessPosition(2,c), new ChessPiece(White, ChessPiece.PieceType.PAWN));
        }
        //Places every black piece in the right place ChessPosition builds the coordinates and ChessPiece creates that specific piece.
        addPiece(new ChessPosition(8,1), new ChessPiece(Black,ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(8,2), new ChessPiece(Black,ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8,3), new ChessPiece(Black,ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8,4), new ChessPiece(Black,ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(8,5), new ChessPiece(Black,ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(8,6), new ChessPiece(Black,ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8,7), new ChessPiece(Black,ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8,8), new ChessPiece(Black,ChessPiece.PieceType.ROOK));
        // for pawns Black
        //This places the  black pawns all in row two
        for(int c = 1; c<=8; c++){
            addPiece(new ChessPosition(7,c), new ChessPiece(Black, ChessPiece.PieceType.PAWN));
        }
    }
    // returns integer hashcode for board
    //Computes a hash based on the contents of the board array and all pieces to make sure it matches.
    //Two identical piece boards will have the same hashcode for tests
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    // Comparing one chess board to another
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; //if references point to same object return true
        if (obj == null || getClass() != obj.getClass()) return false; //if obj is null or is not a chessBoard, not equal return false
        ChessBoard gurt = (ChessBoard) obj; //Cast of the previous check
        return Arrays.deepEquals(this.board, gurt.board); //  compares the two arrays square by square if everything matches return true
    }
} // deep walks through every element and compares hash of every element
