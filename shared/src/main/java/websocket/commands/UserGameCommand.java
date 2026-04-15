package websocket.commands;

import java.util.Objects;

public class UserGameCommand {

    private final CommandType type;

    private final String authToken;

    private final Integer ID;

    public UserGameCommand(CommandType commandType, String authToken, Integer gameID) {
        this.type = commandType;
        this.authToken = authToken;
        this.ID = gameID;
    }

    public enum CommandType {
        CONNECT,
        MAKE_MOVE,
        LEAVE,
        RESIGN
    }

    public CommandType getCommandType() {
        return type;
    }

    public String getAuthToken() {
        return authToken;
    }

    public Integer getGameID() {
        return ID;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof UserGameCommand that)) {
            return false;
        }
        return getCommandType() == that.getCommandType() &&
                Objects.equals(getAuthToken(), that.getAuthToken()) &&
                Objects.equals(getGameID(), that.getGameID());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getCommandType(), getAuthToken(), getGameID());
    }
}
