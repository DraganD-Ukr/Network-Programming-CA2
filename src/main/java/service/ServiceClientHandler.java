package service;

import lombok.extern.slf4j.Slf4j;
import model.email.Email;
import model.email.EmailManager;
import model.email.SearchType;
import model.user.User;
import model.user.UserManager;
import network.TcpNetworkLayer;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Optional;

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

//            Valid client exception can be used for EXIT request, etc..
            boolean validClientSession = true;


            while (validClientSession){
                String request = networkLayer.receive();
                String[] requestParts = request.split(EmailUtils.DELIMITER);

                if(requestParts.length == 0){
                    continue;
                }

                String response = null;

                String requestType = requestParts[0];

                log.info("Received request: {}", requestType);

                switch (requestType) {
                    case EmailUtils.REGISTER :
                        response = handleRegister(requestParts);
                        break;

                    case EmailUtils.LOGIN :
                        response = handleLogin(requestParts);

                        break;

                    case EmailUtils.LOGOUT :
                        response = handleLogout(requestParts);
                        break;

                    case EmailUtils.SEND_EMAIL:
                        if (loggedInUser == null) {
                            response = ResponseStatus.USER_NOT_LOGGED_IN.toString();
                            break;
                        }
                        response = handleSendEmail(requestParts);
                        break;

                    case EmailUtils.GET_RECEIVED_EMAILS:
                        if (loggedInUser == null) {
                            response = ResponseStatus.USER_NOT_LOGGED_IN.toString();
                            break;
                        }
                        response = handleGetReceivedEmails(requestParts);
                        break;

                    case EmailUtils.GET_SENT_EMAILS:
                        if (loggedInUser == null) {
                            response = ResponseStatus.USER_NOT_LOGGED_IN.toString();
                            break;
                        }
                        response = handleGetSentEmails(requestParts);
                        break;

                    case EmailUtils.READ_EMAIL:
                        if (loggedInUser == null) {
                            response = ResponseStatus.USER_NOT_LOGGED_IN.toString();
                            break;
                        }
                        response = handleReadEmail(requestParts);
                        break;

                    case EmailUtils.SEARCH_EMAIL:
                        if (loggedInUser == null) {
                            response = ResponseStatus.USER_NOT_LOGGED_IN.toString();
                            break;
                        }
                        response = handleSearchEmail(requestParts);
                        break;


                    default:
                        response = ResponseStatus.INVALID.toString();
                        break;
                }

                if (response != null) {
                    networkLayer.send(response);
                } else {
                    log.error("Response is null for request: " + request);
                }

                response = null;

            }
