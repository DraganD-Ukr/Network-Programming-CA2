package server;

import auth.SessionManager;
import auth.SessionManagerImpl;
import lombok.extern.slf4j.Slf4j;
import model.email.EmailManager;
import model.email.EmailManagerImpl;
import model.user.UserManager;
import model.user.UserManagerImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class EmailServer {
    private static final int PORT = 6969;
    private int port;

    private final SessionManager sessionManager;
    private final UserManager userManager;
    private final EmailManager emailManager;

    public EmailServer(int port) {
        this.port = port;
        this.sessionManager = new SessionManagerImpl();
        this.userManager = new UserManagerImpl();
        this.emailManager = new EmailManagerImpl();
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("EmailServer started on port {}", port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                log.info("New client connected: {}", clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, sessionManager, userManager, emailManager);
                Thread thread = new Thread(handler);
                thread.start();
            }
        } catch (IOException e) {
            log.error("Error starting server", e);
        }
    }

    public static void main(String[] args) {
        int port = PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port " + PORT);
            }
        }

        EmailServer server = new EmailServer(port);
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
}
