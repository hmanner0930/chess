package client;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import model.*;


public class ServerFacade {
    //address of server and declaration/constructor
    private final String serverUrl;

    public ServerFacade(String url) {
        this.serverUrl = url;
    }
    //These use the post to send data
    public RegisterResult register(RegisterRequest request) throws Exception {
        return this.sendRequest("POST", "/user", null, request, RegisterResult.class);
    }

    public RegisterResult login(RegisterRequest request) throws Exception {
        return this.sendRequest("POST", "/session", null, request, RegisterResult.class);
    }
    //These use delete
    public void logout(String authToken) throws Exception {
        this.sendRequest("DELETE", "/session", authToken, null, null);
    }
    //Uses get to return list of games
    public ListGamesResult listGames(String authToken) throws Exception {
        return this.sendRequest("GET", "/game", authToken, null, ListGamesResult.class);
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest request) throws Exception {
        return this.sendRequest("POST", "/game", authToken, request, CreateGameResult.class);
    }
    //Uses put; Updates state of the game
    public void joinGame(String authToken, JoinGameRequest request) throws Exception {
        this.sendRequest("PUT", "/game", authToken, request, null);
    }
    //Uses Delete
    public void clear() throws Exception {
        this.sendRequest("DELETE", "/db", null, null, null);
    }

    private <T> T sendRequest(String method, String path, String authToken, Object req, Class<T> responseClass) throws Exception {
        try {
            //here combines the url with the path then opens connection
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection httpConnect = (HttpURLConnection) url.openConnection();
            httpConnect.setRequestMethod(method);
            //if user is logged in puts authToken in the header for verification
            if (authToken != null) {
                httpConnect.addRequestProperty("authorization", authToken);
            }
            //creating a game or registering, sends details to the server
            if (req != null) {
                httpConnect.setDoOutput(true);
                writeBody(req, httpConnect);
            }
            //gets to the server if it returns a not successful the method will crash and return
            httpConnect.connect();
            throwIfNotSuccessful(httpConnect);
            return readBody(httpConnect, responseClass);
        } catch (Exception exception) {
            throw new Exception(exception.getMessage());
        }
    }
    //here we have writeBody uses Gson to turn a Java object into JSON string then output stream
    private static void writeBody(Object requestBody, HttpURLConnection http) throws IOException {
        http.addRequestProperty("Content-Type", "application/json");
        String requestData = new Gson().toJson(requestBody);
        try (OutputStream body = http.getOutputStream()) {
            body.write(requestData.getBytes());
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException {
        var status = http.getResponseCode();
        if (status / 100 != 2) {
            //if not 200
            try (InputStream respBody = http.getErrorStream()) {
                if (respBody != null) {
                    InputStreamReader reader = new InputStreamReader(respBody);
                    ErrorResponse error = new Gson().fromJson(reader, ErrorResponse.class);
                    throw new IOException(error.message());
                }
            }
            throw new IOException("Error: " + status);
        }
    }
    //Takes the JSON response string coming back and turns it into Java object
    private static <T> T readBody(HttpURLConnection http, Class<T> type) throws IOException {
        T response = null;
        try (InputStream responseBody = http.getInputStream()) {
            if (type != null) {
                InputStreamReader reader = new InputStreamReader(responseBody);
                response = new Gson().fromJson(reader, type);
            }
        } catch (IOException exception) {

        }
        return response;
    }
}