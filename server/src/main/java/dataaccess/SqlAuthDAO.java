package dataaccess;

import model.AuthData;
import java.sql.*;

public class SqlAuthDAO implements AuthDAO{
    public void createAuth(AuthData auth) throws DataAccessException {
        var sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection(); var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, auth.authToken());
            preparedStatement.setString(2, auth.username());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) { throw new DataAccessException("Failed to Create Auth", ex); }
    }
    public AuthData getAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement("SELECT authToken, username FROM auth WHERE authToken=?");
            preparedStatement.setString(1, authToken);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new AuthData(resultSet.getString(1), resultSet.getString(2));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get Auth", ex);
        }
        return null;
    }
    public void deleteAuth(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement("DELETE FROM auth WHERE authToken=?");
            preparedStatement.setString(1, authToken);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to delete auth", ex);
        }
    }

    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            conn.prepareStatement("TRUNCATE TABLE auth").executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to clear", ex);
        }
    }
}
