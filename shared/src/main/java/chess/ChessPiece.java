package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    public PieceType getPieceType() {
        return type;
    }

    private void straightMoves(ChessBoard board, ChessPosition position, int directions[][],
                               int startRow, int startCol, Collection<ChessMove> moves) {
        for (int[] direct : directions) {
            int row = startRow + direct[0];
            int col = startCol + direct[1];
            while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                ChessPosition next = new ChessPosition(row, col);
                ChessPiece atMove = board.getPiece(next);

                if (atMove == null) {
                    moves.add(new ChessMove(position, next, null));
                } else {
                    if (atMove.getTeamColor() != this.pieceColor) {
                        moves.add(new ChessMove(position, next, null));
                    }
                    break;
                }

                row += direct[0];
                col += direct[1];
            }
        }
    }

    private void pawnPromotion(ChessPosition from, ChessPosition to, Collection<ChessMove> moves){
        moves.add(new ChessMove(from,to,PieceType.QUEEN));
        moves.add(new ChessMove(from,to,PieceType.ROOK));
        moves.add(new ChessMove(from,to,PieceType.BISHOP));
        moves.add(new ChessMove(from,to,PieceType.KNIGHT));
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        int startRow = myPosition.getRow();
        int startCol = myPosition.getColumn();

        int[][] diagonalDirections = {
                {1,1},
                {1,-1},
                {-1,1},
                {-1,-1}
        };

        int [][] straightDirections = {
                {1,0},
                {0,1},
                {-1,0},
                {0,-1},
        };

        int [][] knightMoves = {
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
            straightMoves(board, myPosition, diagonalDirections, startRow, startCol, moves);
        }

        if(type == PieceType.KING){

            for(int[] direct : diagonalDirections){
                int row = startRow + direct[0];
                int col = startCol + direct[1];

                if(row < 1 || row > 8 || col <1 || col > 8){
                    continue;
                }
                ChessPosition next = new ChessPosition(row,col);
                ChessPiece atNext = board.getPiece(next);
                if (atNext == null || atNext.getTeamColor() != this.pieceColor){
                    moves.add(new ChessMove(myPosition,next,null));
                }
            }
            for (int[] direct : straightDirections){
                int row = startRow + direct[0];
                int col = startCol + direct[1];

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
            for(int[] set: knightMoves){
                int row = startRow + set[0];
                int col = startCol + set[1];

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
        if(type == PieceType.PAWN){
            int direct;
            int startRDOUBLE;
            int promotionR;
            if(pieceColor == ChessGame.TeamColor.WHITE){
                direct =1;
                startRDOUBLE = 2;
                promotionR = 8;
            } else {
                direct = -1;
                startRDOUBLE = 7;
                promotionR = 1;

            }
            int stepOnRow = startRow + direct;
            if (stepOnRow >= 1 &&  stepOnRow <=8){
                ChessPosition oneFor = new ChessPosition(stepOnRow, startCol);
                if (board.getPiece(oneFor) == null){
                    if(stepOnRow == promotionR){
                        pawnPromotion(myPosition, oneFor, moves);
                    } else {
                        moves.add(new ChessMove(myPosition,oneFor,null));
                    }
                    if (startRow == startRDOUBLE){
                        int twoOnRow = startRow + 2 * direct;
                        ChessPosition twoFor = new ChessPosition(twoOnRow, startCol);
                        if (board.getPiece(twoFor) == null){
                            moves.add(new ChessMove(myPosition,twoFor,null));
                        }
                    }
                }
            }
        int[] capture = {startCol -1, startCol +1};
            for(int column: capture){
                int row = startRow+ direct;
                if (row < 1 || row > 8 || column < 1 || column > 8) {
                    continue;
                }
                ChessPosition next = new ChessPosition(row,column);
                ChessPiece atNext = board.getPiece(next);

                if(atNext != null && atNext.getTeamColor() != this.pieceColor){
                    if(row == promotionR){
                        pawnPromotion(myPosition,next,moves);
                    } else {
                        moves.add(new ChessMove(myPosition,next,null));
                    }
                }
            }
        }
        if(type == PieceType.QUEEN){
            straightMoves(board,myPosition, straightDirections, startRow, startCol, moves);
            straightMoves(board, myPosition, diagonalDirections, startRow, startCol, moves);
        }

        if(type == PieceType.ROOK){
            straightMoves(board, myPosition, straightDirections, startRow, startCol,moves);
        }
        return moves;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public boolean equals(Object object){
        if (this == object) {return true;}
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ChessPiece other = (ChessPiece) object;
        return Objects.equals(pieceColor, other.pieceColor) && Objects.equals(type, other.type);
    }
}
