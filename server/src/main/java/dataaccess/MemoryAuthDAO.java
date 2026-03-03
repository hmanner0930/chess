package dataaccess;

import model.AuthData;
import java.util.ArrayList;
import java.util.List;

public class MemoryAuthDAO implements AuthDAO{

    private final List<AuthData> authenticate = new ArrayList<>();

    @Override
    public void createAuth(AuthData auth){
        authenticate.add(auth);
    }

    @Override
    public AuthData getAuth(String authToken){
        for(AuthData auth: authenticate){
            if(auth.authToken().equals(authToken)){
                return auth;
            }
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken){
        for(int i = 0; i < authenticate.size(); i++){
            if(authenticate.get(i).authToken().equals(authToken)){
                authenticate.remove(i);
                return;
            }
        }
    }

    @Override
    public void clear(){
        authenticate.clear();
    }
}
