package client;

import java.util.*;
import model.*;
import chess.*;
import ui.BoardDrawer;
import ui.EscapeSequences;
import websocket.ServerMessageObserver;
import websocket.WebSocketFacade;
import websocket.commands.UserGameCommand;

public class ChessClient implements ServerMessageObserver {
    //This stuff stores the specific session stuff
    private final ServerFacade server;
    private WebSocketFacade webSocket;
    //connection to server
    private final String serverUrl;
    private String authToken = null;
    private State stateOfPlayer = State.SIGNEDOUT;
    private ChessGame.TeamColor playerColor = null;
    private ChessGame currentGame = null;
    //local game state
    private int currentGameID = -1;

    private final Map<Integer, Integer> gameListData = new HashMap<>();

    //constructor of HTTP facade
    public ChessClient(String serverUrl) {
        this.serverUrl = serverUrl;
        this.server = new ServerFacade(serverUrl);
    }

    //command sorter eval; sorts strings into the correct java methods
    public String eval(String input) {
        String[] tokens = input.toLowerCase().split("\\s+");
        String command = (tokens.length > 0) ? tokens[0] : "help";
        //had to look copyOfRange up
        String[] args = (tokens.length > 1) ? Arrays.copyOfRange(tokens, 1, tokens.length) : new String[0];

        return switch (command) {
            case "login" -> login(args);
            case "register" -> register(args);
            case "logout" -> logout();
            case "create" -> createGame(args);
            case "list" -> listGames();
            case "join" -> joinGame(args);
            case "observe" -> observeGame(args);
            case "quit" -> "quit";
            case "redraw" -> redrawBoard();
            case "leave" -> leaveGame();
            case "make" -> {
                if (args.length > 0 && args[0].equals("move")) {
                    yield makeMove(Arrays.copyOfRange(args, 1, args.length));
                }
                yield "Use: make move <START> <END>";
            }
            case "resign" -> resignGame();
            case "highlight" -> {
                if (args.length > 0) {
                    yield highlightLegalMoves(args[0]);
                }
                yield "Expected: highlight <POSITION>";
            }
            default -> help();
        };
    }
    //Here tells the server to make a game and then immediately refreshes because there were problems there
    //list gets the game and puts it into game list data
    public String createGame(String... args) {
        assertLoggedIn();
        if (args.length >= 1) {
            try {
                server.createGame(authToken, new CreateGameRequest(args[0]));
                listGames();
                return "Game '" + args[0] + "' created successfully; added to list.";
            } catch (Exception except) {
                return except.getMessage();
            }
        }
        return "Expected: <NAME>";
    }
    //Here it gets the games and then lists them as active games in that certain format
    //Sends HTTP request to update the database then the CONNECT command for websocket
    //HTTP and Websockets
    public String listGames() {
        assertLoggedIn();
        try {
            var listResponse = server.listGames(authToken);
            gameListData.clear();
            StringBuilder stringThing = new StringBuilder("\nActive Games:\n");
            int i = 1;
            for (var game : listResponse.games()) {
                String whitePlayer;
                if (game.whiteUsername() != null) {
                    whitePlayer = game.whiteUsername();
                } else {
                    whitePlayer = "empty";
                }
                String blackPlay;
                if (game.blackUsername() != null) {
                    blackPlay = game.blackUsername();
                } else {
                    blackPlay = "empty";
                }
                gameListData.put(i, game.gameID());
                stringThing.append(String.format(" %d. %s (White: %s, Black: %s)\n",
                        i++, game.gameName(), whitePlayer, blackPlay));
            }
            return stringThing.toString();
        } catch (Exception exception) {
            return exception.getMessage();
        }
    }
    //Here a user tries to join a game would be nice if they put in invalid number to list games
    public String joinGame(String... args) {
        assertLoggedIn();

        if (args.length >= 2) {
            try {
                int listNumber = Integer.parseInt(args[0]);
                String color = args[1].toUpperCase();

                if (!gameListData.containsKey(listNumber)) {
                    listGames();
                }

                Integer gameID = gameListData.get(listNumber);
                if (gameID == null) {
                    return "Error: Game number " + listNumber + " does not exist. Run 'list'.";
                }
                if (color.equals("WHITE")) {
                    this.playerColor = ChessGame.TeamColor.WHITE;
                } else {
                    this.playerColor = ChessGame.TeamColor.BLACK;
                }

                server.joinGame(authToken, new JoinGameRequest(color, gameID));

                if (webSocket == null) {
                    webSocket = new WebSocketFacade(serverUrl, this);
                }

                webSocket.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
                this.stateOfPlayer = State.IN_GAME;
                this.currentGameID = gameID;

                return String.format("Joined game %d as %s", listNumber, color);

            } catch (NumberFormatException exception){
                return "Error: '" + args[0] + "' is not a valid number.";
            } catch (Exception exception) {
                return "Error: " + exception.getMessage();
            }
        }
        return "Expected: <ID> [WHITE|BLACK]";
    }

