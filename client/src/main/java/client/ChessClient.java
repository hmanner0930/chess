package client;

import java.util.*;
import model.*;
import chess.*;
import ui.BoardDrawer;
import websocket.ServerMessageObserver;
import websocket.WebSocketFacade; // Added Import
import websocket.commands.UserGameCommand; // Added for CONNECT

public class ChessClient implements ServerMessageObserver {
    private final ServerFacade server;
    private WebSocketFacade ws;
    private final String serverUrl;
    private String authToken = null;
    private State state = State.SIGNEDOUT;
    private ChessGame.TeamColor playerColor = null;

    private final Map<Integer, Integer> gameListCache = new HashMap<>();

    public ChessClient(String serverUrl) {
        this.serverUrl = serverUrl; // FIX: Initialize the field
        this.server = new ServerFacade(serverUrl);
        // Ensure this matches your ServerFacade setup
    }

    public String eval(String input) {
        String[] tokens = input.toLowerCase().split("\\s+");
        String command = (tokens.length > 0) ? tokens[0] : "help";
        String[] params = (tokens.length > 1) ? Arrays.copyOfRange(tokens, 1, tokens.length) : new String[0];

        return switch (command) {
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
                this.playerColor = color.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
                Integer gameID = gameListCache.get(listNumber);

                if (gameID == null) {
                    return "Invalid game number. Run 'list' first.";
                }

                // 1. HTTP Join
                System.out.println("[DEBUG] Attempting HTTP Join for GameID: " + gameID);
                server.joinGame(authToken, new JoinGameRequest(color, gameID));
                System.out.println("[DEBUG] HTTP Join successful.");

                // 2. WebSocket Connect
                if (ws == null) {
                    System.out.println("[DEBUG] Initializing WebSocketFacade with URL: " + serverUrl);
                    ws = new WebSocketFacade(serverUrl, this);
                }

                System.out.println("[DEBUG] Sending CONNECT command via WebSocket...");
                ws.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
                System.out.println("[DEBUG] CONNECT command sent to server.");

                return String.format("Joined game %d as %s", listNumber, color);

            } catch (NumberFormatException exception){
                return "Error: '" + parameters[0] + "' is not a valid number.";
            } catch (Exception exception) {
                // This is crucial: if WebSocketFacade fails to connect, it will land here.
                System.out.println("[DEBUG] ERROR in joinGame: " + exception.getMessage());
                return "Error: " + exception.getMessage();
            }
        }
        return "Expected: <ID> [WHITE|BLACK]";
    }

    public String observeGame(String... params) {
        assertLoggedIn();
        this.playerColor = ChessGame.TeamColor.WHITE;
        if (params.length >= 1) {
            try {
                int listNumber = Integer.parseInt(params[0]);
                Integer gameID = gameListCache.get(listNumber);
                if (gameID == null) {
                    return "Error: Game number " + listNumber + " not found. Run 'list' first.";
                }

                // 1. HTTP Join
                server.joinGame(authToken, new JoinGameRequest(null, gameID));

                // 2. WebSocket Connect
                if (ws == null) {
                    ws = new WebSocketFacade(serverUrl, this);
                }
                ws.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));

                return "Observing game " + listNumber;
            } catch (NumberFormatException exception) {
                return "Error: '" + params[0] + "' is not a valid number. ";
            } catch (Exception exception) {
                return "Error: " + exception.getMessage();
            }
        }
        return "Expected: <ID>";
    }

    public String logout() {
        try {
            server.logout(authToken);
            authToken = null;
            state = State.SIGNEDOUT;
            gameListCache.clear();
            ws = null; // Reset WebSocket on logout
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

    @Override
    public void notify(websocket.messages.ServerMessage message) {
        System.out.println("DEBUG: Client received a message from server: " + message.getServerMessageType());
        switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                var loadMessage = (websocket.messages.LoadGameMessage) message;
                boolean isWhiteView = (playerColor == null || playerColor == ChessGame.TeamColor.WHITE);
                System.out.println("\n[DEBUG] RECEIVED LOAD_GAME MESSAGE"); // Add this

                if (loadMessage.getGame() != null && loadMessage.getGame().getBoard() != null) {
                    System.out.println("\n");
                    BoardDrawer.drawBoard(loadMessage.getGame().getBoard(), isWhiteView);
                } else {
                    System.out.println("[DEBUG] Game or Board was null in the message!");
                }
                printPrompt();
            }
            case ERROR -> {
                var errorMessage = (websocket.messages.ErrorMessage) message;
                System.out.println("[DEBUG] RECEIVED ERROR FROM SERVER: " + errorMessage.getErrorMessage());
                System.out.println("\n" + ui.EscapeSequences.SET_TEXT_COLOR_RED + errorMessage.getErrorMessage());
                printPrompt();
            }
            case NOTIFICATION -> {
                var notification = (websocket.messages.NotificationMessage) message;
                System.out.println("\n" + ui.EscapeSequences.SET_TEXT_COLOR_YELLOW + notification.getMessage());
                printPrompt();
            }
        }
    }

    private void printPrompt() {
        System.out.print("\n" + ui.EscapeSequences.SET_TEXT_COLOR_WHITE + "[" + getState() + "] >>> ");
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                register <USERNAME> <PASSWORD> <EMAIL> - create account
                login <USERNAME> <PASSWORD> - play chess
                quit - exit
                help - possible commands
                """;
        }
        return """
                create <NAME> - create game
                list - list games
                join <ID> [WHITE|BLACK] - join game
                observe <ID> - observe game
                logout - when done
                quit - exit
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