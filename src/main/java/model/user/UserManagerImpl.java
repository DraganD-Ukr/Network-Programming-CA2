package model.user;

import auth.SessionManager;
import auth.SessionManagerImpl;
import service.ResponseStatus;

import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserManagerImpl implements UserManager {

    private final SessionManager sessionManager = new SessionManagerImpl();
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    @Override
    public ResponseStatus register(String username, String password) {

        if (users.containsKey(username)) {
            return ResponseStatus.USER_ALREADY_EXISTS; // User already exists
        }

//        Build a new user
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .password(password)
                .build();

//        Add the user to the map: username -> user.
//        We still have UUID for user as a unique identifier as it is safer for unique identification + we can
//        implement it with database later.
        users.put(username, user);

        return ResponseStatus.SUCCESS;
    }

    @Override
    public ResponseStatus login(String username, String password, Socket socket) {

        User user = users.get(username);

        if (user == null) {
            return ResponseStatus.INVALID_USERNAME_OR_PASSWORD; // User not found
        }
        if (isUserLoggedIn(socket)) {
            return ResponseStatus.USER_ALREADY_LOGGED;
        }
        if (!user.getPassword().equals(password)) {
            return ResponseStatus.INVALID_USERNAME_OR_PASSWORD; // Invalid password
        }
        sessionManager.addSession(socket, username);
        return ResponseStatus.SUCCESS;

    }

    @Override
    public ResponseStatus logout(String username, Socket socket) {

        User user = users.get(username);

        if (user == null) {
            return ResponseStatus.USER_NOT_FOUND; // User not found
        }
        if (!isUserLoggedIn(socket)) {
            return ResponseStatus.USER_NOT_LOGGED_IN; // User already logged out
        }

        sessionManager.removeSession(socket);
        return ResponseStatus.SUCCESS;
    }

    @Override
    public User getUserByUsername(String username) {
        return users.get(username);
    }

    private boolean isUserLoggedIn(Socket socket) {
        return sessionManager.getUsername(socket) != null;
    }


}
