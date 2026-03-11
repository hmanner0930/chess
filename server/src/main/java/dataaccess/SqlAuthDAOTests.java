package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SqlAuthDAOTests {
    private final SqlAuthDAO authDAO = new SqlAuthDAO();
    private final AuthData testAuth = new AuthData("valid", "username");

    @BeforeEach
    void setup() throws DataAccessException {
        DatabaseManager.configureDatabase();
        authDAO.clear();
    }

    @Test
    void createAuthSuccess() throws DataAccessException {
        authDAO.createAuth(testAuth);
        assertEquals(testAuth, authDAO.getAuth("valid"));
    }
    @Test
    void createAuthFail() throws DataAccessException{
        assertThrows(DataAccessException.class, () ->
                authDAO.createAuth(new AuthData(null,null)));
    }

   @Test
   void getAuthFail() throws DataAccessException {
        assertNull(authDAO.getAuth("invalid"));
   }

   @Test
    void getAuthSuccess() throws DataAccessException {
        authDAO.createAuth(testAuth);
        assertNotNull(authDAO.getAuth("valid"));
   }
    @Test
    void deleteAuthSuccess() throws DataAccessException {
        authDAO.createAuth(testAuth);
        authDAO.deleteAuth(testAuth.authToken());
        assertNull(authDAO.getAuth(testAuth.authToken()));
    }
    @Test
    void clearTest() throws DataAccessException {
        authDAO.createAuth(testAuth);
        authDAO.clear();
        assertNull(authDAO.getAuth("valid"));
    }
}