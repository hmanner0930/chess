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
    public RegisterResult register(RegisterRequest req) throws DataAccessException{
        if(req.username() == null || req.password() == null|| req.email() == null){
            throw new DataAccessException("Error");
        }
        if(userDAO.getUser(req.username()) != null){
            throw new DataAccessException(("Error"));
        }
        UserData user = new UserData(req.username(), req.password(), req.email());
        userDAO.createUser(user);
        String token = UUID.randomUUID().toString(); //
        authDAO.createAuth(new AuthData(token, req.username()));
        return new RegisterResult(req.username(), token);
    }
    public RegisterResult login(LoginRequest req) throws DataAccessException{
        UserData user = userDAO.getUser(req.username());
        if(user == null || !user.password().equals(req.password())){
            throw new DataAccessException("Error");
        }
        String token = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token,req.username()));
        return new RegisterResult(req.username(), token);
    }
    public void logout(String authToken) throws DataAccessException{
        if(authDAO.getAuth(authToken) == null){
            throw new DataAccessException("Error");
        }
        authDAO.deleteAuth(authToken);
    }
}
