package service;
import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.*;

import javax.xml.crypto.Data;
import java.util.Collection;
public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    public GameService(GameDAO gameDAO, AuthDAO authDAO){
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }
    private void verifyAuth(String authToken) throws DataAccessException{
        if(authDAO.getAuth(authToken) == null){
            throw new DataAccessException("Error");
        }
    }
    public Collection<GameData> listGames(String authToken) throws DataAccessException{
        verifyAuth(authToken);
        return gameDAO.listGames();
    }
    public CreateGameResult createGame(String authToken, CreateGameRequest req) throws DataAccessException {
        verifyAuth(authToken);
        if(req.gameName() == null) throw new DataAccessException("Error");
        int gameID = gameDAO.createGame(req.gameName());
        return new CreateGameResult(gameID);
    }
    public void joinGame(String authToken, JoinGameRequest req) throws DataAccessException{
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) throw new DataAccessException("Error: unauthorized");

        GameData game = gameDAO.getGame(req.gameID());
        if (game == null) throw new DataAccessException("Error: bad request");
    }
}
