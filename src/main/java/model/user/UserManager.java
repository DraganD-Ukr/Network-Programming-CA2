package model.user;

import service.ResponseStatus;

import java.net.Socket;

public interface UserManager {

    ResponseStatus register(String username, String password);

    ResponseStatus login(String username, String password, Socket socket);

    ResponseStatus logout(String username, Socket socket);

    User getUserByUsername(String username);

    User getUser(String username);
}
