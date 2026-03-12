import dataaccess.DatabaseManager;
import server.Server;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseManager.configureDatabase();
            Server server = new Server();
            server.run(8080);
        } catch (Exception ex) {
            System.err.printf(ex.getMessage());
        }
    }
}