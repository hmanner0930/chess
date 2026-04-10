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
    private ChessGame currentGame = null;
    private int currentGameID = -1;

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
            case "redraw" -> redrawBoard();
            case "leave" -> leaveGame();
            case "make" -> {
                if (params.length > 0 && params[0].equals("move")) {
                    yield makeMove(Arrays.copyOfRange(params, 1, params.length));
                }
                yield "Use: make move <START> <END>";
            }
            case "resign" -> resignGame();
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
                this.state = State.IN_GAME;
                this.currentGameID = gameID;
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
                this.state = State.IN_GAME;
                this.currentGameID = gameID;
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

                // 1. Update the local data
                this.currentGame = loadMessage.getGame();

                // 2. Use the helper method (This draws it once)
                redrawBoard();

                // 3. Print the prompt so the user knows they can type
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
        if (state == State.IN_GAME){
            return
            """
            redraw - show the board again
            leave - exit the game
            make move <START> <END> - e.g., 'make move e2 e4'
            resign - give up the match
            highlight <POSITION> - show legal moves
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
    public String leaveGame() {
        try {
            // 1. Tell the server we are leaving
            ws.sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, currentGameID));

            // 2. Reset client state
            this.state = State.SIGNEDIN;
            this.currentGame = null;
            this.currentGameID = -1;

            return "You left the game.";
        } catch (Exception e) {
            return "Error leaving game: " + e.getMessage();
        }
    }

    private String redrawBoard() {
        if (currentGame == null) {
            return "Error: No active game to redraw.";
        }
        boolean isWhiteView = (playerColor == null || playerColor == ChessGame.TeamColor.WHITE);

        // Just drawing directly to the console
        BoardDrawer.drawBoard(currentGame.getBoard(), isWhiteView);

        return "Board redrawn.";
    }
    private ChessPosition parsePosition(String pos) throws Exception {
        if (pos == null || pos.length() != 2) {
            throw new Exception("Invalid position: " + pos);
        }

        char colChar = pos.toLowerCase().charAt(0);
        char rowChar = pos.charAt(1);

        // Convert 'a'-'h' to 1-8
        int col = colChar - 'a' + 1;
        // Convert '1'-'8' to 1-8
        int row = Character.getNumericValue(rowChar);

        if (col < 1 || col > 8 || row < 1 || row > 8) {
            throw new Exception("Position out of bounds: " + pos);
        }

        return new ChessPosition(row, col);
    }
    public String makeMove(String... params) {
        if (params.length < 2) {
            return "Expected: <START> <END> (e.g., e2 e4)";
        }

        try {
            ChessPosition start = parsePosition(params[0]);
            ChessPosition end = parsePosition(params[1]);

            // For Phase 4, we usually ignore promotion for basic moves,
            // but you can add it later if needed.
            ChessMove move = new ChessMove(start, end, null);

            // Create the specialized command for making a move
            // Ensure you have a MakeMoveCommand class that extends UserGameCommand
            var command = new websocket.commands.MakeMoveCommand(authToken, currentGameID, move);
            ws.sendCommand(command);

            return "Move sent: " + params[0] + " to " + params[1];
        } catch (Exception e) {
            return "Error making move: " + e.getMessage();
        }
    }

    public String resignGame() {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine().toLowerCase();

        if (response.equals("yes")) {
            try {
                ws.sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, currentGameID));
                return "Resignation request sent.";
            } catch (Exception e) {
                return "Error resigning: " + e.getMessage();
            }
        }
        return "Resignation cancelled.";
    }

    private void assertLoggedIn() {
        if (state != State.SIGNEDIN) {
            throw new RuntimeException("Log in first.");
        }
    }

    public String getState() {
        return state.toString();
    }

    private enum State { SIGNEDOUT, SIGNEDIN, IN_GAME }
}