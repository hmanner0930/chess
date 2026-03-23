package client;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import model.*;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    // --- PRE-LOGIN METHODS ---

    /**
     * Registers a new user.
     */
    public RegisterResult register(RegisterRequest request) throws Exception {
        return this.makeRequest("POST", "/user", null, request, RegisterResult.class);
    }

    /**
     * Logs in an existing user.
     * Uses RegisterRequest (email will be null) to simplify model management.
     */
    public RegisterResult login(RegisterRequest request) throws Exception {
        return this.makeRequest("POST", "/session", null, request, RegisterResult.class);
    }

    // --- POST-LOGIN METHODS ---

    public void logout(String authToken) throws Exception {
        this.makeRequest("DELETE", "/session", authToken, null, null);
    }

    public ListGamesResult listGames(String authToken) throws Exception {
        return this.makeRequest("GET", "/game", authToken, null, ListGamesResult.class);
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest request) throws Exception {
        return this.makeRequest("POST", "/game", authToken, request, CreateGameResult.class);
    }

    public void joinGame(String authToken, JoinGameRequest request) throws Exception {
        this.makeRequest("PUT", "/game", authToken, request, null);
    }

    // --- ADMIN / TESTING METHODS ---

    public void clear() throws Exception {
        this.makeRequest("DELETE", "/db", null, null, null);
    }

    // --- GENERIC HTTP HELPER ---

    private <T> T makeRequest(String method, String path, String authToken, Object requestBody, Class<T> responseClass) throws Exception {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);

            // Set headers
            if (authToken != null) {
                http.addRequestProperty("authorization", authToken);
            }

            // Write body if it's not a GET request
            if (requestBody != null) {
                http.setDoOutput(true);
                writeBody(requestBody, http);
            }

            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    private static void writeBody(Object requestBody, HttpURLConnection http) throws IOException {
        http.addRequestProperty("Content-Type", "application/json");
        String reqData = new Gson().toJson(requestBody);
        try (OutputStream reqBody = http.getOutputStream()) {
            reqBody.write(reqData.getBytes());
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException {
        var status = http.getResponseCode();
        if (status / 100 != 2) {
            throw new IOException("Error: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }
}