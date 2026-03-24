package client;

import java.util.Arrays;
import model.*;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private String authToken = null;
    private State state = State.SIGNEDOUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(8080); // Or pass the port dynamically
        this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);

        return switch (cmd) {
            case "login" -> login(params);
            case "register" -> register(params);
            case "quit" -> "quit";
            default -> help();
        };
    }

    public String login(String... params) {
        if (params.length >= 2) {
            try {
                String username = params[0];
                String password = params[1];
                var res = server.login(new RegisterRequest(username, password, null));
                authToken = res.authToken();
                state = State.SIGNEDIN;
                return String.format("Logged in as %s.", username);
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        return "Expected: <USERNAME> <PASSWORD>";
    }

    public String register(String... params) {
        if (params.length >= 3) {
            try {
                var res = server.register(new RegisterRequest(params[0], params[1], params[2]));
                authToken = res.authToken();
                state = State.SIGNEDIN;
                return "Registered and logged in.";
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        return "Expected: <USERNAME> <PASSWORD> <EMAIL>";
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                    login <USERNAME> <PASSWORD> - to play chess
                    quit - playing chess
                    help - with possible commands
                    """;
        }
        return """
                create <NAME> - a game
                list - games
                join <ID> [WHITE|BLACK] - a game
                observe <ID> - a game
                logout - when you are done
                quit - playing chess
                help - with possible commands
                """;
    }

    public String getState() {
        return state.toString();
    }

    private enum State { SIGNEDOUT, SIGNEDIN }
}