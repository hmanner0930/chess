package dataaccess;
import model.GameData;
import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO{
    private final HashMap<Integer, GameData> games = new HashMap<>();
    private int nextId = 1;

    @Override
    public int createGame(GameData game) {
        GameData newGame = new GameData(nextId, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        games.put(nextId, newGame);
        return nextId++;
    }
    @Override
    public GameData getGame(int gameID){
        return games.get(gameID);
    }
    @Override
    public Collection<GameData> listGames(){
        return games.values();
    }
    @Override
    public void updateGame(GameData game){
        games.put(game.gameID(), game);
    }
    @Override
    public void clear(){
        games.clear();
        nextId = 1;
    }

}
