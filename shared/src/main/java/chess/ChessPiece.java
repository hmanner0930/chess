package chess;

import java.util.ArrayList;
import java.util.Collection;
//for storing lists
import java.util.Objects;
//for comparing at the end
/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor; //what color
    private final ChessPiece.PieceType type; //type of piece
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    } //sets the fields for the constructor

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
    } // this enum is used to identify pieces

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }
    //Getter; returns color/team of piece
    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }
    //Getter; returns type of piece
    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
//     *
//     * @return Collection of valid moves
//     */
    //This is for bishop,queen and rook because of their straight moves
    private void straightMoves(ChessBoard board, ChessPosition pos, int directions[][], int startR, int startC, Collection<ChessMove> moves) {
        for (int[] dir : directions) {
            int row = startR + dir[0];
            int col = startC + dir[1];
            while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                //next is the next square and atMove is the piece(or null) on that square
                ChessPosition next = new ChessPosition(row, col);
                ChessPiece atMove = board.getPiece(next);

                if (atMove == null) {
                    moves.add(new ChessMove(pos, next, null));
                } else {
                    if (atMove.getTeamColor() != this.pieceColor) {
                        moves.add(new ChessMove(pos, next, null));
                    }
                    break;//if it is blocked
                }

                row += dir[0];
                col += dir[1];
            }
        }
    }
    //returns all moves that a piece can make
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>(); //this collects the legal places a move can be made
        int startR = myPosition.getRow(); //current row
        int startC = myPosition.getColumn(); // current column


        int[][] dDirections = {
                {1,1},
                {1,-1},
                {-1,1},
                {-1,-1}
        }; // These are the diagonal directions {row,col}

        int [][] sDirections = {
                {1,0},
                {0,1},
                {-1,0},
                {0,-1},
        };// These are the straight directions {row,col}

        int [][] knightMoves = {
                {2,1},
                {2,-1},
                {-2,1},
                {-2,-1},
                {1,2},
                {1,-2},
                {-1,2},
                {-1,-2}
        };//These are the moves for the knights

        //If piece is bishop execute this
        if(type == PieceType.BISHOP) {

            //Loops over the dDirections from the adjacent square in that direction
            for (int[] dir : dDirections) {
                int row = startR + dir[0];
                int col = startC + dir[1];

                //moves step by step along the diagonal on the bounds of the board
                while (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    //next is the next square and atMove is the piece(or null) on that square
                    ChessPosition next = new ChessPosition(row, col);
                    ChessPiece atMove = board.getPiece(next);
                    //First if the square is empty the bishop can move there
                    //if there is a piece there the bishop can capture it or stops there
                    if (atMove == null) {
                        moves.add(new ChessMove(myPosition, next, null));
                    } else {
                        if (atMove.getTeamColor() != this.pieceColor) {
                            moves.add(new ChessMove(myPosition, next, null));
                        }
                        break;
                    }
                    //advances to the next square repeatedly until blocked
                    row += dir[0];
                    col += dir[1];
                }
            }
        }
        //If piece is king
        if(type == PieceType.KING){
            //Looks for neighbors in the diagonal direction
            //gets neighbor position or skips if it is off the board
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
            //adds the move if the king can move diagonally one square if it is empty or has the enemy piece

            //Looks for neighbors in the straight direction
            //gets position of neighbors or skips if it is off the board
            for (int[] dir: sDirections){
                int row = startR + dir[0];
                int col = startC + dir[1];
                if(row < 1 || row > 8 || col <1 || col > 8){
                    continue;
                }
                //adds the move if the king can move in the straight directions one square if it is empty or has the enemy piece
                ChessPosition next = new ChessPosition(row,col);
                ChessPiece atMove = board.getPiece(next);

                if (atMove == null || atMove.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(myPosition, next, null));
                }
            }
        }
        //No rules yet I don't think

        //If the piece is knight
        //gets the appropiate square and skips it if out of board
        if(type == PieceType.KNIGHT){
            for(int[] set: knightMoves){
                int row = startR + set[0];
                int col = startC + set[1];
                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    continue;
                }
                //knights can go straight to that square through jumps if empty or has enemy piece
                ChessPosition next = new ChessPosition(row, col);
                ChessPiece atMove = board.getPiece(next);
                if (atMove == null || atMove.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(myPosition, next, null));
                }
            }
        }

        //If piece is Pawn
        //Depending on the color of the pawn moving is different
        if(type == PieceType.PAWN){
            int dir;
            int startRDOUBLE;
            int promotionR;
            //dir=1 means the white moves up and dir=-1 means the black moves down
            //startRDOUBLE is at the beginning move
            // promotionR where the promotion is possible
            if(pieceColor == ChessGame.TeamColor.WHITE){
                dir=1;
                startRDOUBLE = 2;
                promotionR = 8;
            } else {
                dir = -1;
                startRDOUBLE = 7;
                promotionR = 1;

            }
            //forward 1
            int stepOnRow = startR + dir;
            if (stepOnRow >= 1 &&  stepOnRow <=8){
                ChessPosition oneFor = new ChessPosition(stepOnRow, startC);
                if (board.getPiece(oneFor) == null){
                    //promotion
                    if(stepOnRow == promotionR){
                        moves.add(new ChessMove(myPosition,oneFor,PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition,oneFor,PieceType.ROOK));
                        moves.add(new ChessMove(myPosition,oneFor,PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition,oneFor,PieceType.KNIGHT));
                    } else {
                        moves.add(new ChessMove(myPosition,oneFor,null));
                    }

                    //forward 2
                    if (startR == startRDOUBLE){
                        int twoOnRow = startR + 2 * dir;
                        ChessPosition twoFor = new ChessPosition(twoOnRow, startC);

                        if (board.getPiece(twoFor) == null){
                            moves.add(new ChessMove(myPosition,twoFor,null));
                        }
                    }
                }
            }

            //this is all the cases of capturing from what I see there is en passant is what it is called in chess but whatever
        int[] capture = {startC -1, startC+1};
            for(int col: capture){
                int row = startR+dir;
                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    continue;
                }
                ChessPosition next = new ChessPosition(row,col);
                ChessPiece atMove = board.getPiece(next);

                if(atMove != null && atMove.getTeamColor() != this.pieceColor){
                    if(row == promotionR){
                        moves.add(new ChessMove(myPosition,next,PieceType.QUEEN));
                        moves.add(new ChessMove(myPosition,next,PieceType.ROOK));
                        moves.add(new ChessMove(myPosition,next,PieceType.BISHOP));
                        moves.add(new ChessMove(myPosition,next,PieceType.KNIGHT));
                    } else {
                        moves.add(new ChessMove(myPosition,next,null));
                    }
                }
            }
        }
        //If the piece is queen first it goes through the diagonal directions first
        if(type == PieceType.QUEEN){
            for(int[] dir: dDirections){
                int row = startR + dir[0];
                int col = startC + dir[1];
                //go through empty squares, stop on enemy and capture, stop on same team
                while(row>=1 && row<=8 && col>=1 && col<=8){
                    ChessPosition next = new ChessPosition(row,col);
                    ChessPiece atMove = board.getPiece(next);

                    if(atMove == null){
                        moves.add(new ChessMove(myPosition,next,null));
                    } else{
                        if(atMove.getTeamColor() != this.pieceColor){
                            moves.add(new ChessMove(myPosition,next,null));
                        }
                        break;
                    }
                    row+=dir[0];
                    col+=dir[1];
                }
            }
            //this goes through the straight directions just combined of bishop and rook through these two
            for (int[] dir: sDirections){
                int row = startR + dir[0];
                int col = startC + dir[1];
                while(row >=1 && row <=8 && col >=1 && col <=8){
                    ChessPosition next = new ChessPosition(row,col);
                    ChessPiece atMove = board.getPiece(next);

                    if(atMove==null){
                        moves.add(new ChessMove(myPosition,next,null));
                    } else {
                        if(atMove.getTeamColor() != this.pieceColor){
                            moves.add(new ChessMove(myPosition,next,null));
                        }
                        break;
                    }
                    row+=dir[0];
                    col+=dir[1];
                }
            }
        }
        //If piece is rook straight directions are used
        if(type == PieceType.ROOK){
            for(int[] dir: sDirections){
                int row = startR+dir[0];
                int col = startC + dir[1];
                //go through empty squares capture it enemy, stop if teammate or off board
                while(row >=1 && row <=8 && col >=1 && col <=8){
                    ChessPosition next = new ChessPosition(row,col);
                    ChessPiece atMove = board.getPiece(next);
                    if(atMove ==null){
                        moves.add(new ChessMove(myPosition,next,null));
                    } else {
                        if (atMove.getTeamColor() != this.pieceColor){
                            moves.add(new ChessMove(myPosition,next,null));
                        }
                        break;
                    }
                    row += dir[0];
                    col += dir[1];
                }
            }
        }
        return moves;
    }
    //this combines the color and type into a single hashcode for tests
    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj) return true; //returns true if two objects are identical
        if (obj == null || getClass() != obj.getClass()) return false; // if obj is null or not a chess piece returns false
        ChessPiece gurt = (ChessPiece) obj; // casts it to gurt as a chess piece
        return Objects.equals(pieceColor, gurt.pieceColor) && Objects.equals(type,gurt.type); //are equal if when same color and piece type
    }
}
