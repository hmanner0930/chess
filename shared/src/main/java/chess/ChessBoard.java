package chess;

import java.util.Arrays;

public class ChessBoard {
    private final ChessPiece[][] board;
    private static final int BOARD_SIZE = 8;

    public ChessBoard() {
        this.board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
    }

    public void addPiece(ChessPosition position, ChessPiece piece) {
        int arrayRow = position.getRow() -1;
        int arrayCol = position.getColumn()-1;
        board[arrayRow][arrayCol] = piece;

    }

    public ChessPiece getPiece(ChessPosition position) {
        int arrayRow = position.getRow() -1;
        int arrayCol = position.getColumn()-1;
        return board[arrayRow][arrayCol];
    }

    public void resetBoard(){
        clearBoard();
        setupMajorPieces(1, ChessGame.TeamColor.WHITE);
        setupPawns(2,ChessGame.TeamColor.WHITE);

        setupMajorPieces(8,ChessGame.TeamColor.BLACK);
        setupPawns(7,ChessGame.TeamColor.BLACK);
    }

    private void clearBoard(){
        for(int row = 0; row < BOARD_SIZE; row++){
            for(int col = 0; col < BOARD_SIZE; col++){
                board[row][col] = null;
            }
        }
    }

    private void setupMajorPieces(int row, ChessGame.TeamColor color){
        ChessPiece.PieceType[] pieces = {
                ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.KING, ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.ROOK
        };

        for(int col = 1; col <= BOARD_SIZE; col++){
            addPiece(new ChessPosition(row, col), new ChessPiece(color, pieces[col-1]));
        }
    }

    private void setupPawns(int row, ChessGame.TeamColor color){
        for(int col = 1; col <= BOARD_SIZE; col++){
            addPiece(new ChessPosition(row,col), new ChessPiece(color, ChessPiece.PieceType.PAWN));
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
        ChessBoard other = (ChessBoard) o;
        return Arrays.deepEquals(this.board, other.board);
    }
}
