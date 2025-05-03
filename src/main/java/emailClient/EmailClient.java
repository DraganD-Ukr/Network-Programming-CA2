package emailClient;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


@Slf4j
public class EmailClient {
    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private final Scanner console = new Scanner(System.in);

    // Default constructor
    public EmailClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // Constructor with default host and port
    public void start() {
        try {
            connect();
            if (authenticate()) {
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
        String greeting = reader.readLine();
        if (greeting == null) return false;
        System.out.println(greeting);

        while (true) {
            System.out.print("[auth] > ");
            String line = console.nextLine();
            if (line == null) return false;
            String[] tokens = line.trim().split(" ");
            if (tokens.length != 3) {
                System.out.println("Usage: LOGIN <user> <pass>  or  REGISTER <user> <pass>");
                continue;
            }
            String cmd = tokens[0].toUpperCase();
            if (!cmd.equals("LOGIN") && !cmd.equals("REGISTER")) {
                System.out.println("Supported: LOGIN or REGISTER");
                continue;
            }
            sendLine(line);
            String resp = reader.readLine();
            if (resp == null) return false;
            System.out.println(resp);
            if (resp.startsWith(cmd + "_SUCCESS")) {
                return true;
            }
            System.out.println("Failed - try again or type EXIT to quit");
            if (line.equalsIgnoreCase("EXIT")) return false;
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
            log.info("[mail] > ");
            String cmd = console.nextLine();
            if (cmd == null) {
                continue;
            }

            String[] parts = cmd.trim().split(" ", 2);
            String verb = cmd.trim().split(" ", 2)[0].toUpperCase();

            if (verb.equals("QUIT") || verb.equals("EXIT")) {
                sendLine("QUIT");
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
            } else {
                sendLine(cmd);
            }

            sendLine(cmd);
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
}