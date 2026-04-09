package ui;

import client.ChessClient;
import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl {
    private final ChessClient client;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);
    }

    public void run() {
        System.out.println(WHITE_PAWN + " Welcome to 240 Chess." + WHITE_PAWN);

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            String printState = client.getState();
            // This line below is essentially your "printPrompt"
            System.out.print("\n" + SET_TEXT_COLOR_WHITE + "[" + printState + "] >>> " + SET_TEXT_COLOR_GREEN);

            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                // If the result is null or empty (like after a join),
                // don't print anything extra to avoid messing up the board
                if (result != null && !result.isEmpty()) {
                    System.out.print(SET_TEXT_COLOR_BLUE + result);
                }
            } catch (Throwable exception) {
                System.out.print(SET_TEXT_COLOR_RED + "Error: " + exception.getMessage());
            }
        }
    }
}