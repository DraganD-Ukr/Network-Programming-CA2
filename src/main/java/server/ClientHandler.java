package server;

import auth.SessionManager;
import model.email.EmailManager;
import model.user.UserManager;
import service.ResponseStatus;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final SessionManager sessionManager;
    private final UserManager userManager;
    private final EmailManager emailManager;
    private BufferedReader in;
    private BufferedWriter out;
    private String currentUser;

    public ClientHandler(Socket socket,
                         SessionManager sessionManager,
                         UserManager userManager,
                         EmailManager emailManager) {
        this.socket = socket;
        this.sessionManager = sessionManager;
        this.userManager = userManager;
        this.emailManager = emailManager;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            if (!authenticateUser()) {
                out.write("Authentication failed. Closing connection.\n");
                out.flush();
                return;
            }

            sessionManager.addSession(socket, currentUser);
            out.write("Welcome, " + currentUser + "!\n");
            out.flush();

        } catch (Exception e) {
            System.err.println("Client handler error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
            System.out.println("Client disconnected: " + socket.getInetAddress());
        }
    }

    private boolean authenticateUser() throws IOException {
        out.write("Welcome to SimpleMail. LOGIN <username> <password> or REGISTER <username> <password>\n");
        out.flush();

        String line = in.readLine();

        if (line == null) {
            return false;
        }

        String[] parts = line.split(" ");

        if (parts.length < 3) {
            out.write("Invalid command. Please use LOGIN <username> <password> or REGISTER <username> <password>\n");
            out.flush();
            return false;
        }

        String command = parts[0].toUpperCase();
        String username = parts[1];
        String password = parts[2];

        if (command.equals("LOGIN")) {
            if (userManager.login(username, password) == ResponseStatus.SUCCESS) {
                currentUser = username;
                return true;
            } else {
                out.write("Login failed. Please check your username and password.\n");
                out.flush();
                return false;
            }
        } else if (command.equals("REGISTER")) {
            if (userManager.register(username, password) == ResponseStatus.SUCCESS) {
                currentUser = username;
                return true;
            } else {
                out.write("Registration failed. Username may already exist.\n");
                out.flush();
                return false;
            }
        } else {
            out.write("Invalid command. Please use LOGIN <username> <password> or REGISTER <username> <password>\n");
            out.flush();
            return false;
        }
    }
}
