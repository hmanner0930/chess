package service;

import dataaccess.*;
import org.junit.jupiter.api.*;

public class UserServiceTests {

    private UserDAO userDAO = new MemoryUserDAO();
    private AuthDAO authDAO = new MemoryAuthDAO();
    private GameDAO gameDAO = new MemoryGameDAO();
    private UserService service = new UserService(userDAO, authDAO);
    private ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);

    @BeforeEach
    public void setup() throws DataAccessException {
        clearService.clear();
    }

    @Test
    @DisplayName("Register_Success ")
    public void registerSuccess() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("player1", "password", "1@email.com");
        RegisterResult result = service.register(request);
        Assertions.assertNotNull(result.authToken());
        Assertions.assertEquals("player1", result.username());
        Assertions.assertNotNull(authDAO.getAuth(result.authToken()));
    }

    @Test
    @DisplayName("Register_Duplicate")
    public void registerDuplicate() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("player1", "password", "1@email.com");
        service.register(request);
        DataAccessException except = Assertions.assertThrows(DataAccessException.class,
                () -> service.register(request));
        Assertions.assertTrue(except.getMessage().contains("taken"));
    }
}