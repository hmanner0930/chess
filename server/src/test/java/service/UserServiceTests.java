package service;

import dataaccess.*;
import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserService service;
    private ClearService clearService;

    @BeforeEach
    public void setup() {
        // Use your Memory versions for testing
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();

        service = new UserService(userDAO, authDAO);
        clearService = new ClearService(userDAO, authDAO, gameDAO);

        // Ensure a clean state before every test
        try {
            clearService.clear();
        } catch (DataAccessException e) {
            fail("Clear failed during setup");
        }
    }

    @Test
    @DisplayName("Register Success (Positive)")
    public void registerSuccess() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");
        RegisterResult result = service.register(request);

        assertNotNull(result.authToken(), "Auth token should be generated");
        assertEquals("player1", result.username());

        // Verify it's actually in the DAO
        AuthData auth = authDAO.getAuth(result.authToken());
        assertNotNull(auth, "AuthData should exist in DAO");
    }

    @Test
    @DisplayName("Register Duplicate User (Negative)")
    public void registerDuplicate() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("player1", "password", "p1@email.com");

        // First registration
        service.register(request);

        // Second registration with same username should throw exception
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            service.register(request);
        });

        assertTrue(exception.getMessage().contains("already taken"), "Should return 'already taken' error");
    }
}