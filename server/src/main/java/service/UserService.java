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
    public RegisterResult register(RegisterRequest reg) throws DataAccessException{
        if(reg.username() == null || reg.password() == null|| reg.email() == null){
            throw new DataAccessException("Error");
        }
        if(userDAO.getUser(reg.username()) != null){
            throw new DataAccessException(("Error"));
        }
        userDAO.createUser(new UserData(reg.username(), reg.password(), reg.email()));
        String token = UUID.randomUUID().toString(); //
        authDAO.createAuth(new AuthData(token, reg.username()));
        return new RegisterResult(reg.username(), token);
    }
    public LoginResult login(LoginRequest reg) throws DataAccessException{
        UserData user = userDAO.getUser(reg.username());
        if(user == null || !user.password().equals(reg.password())){
            throw new DataAccessException("Error");
        }
        String token = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(token,reg.username()));
        return new LoginResult(reg.username(), token);
    }
    public void logout(String authToken) throws DataAccessException{
        if(authDAO.getAuth(authToken) == null){
            throw new DataAccessException("Error");
        }
        authDAO.deleteAuth(authToken);
    }
}
