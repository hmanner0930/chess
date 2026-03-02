package service;

import dataaccess.*;
import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService service;

    @BeforeEach
    public void setup() {
        // Initialize your memory DAOs and Service before every test
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO(); // Make sure you have this implemented!
        service = new UserService(userDAO, authDAO);
    }

    @Test
    @DisplayName("Register Success")
    public void registerPositive() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("NewUser", "password123", "email@test.com");
        RegisterResult result = service.register(request);

        assertNotNull(result.authToken());
        assertEquals("NewUser", result.username());
    }

    @Test
    @DisplayName("Register Fail - User Already Exists")
    public void registerNegative() throws DataAccessException {
        RegisterRequest request = new RegisterRequest("ExistingUser", "password", "e@mail.com");

        // Register the user the first time
        service.register(request);

        // Try to register the same user again; it should throw an exception
        DataAccessException exception = assertThrows(DataAccessException.class, () -> {
            service.register(request);
        });

        // Verify the error message matches your logic from Step 3
        assertEquals("Error: already taken", exception.getMessage());
    }
}
