package service;
import chess.ChessGame;
import dataaccess.*;
import model.*;
import java.util.Collection;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO){
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        verifyAuth(authToken);
        return gameDAO.listGames();
    }

    public int createGame(String authToken, String gameName) throws DataAccessException {
        verifyAuth(authToken);
        if(gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }
        // Initialize with a fresh ChessGame to ensure persistence tests pass
        return gameDAO.createGame(new GameData(0, null, null, gameName, new ChessGame()));
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws DataAccessException {
        AuthData auth = verifyAuth(authToken);
        GameData game = gameDAO.getGame(gameID);

        if (game == null) { throw new DataAccessException("Error: bad request"); }

        // Catch NULL or EMPTY color strings
        if (playerColor == null || playerColor.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        String white = game.whiteUsername();
        String black = game.blackUsername();

        if (playerColor.equals("WHITE")) {
            if (white != null) { throw new DataAccessException("Error: already taken"); }
            white = auth.username();
        } else if (playerColor.equals("BLACK")) {
            if (black != null) { throw new DataAccessException("Error: already taken"); }
            black = auth.username();
        } else {
            // This catches "GREEN" or any other invalid string
            throw new DataAccessException("Error: bad request");
        }

        gameDAO.updateGame(new GameData(gameID, white, black, game.gameName(), game.game()));
    }

    private AuthData verifyAuth(String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return auth;
    }
}