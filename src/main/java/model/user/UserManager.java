package model.user;

import service.ResponseStatus;

public interface UserManager {

    ResponseStatus register(String username, String password);

    ResponseStatus login(String username, String password);

    ResponseStatus logout(String username);

}
