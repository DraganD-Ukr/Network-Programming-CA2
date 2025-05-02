package model.email;

import service.ResponseStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class EmailManagerImpl implements EmailManager {

    private final AtomicInteger emailIdGenerator = new AtomicInteger(0);
    private final ConcurrentHashMap<String, ArrayList<Email>> emails;

    public EmailManagerImpl() {
       this.emails = new ConcurrentHashMap<>();
    }


    public ResponseStatus sendEmail(String senderUsername, String recipientUsername, String subject, String body) {

        int emailId = emailIdGenerator.incrementAndGet();

        Email email = Email.builder()
                .id(emailId)
                .senderUsername(senderUsername)
                .recipientUsername(recipientUsername)
                .subject(subject)
                .content(body)
                .sentAt(LocalDateTime.now())
                .read(false)
                .build();

        emails.get(senderUsername).add(email);
        emails.get(recipientUsername).add(email);

        return ResponseStatus.SUCCESS;
    }


    public List<Email> getReceivedEmails(String recipientUserName) {

        return emails.get(recipientUserName).stream()
                .filter(email -> email.getRecipientUsername().equals(recipientUserName))
                .toList();

    }

    public List<Email> getSentEmails(String senderUserName) {

        return emails.get(senderUserName).stream()
                .filter(email -> email.getSenderUsername().equals(senderUserName))
                .toList();

    }

    public ResponseStatus readEmail(Integer emailId, UUID userId) {

        Email email = emails.get(userId).stream()
                .filter(m -> m.getId() == emailId)
                .findFirst()
                .orElse(null);


        if (email == null) {
            return ResponseStatus.RESOURCE_NOT_FOUND;
        }

        email.setRead(true);
        return ResponseStatus.SUCCESS;
    }

    public List<Email> searchEmails(String userName, SearchType type, String subjectQuery){

        return emails.get(userName).stream()
                .filter(email -> {
                    if (type == SearchType.RECEIVED) {
                        return email.getRecipientUsername().equals(userName);
                    } else {
                        return email.getSenderUsername().equals(userName);
                    }
                })
                .filter(email -> email.getSubject().contains(subjectQuery))
                .toList();
    }



}
