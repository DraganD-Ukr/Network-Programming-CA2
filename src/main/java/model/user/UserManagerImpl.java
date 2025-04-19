package model.user;

import service.ResponseStatus;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserManagerImpl implements UserManager {

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
                .loggedIn(false)
                .build();

//        Add the user to the map: username -> user.
//        We still have UUID for user as a unique identifier as it is safer for unique identification + we can
//        implement it with database later.
        users.put(username, user);

        return ResponseStatus.SUCCESS;
    }

    @Override
    public ResponseStatus login(String username, String password) {

        User user = users.get(username);

        if (user == null) {
            return ResponseStatus.INVALID_USERNAME_OR_PASSWORD; // User not found
        }
        if (user.isLoggedIn()) {
            return ResponseStatus.USER_ALREADY_LOGGED;
        }
        if (!user.getPassword().equals(password)) {
            return ResponseStatus.INVALID_USERNAME_OR_PASSWORD; // Invalid password
        }
        user.setLoggedIn(true);
        return ResponseStatus.SUCCESS;

    }

    @Override
    public ResponseStatus logout(String username) {

        User user = users.get(username);

        if (user == null) {
            return ResponseStatus.USER_NOT_FOUND; // User not found
        }
        if (!user.isLoggedIn()) {
            return ResponseStatus.USER_NOT_LOGGED_IN; // User already logged out
        }

        user.setLoggedIn(false);
        return ResponseStatus.SUCCESS;
    }


}
