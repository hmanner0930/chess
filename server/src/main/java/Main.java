import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import server.Server;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseManager.configureDatabase();

            int port = 8080;
            Server server = new Server();
            server.run(port);
            System.out.println("♕ 240 Chess Server started on port " + port);
        } catch (DataAccessException e) {
            System.err.println("❌ Failed to initialize database: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Server failed to start: " + e.getMessage());
        }
    }
}