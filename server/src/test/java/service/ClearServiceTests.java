package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;

public class ClearServiceTests {

    @Test
    @DisplayName("Clear_Everything")
    public void clearEverything() throws DataAccessException {
        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        userDAO.createUser(new UserData("a", "b", "c"));
        authDAO.createAuth(new AuthData("auth", "a"));
        gameDAO.createGame(new GameData(1, null, null, "test", null));

        ClearService service = new ClearService(userDAO, authDAO, gameDAO);
        service.clear();
        Assertions.assertNull(userDAO.getUser("a"));
        Assertions.assertNull(authDAO.getAuth("auth"));
        Assertions.assertTrue(gameDAO.listGames().isEmpty());
    }
}