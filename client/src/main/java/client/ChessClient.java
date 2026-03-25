package client;

import java.util.*;
import model.*;
import chess.*;
import ui.BoardDrawer;

public class ChessClient {
    private final ServerFacade server;
    private String authToken = null;
    private State state = State.SIGNEDOUT;

    // Maps the UI number (1, 2, 3) to the actual Game ID (5829, etc.)
    private final Map<Integer, Integer> gameListCache = new HashMap<>();

    public ChessClient(String serverUrl) {
        // Pass the port to your facade
        server = new ServerFacade(8080);
    }

    public String eval(String input) {
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);

        return switch (cmd) {
            case "login" -> login(params);
            case "register" -> register(params);
            case "logout" -> logout();
            case "create" -> createGame(params);
            case "list" -> listGames();
            case "join" -> joinGame(params);
            case "observe" -> observeGame(params);
            case "quit" -> "quit";
            default -> help();
        };
    }

    // --- Post-Login Logic ---

    public String createGame(String... params) {
        assertLoggedIn();
        if (params.length >= 1) {
            try {
                server.createGame(authToken, new CreateGameRequest(params[0]));
                return "Game '" + params[0] + "' created successfully!";
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        return "Expected: <NAME>";
    }

    public String listGames() {
        assertLoggedIn();
        try {
            var res = server.listGames(authToken);
            gameListCache.clear();

            StringBuilder sb = new StringBuilder("\nActive Games:\n");
            int i = 1;
            for (var game : res.games()) {
                gameListCache.put(i, game.gameID());
                sb.append(String.format(" %d. %s (White: %s, Black: %s)\n",
                        i++, game.gameName(),
                        game.whiteUsername() != null ? game.whiteUsername() : "empty",
                        game.blackUsername() != null ? game.blackUsername() : "empty"));
            }
            return sb.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String joinGame(String... params) {
        assertLoggedIn();
        if (params.length >= 2) {
            try {
                int listNum = Integer.parseInt(params[0]);
                String color = params[1].toUpperCase();

                Integer gameID = gameListCache.get(listNum);
                if (gameID == null) return "Invalid game number. Please run 'list' first.";

                server.joinGame(authToken, new JoinGameRequest(color, gameID));

                // Requirement: Draw the board upon joining
                displayBoard(color.equalsIgnoreCase("WHITE"));

                return String.format("Joined game %d as %s", listNum, color);
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        }
        return "Expected: <ID> [WHITE|BLACK]";
    }

    public String observeGame(String... params) {
        assertLoggedIn();
        if (params.length >= 1) {
            try {
                int listNum = Integer.parseInt(params[0]);
                Integer gameID = gameListCache.get(listNum);
                if (gameID == null) return "Invalid game number. Please run 'list' first.";

                server.joinGame(authToken, new JoinGameRequest(null, gameID));

                // Requirement: Draw the board (defaulting to white view for observers)
                displayBoard(true);

                return "Observing game " + listNum;
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        return "Expected: <ID>";
    }

    private void displayBoard(boolean isWhiteView) {
        ChessBoard board = new ChessBoard();
        board.resetBoard(); // Setup default pieces
        System.out.println();
        BoardDrawer.drawBoard(board, true);  // Show White Perspective
        System.out.println();
        BoardDrawer.drawBoard(board, false); // Show Black Perspective
        System.out.println();
    }

    public String logout() {
        try {
            server.logout(authToken);
            authToken = null;
            state = State.SIGNEDOUT;
            gameListCache.clear();
            return "Logged out.";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    // --- Auth Logic ---

    public String login(String... params) {
        if (params.length >= 2) {
            try {
                var res = server.login(new RegisterRequest(params[0], params[1], null));
                authToken = res.authToken();
                state = State.SIGNEDIN;
                return String.format("Logged in as %s.", params[0]);
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

    private void assertLoggedIn() {
        if (state != State.SIGNEDIN) {
            throw new RuntimeException("You must log in first.");
        }
    }

    public String getState() {
        return state.toString();
    }

    private enum State { SIGNEDOUT, SIGNEDIN }
}