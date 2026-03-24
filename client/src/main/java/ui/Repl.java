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
        System.out.println(WHITE_PAWN + " Welcome to 240 Chess. Type Help to get started. " + WHITE_PAWN);

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            String printState = client.getState(); // e.g., "LOGGED_OUT" or "LOGGED_IN"
            System.out.print("\n" + SET_TEXT_COLOR_WHITE + "[" + printState + "] >>> " + SET_TEXT_COLOR_GREEN);
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                System.out.print(SET_TEXT_COLOR_RED + e.getMessage());
            }
        }
        System.out.println();
    }
}