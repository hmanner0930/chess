package dataaccess;

import model.UserData;
import java.util.ArrayList;
import java.util.List;

public class MemoryUserDAO implements UserDAO {

    private final List<UserData> users = new ArrayList<>();

    @Override
    public void createUser(UserData user) {
        users.add(user);
    }

    @Override
    public UserData getUser(String username) {
        for(UserData user: users){
            if(user.username().equals(username)){
                return user;
            }
        }
        return null;
    }

    @Override
    public void clear() {
        users.clear();
    }
}
