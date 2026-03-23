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
        var authData = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));
        assertTrue(authData.authToken().length() > 10);
        assertEquals("player1", authData.username());
    }

    @Test
    void registerNegative() throws Exception {
        facade.register(new RegisterRequest("player1", "password", "p1@email.com"));
        // Attempt to register same username again - should throw exception (403)
        assertThrows(Exception.class, () -> {
            facade.register(new RegisterRequest("player1", "pass2", "p2@email.com"));
        });
    }

    @Test
    void loginPositive() throws Exception {
        facade.register(new RegisterRequest("player1", "password", "p1@email.com"));
        // Using RegisterRequest for login as requested (email is null)
        var res = facade.login(new RegisterRequest("player1", "password", null));
        assertNotNull(res.authToken());
    }

    @Test
    void loginNegative() throws Exception {
        facade.register(new RegisterRequest("player1", "password", "p1@email.com"));
        // Wrong password - should throw exception (401)
        assertThrows(Exception.class, () -> {
            facade.login(new RegisterRequest("player1", "wrong_password", null));
        });
    }

    @Test
    void logoutPositive() throws Exception {
        var auth = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    void logoutNegative() throws Exception {
        // Unauthorized logout (invalid token)
        assertThrows(Exception.class, () -> facade.logout("invalid_token"));
    }

    @Test
    void createGamePositive() throws Exception {
        var auth = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));
        var gameRes = facade.createGame(auth.authToken(), new CreateGameRequest("Test Game"));
        assertTrue(gameRes.gameID() > 0);
    }

    @Test
    void createGameNegative() throws Exception {
        // Attempt to create game without being logged in
        assertThrows(Exception.class, () -> {
            facade.createGame("bad_token", new CreateGameRequest("Ghost Game"));
        });
    }

    @Test
    void listGamesPositive() throws Exception {
        var auth = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));
        facade.createGame(auth.authToken(), new CreateGameRequest("Game 1"));

        var listRes = facade.listGames(auth.authToken());
        assertNotNull(listRes.games());
        assertFalse(listRes.games().isEmpty());
    }

    @Test
    void listGamesNegative() throws Exception {
        // Unauthorized list
        assertThrows(Exception.class, () -> {
            facade.listGames("fake_token");
        });
    }

    @Test
    void joinGamePositive() throws Exception {
        var auth = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));
        var gameRes = facade.createGame(auth.authToken(), new CreateGameRequest("Joinable"));

        assertDoesNotThrow(() -> {
            facade.joinGame(auth.authToken(), new JoinGameRequest("WHITE", gameRes.gameID()));
        });
    }

    @Test
    void joinGameNegative() throws Exception {
        var auth = facade.register(new RegisterRequest("player1", "password", "p1@email.com"));
        // Join a game that doesn't exist
        assertThrows(Exception.class, () -> {
            facade.joinGame(auth.authToken(), new JoinGameRequest("BLACK", 12345));
        });
    }
}