package unit;


import model.user.User;
import model.user.UserManagerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.ResponseStatus;

import java.net.Socket;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerImplTests {

    private UserManagerImpl userManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManagerImpl();
    }

    @Test
    void register_shouldAddNewUser_andReturnSuccess() {
        ResponseStatus status = userManager.register("alice", "password123");

        assertEquals(ResponseStatus.SUCCESS, status);

        User user = userManager.getUser("alice");
        assertNotNull(user);
        assertEquals("alice", user.getUsername());
        assertEquals("password123", user.getPassword());
    }

    @Test
    void register_existingUsername_shouldReturnUserAlreadyExists() {
        userManager.register("bob", "pass1");
        ResponseStatus status = userManager.register("bob", "pass2");

        assertEquals(ResponseStatus.USER_ALREADY_EXISTS, status);
        assertEquals("pass1", userManager.getUser("bob").getPassword()); // Original password unchanged
    }

    @Test
    void login_validCredentials_shouldReturnSuccess() {
        userManager.register("carol", "secret");

        ResponseStatus status = userManager.login("carol", "secret", new Socket());

        assertEquals(ResponseStatus.SUCCESS, status);
    }

    @Test
    void login_invalidUsername_shouldReturnError() {
        ResponseStatus status = userManager.login("nonexistent", "whatever", new Socket());

        assertEquals(ResponseStatus.INVALID_USERNAME_OR_PASSWORD, status);
    }

    @Test
    void login_invalidPassword_shouldReturnError() {
        userManager.register("dave", "correctPass");

        ResponseStatus status = userManager.login("dave", "wrongPass", new Socket());

        assertEquals(ResponseStatus.INVALID_USERNAME_OR_PASSWORD, status);
    }

    @Test
    void getUserByUsername_shouldReturnUser_ifExists() {
        userManager.register("erin", "pwd");

        User user = userManager.getUserByUsername("erin");

        assertNotNull(user);
        assertEquals("erin", user.getUsername());
    }

    @Test
    void getUserByUsername_shouldReturnNull_ifUserDoesNotExist() {
        assertNull(userManager.getUserByUsername("ghost"));
    }

    @Test
    void register_shouldAssignUniqueUUID() {
        userManager.register("user1", "pass");
        userManager.register("user2", "pass");

        UUID uuid1 = userManager.getUser("user1").getId();
        UUID uuid2 = userManager.getUser("user2").getId();

        assertNotEquals(uuid1, uuid2);
    }

    @Test
    void login_nullSocket_shouldStillAllowLogin() {
        userManager.register("tom", "pw");
        ResponseStatus status = userManager.login("tom", "pw", null);

        assertEquals(ResponseStatus.SUCCESS, status);
    }

    @Test
    void register_nullUsernameOrPassword_shouldReturnInvalidResponse() {

        ResponseStatus status = userManager.register(null, "password");
        assertEquals(ResponseStatus.INVALID_USERNAME_OR_PASSWORD, status);

        status = userManager.register("username", null);
        assertEquals(ResponseStatus.INVALID_USERNAME_OR_PASSWORD, status);

        status = userManager.register(null, null);
        assertEquals(ResponseStatus.INVALID_USERNAME_OR_PASSWORD, status);
    }
}
