package chess;

import java.util.Collection;

public class ChessGame {
    private ChessBoard board;
    private ChessGame.TeamColor teamTurn;
    private static final int BOARD_SIZE = 8;
    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.teamTurn = TeamColor.WHITE;
    }

    public TeamColor getTeamTurn() {return teamTurn;}

    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

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
        Collection<ChessMove> allMoves = piece.pieceMoves(board,startPosition);
        Collection<ChessMove> legalMoves = new java.util.ArrayList<>();
        TeamColor team = piece.getTeamColor();

        for(ChessMove move: allMoves){
            ChessBoard temp = copyBoard(this.board);
            ChessPosition from = move.getStartPosition();
            ChessPosition to = move.getEndPosition();
            ChessPiece movingPiece = temp.getPiece(from);
            temp.addPiece(from,null);
            ChessPiece piecePlace = movingPiece;

            if(move.getPromotionPiece() != null){
                piecePlace = new ChessPiece(team, move.getPromotionPiece());
            }

            temp.addPiece(to,piecePlace);
            ChessBoard original = this.board;
            this.board = temp;
            boolean inCheck = isInCheck(team);
            this.board = original;

            if(!inCheck){
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        ChessPosition end  = move.getEndPosition();
        ChessPiece piece = board.getPiece(start);

        if(piece==null || piece.getTeamColor() != teamTurn){
            throw new InvalidMoveException("This move is not legal!");
        }

        Collection<ChessMove> legalMoves = validMoves(start);
        if(legalMoves == null || !legalMoves.contains(move)){
            throw new InvalidMoveException("This move is not legal!");
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

    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public int hashCode(){
       return java.util.Objects.hash(teamTurn,board);
    }

    @Override
    public boolean equals(Object object){
        if(this == object) {return true;}
        if(!(object instanceof ChessGame other)) {return false;}
        return teamTurn == other.teamTurn && java.util.Objects.equals(board,other.board);
    }
}
