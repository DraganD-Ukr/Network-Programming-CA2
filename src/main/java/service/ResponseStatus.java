package service;

public enum ResponseStatus {


//    ---------------------------------------------Success responses---------------------------------------------
    /**
     * Generic success status.
     */
    SUCCESS,


    //    ---------------------------------------------Failure responses---------------------------------------------

//  GENERIC
    /**
     * User not found.
     */
    USER_NOT_FOUND,

    /**
     * Resource not found (e.g. find by id or find by username ).
     */
    RESOURCE_NOT_FOUND,





//    Register responses:
    /**
     * Wrong password format.
     */
    WRONG_PASSWORD_FORMAT,

    /**
     * User already exists.
     */
    USER_ALREADY_EXISTS,

    /**
     * Passwords(when entered your password again) do not match.
     */
    PASSWORDS_DOESNT_MATCH,



//    Login response
    /**
     * Invalid username or password.
     */
    INVALID_USERNAME_OR_PASSWORD,

    /**
     * User already logged in.
     */
    USER_ALREADY_LOGGED,



//    Send email response
    /**
     * Recipient not found.
     */
    RECIPIENT_NOT_FOUND,



//    Get received and sent emails response
    /**
     * No emails found.
     */
    NO_EMAILS_FOUND,




//    Logout response
    /**
     * User not logged in.
     */
    USER_NOT_LOGGED_IN,





//    General malformed response:
    /**
     * Malformed request.
     */
    INVALID

}
