package emailClient;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.regex.Pattern;

@Slf4j
public class EmailClient {
    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String currentUsername;
    private static final String EMAIL_DELIMITER = "##";
    private static final String FIELD_DELIMITER = "%%";
    // 12 characters minimum, 2 special characters, 1 digit, 1 uppercase letter, 1 lowercase letter
    private static final Pattern PASSWORD_POLICY = Pattern.compile("^(?=.{12,}$)(?=(?:.*\\W){2,})(?=.*\\d)(?=.*[A-Z])(?=.*[a-z]).*$");

    /**
     * Constructor for EmailClient.
     * Initializes the host and port for the email server.
     *
     * @param host the hostname of the email server
     * @param port the port number of the email server
     */
    public EmailClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Validates the password against the defined policy.
     * The password must be at least 12 characters long, contain at least 2 special characters,
     * 1 uppercase letter, 1 lowercase letter, and 1 digit.
     *
     * @param pw the password to validate
     * @return true if the password is valid, false otherwise
     */
    private boolean isValidPassword(String pw) {
        return PASSWORD_POLICY.matcher(pw).matches();
    }

    private void log(String msg) {
        System.out.println("[DEBUG] " + msg);
    }

    /**
     * Starts the email client.
     * Connects to the server, handles user authentication, and processes commands.
     */
    public void start() {
        //log("Starting client");
        try {
            connect();
            Scanner console = new Scanner(System.in);
            boolean running = true;

            while (running) {
                if (!showAuthentication(console)) {
                    break;
                }

                //log("User authenticated: " + currentUsername);

                boolean inSession = true;
                while (inSession) {
                    printMainMenu();
                    System.out.print("> ");
                    String input = console.nextLine().trim();
                    if (input.isEmpty()) continue;

                    String[] parts = input.split("\\s+", 2);
                    String command = parts[0].toUpperCase();

                    switch (command) {
                        case "SEND":
                            handleSend(parts, console);
                            break;
                        case "LIST":
                            handleListReceived();
                            break;
                        case "SEARCH":
                            handleSearchReceived(parts.length > 1 ? parts[1] : "");
                            break;
                        case "SENT":
                            handleListSent();
                            break;
                        case "SEARCH_SENT":
                            handleSearchSent(parts.length > 1 ? parts[1] : "", console);
                            break;
                        case "READ":
                            handleRead(parts.length > 1 ? parts[1] : "");
                            break;
                        case "LOGOUT":
                            //log("Logging out user " + currentUsername);
                            handleLogout();
                            currentUsername = null;
                            inSession = false;
                            break;
                        case "EXIT":
                            //log("Exiting client");
                            handleExit();
                            inSession = false;
                            running = false;
                            break;
                        default:
                            System.out.println("Unknown command. Available: SEND, LIST, SEARCH, SENT, SEARCH_SENT, READ, LOGOUT, EXIT");
                    }
                }
            }

            console.close();
        } catch (UnknownHostException e) {
            //log("Unknown host: " + e.getMessage());
        } catch (IOException e) {
            //log("I/O error: " + e.getMessage());
        } finally {
            shutdown();
            //log("Client terminated");
        }
    }

    /**
     * Connects to the email server using the specified host and port.
     * Initializes the socket, reader, and writer for communication.
     *
     * @throws IOException if an I/O error occurs
     */
    private void connect() throws IOException {
        //log("Connecting to " + host + ":" + port);
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        //log("Connection established");
    }

    /**
     * Prints the emails received from the server.
     * If the response contains multiple emails, it splits them and prints each one.
     *
     * @param raw the raw response string from the server
     */
    private void printEmails(String raw) {
        if (raw == null) return;

        if (raw.contains(EMAIL_DELIMITER)) {
            String[] chunks = raw.split(EMAIL_DELIMITER, -1);
            for (int i = 1; i < chunks.length; i++) {
                printOneEmail(chunks[i]);
            }
            return;
        }

        String prefix = "SUCCESS" + FIELD_DELIMITER;
        if (raw.startsWith(prefix)) {
            String single = raw.substring(prefix.length());
            printOneEmail(single);
            return;
        }

        System.out.println(raw);
    }

