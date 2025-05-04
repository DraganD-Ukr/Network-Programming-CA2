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


    // DELIMITERS
    public static final String DELIMITER = "%%";
    public static final String EMAIL_DELIMITER = "##";

}
