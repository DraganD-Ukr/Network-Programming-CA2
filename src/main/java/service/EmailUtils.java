package service;

public class EmailUtils {

    public static final String HOSTNAME = "localhost";
    public static final int PORT = 6969;

    // REQUESTS
    public static final String REGISTER = "REGISTER";
    public static final String LOGIN = "LOGIN";
    public static final String SEND_EMAIL = "SEND_EMAIL";
    public static final String GET_RECEIVED_EMAILS = "GET_RECEIVED_EMAILS";
    public static final String GET_SENT_EMAILS = "GET_SENT_EMAILS";
    public static final String READ_EMAIL = "READ_EMAIL";
    public static final String SEARCH_EMAIL = "SEARCH_DETAILS";
    public static final String LOGOUT = "LOGOUT";
    private static final String NOTIFICATION = "NEW_EMAIL";


    // DELIMITERS
    public static final String DELIMITER = "%%";
    public static final String EMAIL_DELIMITER = "##";

    // RESPONSES
    /**
     * Generic success status.
     */
    public static final String SUCCESS = "SUCCESS";


//    Fail responses:

//    Register responses
    public static final String WRONG_PASSWORD_FORMAT = "WRONG_PASSWORD_FORMAT";
    public static final String USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
    public static final String PASSWORDS_DOESNT_MATCH = "PASSWORDS_DOESNT_MATCH";

//    Login response
    public static final String INVALID_USERNAME_OR_PASSWORD = "INVALID_USERNAME_OR_PASSWORD";
    public static final String USER_ALREADY_LOGGED = "USER_ALREADY_LOGGED";

//    Send email response
    public static final String RECIPIENT_NOT_FOUND = "RECIPIENT_NOT_FOUND";

//    Get received and sent emails response
    public static final String NO_EMAILS_FOUND = "NO_EMAILS_FOUND";

//    Read email response
    public static final String INVALID_ID = "INVALID_ID";

//    Logout response
    public static final String ALREADY_LOGGED_OUT = "ALREADY_LOGGED_OUT";

//    General malformed response:
    public static final String INVALID = "INVALID";

}
