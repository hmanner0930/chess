package service;

import dataaccess.*;
import model.*;
import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO){
        this.authDAO = authDAO;
        this.userDAO = userDAO;
    }

    public RegisterResult register(RegisterRequest req) throws DataAccessException {
        // Check for missing fields (400 Bad Request)
        if(req.username() == null || req.password() == null || req.email() == null || req.username().isEmpty()){
            throw new DataAccessException("Error: bad request");
        }
        if(userDAO.getUser(req.username()) != null){
            throw new DataAccessException("Error: already taken");
        }

        userDAO.createUser(new UserData(req.username(), req.password(), req.email()));
        String token = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token, req.username()));
        return new RegisterResult(req.username(), token);
    }

    public RegisterResult login(LoginRequest req) throws DataAccessException {
        // Check for missing fields (400 Bad Request)
        if (req.username() == null || req.password() == null) {
            throw new DataAccessException("Error: bad request");
        }

        UserData user = userDAO.getUser(req.username());
        // Check credentials (401 Unauthorized)
        if(user == null || !user.password().equals(req.password())){
            throw new DataAccessException("Error: unauthorized");
        }

        String token = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token, req.username()));
        return new RegisterResult(req.username(), token);
    }

    public void logout(String authToken) throws DataAccessException {
        if(authToken == null || authDAO.getAuth(authToken) == null){
            throw new DataAccessException("Error: unauthorized");
        }
        authDAO.deleteAuth(authToken);
    }
}