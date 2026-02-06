package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private ChessGame.TeamColor teamTurn;
    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.teamTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {return teamTurn;}

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
//     *
//     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */

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

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if(piece==null) return null;
        Collection<ChessMove> rawM = piece.pieceMoves(board,startPosition);
        Collection<ChessMove> legalM = new java.util.ArrayList<>();
        TeamColor team = piece.getTeamColor();

        for(ChessMove move: rawM){
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
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if(move == null){
            throw new InvalidMoveException("Not this");
        }
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
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKing(teamColor);
        if(kingPosition==null){
            return false;
        }

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
    public boolean isInStalemate(TeamColor teamColor) {
        if(isInCheck(teamColor)){
            return false;
        }
        for(int row = 1; row<=8; row++){
            for(int col = 1; col<=8; col++){
                ChessPosition pos = new ChessPosition(row,col);
                ChessPiece piece = board.getPiece(pos);
                if(piece == null || piece.getTeamColor() != teamColor){
                    continue;
                }

                Collection<ChessMove> moves = validMoves(pos);
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
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public int hashCode(){
        int result = 17;
        result = 31 * result + (teamTurn != null ? teamTurn.hashCode(): 0);
        result = 31 * result + (board != null ? board.hashCode(): 0);
        return result;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        ChessGame other = (ChessGame) obj;
        if(this.teamTurn != other.teamTurn) return false;
        if(this.board == null && other.board == null) return true;
        if(this.board == null || other.board == null) return false;
        return this.board.equals(other.board);
    }
}
