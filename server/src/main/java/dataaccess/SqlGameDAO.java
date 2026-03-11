package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class SqlGameDAO implements GameDAO {

    private final Gson gson = new Gson();

    public int createGame(model.GameData gameData) throws DataAccessException {
        String sql = "INSERT INTO game (gameName, game) VALUES (?, ?)";
        String jsonGame = new com.google.gson.Gson().toJson(new chess.ChessGame());

        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, gameData.gameName());
                preparedStatement.setString(2, jsonGame);
                preparedStatement.executeUpdate();
                var resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("failed to create game", ex);
        }
            return 0;
    }

    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement("SELECT * FROM game WHERE gameID=?");
            preparedStatement.setInt(1, gameID);
            try (var resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new GameData(
                            resultSet.getInt("gameID"),
                            resultSet.getString("whiteUsername"),
                            resultSet.getString("blackUsername"),
                            resultSet.getString("gameName"),
                            gson.fromJson(resultSet.getString("game"), ChessGame.class)
                    );
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get game", ex);
        }
        return null;
    }

    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE game SET whiteUsername=?, blackUsername=?, game=? WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setString(1, game.whiteUsername());
                preparedStatement.setString(2, game.blackUsername());
                preparedStatement.setString(3, new com.google.gson.Gson().toJson(game.game()));
                preparedStatement.setInt(4, game.gameID());
                preparedStatement.executeUpdate();
            }
        } catch (java.sql.SQLException ex) {
            throw new DataAccessException("failed to update game",ex);
        }
    }

    public Collection<GameData> listGames() throws DataAccessException {
        var gamesAvailable = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            var preparedStatement = conn.prepareStatement("SELECT * FROM game");
            try (var resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    gamesAvailable.add(new GameData(
                            resultSet.getInt("gameID"),
                            resultSet.getString("whiteUsername"),
                            resultSet.getString("blackUsername"),
                            resultSet.getString("gameName"),
                            gson.fromJson(resultSet.getString("game"), ChessGame.class)
                    ));
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to list games", ex);
        }
        return gamesAvailable;
    }

    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            conn.prepareStatement("TRUNCATE TABLE game").executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to clear", ex);
        }
    }
}