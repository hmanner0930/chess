package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTests {

    @Test
    @DisplayName("Clear Full Database (Positive)")
    public void clearEverything() throws DataAccessException {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        // Populate with dummy data
        userDAO.createUser(new UserData("u", "p", "e"));
        authDAO.createAuth(new AuthData("token", "u"));
        gameDAO.createGame(new GameData(1, null, null, "test", null));

        ClearService service = new ClearService(userDAO, authDAO, gameDAO);
        service.clear();

        // Assert all DAOs are empty
        assertNull(userDAO.getUser("u"));
        assertNull(authDAO.getAuth("token"));
        assertTrue(gameDAO.listGames().isEmpty());
    }
}