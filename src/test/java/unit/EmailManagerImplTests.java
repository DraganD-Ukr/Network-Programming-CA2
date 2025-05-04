package unit;



import model.email.Email;
import model.email.EmailManagerImpl;
import model.email.SearchType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.ResponseStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EmailManagerImplTests {

    private EmailManagerImpl emailManager;

    @BeforeEach
    void setUp() {
        emailManager = new EmailManagerImpl();
        emailManager.initializeMailbox("alice");
        emailManager.initializeMailbox("bob");
    }

    @Test
    void sendEmail_shouldStoreEmailInBothMailboxes() {
        ResponseStatus status = emailManager.sendEmail("alice", "bob", "Subject", "Body");

        assertEquals(ResponseStatus.SUCCESS, status);

        List<Email> sent = emailManager.getSentEmails("alice");
        List<Email> received = emailManager.getReceivedEmails("bob");

        assertEquals(1, sent.size());
        assertEquals(1, received.size());
        assertEquals(sent.get(0), received.get(0)); // Same object
    }

    @Test
    void getReceivedEmails_shouldReturnOnlyReceivedEmails() {
        emailManager.sendEmail("alice", "bob", "Hello", "This is a test");
        emailManager.sendEmail("bob", "alice", "Reply", "This is a reply");

        List<Email> aliceInbox = emailManager.getReceivedEmails("alice");
        List<Email> bobInbox = emailManager.getReceivedEmails("bob");

        assertEquals(1, aliceInbox.size());
        assertEquals("bob", aliceInbox.get(0).getSenderUsername());

        assertEquals(1, bobInbox.size());
        assertEquals("alice", bobInbox.get(0).getSenderUsername());
    }

    @Test
    void getSentEmails_shouldReturnOnlySentEmails() {
        emailManager.sendEmail("alice", "bob", "Hi", "Msg");
        emailManager.sendEmail("alice", "bob", "Another", "Msg");

        List<Email> sent = emailManager.getSentEmails("alice");

        assertEquals(2, sent.size());
        assertTrue(sent.stream().allMatch(e -> e.getSenderUsername().equals("alice")));
    }

    @Test
    void readEmail_shouldMarkEmailAsRead() {
        emailManager.sendEmail("alice", "bob", "Hello", "Body");

        Email email = emailManager.getReceivedEmails("bob").get(0);
        assertFalse(email.isRead());

        Optional<Email> readEmail = emailManager.readEmail(email.getId(), "bob");

        assertTrue(readEmail.isPresent());
        assertTrue(readEmail.get().isRead());
    }

    @Test
    void readEmail_invalidEmailId_returnsEmpty() {
        Optional<Email> result = emailManager.readEmail(999, "alice");
        assertTrue(result.isEmpty());
    }

    @Test
    void searchEmails_shouldMatchBySubject_andFilterByDirection() {
        emailManager.sendEmail("alice", "bob", "Hello World", "Body");
        emailManager.sendEmail("bob", "alice", "Hi World", "Body");
        emailManager.sendEmail("bob", "alice", "Not Match", "Body");

        List<Email> results = emailManager.searchEmails("alice", SearchType.RECEIVED, "World");

        assertEquals(1, results.size());
        assertTrue(results.get(0).getSubject().contains("World"));
        assertEquals("bob", results.get(0).getSenderUsername());
    }

    @Test
    void sendEmail_toUninitializedUser_shouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            emailManager.sendEmail("alice", "charlie", "Oops", "Body");
        });
    }

    @Test
    void initializeMailbox_ofNotEmptyMailbox_shouldThrowException() {

        emailManager.sendEmail("alice", "bob", "Hello", "Body");

        assertThrows(IllegalArgumentException.class, () -> {
            emailManager.initializeMailbox("alice");
        });
    }
}

