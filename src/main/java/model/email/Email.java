package model.email;

import lombok.*;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Email {

    /**
     * Unique identifier for the email.
     */
    @EqualsAndHashCode.Include
    private int id;

    /**
     * Unique identifier for the sender.
     */
    private String senderUsername;

    /**
     * Unique identifier for the recipient.
     */
    private String recipientUsername;

    /**
     * Subject of the email.
     */
    private String subject;

    /**
     * Content of the email.
     */
    private String content;

    /**
     * Timestamp of when the email was sent.
     */
    private LocalDateTime sentAt;

    /**
     * Flag indicating whether the email has been read.
     */
    private boolean read;

}