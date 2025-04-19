package auth;

import lombok.extern.slf4j.Slf4j;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SessionManagerImpl implements SessionManager {

    /**
     * A thread-safe map to store sessions, where the key is the socket and the value is the username.
     */
    private final ConcurrentHashMap<Socket, String> sessions = new ConcurrentHashMap<>();

    /**
     * Adds a new session for the given socket and username.
     *
     * @param socket   The socket associated with the session.
     * @param username The username associated with the session.
     */
    @Override
    public void addSession(Socket socket, String username) {
        sessions.put(socket, username);
        log.info("Session added for user: {}", username);
    }


    /**
     * Removes the session associated with the given socket.
     *
     * @param socket The socket whose session is to be removed.
     */
    @Override
    public void removeSession(Socket socket) {
        sessions.remove(socket);
        log.info("Session removed for socket: {}", socket);
    }

    /**
     * Retrieves the username associated with the given socket.
     *
     * @param socket The socket whose associated username is to be retrieved.
     * @return The username associated with the given socket, or null if no session exists.
     */
    @Override
    public String getUsername(Socket socket) {
        return sessions.get(socket);
    }
}
