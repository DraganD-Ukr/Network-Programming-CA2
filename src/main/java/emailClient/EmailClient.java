package emailClient;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


public class EmailClient {
    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private Scanner console;

    public EmailClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.console = new Scanner(System.in);
    }

    public void start() {
        try {
            socket = new Socket(host, port);
            System.out.println("Connected to server " + host + ":" + port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            new Thread(this::readServer).start();

            while (true) {
                String userInput = console.nextLine();
                if (userInput == null || userInput.equalsIgnoreCase("quit")) {
                    out.write("QUIT\n");
                    out.flush();
                    break;
                }
                out.write(userInput + "\n");
                out.flush();
            }
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + host);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        } finally {
            close();
        }
    }

    private void readServer() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Server: " + line);
            }
        } catch (IOException e) {
            System.err.println("Error reading from server: " + e.getMessage());
        } finally {
            close();
        }
    }

    private void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            if (console != null) console.close();
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 6969;

        if (args.length > 0) {
            host = args[0];
        }
        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default " + port);
            }
        }

        EmailClient client = new EmailClient(host, port);
        client.start();
    }
}
