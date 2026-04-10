package dataaccess;

import model.GameData;
import chess.ChessGame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MemoryGameDAO implements GameDAO {
    private final List<GameData> games = new ArrayList<>();
    private int next = 1;

    @Override
    public int createGame(GameData game) {
        GameData newGame = new GameData(next, game.whiteUsername(),
                game.blackUsername(), game.gameName(), game.game());
        games.add(newGame);
        return next++;
    }

    @Override
    public GameData getGame(int gameID) {
        for (GameData game : games) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        return null;
    }

    @Override
    public Collection<GameData> listGames() {
        return games;
    }

    @Override
    public void updateGame(int gameID, ChessGame game) throws DataAccessException {
        GameData existingGame = getGame(gameID);
        if (existingGame == null) {
            throw new DataAccessException("Error: game not found");
        }
        GameData updatedGame = new GameData(gameID, existingGame.whiteUsername(),
                existingGame.blackUsername(), existingGame.gameName(), game);
        updateGame(updatedGame);
    }

    @Override
    public void updateGame(int gameID, String whiteUsername, String blackUsername) throws DataAccessException {
        GameData existingGame = getGame(gameID);
        if (existingGame == null) {
            throw new DataAccessException("Error: game not found");
        }
        GameData updatedGame = new GameData(gameID, whiteUsername, blackUsername,
                existingGame.gameName(), existingGame.game());
        updateGame(updatedGame);
    }

    public void updateGame(GameData updateGame) {
        for (int i = 0; i < games.size(); i++) {
            if (games.get(i).gameID() == updateGame.gameID()) {
                games.set(i, updateGame);
                return;
            }
        }
    }

    @Override
    public void clear() {
        games.clear();
        next = 1;
    }
}