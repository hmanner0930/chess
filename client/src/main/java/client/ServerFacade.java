package client;

import com.google.gson.Gson;
import java.io.*;
import java.net.*;
import model.*;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(int port){
        this.serverUrl = "http://localhost:" + port;
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

    private static void writeBody(Object requestBody, HttpURLConnection http) throws IOException {
        if (requestBody != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(requestBody);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private <T> T makeRequest(String method, String path, String authToken, Object requestBody, Class<T> responseClass) throws Exception {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (authToken != null) {
                http.addRequestProperty("authorization", authToken);
            }

            writeBody(requestBody, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    public AuthData register(String username, String password, String email) throws Exception {
        var path = "/user";
        var requestBody = new UserData(username, password, email);
        return this.makeRequest("POST", path, null, requestBody, AuthData.class);
    }

    public AuthData login(String username, String password) throws Exception {
        var path = "/session";
        var requestBody = new UserData(username, password, null);
        return this.makeRequest("POST", path, null, requestBody, AuthData.class);
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException {
        var status = http.getResponseCode();
        if (status / 100 != 2) {
            throw new IOException("Server returned HTTP status: " + status);
        }
    }
}
