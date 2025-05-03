package model.email;

import service.ResponseStatus;

import java.util.List;
import java.util.Optional;

/**
 * Interface for managing email operations.
 * Where possible status codes are used to indicate success or failure.
 * In case of status not being possible to be used, other types like Lists are used.
 */
public interface EmailManager {

    void initializeMailbox(String userName);

    ResponseStatus sendEmail(String senderUsername, String recipientUsername, String subject, String body);

    List<Email> getReceivedEmails(String recipientUserName);

    List<Email> getSentEmails(String senderUserName);

    Optional<Email> readEmail(Integer emailId, String userName);

    List<Email> searchEmails(String userName, SearchType type, String subjectQuery);
}
