package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SqlUserDAOTests {
    private final SqlUserDAO userDAO = new SqlUserDAO();
    private final UserData testUser = new UserData("player1", "password", "p1@chess.com");

    @BeforeEach
    void setup() throws DataAccessException {
        DatabaseManager.configureDatabase();
        userDAO.clear();
    }

    @Test
    @DisplayName("Create User Success (Positive)")
    void createUserSuccess() throws DataAccessException {
        assertDoesNotThrow(() -> userDAO.createUser(testUser));
        UserData result = userDAO.getUser(testUser.username());
        assertNotNull(result);
        assertEquals(testUser.username(), result.username());
    }

    @Test
    @DisplayName("Create User Duplicate (Negative)")
    void createUserDuplicate() throws DataAccessException {
        userDAO.createUser(testUser);
        // Attempting to add the same user again should throw a DataAccessException
        assertThrows(DataAccessException.class, () -> userDAO.createUser(testUser));
    }
}