    /**
     * Prints a single email's details.
     * Splits the email string into its components and formats them for display.
     *
     * @param chunk the email string to print
     */
    private void printOneEmail(String chunk) {
        String[] f = chunk.split(FIELD_DELIMITER, -1);
        if (f.length < 7) return;

        String id = f[0];
        String from = f[1];
        String to = f[2];
        String subject = f[3];
        String body = f[4];
        String sentAt = f[5];
        String isRead = f[6];

        System.out.printf("ID: %s | From: %s | To: %s | Subject: %s | Date: %s | Read: %s%n" +
                        "    %s%n%n",
                id, from, to, subject, sentAt, isRead, body
        );
    }

    /**
     * Displays the authentication menu and handles user input for login or registration.
     * If the user chooses to exit, it sends an exit command to the server.
     *
     * @param console the scanner for user input
     * @return true if authentication is successful, false if the user chooses to exit
     * @throws IOException if an I/O error occurs
     */
    private boolean showAuthentication(Scanner console) throws IOException {
        System.out.println("\n--- Authentication ---");
        System.out.println("LOGIN");
        System.out.println("REGISTER");
        System.out.println("EXIT\n");

        while (true) {
            System.out.print("[auth] > ");
            String cmd = console.nextLine().trim().toUpperCase();
            if (cmd.equals("EXIT")) {
                sendLine("EXIT");
                return false;
            }
            if (cmd.equals("LOGIN") || cmd.equals("REGISTER")) {
                boolean isLogin = cmd.equals("LOGIN");
                System.out.print("Username: ");
                String user = console.nextLine().trim();
                System.out.print("Password: ");
                String pass = console.nextLine().trim();

                String message;
                if (!isLogin) {
                    System.out.print("Repeat Password: ");
                    String pass2 = console.nextLine().trim();

                    if (!pass.equals(pass2)) {
                        System.out.println("Passwords do not match. Try again.");
                        continue;
                    }
                    if (!isValidPassword(pass)) {
                        System.out.println("Your password must be at least 12 characters, include at least 2 special characters, 1 uppercase, 1 lowercase and 1 digit.");
                        continue;
                    }
                    message = "REGISTER%%" + user + "%%" + pass + "%%" + pass2;

                } else {
                    message = "LOGIN%%" + user + "%%" + pass;
                }
                sendLine(message);
                currentUsername = user;

                String resp = reader.readLine();
                //log("Auth response: " + resp);
                System.out.println(resp);
                if (resp != null && resp.startsWith("SUCCESS")) {
                    return true;
                } else {
                    System.out.println("Authentication failed. Try again.");
                }

            } else {
                System.out.println("Usage: LOGIN | REGISTER | EXIT");
            }
        }
    }

    /**
     * Prints the main menu with available commands.
     */
    private void printMainMenu() {
        System.out.println("\nAvailable commands:");
        System.out.println("  SEND <user>       – send an email");
        System.out.println("  LIST              – list received emails");
        System.out.println("  SEARCH <term>     – search received emails");
        System.out.println("  SENT              – list sent emails");
        System.out.println("  SEARCH_SENT <term>– search sent emails");
        System.out.println("  READ <id>         – read a specific email");
        System.out.println("  LOGOUT            – log out");
        System.out.println("  EXIT              – exit client\n");
    }

