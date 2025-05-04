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
    private String currentUsername;

    public EmailClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private void log(String msg) {
        System.out.println("[DEBUG] " + msg);
    }

    public void start() {
        log("Starting client");
        try {
            connect();
            Scanner console = new Scanner(System.in);
            boolean running = true;

            while (running) {
                if (!showAuthentication(console)) {
                    break;
                }

                log("User authenticated: " + currentUsername);

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
                            handleSend(console);
                            break;
                        case "LIST":
                            handleListReceived();
                            break;
                        case "SEARCH":
                            handleSearch("received", parts.length > 1 ? parts[1] : "");
                            break;
                        case "SENT":
                            handleListSent();
                            break;
                        case "SEARCH_SENT":
                            handleSearch("sent", parts.length > 1 ? parts[1] : "");
                            break;
                        case "READ":
                            handleRead(parts.length > 1 ? parts[1] : "");
                            break;
                        case "LOGOUT":
                            log("Logging out user " + currentUsername);
                            handleLogout();
                            currentUsername = null;
                            inSession = false;
                            break;
                        case "EXIT":
                            log("Exiting client");
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
            log("Unknown host: " + e.getMessage());
        } catch (IOException e) {
            log("I/O error: " + e.getMessage());
        } finally {
            shutdown();
            log("Client terminated");
        }
    }

    private void connect() throws IOException {
        log("Connecting to " + host + ":" + port);
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        log("Connection established");
    }

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

                if (!isLogin) {
                    System.out.print("Repeat Password: ");
                    String pass2 = console.nextLine().trim();
                    sendLine("REGISTER%%" + user + "%%" + pass + "%%" + pass2);
                } else {
                    sendLine("LOGIN%%" + user + "%%" + pass);
                }
                currentUsername = user;

                String resp = reader.readLine();
                log("Auth response: " + resp);
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

    private void handleSend(Scanner console) throws IOException {
        System.out.print("Recipient: ");
        String recipient = console.nextLine().trim();
        System.out.print("Subject: ");
        String subject = console.nextLine().trim();
        System.out.print("Body: ");
        String body = console.nextLine().trim();
        sendLine("SEND_EMAIL%%" + recipient + "%%" + subject + "%%" + body);
        System.out.println(reader.readLine());
    }

    private void handleListReceived() throws IOException {
        sendLine("GET_RECEIVED_EMAILS");
        System.out.println(reader.readLine());
    }

    private void handleListSent() throws IOException {
        sendLine("GET_SENT_EMAILS");
        System.out.println(reader.readLine());
    }

    private void handleSearch(String type, String keyword) throws IOException {
        sendLine("SEARCH_DETAILS%%" + type + "%%" + keyword);
        System.out.println(reader.readLine());
    }

    private void handleRead(String id) throws IOException {
        sendLine("READ_EMAIL%%" + id);
        System.out.println(reader.readLine());
    }

    private void handleLogout() throws IOException {
        sendLine("LOGOUT%%" + currentUsername);
        System.out.println(reader.readLine());
    }

    private void handleExit() throws IOException {
        sendLine("EXIT");
    }

    private void sendLine(String line) throws IOException {
        writer.write(line);
        writer.newLine();
        writer.flush();
        log("Sent: " + line);
    }

    private void shutdown() {
        log("Shutting down client");
        try {
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

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