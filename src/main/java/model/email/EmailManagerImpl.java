package model.email;

import service.ResponseStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class EmailManagerImpl implements EmailManager {

    private final AtomicInteger emailIdGenerator = new AtomicInteger(0);
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Email>> emails;

    public EmailManagerImpl() {
       this.emails = new ConcurrentHashMap<>();
    }


    @Override
    public void initializeMailbox(String userName) {
        emails.put(userName, new CopyOnWriteArrayList<>());
    }


    @Override
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


    @Override
    public List<Email> getReceivedEmails(String recipientUserName) {

        return emails.get(recipientUserName).stream()
                .filter(email -> email.getRecipientUsername().equals(recipientUserName))
                .toList();

    }

    @Override
    public List<Email> getSentEmails(String senderUserName) {

        return emails.get(senderUserName).stream()
                .filter(email -> email.getSenderUsername().equals(senderUserName))
                .toList();

    }

    @Override
    public Optional<Email> readEmail(Integer emailId, String userName) {

         Optional<Email> email = emails.getOrDefault(userName, new CopyOnWriteArrayList<>()).stream()
                .filter(m -> m.getId() == emailId)
                .findFirst()
                .map(e -> {
                    e.setRead(true);
                    return e;
                });


        email.ifPresent(value -> value.setRead(true));

        return email;
    }

    @Override
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
