package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;

public class SqlGameDAOTests {
    private final SqlGameDAO gameDAO = new SqlGameDAO();
    private final GameData test = new GameData(0, null, null, "test", new ChessGame());

    @BeforeEach
    void setup() throws DataAccessException {
        DatabaseManager.configureDatabase();
        gameDAO.clear();
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        int id = gameDAO.createGame(test);
        assertTrue(id>0);
    }

    @Test
    void createGameFail() throws DataAccessException {
        assertNull(gameDAO.getGame(-1));
    }

    @Test
    void updateGameSuccess() throws DataAccessException {
        int id = gameDAO.createGame(test);
        GameData update = new GameData(id, "white",
                "black", "New", new ChessGame());
        gameDAO.updateGame(update);
        assertEquals("white", gameDAO.getGame(id).whiteUsername());
    }

    @Test
    void updateGameFail() throws DataAccessException {
        GameData noExist = new GameData(10, "a",
                "b","name", new ChessGame());
        assertDoesNotThrow(() -> gameDAO.updateGame(noExist));
        assertNull(gameDAO.getGame(10));
    }

    @Test
    void listGamesSuccess() throws DataAccessException {
        gameDAO.createGame(test);
        Collection<GameData> games = gameDAO.listGames();
        assertEquals(1, games.size());
    }

    @Test
    void listGamesNone() throws DataAccessException {
        assertTrue(gameDAO.listGames().isEmpty());
    }

    @Test
    void clearTest() throws DataAccessException {
        gameDAO.createGame(test);
        gameDAO.clear();
        assertTrue(gameDAO.listGames().isEmpty());
    }
}