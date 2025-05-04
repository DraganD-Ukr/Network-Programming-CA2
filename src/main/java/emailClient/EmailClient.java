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
                System.out.println("  SEND <user> – send an email");
                System.out.println("  LIST – list received emails");
                System.out.println("  SEARCH <term> – search received emails");
                System.out.println("  SENT – list sent emails");
                System.out.println("  SEARCH_SENT $term – search sent emails");
                System.out.println("  RETR $id – read a specific email");
                System.out.println("  LOGOUT – log out of session");
                System.out.println("  EXIT – exit client");
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
        System.out.println("\n--- Authentication ---");
        System.out.println("LOGIN");
        System.out.println("REGISTER");
        System.out.println("EXIT\n");

        while (true) {
            System.out.print("[auth] > ");
            String cmd = console.nextLine().trim().toUpperCase();
            if (cmd.equals("EXIT")) {
                System.out.println("Exiting client.");
                return false;
            }

            if (cmd.equals("LOGIN")) {
                System.out.print("Username: ");
                String user = console.nextLine().trim();
                System.out.print("Password: ");
                String pass = console.nextLine().trim();
                sendLine("LOGIN%%" + user + "%%" + pass);

            } else if (cmd.equals("REGISTER")) {
                System.out.print("Username: ");
                String user = console.nextLine().trim();
                System.out.print("Password: ");
                String pass = console.nextLine().trim();
                System.out.print("Repeat Password: ");
                String pass2 = console.nextLine().trim();
                sendLine("REGISTER%%" + user + "%%" + pass + "%%" + pass2);

            } else {
                System.out.println("Usage: LOGIN | REGISTER | EXIT");
                continue;
            }

            String resp = reader.readLine();
            if (resp == null) {
                return false;
            }
            System.out.println(resp);

            if (resp.startsWith("SUCCESS")) {
                return true;
            } else {
                System.out.println("Try again or type EXIT.");
            }
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
            String line = console.nextLine();
            if (line == null) continue;

            String[] parts = line.trim().split("\\s+", 2);
            String verb = parts[0].toUpperCase();

            switch (verb) {
                // SEND_EMAIL command
                case "SEND":
                    handleSend();
                    break;
                case "LIST RECEIVED":
                    handleListReceived();
                    break;
                case "SEARCH RECEIVED":
                    handleSearch("received", parts.length > 1 ? parts[1] : "");
                    break;
                case "SENT":
                    handleListSent();
                    break;
                case "SEARCH_SENT":
                    handleSearch("sent", parts.length > 1 ? parts[1] : "");
                    break;
                case "READ":
                    handleRead(parts.length>1?parts[1]:"");
                    break;
                    // LOGOUT command
                case "LOGOUT":
                    handleLogout();
                    return;
                case "EXIT":
                    System.out.println("Exiting client.");
                    return;
                default:
                    System.out.println("Unknown command.");
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
     * @throws IOException if an I/O error occurs while reading from or writing to the socket
     */
    private void handleSend() throws IOException {
        System.out.print("Recipient: ");
        String recipient = console.nextLine().trim();
        System.out.print("Subject: ");
        String subject = console.nextLine().trim();
        System.out.print("Body: ");
        String content = console.nextLine().trim();

        sendLine("SEND_EMAIL%%" + recipient + "%%" + subject + "%%" + content);

        String resp = reader.readLine();
        if (resp.startsWith("RECIPIENT_NOT_FOUND")) {
            System.out.println("Invalid recipient.");
        } else if (resp.startsWith("SUCCESS")) {
            System.out.println("Email sent successfully.");
        } else {
            System.out.println("Failed to send email.");
        }
    }

    /**
     * Handles the reading of a specific email.
     *
     * @param id the ID of the email to read
     * @throws IOException if an I/O error occurs while reading from or writing to the socket
     */
    private void handleRead(String id) throws IOException {
        sendLine("READ_EMAIL%%" + id);
        String resp = reader.readLine();
        if (resp.startsWith("INVALID_ID")) {
            System.out.println("Invalid message ID.");
        } else if (resp.startsWith("SUCCESS%%")) {
            String[] f = resp.substring(9).split("%%", 6);
            System.out.println("From: " + f[1]);
            System.out.println("To: " + f[2]);
            System.out.println("Subject: " + f[3]);
            System.out.println("Date: " + f[5]);
            System.out.println("Context:\n\n" + f[4]);
        }
    }

    /**
     * Handles the listing of received emails.
     *
     * @throws IOException if an I/O error occurs while reading from or writing to the socket
     */
    private void handleListReceived() throws IOException {
        sendLine("GET_RECEIVED_EMAILS");
        String resp = reader.readLine();
        if (resp.startsWith("NO_EMAILS_FOUND")) {
            System.out.println("No received emails.");
        } else if (resp.startsWith("SUCCESS##")) {
            String[] items = resp.substring(9).split("##");
            System.out.println("ID | Sender | Subject | Timestamp | Read");
            for (String item : items) {
                String[] f = item.split("%%");
                System.out.printf("%s | %s | %s | %s | %s%n", f[0], f[1], f[2], f[3], f[4]);
            }
        }
    }

    /**
     * Handles the search for sent emails.
     *
     * @param keyword the search term
     * @throws IOException if an I/O error occurs while reading from or writing to the socket
     */
    private void handleSearch(String type, String keyword) throws IOException {
        if (type.equalsIgnoreCase("received")) {
            sendLine("SEARCH_DETAILS%%received%%" + keyword);
        } else if (type.equalsIgnoreCase("sent")) {
            sendLine("SEARCH_DETAILS%%sent%%" + keyword);
        } else {
            System.out.println("Invalid type. Use 'received' or 'sent'.");
            return;
        }
        String resp = reader.readLine();
        if (resp == null) {
            System.out.println("No response from server.");
            return;
        }

        if (resp.startsWith("NO_RECEIVED_EMAIL") && type.equalsIgnoreCase("received")) {
            System.out.println("No matches in received.");
        } else if (resp.startsWith("NO_SENT_EMAIL") && type.equalsIgnoreCase("sent")) {
            System.out.println("No matches in sent.");
        } else if (resp.startsWith("SUCCESS##")) {
            String[] items = resp.substring("SUCCESS##".length()).split("##");
            if (type.equalsIgnoreCase("received")) {
                System.out.println("ID | Sender | Subject | Timestamp | Read");
                for (String item : items) {
                    String[] f = item.split("%%");
                    System.out.printf("%s | %s | %s | %s | %s%n", f[0], f[1], f[2], f[3], f[4]);
                }
            } else {
                System.out.println("ID | Recipient | Subject | Timestamp");
                for (String item : items) {
                    String[] f = item.split("%%");
                    System.out.printf("%s | %s | %s | %s%n", f[0], f[1], f[2], f[3]);
                }
            }
        }
    }


    /**
     * Handles the listing of sent emails.
     *
     * @throws IOException if an I/O error occurs while reading from or writing to the socket
     */
    private void handleListSent() throws IOException {
        sendLine("GET_SENT_EMAILS");
        String resp = reader.readLine();
        if (resp.startsWith("NO_EMAILS_FOUND")) {
            System.out.println("No sent emails.");
        } else if (resp.startsWith("SUCCESS##")) {
            String[] items = resp.substring(9).split("##");
            System.out.println("ID | Recipient | Subject | Timestamp");
            for (String item : items) {
                String[] f = item.split("%%");
                System.out.printf("%s | %s | %s | %s%n", f[0], f[1], f[2], f[3]);
            }
        } else {
            System.out.println("Failed to retrieve sent emails.");
        }
    }

    /**
     * Handles the logout process.
     *
     * @throws IOException if an I/O error occurs while reading from or writing to the socket
     */
    private void handleLogout() throws IOException {
        sendLine("LOGOUT");
        String resp = reader.readLine();
        System.out.println(resp);
    }
}