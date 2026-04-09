package dataaccess;

import chess.ChessGame;
import model.GameData;
import java.util.Collection;

public interface GameDAO {
    int createGame(GameData game) throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    Collection<GameData> listGames() throws DataAccessException;

    // Original: Updates everything (Usernames + Game)
    void updateGame(GameData game) throws DataAccessException;

    // NEW: Updates only the board state (Used in MAKE_MOVE)
    void updateGame(int gameID, ChessGame chessGame) throws DataAccessException;

    // NEW: Updates only the players (Used in LEAVE)
    void updateGame(int gameID, String whiteUsername, String blackUsername) throws DataAccessException;

    void clear() throws DataAccessException;
}