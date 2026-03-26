package client;

import java.util.*;
import model.*;
import chess.*;
import ui.BoardDrawer;

public class ChessClient {
    private final ServerFacade server;
    private String authToken = null;
    private State state = State.SIGNEDOUT;

    private final Map<Integer, Integer> gameListCache = new HashMap<>();

    public ChessClient(String serverUrl) {
        server = new ServerFacade(8080);
    }

    public String eval(String input) {
        String[] tokens = input.toLowerCase().split("\\s+");
        String command;
        if (tokens.length > 0) {
            command = tokens[0];
        } else {
            command = "help";
        }
        return switch (command) {
            case "login" -> login(tokens);
            case "register" -> register(tokens);
            case "logout" -> logout();
            case "create" -> createGame(tokens);
            case "list" -> listGames();
            case "join" -> joinGame(tokens);
            case "observe" -> observeGame(tokens);
            case "quit" -> "quit";
            default -> help();
        };
    }

    public String createGame(String... parameters) {
        assertLoggedIn();
        if (parameters.length >= 1) {
            try {
                server.createGame(authToken, new CreateGameRequest(parameters[0]));
                return "Game '" + parameters[0] + "' created successfully.";
            } catch (Exception except) {
                return except.getMessage();
            }
        }
        return "Expected: <NAME>";
    }

    public String listGames() {
        assertLoggedIn();
        try {
            var listResponse = server.listGames(authToken);
            gameListCache.clear();
            StringBuilder stringThing = new StringBuilder("\nActive Games:\n");
            int i = 1;
            for (var game : listResponse.games()) {
                gameListCache.put(i, game.gameID());
                stringThing.append(String.format(" %d. %s (White: %s, Black: %s)\n",
                        i++, game.gameName(),
                        game.whiteUsername() != null ? game.whiteUsername() : "empty",
                        game.blackUsername() != null ? game.blackUsername() : "empty"));
            }
            return stringThing.toString();
        } catch (Exception exception) {
            return exception.getMessage();
        }
    }

    public String joinGame(String... parameters) {
        assertLoggedIn();
        if (parameters.length >= 2) {
            try {
                int listNumber = Integer.parseInt(parameters[0]);
                String color = parameters[1].toUpperCase();
                Integer gameID = gameListCache.get(listNumber);
                if (gameID == null) {
                    return "Invalid game number. Run 'list' first.";
                }
                server.joinGame(authToken, new JoinGameRequest(color, gameID));
                displayBoard(color.equals("WHITE"));

                return String.format("Joined game %d as %s", listNumber, color);
            } catch (Exception exception) {
                return "Error: " + exception.getMessage();
            }
        }
        return "Expected: <ID> [WHITE|BLACK]";
    }

    public String observeGame(String... params) {
        assertLoggedIn();
        if (params.length >= 1) {
            try {
                int listNumber = Integer.parseInt(params[0]);
                Integer gameID = gameListCache.get(listNumber);
                if (gameID == null) {
                    return "Invalid game number. Run 'list' first.";
                }
                server.joinGame(authToken, new JoinGameRequest(null, gameID));
                displayBoard(true);
                return "Observing game " + listNumber;
            } catch (Exception exception) {
                return exception.getMessage();
            }
        }
        return "Expected: <ID>";
    }

    private void displayBoard(boolean isWhitePerspective) {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        System.out.println();
        BoardDrawer.drawBoard(board, true);
        System.out.println();
        BoardDrawer.drawBoard(board, false);
        System.out.println();
    }

    public String logout() {
        try {
            server.logout(authToken);
            authToken = null;
            state = State.SIGNEDOUT;
            gameListCache.clear();
            return "Logged out.";
        } catch (Exception exception) {
            return exception.getMessage();
        }
    }

    public String login(String... parameters) {
        if (parameters.length >= 2) {
            try {
                var listResponse = server.login(new RegisterRequest(parameters[0], parameters[1], null));
                authToken = listResponse.authToken();
                state = State.SIGNEDIN;
                return String.format("Logged in as %s.", parameters[0]);
            } catch (Exception exception) {
                return exception.getMessage();
            }
        }
        return "Expected: <USERNAME> <PASSWORD>";
    }

    public String register(String... parameters) {
        if (parameters.length >= 3) {
            try {
                var listResponse = server.register(new RegisterRequest(parameters[0], parameters[1], parameters[2]));
                authToken = listResponse.authToken();
                state = State.SIGNEDIN;
                return "Registered and logged in.";
            } catch (Exception exception) {
                return exception.getMessage();
            }
        }
        return "Expected: <USERNAME> <PASSWORD> <EMAIL>";
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                register <USERNAME> <PASSWORD> <EMAIL> - create an account
                login <USERNAME> <PASSWORD> - play chess
                quit - play chess
                help - possible commands
                """;
        }
        return """
                create <NAME> - game
                list - games
                join <ID> [WHITE|BLACK] - game
                observe <ID> - game
                logout - when done
                quit - play chess
                help - possible commands
                """;
    }

    private void assertLoggedIn() {
        if (state != State.SIGNEDIN) {
            throw new RuntimeException("Log in first.");
        }
    }

    public String getState() {
        return state.toString();
    }

    private enum State { SIGNEDOUT, SIGNEDIN }
}