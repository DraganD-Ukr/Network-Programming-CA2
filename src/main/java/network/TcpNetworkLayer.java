package network;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class TcpNetworkLayer {

    private Socket dataSocket;
    private Scanner inputStream;
    private PrintWriter outputStream;
    private String hostname;
    private int port;

    public TcpNetworkLayer(String hostname, int port){
        this.hostname = hostname;
        this.port = port;
    }

    public TcpNetworkLayer(Socket dataSocket) throws IOException {
        if(dataSocket == null){
            throw new IllegalArgumentException("Socket cannot be null");
        }

        this.dataSocket = dataSocket;
        setStreams();
    }

    private void setStreams() throws IOException {
        this.inputStream = new Scanner(dataSocket.getInputStream());
        this.outputStream = new PrintWriter(dataSocket.getOutputStream());
    }


    public void send(String message){
        outputStream.println(message);
        outputStream.flush();
    }

    public String receive(){
        return inputStream.nextLine();
    }

    public void disconnect() throws IOException {
        if(this.dataSocket != null) {
            this.outputStream.close();
            this.inputStream.close();
            this.dataSocket.close();
        }
    }

    public void connect() throws IOException {
        dataSocket = new Socket(hostname, port);
        setStreams();
    }
}
