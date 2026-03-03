package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import java.util.Collection;

public class GameServiceTests {

    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private GameService gameService;
    private String existingAuth;

    @BeforeEach
    public void setup() throws DataAccessException {
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        gameService = new GameService(gameDAO, authDAO);

        existingAuth = "testToken";
        authDAO.createAuth(new AuthData(existingAuth, "player1"));
    }

    @Test
    @DisplayName("Create_Game_Success")
    public void createGameSuccess() throws DataAccessException {
        int gameID = gameService.createGame(existingAuth, "New Game");
        Assertions.assertNotNull(gameDAO.getGame(gameID));
    }

    @Test
    @DisplayName("Create_Game_Bad_Request")
    public void createGameBadRequest() {
        Assertions.assertThrows(DataAccessException.class, () ->
                gameService.listGames("invalid")
        );
    }

    @Test
    @DisplayName("List_Games_Success")
    public void listGamesSuccess() throws DataAccessException {
        gameService.createGame(existingAuth, "game 1");
        gameService.createGame(existingAuth, "game 2");

        Collection<GameData> games = gameService.listGames(existingAuth);
        Assertions.assertEquals(2, games.size());
    }

    @Test
    @DisplayName("List_Games_Unauthorized")
    public void listGamesUnauthorized() {
        Assertions.assertThrows(DataAccessException.class, () -> gameService.listGames(null));
    }

    @Test
    @DisplayName("Join_Game_Success")
    public void joinGameSuccess() throws DataAccessException {
        int gameID = gameService.createGame(existingAuth, "Join");
        gameService.joinGame(existingAuth, "WHITE", gameID);
        Assertions.assertEquals("player1", gameDAO.getGame(gameID).whiteUsername());
    }

    @Test
    @DisplayName("Join_Game_Color_Taken")
    public void joinGameAlreadyTaken() throws DataAccessException {
        int gameID = gameService.createGame(existingAuth, "full");
        gameService.joinGame(existingAuth, "WHITE", gameID);
        authDAO.createAuth(new AuthData("token2", "player2"));
        Assertions.assertThrows(DataAccessException.class, () ->
                gameService.joinGame("token2", "WHITE", gameID)
        );
    }
}