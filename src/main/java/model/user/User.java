package model.user;


import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class User {

    /**
     * Unique identifier for the user.
     */
    @EqualsAndHashCode.Include
    private UUID id;

    /**
     * Username of the user.
     */
    private String username;

    /**
     * Password of the user.
     */
    private String password;

    /**
     * Flag indicating whether the user is logged in.
     */
    private boolean loggedIn;


}
