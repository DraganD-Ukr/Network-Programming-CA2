package model.email;

import model.user.UserManager;
import service.ResponseStatus;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public class EmailHandler {
    private final UserManager userManager;
    private final EmailManager emailManager;

    public EmailHandler(UserManager userManager,
                        EmailManager emailManager){
        this.userManager = userManager;
        this.emailManager = emailManager;
    }

    public boolean handleSend(String username, String currentUser, BufferedWriter out, BufferedReader in) throws Exception {
        String senderId = userManager.getUser(username) == null
                ? null
                : String.valueOf(userManager.getUser(currentUser).getId());

        String recipientId = String.valueOf(userManager.getUser(username).getId());
        if (senderId == null || recipientId == null) {
            out.write("ERROR Unknown recipient\n");
            out.flush();
            return false;
        }

        out.write("Enter subject: ");
        out.flush();
        String subject = in.readLine();

        out.write("Enter body: ");
        out.flush();
        StringBuilder body = new StringBuilder();
        String line;
        while (!(line = in.readLine()).equals("END")) {
            body.append(line).append("\n");
        }

        ResponseStatus status = emailManager.sendEmail(senderId, recipientId, subject, body.toString());
        if (status == ResponseStatus.SUCCESS) {
            out.write("Email sent successfully\n");
        } else {
            out.write("ERROR Failed to send email\n");
        }
        out.flush();
        return true;
    }
}
