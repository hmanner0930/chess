package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SqlGameDAOTests {
    private final SqlGameDAO gameDAO = new SqlGameDAO();

    @BeforeEach
    void setup() throws DataAccessException {
        DatabaseManager.configureDatabase();
        gameDAO.clear();
    }

    @Test
    void createGameSuccess() throws DataAccessException {
        int id = gameDAO.createGame(new GameData(0, null, null, "Test Game", new ChessGame()));
        assertTrue(id > 0);
    }

    @Test
    void getGameFail() throws DataAccessException {
        // Negative: Search for an ID that doesn't exist
        assertNull(gameDAO.getGame(9999));
    }

    @Test
    void updateGameSuccess() throws DataAccessException {
        GameData initial = new GameData(0, null, null, "Update Test", new ChessGame());
        int id = gameDAO.createGame(initial);

        GameData updated = new GameData(id, "whitePlayer", "blackPlayer", "Update Test", new ChessGame());
        gameDAO.updateGame(updated);

        GameData result = gameDAO.getGame(id);
        assertEquals("whitePlayer", result.whiteUsername());
        assertEquals("blackPlayer", result.blackUsername());
    }
}