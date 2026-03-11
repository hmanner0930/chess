import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import server.Server;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseManager.configureDatabase();
            Server server = new Server();
            server.run(8080); // This is fine for manual use
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}