package client;

import org.junit.jupiter.api.*;
import model.*;
import server.Server;
import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clear() throws Exception {
        facade.clear();
    }

    @Test
    void registerPositive() throws Exception {
        var authData = facade.register(new RegisterRequest("player1", "password", "1@email.com"));
        assertTrue(authData.authToken().length() > 10);
        assertEquals("player1", authData.username());
    }

    @Test
    void registerNegative() throws Exception {
        facade.register(new RegisterRequest("player1", "password", "1@email.com"));
        assertThrows(Exception.class, () -> {
            facade.register(new RegisterRequest("player1", "password2", "1@email.com"));
        });
    }

    @Test
    void loginPositive() throws Exception {
        facade.register(new RegisterRequest("player1", "password", "1@email.com"));
        var response = facade.login(new RegisterRequest("player1", "password", null));
        assertNotNull(response.authToken());
    }

    @Test
    void loginNegative() throws Exception {
        facade.register(new RegisterRequest("player1", "password", "1@email.com"));
        assertThrows(Exception.class, () -> {
            facade.login(new RegisterRequest("player1", "wrong", null));
        });
    }

    @Test
    void logoutPositive() throws Exception {
        var authenticate = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));
        assertDoesNotThrow(() -> facade.logout(authenticate.authToken()));
    }

    @Test
    void logoutNegative() throws Exception {
        assertThrows(Exception.class, () -> facade.logout("invalid"));
    }

    @Test
    void createGamePositive() throws Exception {
        var authenticate = facade.register(new RegisterRequest("player1", "password", "1@email.com"));
        var gameResponse = facade.createGame(authenticate.authToken(), new CreateGameRequest("Test"));
        assertTrue(gameResponse.gameID() > 0);
    }

    @Test
    void createGameNegative() throws Exception {
        assertThrows(Exception.class, () -> {
            facade.createGame("bad", new CreateGameRequest("Fake Game"));
        });
    }

    @Test
    void listGamesPositive() throws Exception {
        var authenticate = facade.register(new RegisterRequest("player1", "password", "1@email.com"));
        facade.createGame(authenticate.authToken(), new CreateGameRequest("Game 1"));
        var listResponse = facade.listGames(authenticate.authToken());
        assertNotNull(listResponse.games());
        assertFalse(listResponse.games().isEmpty());
    }

    @Test
    void listGamesNegative() throws Exception {
        assertThrows(Exception.class, () -> {
            facade.listGames("fake");
        });
    }

    @Test
    void joinGamePositive() throws Exception {
        var authenticate = facade.register(new RegisterRequest("player1", "password", "1@email.com"));
        var gameResponse = facade.createGame(authenticate.authToken(), new CreateGameRequest("Joinable"));
        assertDoesNotThrow(() -> {
            facade.joinGame(authenticate.authToken(), new JoinGameRequest("WHITE", gameResponse.gameID()));
        });
    }

    @Test
    void joinGameNegative() throws Exception {
        var authenticate = facade.register(new RegisterRequest("player1", "password", "1@email.com"));
        assertThrows(Exception.class, () -> {
            facade.joinGame(authenticate.authToken(), new JoinGameRequest("BLACK", 12345));
        });
    }
}