package model.email;

import service.ResponseStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class EmailManagerImpl implements EmailManager {

    private final AtomicInteger emailIdGenerator = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, Email> emails = new ConcurrentHashMap<>();


    public ResponseStatus sendEmail(UUID sender, UUID recipient, String subject, String body) {

        int emailId = emailIdGenerator.incrementAndGet();

        Email email = Email.builder()
                .id(emailId)
                .senderId(sender)
                .recipientId(recipient)
                .subject(subject)
                .content(body)
                .sentAt(LocalDateTime.now())
                .read(false)
                .build();

        emails.put(emailId, email);

        return ResponseStatus.SUCCESS;
    }


    public List<Email> getReceivedEmails(UUID userId) {

        return emails.values().stream()
                .filter(email -> email.getRecipientId().equals(userId))
                .toList();

    }

    public List<Email> getSentEmails(UUID userId) {

        return emails.values().stream()
                .filter(email -> email.getSenderId().equals(userId))
                .toList();

    }

    public ResponseStatus readEmail(Integer emailId) {

        Email email = emails.get(emailId);

        if (email == null) {
            return ResponseStatus.RESOURCE_NOT_FOUND;
        }

        email.setRead(true);
        return ResponseStatus.SUCCESS;
    }

    public List<Email> searchEmails(UUID userId, SearchType type, String subjectQuery){

        return emails.values().stream()
                .filter(email -> {
                    if (type == SearchType.RECEIVED) {
                        return email.getRecipientId().equals(userId);
                    } else {
                        return email.getSenderId().equals(userId);
                    }
                })
                .filter(email -> email.getSubject().contains(subjectQuery))
                .toList();
    }



}
