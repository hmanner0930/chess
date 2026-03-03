package service;

import chess.ChessGame;
import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import java.util.Collection;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private GameService gameService;
    private UserService userService;
    private String existingAuth;

    @BeforeEach
    public void setup() throws DataAccessException {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        gameService = new GameService(gameDAO, authDAO);
        userService = new UserService(userDAO, authDAO);

        // Register a default user to get a valid authToken for testing
        RegisterResult result = userService.register(new RegisterRequest("player1", "password", "p1@email.com"));
        existingAuth = result.authToken();
    }

    @Test
    @DisplayName("Create Game Success (Positive)")
    public void createGameSuccess() throws DataAccessException {
        int gameID = gameService.createGame(existingAuth, "Epic Chess Match");

        assertNotNull(gameDAO.getGame(gameID), "Game should exist in DAO after creation");
        assertEquals("Epic Chess Match", gameDAO.getGame(gameID).gameName());
    }

    @Test
    @DisplayName("Create Game Invalid Auth (Negative)")
    public void createGameUnauthorized() {
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame("fake-token", "Failed Game");
        });
        assertTrue(exception.getMessage().contains("unauthorized"), "Should throw unauthorized error");
    }

    @Test
    @DisplayName("List Games Success (Positive)")
    public void listGamesSuccess() throws DataAccessException {
        gameService.createGame(existingAuth, "Game 1");
        gameService.createGame(existingAuth, "Game 2");

        Collection<GameData> games = gameService.listGames(existingAuth);
        assertEquals(2, games.size(), "Should return exactly two games");
    }

    @Test
    @DisplayName("List Games Unauthorized (Negative)")
    public void listGamesUnauthorized() {
        assertThrows(DataAccessException.class, () -> gameService.listGames(null));
    }

    @Test
    @DisplayName("Join Game Success (Positive)")
    public void joinGameSuccess() throws DataAccessException {
        int gameID = gameService.createGame(existingAuth, "Joinable Game");

        gameService.joinGame(existingAuth, "WHITE", gameID);

        GameData game = gameDAO.getGame(gameID);
        assertEquals("player1", game.whiteUsername(), "User should be assigned to the WHITE position");
    }

    @Test
    @DisplayName("Join Game Color Already Taken (Negative)")
    public void joinGameAlreadyTaken() throws DataAccessException {
        int gameID = gameService.createGame(existingAuth, "Full Game");
        gameService.joinGame(existingAuth, "BLACK", gameID);

        // Create a second user to try and take the same spot
        RegisterResult result2 = userService.register(new RegisterRequest("player2", "pass", "p2@email.com"));
        String secondAuth = result2.authToken();

        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(secondAuth, "BLACK", gameID);
        });

        assertTrue(exception.getMessage().contains("already taken"), "Should throw already taken error");
    }
}