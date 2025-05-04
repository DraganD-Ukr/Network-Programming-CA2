package emailClient;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class EmailClient {
    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private final Scanner console = new Scanner(System.in);

    /**
     * Constructor for EmailClient.
     *
     * @param host the server host
     * @param port the server port
     */
    public EmailClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Starts the email client.
     */
    public void start() {
        try {
            connect();
            if (authenticate()) {
                System.out.println("\nAvailable commands:");
                System.out.println("  SEND <user>           – send an email");
                System.out.println("  LIST                  – list received emails");
                System.out.println("  SEARCH <term>         – search received emails");
                System.out.println("  SENT                  – list sent emails");
                System.out.println("  SEARCH_SENT <term>    – search sent emails");
                System.out.println("  RETR <emailId>        – read a specific email");
                System.out.println("  LOGOUT                – log out of session");
                System.out.println("  EXIT                  – exit client");
                System.out.println();
                launchListener();
                commandLoop();
            }
        } catch (UnknownHostException uhe) {
            System.err.println("Unknown host: " + host);
        } catch (IOException ioe) {
            System.err.println("I/O error: " + ioe.getMessage());
        } finally {
            shutdown();
        }
    }

    /**
     * Connects to the server.
     *
     * @throws IOException if an I/O error occurs when creating the socket or streams
     */
    private void connect() throws IOException {
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        System.out.println("Connected to " + host + ":" + port);
    }

    /**
     * Authenticates the user with the server.
     *
     * @return true if authentication is successful, false otherwise
     * @throws IOException if an I/O error occurs while reading from or writing to the socket
     */
    private boolean authenticate() throws IOException {
        if (reader.ready()) {
            String greeting = reader.readLine();
            if (greeting != null) System.out.println(greeting);
        }

        // Show the auth menu
        System.out.println("\nPlease choose:");
        System.out.println("  LOGIN <username> <password>");
        System.out.println("  REGISTER <username> <password>");
        System.out.println("  EXIT");
        System.out.println();

        while (true) {
            System.out.print("[auth] > ");
            String line = console.nextLine();
            if (line == null) return false;

            String[] tokens = line.trim().split("\\s+");
            String cmd = tokens[0].toUpperCase();

            if (cmd.equals("EXIT")) {
                System.out.println("Exiting client.");
                return false;
            }

            if (tokens.length != 3 || (!cmd.equals("LOGIN") && !cmd.equals("REGISTER"))) {
                System.out.println("Usage: LOGIN <user> <pass>  or  REGISTER <user> <pass>  or  EXIT");
                continue;
            }

            sendLine(line);
            String resp = reader.readLine();
            if (resp == null) return false;
            System.out.println(resp);

            if (resp.startsWith(cmd + "_SUCCESS")) {
                return true;
            }

            System.out.println("Authentication failed. Try again or type EXIT.");
        }
    }

    /**
     * Launches a listener thread to read messages from the server.
     */
    private void launchListener() {
        Thread listener = new Thread(() -> {
            try {
                String srvMsg;
                while ((srvMsg = reader.readLine()) != null) {
                    System.out.println(srvMsg);
                }
            } catch (IOException e) {
                System.err.println("Error reading from server: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    /**
     * Command loop to read user commands and send them to the server.
     *
     * @throws IOException if an I/O error occurs while reading from or writing to the socket
     */
    private void commandLoop() throws IOException {
        while (true) {
            System.out.print("[mail] > ");
            String cmd = console.nextLine();
            if (cmd == null) {
                continue;
            }

            String[] parts = cmd.trim().split(" ", 2);
            String verb = parts[0].toUpperCase();

            if (verb.equals("QUIT")) {
                sendLine("QUIT");
                break;
            } else if (verb.equals("EXIT")) {
                System.out.println("Exiting client.");
                break;
            } else if (verb.equals("LIST")) {
                handleList();
            } else if (verb.equals("SEND")){
                handleSend(parts.length > 1 ? parts[1] : null);
            } else if (verb.equals("SEARCH")) {
                handleSearch(parts.length > 1 ? parts[1] : "");
            } else if (verb.equals("SENT")) {
                handleSent();
            } else if (verb.equals("SEARCH_SENT")) {
                handleSearchSent(parts.length > 1 ? parts[1] : "");
            } else if (verb.equals("RETRIEVE")) {
                handleRetrieve(parts.length > 1 ? parts[1].trim() : "");
            } else if (verb.equals("LOGOUT")) {
                handleLogout();
                break;
            } else {
                sendLine(cmd);
            }
        }
    }

    /**
     * Sends a line of text to the server.
     *
     * @param line the line to send
     * @throws IOException if an I/O error occurs while writing to the socket
     */
    private void sendLine(String line) throws IOException {
        writer.write(line + "\n");
        writer.flush();
    }

    /**
     * Shuts down the client by closing the socket and console.
     */
    private void shutdown() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
        console.close();
        System.out.println("Disconnected.");
    }

    /**
     * Main method to start the EmailClient.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        String host = "localhost";
        int port = 6969;
        if (args.length == 2) {
            host = args[0];
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignore) {}
        }
        new EmailClient(host, port).start();
    }

    /**
     * Handles the sending of an email.
     *
     * @param recipient the recipient of the email
     * @throws IOException if an I/O error occurs while reading from or writing to the socket
     */
    private void handleSend(String recipient) throws IOException {
        if (recipient == null || recipient.isEmpty()) {
            System.out.println("Usage: SEND <recipient>");
            return;
        }

        sendLine("SEND " + recipient);

        System.out.print("Enter subject: ");
        String subject = console.nextLine();
        sendLine(subject);

        System.out.print("Enter body (end with a single '.' on its own line): ");
        String line;

        while (!(line = console.nextLine()).equals(".")) {
            sendLine(line);
        }

        sendLine(".");

        String response = reader.readLine();
        if (response == null) {
            System.out.println("Error: No response from server.");
            return;
        }
    }

    private void handleList() throws IOException {
        sendLine("LIST");

        System.out.println("Fetching email list...");

        String response;
        try {
            while ((response = reader.readLine()) != null && !response.equals(".")) {
                System.out.println(response);
            }
        } catch (IOException e) {
            System.err.println("Error reading from server: " + e.getMessage());
        }
    }

    /**
     * Handles the search for emails.
     *
     * @param search the search term
     * @throws IOException if an I/O error occurs while reading from or writing to the socket
     */
    private void handleSearch(String search) throws IOException {
        if (search == null || search.isEmpty()) {
            System.out.println("Usage: SEARCH <search term>");
            return;
        }

        sendLine("SEARCH " + search);

        System.out.println("Searching for emails...");

        String response;
        try {
            while ((response = reader.readLine()) != null && !response.equals(".")) {
                System.out.println(response);
            }
        } catch (IOException e) {
            System.err.println("Error reading from server: " + e.getMessage());
        }
    }

    /**
     * Handles the retrieval of sent emails.
     *
     * @throws IOException if an I/O error occurs while reading from or writing to the socket
     */
    private void handleSent() throws IOException {
        sendLine("SENT");

        System.out.println("Fetching sent emails...");

        String response;
        try {
            while ((response = reader.readLine()) != null && !response.equals(".")) {
                System.out.println(response);
            }
        } catch (IOException e) {
            System.err.println("Error reading from server: " + e.getMessage());
        }
    }

    /**
     * Handles the search for sent emails.
     *
     * @param search the search term
     * @throws IOException if an I/O error occurs while reading from or writing to the socket
     */
    private void handleSearchSent(String search) throws IOException {
        if (search == null || search.isEmpty()) {
            System.out.println("Usage: SEARCH_SENT <search term>");
            return;
        }

        sendLine("SEARCH_SENT " + search);

        System.out.println("Searching sent emails...");

        String response;
        try {
            while ((response = reader.readLine()) != null && !response.equals(".")) {
                System.out.println(response);
            }
        } catch (IOException e) {
            System.err.println("Error reading from server: " + e.getMessage());
        }
    }

    /**
     * Handles the retrieval of a specific email.
     *
     * @param emailId the ID of the email to retrieve
     * @throws IOException if an I/O error occurs while reading from or writing to the socket
     */
    private void handleRetrieve(String emailId) throws IOException {
        if (emailId == null || emailId.isEmpty()) {
            System.out.println("Usage: RETRIEVE <email ID>");
            return;
        }

        sendLine("RETRIEVE " + emailId);

        System.out.println("Fetching email...");

        String response;
        try {
            while ((response = reader.readLine()) != null && !response.equals(".")) {
                System.out.println(response);
            }
        } catch (IOException e) {
            System.err.println("Error reading from server: " + e.getMessage());
        }
    }

    /**
     * Handles the logout process.
     *
     * @throws IOException if an I/O error occurs while reading from or writing to the socket
     */
    private void handleLogout() throws IOException {
        sendLine("LOGOUT");
        System.out.println("Logging out...");
        String response = reader.readLine();
        if (response == null) {
            System.out.println("Error: No response from server.");
            return;
        }
        System.out.println(response);
    }
}