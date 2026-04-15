package service;

import chess.ChessGame;
import dataaccess.*;
import model.*;
import java.util.Collection;

public class GameService {
    private final GameDAO games;
    private final AuthDAO auths;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.games = gameDAO;
        this.auths = authDAO;
    }

    public Collection<GameData> listGames(String token) throws DataAccessException {
        checkAuth(token);
        return games.listGames();
    }

    public int createGame(String token, String gameName) throws DataAccessException {
        checkAuth(token);
        if (gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }
        return games.createGame(new GameData(0,
                null, null, gameName, new ChessGame()));
    }

    public void joinGame(String token, String color, int gameID) throws DataAccessException {
        AuthData user = checkAuth(token);
        GameData game = games.getGame(gameID);

        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }
        if (color == null) {
            return;
        }

        if (color.isEmpty() || (!color.equals("WHITE") && !color.equals("BLACK"))) {
            throw new DataAccessException("Error: bad request");
        }

        String white = game.whiteUsername();
        String black = game.blackUsername();

        if (color.equals("WHITE")) {
            if (white != null) {
                throw new DataAccessException("Error: already taken");
            }
            white = user.username();
        } else {
            if (black != null) {
                throw new DataAccessException("Error: already taken");
            }
            black = user.username();
        }

        games.updateGame(new GameData(gameID, white, black, game.gameName(), game.game()));
    }
    private AuthData checkAuth(String token) throws DataAccessException {
        AuthData auth = auths.getAuth(token);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return auth;
    }
}