//            TODO: Specify exception type
        } catch (Exception e){
            log.error("Error while handling client request: ", e);
        }
    }


    private String handleRegister(String[] requestParts) {


        if (requestParts.length != 4) {
            log.error("Invalid register request! Expected 4 parts, got: {}", requestParts.length);
            return ResponseStatus.INVALID.toString();
        }

        String username = requestParts[1];
        String password = requestParts[2];
        String confirmedPassword = requestParts[3];

        if (!password.equals(confirmedPassword)) {
            log.error("Error in register request! Passwords do not match.");
            return ResponseStatus.PASSWORDS_DO_NOT_MATCH.toString();
        }

        String result = null;

        try {
            ResponseStatus responseStatus = userManager.register(username, password);
            emailManager.initializeMailbox(username);
            result = responseStatus.toString();
        } catch (Exception e) {
            log.error("Error in register request! Username already exists: {}", username);
            result = ResponseStatus.USER_ALREADY_EXISTS.toString();
        }

        return result;

    }

    private String handleLogin(String[] requestParts) {

        if (requestParts.length != 3) {
            log.error("Invalid login request! Expected 3 parts, got: {}", requestParts.length);
            return ResponseStatus.INVALID.toString();
        }

        String username = requestParts[1];
        String password = requestParts[2];

        ResponseStatus responseStatus = userManager.login(username, password, clientDataSocket);
        loggedInUser = userManager.getUserByUsername(username);

        log.info("User logged in successfully: {}", username);
        return responseStatus.toString();
    }

    private String handleLogout(String[] requestParts) {

        if (requestParts.length != 2) {
            log.error("Invalid logout request! Expected 2 parts, got: {}", requestParts.length);
            return ResponseStatus.INVALID.toString();
        }

        String username = requestParts[1];

        ResponseStatus responseStatus = logoutUser(username);

        if (responseStatus == ResponseStatus.SUCCESS) {
            loggedInUser = null;
            log.info("User logged out successfully: {}", username);
        } else {
            log.error("Error logging out user: {}", username);
        }
        return responseStatus.toString();
    }

    private String handleSendEmail(String[] requestParts) {
        if (requestParts.length != 4) {
            log.error("Invalid send email request! Expected 4 parts, got: {}", requestParts.length);
            return ResponseStatus.INVALID.toString();
        }

        String senderUsername = loggedInUser.getUsername();
        String recipientUsername = requestParts[1];
        String subject = requestParts[2];
        String content = requestParts[3];

        ResponseStatus responseStatus = emailManager.sendEmail(senderUsername, recipientUsername, subject, content);

        if (responseStatus == ResponseStatus.SUCCESS) {
            log.info("Email sent successfully from {} to {}", senderUsername, recipientUsername);
        } else {
            log.error("Error sending email from {} to {}: {}", senderUsername, recipientUsername, responseStatus);
        }
        return responseStatus.toString();
    }

    private String handleGetReceivedEmails(String[] requestParts) {
        if (requestParts.length != 1) {
            log.error("Invalid get received emails request! Expected 2 parts, got: {}", requestParts.length);
            return ResponseStatus.INVALID.toString();
        }

        String recipientUsername = loggedInUser.getUsername();

        List<Email> receivedEmails = emailManager.getReceivedEmails(recipientUsername);

        log.info("Received emails for user {}: {}", recipientUsername, receivedEmails.size());

        return receivedEmails.toString();
    }

    private String handleGetSentEmails(String[] requestParts) {

        String senderUsername = loggedInUser.getUsername();

        List<Email> sentEmails = emailManager.getSentEmails(senderUsername);

        log.info("Sent emails for user {}: {}", senderUsername, sentEmails.size());
        return serializeEmails(sentEmails);
    }

    private String handleReadEmail(String[] requestParts) {
        if (requestParts.length != 2) {
            return ResponseStatus.INVALID.toString();
        }

        int emailId = Integer.parseInt(requestParts[1]);
        String userName = loggedInUser.getUsername();

        Optional<Email> result = emailManager.readEmail(emailId, userName);

        if (result.isPresent()) {
            log.info("Email read successfully: {}", result.get());
            return serializeEmail(result.get());
        } else {
            log.error("Error reading email with ID {}: Email not found", emailId);
            return ResponseStatus.RESOURCE_NOT_FOUND.toString();
        }

    }

    private String handleSearchEmail(String[] requestParts) {
        if (requestParts.length != 3) {
            log.error("Invalid search email request! Expected 3 parts, got: {}", requestParts.length);
            return ResponseStatus.INVALID.toString();
        }

        String userName = loggedInUser.getUsername();
        String subjectQuery = requestParts[2];

        SearchType searchType;
        try {
            searchType = SearchType.valueOf(requestParts[1]);
        } catch (IllegalArgumentException e) {
            return ResponseStatus.INVALID.toString();
        }

        List<Email> result = emailManager.searchEmails(userName, searchType, subjectQuery);

        if (result.isEmpty()) {
            log.info("No emails found for user {} with subject query: {}", userName, subjectQuery);
        }

        return serializeEmails(result);
    }


    private ResponseStatus logoutUser(String username){

        String result;

        if (loggedInUser != null) {
            loggedInUser = null;
            log.info("User logged out successfully: {}", username);
            result = ResponseStatus.SUCCESS.toString();
        } else {
            log.error("Error logging out user: {}. User is not logged in", username);
            result = ResponseStatus.USER_NOT_LOGGED_IN.toString();
        }

        return ResponseStatus.valueOf(result);
    }

    private String serializeEmails(List<Email> emails) {
        StringBuilder sb = new StringBuilder();

        // Set success status
        sb.append(ResponseStatus.SUCCESS).append(EmailUtils.EMAIL_DELIMITER);

        for (int i = 0; i < emails.size(); i++) {
            Email email = emails.get(i);
            sb
                    .append(email.getId()).append(EmailUtils.DELIMITER)
                    .append(email.getSenderUsername()).append(EmailUtils.DELIMITER)
                    .append(email.getRecipientUsername()).append(EmailUtils.DELIMITER)
                    .append(email.getSubject()).append(EmailUtils.DELIMITER)
                    .append(email.getContent()).append(EmailUtils.DELIMITER)
                    .append(email.getSentAt()).append(EmailUtils.DELIMITER)
                    .append(email.isRead());

            // Add object delimiter if not the last email
            if (i < emails.size() - 1) {
                sb.append(EmailUtils.EMAIL_DELIMITER);
            }
        }

        return sb.toString();
    }

    private String serializeEmail(Email email) {

        return
                ResponseStatus.SUCCESS + EmailUtils.DELIMITER +
                email.getId() + EmailUtils.DELIMITER +
                email.getSenderUsername() + EmailUtils.DELIMITER +
                email.getRecipientUsername() + EmailUtils.DELIMITER +
                email.getSubject() + EmailUtils.DELIMITER +
                email.getContent() + EmailUtils.DELIMITER +
                email.getSentAt() + EmailUtils.DELIMITER +
                email.isRead();

    }



}