    /**
     * Sends an email to a recipient.
     * If no recipient is provided, prompts the user for input.
     *
     * @param parts the command parts
     * @param console the scanner for user input
     * @throws IOException if an I/O error occurs
     */
    private void handleSend(String[] parts, Scanner console) throws IOException {
        String recipient;
        if (parts.length > 1 && !parts[1].isBlank()) {
            recipient = parts[1].trim();
        } else {
            System.out.print("Recipient: ");
            recipient = console.nextLine().trim();
        }
        System.out.print("Subject: ");
        String subject = console.nextLine().trim();
        System.out.print("Body: ");
        String body = console.nextLine().trim();
        sendLine("SEND_EMAIL%%" + recipient + "%%" + subject + "%%" + body);
        System.out.println(reader.readLine());
    }

    /**
     * Lists received emails.
     * Sends a request to the server and prints the response.
     *
     * @throws IOException if an I/O error occurs
     */
    private void handleListReceived() throws IOException {
        sendLine("GET_RECEIVED_EMAILS");
        String resp = reader.readLine();
        printEmails(resp);
    }

    /**
     * Lists sent emails.
     * Sends a request to the server and prints the response.
     *
     * @throws IOException if an I/O error occurs
     */
    private void handleListSent() throws IOException {
        sendLine("GET_SENT_EMAILS");
        String resp = reader.readLine();
        if (resp != null && resp.startsWith("SUCCESS")) {
            printEmails(resp);
        } else {
            System.out.println(resp);
        }
    }

    /**
     * Searches received emails by subject.
     * If no term is provided, prompts the user for input.
     *
     * @param term the search term
     * @throws IOException if an I/O error occurs
     */
    private void handleSearchReceived(String term) throws IOException {
        sendLine("SEARCH_DETAILS%%RECEIVED%%" + term);
        String resp = reader.readLine();
        if (resp != null && resp.startsWith("SUCCESS")) {
            printEmails(resp);
        } else {
            System.out.println(resp);
        }
    }

    /**
     * Searches sent emails by subject.
     * If no term is provided, prompts the user for input.
     *
     * @param term the search term
     * @param console the scanner for user input
     * @throws IOException if an I/O error occurs
     */
    private void handleSearchSent(String term, Scanner console) throws IOException {
        if (term.isBlank()) {
            System.out.print("Subject to search sent for: ");
            term = console.nextLine().trim();
        }
        sendLine("SEARCH_DETAILS%%SENT%%" + term);
        String resp = reader.readLine();
        if (resp != null && resp.startsWith("SUCCESS")) {
            printEmails(resp);
        } else {
            System.out.println(resp);
        }
    }

    /**
     * Reads a specific email by ID.
     * If no ID is provided, prompts the user for input.
     *
     * @param id the email ID
     * @throws IOException if an I/O error occurs
     */
    private void handleRead(String id) throws IOException {
        sendLine("READ_EMAIL%%" + id);
        String resp = reader.readLine();
        printEmails(resp);
    }

    /**
     * Handles user logout.
     * Sends a logout request to the server and prints the response.
     *
     * @throws IOException if an I/O error occurs
     */
    private void handleLogout() throws IOException {
        sendLine("LOGOUT%%" + currentUsername);
        System.out.println(reader.readLine());
    }

    /**
     * Handles client exit.
     * Sends an exit request to the server.
     *
     * @throws IOException if an I/O error occurs
     */
    private void handleExit() throws IOException {
        sendLine("EXIT");
    }

    /**
     * Sends a line of text to the server.
     * Flushes the writer after sending the line.
     *
     * @param line the line to send
     * @throws IOException if an I/O error occurs
     */
    private void sendLine(String line) throws IOException {
        writer.write(line);
        writer.newLine();
        writer.flush();
        //log("Sent: " + line);
    }

    /**
     * Shuts down the client by closing the socket and streams.
     * Catches and ignores any IOExceptions that occur during shutdown.
     */
    private void shutdown() {
        //log("Shutting down client");
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    /**
     * Main method to start the EmailClient.
     * Accepts optional command line arguments for host and port.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        String host = "localhost";
        int port = 6969;
        if (args.length == 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        new EmailClient(host, port).start();
    }
}