    public String observeGame(String... args) {
        assertLoggedIn();
        this.playerColor = ChessGame.TeamColor.WHITE;
        if (args.length >= 1) {
            try {
                int listNumber = Integer.parseInt(args[0]);
                if (!gameListData.containsKey(listNumber)) {
                    listGames();
                }

                Integer gameID = gameListData.get(listNumber);
                if (gameID == null) {
                    return "Error: Game number " + listNumber + " does not exist. Run 'list'.";
                }

                server.joinGame(authToken, new JoinGameRequest(null, gameID));
                if (webSocket == null) {
                    webSocket = new WebSocketFacade(serverUrl, this);
                }

                webSocket.sendCommand(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
                this.stateOfPlayer = State.IN_GAME;
                this.currentGameID = gameID;
                return "Observing game " + listNumber;
            } catch (NumberFormatException exception) {
                return "Error: '" + args[0] + "' is not a valid number.";
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
            stateOfPlayer = State.SIGNEDOUT;
            gameListData.clear();
            webSocket = null;
            return "Logged out.";
        } catch (Exception exception) {
            return exception.getMessage();
        }
    }
//calls registerRequest to login and then standard HTTP to create a session
    public String login(String... args) {
        if (args.length >= 2) {
            try {
                var listResponse = server.login(new RegisterRequest(args[0], args[1], null));
                authToken = listResponse.authToken();
                stateOfPlayer = State.SIGNEDIN;
                return String.format("Logged in as %s.", args[0]);
            } catch (Exception exception) {
                return exception.getMessage();
            }
        }
        return "Expected: <USERNAME> <PASSWORD>";
    }
    //calls registerRequest to login and then standard HTTP to create a session
    public String register(String... args) {
        if (args.length >= 3) {
            try {
                var listResponse = server.register(new RegisterRequest(args[0], args[1], args[2]));
                authToken = listResponse.authToken();
                stateOfPlayer = State.SIGNEDIN;
                return "Registered and logged in.";
            } catch (Exception exception) {
                return exception.getMessage();
            }
        }
        return "Expected: <USERNAME> <PASSWORD> <EMAIL>";
    }

    //The server has to communicate with us
    //LOAD game updates the local board then redraws
    //ERROR and NOTIFICATION Prints the messages form server in certain color
    //How client knows
    @Override
    public void notify(websocket.messages.ServerMessage msg) {
        switch (msg.getServerMessageType()) {
            case LOAD_GAME -> {
                var loadGameMessage = (websocket.messages.LoadGameMessage) msg;
                this.currentGame = loadGameMessage.getGame();
                System.out.println();
                redrawBoard();
                System.out.print("\n" + ui.EscapeSequences.SET_TEXT_COLOR_WHITE + "[" + getState() + "] >>> ");
            }
            case ERROR -> {
                var errorMessage = (websocket.messages.ErrorMessage) msg;
                System.out.println("\n" + ui.EscapeSequences.SET_TEXT_COLOR_RED + errorMessage.getErrorMessage());
                System.out.print("\n" + ui.EscapeSequences.SET_TEXT_COLOR_WHITE + "[" + getState() + "] >>> ");
            }
            case NOTIFICATION -> {
                var notification = (websocket.messages.NotificationMessage) msg;
                System.out.println("\n" + EscapeSequences.SET_TEXT_COLOR_MAGENTA + notification.getMessage());
                System.out.print("\n" + ui.EscapeSequences.SET_TEXT_COLOR_WHITE + "[" + getState() + "] >>> ");
            }
        }
    }

    public String help() {
        if (stateOfPlayer == State.SIGNEDOUT) {
            return """
                register <USERNAME> <PASSWORD> <EMAIL> - create account
                login <USERNAME> <PASSWORD> - play chess
                quit - exit
                help - possible commands
                """;
        }
        if (stateOfPlayer == State.IN_GAME){
            return
            """
            redraw - show board again
            leave - exit game
            make move - <START> <END>
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
            webSocket.sendCommand(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, currentGameID));
            this.stateOfPlayer = State.SIGNEDIN;
            this.currentGame = null;
            this.currentGameID = -1;

            return "You left the game.";
        } catch (Exception exception) {
            return "Error leaving game: " + exception.getMessage();
        }
    }
    //redraws the board pretty simply
    private String redrawBoard() {
        if (currentGame == null) {
            return "Error: No game to redraw.";
        }
        boolean isWhiteView = (playerColor == null || playerColor == ChessGame.TeamColor.WHITE);
        BoardDrawer.drawBoard(currentGame.getBoard(), isWhiteView, null);

        return "Board redrawn.";
    }
    //Had to look this up
    private ChessPosition parsePosition(String position) throws Exception {
        if (position == null || position.length() != 2) {
            throw new Exception("Invalid position: " + position);
        }

        char columnC = position.toLowerCase().charAt(0);
        char rowC = position.charAt(1);
        int column = columnC - 'a' + 1;
        int row = Character.getNumericValue(rowC);
        if (column < 1 || column > 8 || row < 1 || row > 8) {
            throw new Exception("Position out of bounds: " + position);
        }
        return new ChessPosition(row, column);
    }
    //converts the certain chess positions to ChessMove object but also checks for promotion
    public String makeMove(String... params) {
        if (params.length < 2) {
            return "Expected: <START> <END>";
        }

        try {
            ChessPosition start = parsePosition(params[0]);
            ChessPosition end = parsePosition(params[1]);

            ChessPiece piece = currentGame.getBoard().getPiece(start);
            if (piece == null) {
                return "No piece at " + params[0];
            }

            ChessPiece.PieceType promotionPiece = null;

            if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                if ((piece.getTeamColor() == ChessGame.TeamColor.WHITE && end.getRow() == 8) ||
                        (piece.getTeamColor() == ChessGame.TeamColor.BLACK && end.getRow() == 1)) {
                    promotionPiece = promptForPromotion();
                }
            }

            ChessMove move = new ChessMove(start, end, promotionPiece);
            var command = new websocket.commands.MakeMoveCommand(authToken, currentGameID, move);
            webSocket.sendCommand(command);

            return "";
        } catch (Exception exception) {
            return "Error making move: " + exception.getMessage();
        }
    }
    //This asks for permission to resign from game a suggested added feature then tells the server the decision
    public String resignGame() {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        Scanner scanner = new Scanner(System.in);
        String decision = scanner.nextLine().toLowerCase();

        if (decision.equals("yes")) {
            try {
                webSocket.sendCommand(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, currentGameID));
                return "Resignation sent.";
            } catch (Exception exception) {
                return "Error resigning: " + exception.getMessage();
            }
        }
        return "Resignation not.";
    }
    //calls the board with a set of valid moves to color those squares
    //of course calls validMoves
    public String highlightLegalMoves(String position) {
        if (currentGame == null) {
            return "No active game.";
        }

        try {
            ChessPosition startPosition = parsePosition(position);
            Collection<ChessMove> validMoves = currentGame.validMoves(startPosition);

            if (validMoves == null || validMoves.isEmpty()) {
                return "No valid moves for " + position;
            }

            boolean isWhitePerspective = (playerColor == null || playerColor == ChessGame.TeamColor.WHITE);

            System.out.println("\n");
            BoardDrawer.drawBoard(currentGame.getBoard(), isWhitePerspective, validMoves);

            return "Moves for " + position;
        } catch (Exception exception) {
            return "Error: " + exception.getMessage();
        }
    }
    //This I kind of looked up because I was having trouble with full words not working just letters
    private ChessPiece.PieceType promptForPromotion() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Promote to (Q/B/N/R)? ");
            String input = scanner.nextLine().toUpperCase().trim();
            switch (input) {
                case "Q" -> {
                    return ChessPiece.PieceType.QUEEN;
                }
                case "B" -> {
                    return ChessPiece.PieceType.BISHOP;
                }
                case "N" -> {
                    return ChessPiece.PieceType.KNIGHT;
                }
                case "R" -> {
                    return ChessPiece.PieceType.ROOK;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void assertLoggedIn() {
        if (stateOfPlayer != State.SIGNEDIN) {
            throw new RuntimeException("Log in first.");
        }
    }

    public String getState() {
        return stateOfPlayer.toString();
    }

    private enum State { SIGNEDOUT, SIGNEDIN, IN_GAME }
}