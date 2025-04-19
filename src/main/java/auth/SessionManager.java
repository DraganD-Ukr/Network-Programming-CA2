package auth;

import java.net.Socket;

public interface SessionManager {

    /**
     * Adds a new session for the given socket and username.
     *
     * @param socket   The socket associated with the session.
     * @param username The username associated with the session.
     */
    void addSession(Socket socket, String username);

    /**
     * Removes the session associated with the given socket.
     *
     * @param socket The socket whose session is to be removed.
     */
    void removeSession(Socket socket);

    /**
     * Retrieves the username associated with the given socket.
     *
     * @param socket The socket whose associated username is to be retrieved.
     * @return The username associated with the given socket, or null if no session exists.
     */
    String getUsername(Socket socket);

}
