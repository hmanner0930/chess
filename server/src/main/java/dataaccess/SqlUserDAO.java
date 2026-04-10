package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class SqlUserDAO implements UserDAO{
    public void createUser(UserData user) throws DataAccessException{
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        String sql = "Insert INTO user (username, password, email) VALUES (?, ?, ?)";
        try(var connection = DatabaseManager.getConnection()){
            try(var preparedStatement = connection.prepareStatement(sql)){
                preparedStatement.setString(1, user.username());
                preparedStatement.setString(2, hashedPassword);
                preparedStatement.setString(3, user.email());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException exception){
            throw new DataAccessException("failed create user", exception);
        }
    }

    public UserData getUser(String username) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            var preparedStatement = connection.prepareStatement("SELECT username, password, email FROM user WHERE username=?");
            preparedStatement.setString(1, username);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new UserData(resultSet.getString(1),
                            resultSet.getString(2), resultSet.getString(3));
                }
            }
        } catch (SQLException exception) {
            throw new DataAccessException("failed et user", exception);
        }
        return null;
    }

    public void clear() throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            connection.prepareStatement("TRUNCATE TABLE user").executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to clear", ex);
        }
    }
}
