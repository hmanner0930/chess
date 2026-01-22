package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        int startR = myPosition.getRow();
        int startC = myPosition.getColumn();

        int[][] dDirections = {
                {1,1},
                {1,-1},
                {-1,1},
                {-1,-1}
        };
        int [][] sDirections = {
                {1,0},
                {0,1},
                {-1,0},
                {0,-1},
        };
        int [][] knightSets = {
                {2,1},
                {2,-1},
                {-2,1},
                {-2,-1},
                {1,2},
                {1,-2},
                {-1,2},
                {-1,-2}
        };
        if(type == PieceType.BISHOP) {


            for (int[] dir : dDirections) {
                int row = startR + dir[0];
                int col = startC + dir[1];

                while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition next = new ChessPosition(row, col);
                    ChessPiece atMove = board.getPiece(next);
                    if (atMove == null) {
                        moves.add(new ChessMove(myPosition, next, null));
                    } else {
                        if (atMove.getTeamColor() != this.pieceColor) {
                            moves.add(new ChessMove(myPosition, next, null));
                        }
                        break;
                    }
                    row += dir[0];
                    col += dir[1];
                }
            }
        }
        if(type == PieceType.KING){
            for(int[] dir: dDirections){
                int row = startR + dir[0];
                int col = startC + dir[1];
                if(row < 1 || row > 8 || col <1 || col > 8){
                    continue;
                }
                ChessPosition next = new ChessPosition(row,col);
                ChessPiece atMove = board.getPiece(next);
                if (atMove == null || atMove.getTeamColor() != this.pieceColor){
                    moves.add(new ChessMove(myPosition,next,null));
                }
            }
            for (int[] dir: sDirections){
                int row = startR + dir[0];
                int col = startC + dir[1];
                if(row < 1 || row > 8 || col <1 || col > 8){
                    continue;
                }
                ChessPosition next = new ChessPosition(row,col);
                ChessPiece atMove = board.getPiece(next);

                if (atMove == null || atMove.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(myPosition, next, null));
                }
            }
        }
        if(type == PieceType.KNIGHT){
            for(int[] set: knightSets){
                int row = startR + set[0];
                int col = startC + set[1];

                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    continue;
                }
                ChessPosition next = new ChessPosition(row, col);
                ChessPiece atMove = board.getPiece(next);
                if (atMove == null || atMove.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(myPosition, next, null));
                }
            }
        }
        return moves;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return Objects.equals(pieceColor, that.pieceColor) && Objects.equals(type,that.type);
    }
}
