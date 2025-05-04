package model.user;


import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import service.ResponseStatus;

import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class UserManagerImpl implements UserManager {

    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    @Override
    public ResponseStatus register(String username, String password) {

        if (username == null || password == null) {
            return ResponseStatus.INVALID_USERNAME_OR_PASSWORD; // Invalid input
        }

        if (users.containsKey(username)) {
            return ResponseStatus.USER_ALREADY_EXISTS; // User already exists
        }

//        Build a new user
        User user = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .password(hashPassword(password))
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
            log.debug("User {} tried to login but was not found", username);
            return ResponseStatus.INVALID_USERNAME_OR_PASSWORD; // User not found
        }

        if (!checkPassword(password, user.getPassword())) {
            log.debug("User {} tried to login with invalid password", username);
            return ResponseStatus.INVALID_USERNAME_OR_PASSWORD; // Invalid password
        }

        return ResponseStatus.SUCCESS;

    }


    @Override
    public User getUserByUsername(String username) {
        return users.get(username);
    }




    @Override
    public User getUser(String username) {
        return users.get(username);
    }


    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    private boolean checkPassword(String password, String hashed) {
        return BCrypt.checkpw(password, hashed);
    }
}
