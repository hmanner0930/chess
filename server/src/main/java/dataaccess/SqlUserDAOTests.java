package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SqlUserDAOTests {
    private final SqlUserDAO userDAO = new SqlUserDAO();
    private final UserData testUser = new UserData(
            "player", "password", "1@email.com");

    @BeforeEach
    void setup() throws DataAccessException {
        DatabaseManager.configureDatabase();
        userDAO.clear();
    }

    @Test
    @DisplayName("Create User Success(Positive)")
    void createUserSuccess() throws DataAccessException {
        userDAO.createUser(testUser);
        UserData result = userDAO.getUser(testUser.username());
        assertNotNull(result);
        assertEquals(testUser.username(), result.username());
    }

    @Test
    @DisplayName("Create User Duplicate(Negative)")
    void createUserDuplicate() throws DataAccessException {
        userDAO.createUser(testUser);
        assertThrows(DataAccessException.class, () -> userDAO.createUser(testUser));
    }

    @Test
    @DisplayName("Get User Success(Positive)")
    void getUserSuccess() throws DataAccessException {
        userDAO.createUser(testUser);
        UserData result = userDAO.getUser(testUser.username());
        assertNotNull(result);
        assertEquals("1@email.com", result.email());
    }

    @Test
    @DisplayName("Get User Not Found(Negative)")
    void getUserNotFound() throws DataAccessException {
        assertNull(userDAO.getUser("random"));
    }

    @Test
    @DisplayName("Clear Table Success")
    void clearTest() throws DataAccessException {
        userDAO.createUser(testUser);
        userDAO.clear();
        assertNull(userDAO.getUser(testUser.username()));
    }
}