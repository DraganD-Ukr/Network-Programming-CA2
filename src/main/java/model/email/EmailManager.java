package model.email;

import service.ResponseStatus;

import java.util.List;
import java.util.UUID;

/**
 * Interface for managing email operations.
 * Where possible status codes are used to indicate success or failure.
 * In case of status not being possible to be used, other types like Lists are used.
 */
public interface EmailManager {


    ResponseStatus sendEmail(UUID sender, UUID recipient, String subject, String body);

    List<Email> getReceivedEmails(UUID userId);

    List<Email> getSentEmails(UUID userId);

    ResponseStatus readEmail(Integer emailId);

    List<Email> searchEmails(UUID userId, SearchType type, String subjectQuery);
}
