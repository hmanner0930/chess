package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;

public class SqlGameDAO implements GameDAO {

    private final Gson gson = new Gson();

    public int createGame(GameData gameData) throws DataAccessException {
        String sql = "INSERT INTO game (gameName, game) VALUES (?, ?)";
        String jsonGame = gson.toJson(gameData.game());
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, gameData.gameName());
            preparedStatement.setString(2, jsonGame);
            preparedStatement.executeUpdate();
            try (var resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create game");
        }
        return 0;
    }

    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT gameID, whiteUsername, blackUsername, " +
                "gameName, game FROM game WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {

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
            throw new DataAccessException("failed to get game");
        }
        return null;
    }

    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE game SET whiteUsername=?, blackUsername=?, game=? WHERE gameID=?";
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, game.whiteUsername());
            preparedStatement.setString(2, game.blackUsername());
            preparedStatement.setString(3, gson.toJson(game.game()));
            preparedStatement.setInt(4, game.gameID());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to update game");
        }
    }

    public Collection<GameData> listGames() throws DataAccessException {
        var games = new ArrayList<GameData>();
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game";
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(sql);
             var resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                games.add(new GameData(
                        resultSet.getInt("gameID"),
                        resultSet.getString("whiteUsername"),
                        resultSet.getString("blackUsername"),
                        resultSet.getString("gameName"),
                        gson.fromJson(resultSet.getString("game"), ChessGame.class)
                ));
            }
        } catch (SQLException ex) {
            throw new DataAccessException("failed to list games");
        }
        return games;
    }

    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement("TRUNCATE TABLE game")) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to clear");
        }
    }
}