package client;

import ui.Repl;

public class ClientMain {
    public static void main(String[] args) {
        // You can change 8080 to whatever port your server is running on
        var serverUrl = "http://localhost:8080";

        // If you want to support command line arguments for the URL:
        if (args.length == 1) {
            serverUrl = args[0];
        }

        System.out.println("♕ Welcome to the 240 Chess Client ♕");

        // Start the Read-Eval-Print-Loop
        new Repl(serverUrl).run();
    }
}