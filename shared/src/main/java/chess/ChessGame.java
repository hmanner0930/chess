package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board; //Current state of the board
    private ChessGame.TeamColor teamTurn; //turn of black or white
    public ChessGame() {
        this.board = new ChessBoard(); //board created white goes first
        this.board.resetBoard();
        this.teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {return teamTurn;}
    //getter for the turn
    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }
    //setter for team turn
    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }
    //two colors in chess IDK exactly why enum
    /**
     * Gets a valid moves for a piece at the given location
//     *
//     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    //creates a copy of the board like making a move temporarily but then see if king in check
    //If king in check go back on the move
    //Goes through every square copying every current piece
    private ChessBoard copyBoard(ChessBoard src){
        ChessBoard copy = new ChessBoard();
        for(int row = 1; row<=8; row++){
            for(int col=1;col<=8; col++){
                ChessPosition position =new ChessPosition(row,col);
                ChessPiece piece = src.getPiece(position);
                if(piece != null){
                    copy.addPiece(position,new ChessPiece(piece.getTeamColor(), piece.getPieceType()));
                }
            }
        }
        return copy;
    }
    //this returns the legal moves for a piece
    //gets all the moves legal or not; does this by copying the board, applying the move, swapping board with copy
    //then checks if moving team in check then goes back to the original board
    //keeps moves that don't put king in check
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        Collection<ChessMove> allM = piece.pieceMoves(board,startPosition);
        Collection<ChessMove> legalM = new java.util.ArrayList<>();
        TeamColor team = piece.getTeamColor();

        for(ChessMove move: allM){
            ChessBoard temp = copyBoard(this.board);
            ChessPosition from = move.getStartPosition();
            ChessPosition to = move.getEndPosition();
            ChessPiece movingP = temp.getPiece(from);
            temp.addPiece(from,null);
            ChessPiece piecePlace = movingP;

            if(move.getPromotionPiece() != null){
                piecePlace = new ChessPiece(team, move.getPromotionPiece());
            }
            temp.addPiece(to,piecePlace);
            ChessBoard original = this.board;
            this.board = temp;
            boolean inCheck = isInCheck(team);
            this.board = original;

            if(!inCheck){
                legalM.add(move);
            }
        }
        return legalM;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    //Does a move only if it is legal
    //the piece has to exist at the starting place
    //has to be the correct player's turn and the move has to be in valid moves.
    //later on if the pawn promotes replace with new piece
    //it removes piece from the startPosition and places piece at the endPosition then switches turns
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end  = move.getEndPosition();
        ChessPiece piece = board.getPiece(start);

        if(piece==null || piece.getTeamColor() != teamTurn){
            throw new InvalidMoveException("Not this");
        }

        Collection<ChessMove> legalM = validMoves(start);
        if(legalM == null || !legalM.contains(move)){
            throw new InvalidMoveException("Not this");
        }
        ChessPiece lastPiece = piece;
        if(move.getPromotionPiece() != null){
            lastPiece = new ChessPiece(teamTurn, move.getPromotionPiece());
        }

        board.addPiece(start,null);
        board.addPiece(end, lastPiece);

        if(teamTurn == TeamColor.WHITE){
            teamTurn = TeamColor.BLACK;
        } else{
            teamTurn = TeamColor.WHITE;
        }
    }

//    /**
//     * Determines if the given team is in check
//     *
//     * @paramwhich team to check for check
//     * @return True if the specified team is in check
//     */

    //Finds the king's position
    //scans board looking for king
    //then returns its position
    private ChessPosition findKing(TeamColor team){
        for (int row = 1; row<= 8; row++){
            for(int col = 1; col<=8; col++){
                ChessPosition position = new ChessPosition(row,col);
                ChessPiece piece = board.getPiece(position);
                if(piece!= null && piece.getTeamColor() == team && piece.getPieceType() == ChessPiece.PieceType.KING){
                    return position;
                }
            }
        }
        return null;
    }

    //Finds the king
    //Goes through all the enemy pieces
    //Gets possible moves
    //If any move has the king is it check
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);
        TeamColor enemy;

        if(teamColor == TeamColor.WHITE){
            enemy = TeamColor.BLACK;
        } else {
            enemy = TeamColor.WHITE;
        }

        for(int row = 1; row<=8;row++){
            for(int col = 1; col <=8; col++){
                ChessPosition from = new ChessPosition(row,col);
                ChessPiece piece = board.getPiece(from);
                if(piece==null||piece.getTeamColor() != enemy){
                    continue;
                }
                Collection<ChessMove> moves = piece.pieceMoves(board,from);
                for(ChessMove move : moves){
                    if(move.getEndPosition().equals(kingPosition)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */

    //In check and has no valid moves
    //obviously not in check not checkmate
    //For every piece on team get their valid moves and if any are possible to escape check then not checkmate
    //all those fail it is checkmate
    public boolean isInCheckmate(TeamColor teamColor) {
        if(!isInCheck(teamColor)){
            return false;
        }
        for(int row =1; row <=8; row++){
            for(int col = 1; col<=8; col++){
                ChessPosition position = new ChessPosition(row,col);
                ChessPiece piece = board.getPiece(position);
                if(piece==null ||piece.getTeamColor() != teamColor){
                    continue;
                }
                Collection<ChessMove> moves = validMoves(position);
                if(moves != null && !moves.isEmpty()){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */

    //Same logic as isInCheckmate but it is not in check
    public boolean isInStalemate(TeamColor teamColor) {
        if(isInCheck(teamColor)){
            return false;
        }

        for(int row = 1; row<=8; row++){
            for(int col = 1; col<=8; col++){
                ChessPosition position = new ChessPosition(row,col);
                ChessPiece piece = board.getPiece(position);
                if(piece == null || piece.getTeamColor() != teamColor){
                    continue;
                }

                Collection<ChessMove> moves = validMoves(position);
                if(moves != null && !moves.isEmpty()){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */

    //
    public void setBoard(ChessBoard board) {
        this.board = board;
    }
    //setter for board
    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
    //getter for board

    @Override
    public int hashCode(){
       return java.util.Objects.hash(teamTurn,board);
    }

    //Two ChessGame objects are equal if same team's turn and they have the same board
    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(!(obj instanceof ChessGame other)) return false;
        return teamTurn == other.teamTurn && java.util.Objects.equals(board,other.board);
    }
}
