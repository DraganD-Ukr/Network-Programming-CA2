package model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


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
    private UUID senderId;

    /**
     * Unique identifier for the recipient.
     */
    private UUID recipientId;

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