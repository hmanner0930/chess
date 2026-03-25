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

    // Methods Here
    public RegisterResult register(RegisterRequest request) throws Exception {
        return this.makeRequest("POST", "/user", null, request, RegisterResult.class);
    }

    public RegisterResult login(RegisterRequest request) throws Exception {
        return this.makeRequest("POST", "/session", null, request, RegisterResult.class);
    }

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

    public void clear() throws Exception {
        this.makeRequest("DELETE", "/db", null, null, null);
    }

    private <T> T makeRequest(String method, String path, String authToken, Object requestBody, Class<T> responseClass) throws Exception {
        try {
            //Full internet address
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection httpConnect = (HttpURLConnection) url.openConnection();
            httpConnect.setRequestMethod(method);

            if (authToken != null) {
                httpConnect.addRequestProperty("authorization", authToken);
            }

            if (requestBody != null) {
                httpConnect.setDoOutput(true);
                writeBody(requestBody, httpConnect);
            }

            httpConnect.connect();
            throwIfNotSuccessful(httpConnect);
            return readBody(httpConnect, responseClass);
        } catch (Exception exception) {
            throw new Exception(exception.getMessage());
        }
    }

    private static void writeBody(Object requestBody, HttpURLConnection http) throws IOException {
        http.addRequestProperty("Content-Type", "application/json");
        String requestData = new Gson().toJson(requestBody);
        try (OutputStream body = http.getOutputStream()) {
            body.write(requestData.getBytes());
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException {
        var status = http.getResponseCode();
        //If error not in 400s or 500s
        if (status / 100 != 2) {
            throw new IOException("Error: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        try (InputStream responseBody = http.getInputStream()) {
            if (responseClass != null) {
                InputStreamReader reader = new InputStreamReader(responseBody);
                response = new Gson().fromJson(reader, responseClass);
            }
        } catch (IOException exception) {

        }
        return response;
    }
}