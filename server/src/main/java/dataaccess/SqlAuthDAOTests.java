package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SqlAuthDAOTests {
    private final SqlAuthDAO authDAO = new SqlAuthDAO();
    private final AuthData testAuth = new AuthData("valid-token", "username");

    @BeforeEach
    void setup() throws DataAccessException {
        DatabaseManager.configureDatabase();
        authDAO.clear();
    }

    @Test
    void createAuthSuccess() throws DataAccessException {
        assertDoesNotThrow(() -> authDAO.createAuth(testAuth));
        assertEquals(testAuth, authDAO.getAuth(testAuth.authToken()));
    }

    @Test
    void getAuthNotFound() throws DataAccessException {
        // Negative: Try to get a token that was never created
        assertNull(authDAO.getAuth("fake-token"));
    }

    @Test
    void deleteAuthSuccess() throws DataAccessException {
        authDAO.createAuth(testAuth);
        authDAO.deleteAuth(testAuth.authToken());
        assertNull(authDAO.getAuth(testAuth.authToken()));
    }
}