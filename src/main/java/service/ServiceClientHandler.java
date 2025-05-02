package service;

import lombok.extern.slf4j.Slf4j;
import model.email.EmailManager;
import model.user.User;
import model.user.UserManager;
import network.TcpNetworkLayer;

import java.io.IOException;
import java.net.Socket;

@Slf4j
public class ServiceClientHandler implements Runnable{


    private Socket clientDataSocket;
    private TcpNetworkLayer networkLayer;
    private EmailManager emailManager;
    private UserManager userManager;
    private User loggedInUser;

    public ServiceClientHandler(Socket clientDataSocket, EmailManager emailManager, UserManager userManager) throws IOException {
        this.clientDataSocket = clientDataSocket;
        this.networkLayer = new TcpNetworkLayer(clientDataSocket);
        this.emailManager = emailManager;
        this.userManager = userManager;
    }



    @Override
    public void run() {

        try {
            boolean validClientSession = true;
            boolean loginStatus = false;

            while (validClientSession){
                String request = networkLayer.receive();
                String[] requestParts = request.split(EmailUtils.DELIMITER);

                if(requestParts.length == 0){
                    continue;
                }

                String response = null;

                String requestType = requestParts[0];

                switch (requestType) {
                    case EmailUtils.REGISTER :
                        response = handleRegister(requestParts);
                        break;

                    case EmailUtils.LOGIN :
                        response = handleLogin(requestParts);
                        if (response.equals(ResponseStatus.SUCCESS)) {
                            loginStatus = true;
                        }
                        break;

//                    case EmailUtils.SEND_EMAIL -> {
//                        // Handle sending email
//                    }
//                    case EmailUtils.GET_RECEIVED_EMAILS -> {
//                        // Handle getting received emails
//                    }
//                    case EmailUtils.GET_SENT_EMAILS -> {
//                        // Handle getting sent emails
//                    }
//                    case EmailUtils.READ_EMAIL -> {
//                        // Handle reading email
//                    }
//                    case EmailUtils.SEARCH_EMAIL -> {
//                        // Handle searching emails
//                    }
                    case EmailUtils.LOGOUT :
                        handleLogout(requestParts);
                        validClientSession = false;
                        break;
                }



            }
//            TODO: Specify exception type
        } catch (Exception e){
            log.error("Error while handling client request: ", e);
        }
    }


    private String handleRegister(String[] requestParts) {


        if (requestParts.length != 4) {
            return ResponseStatus.INVALID.toString();
        }

        String username = requestParts[1];
        String password = requestParts[2];
        String confirmedPassword = requestParts[3];

        if (!password.equals(confirmedPassword)) {
            return ResponseStatus.PASSWORDS_DO_NOT_MATCH.toString();
        }

        ResponseStatus responseStatus = userManager.register(username, password);
        return responseStatus.toString();
    }

    private String handleLogin(String[] requestParts) {

        if (requestParts.length != 3) {
            return ResponseStatus.INVALID.toString();
        }

        String username = requestParts[1];
        String password = requestParts[2];

        ResponseStatus responseStatus = userManager.login(username, password, clientDataSocket);
        loggedInUser = userManager.getUserByUsername(username);

        return responseStatus.toString();
    }

    private String handleLogout(String[] requestParts) {

        if (requestParts.length != 2) {
            return ResponseStatus.INVALID.toString();
        }

        String username = requestParts[1];

        ResponseStatus responseStatus = userManager.logout(username, clientDataSocket);
        return responseStatus.toString();
    }

    private String handleSendEmail(String[] requestParts) {
        if (requestParts.length != 4) {
            return ResponseStatus.INVALID.toString();
        }

        String senderUsername = loggedInUser.getUsername();
        String recipientUsername = requestParts[1];
        String subject = requestParts[2];
        String content = requestParts[3];

        ResponseStatus responseStatus = emailManager.sendEmail(senderUsername, recipientUsername, subject, content);
        return responseStatus.toString();
    }


}
