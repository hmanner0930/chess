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

    public RegisterResult register(RegisterRequest request) throws DataAccessException {

        if(request.username() == null || request.password() == null || request.email() == null){
            throw new DataAccessException("Error: bad request");
        }
        if(userDAO.getUser(request.username()) != null){
            throw new DataAccessException("Error: already taken");
        }

        userDAO.createUser(new UserData(request.username(), request.password(), request.email()));
        String token = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token, request.username()));
        return new RegisterResult(request.username(), token);
    }

    public RegisterResult login(LoginRequest request) throws DataAccessException {
        if (request.username() == null || request.password() == null) {
            throw new DataAccessException("Error: bad request");
        }

        UserData user = userDAO.getUser(request.username());
        if(user == null || !user.password().equals(request.password())){
            throw new DataAccessException("Error: unauthorized");
        }

        String token = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token, request.username()));
        return new RegisterResult(request.username(), token);
    }

    public void logout(String authToken) throws DataAccessException {
        if(authDAO.getAuth(authToken) == null){
            throw new DataAccessException("Error: unauthorized");
        }
        authDAO.deleteAuth(authToken);
    }
}