package services;

import utils.SmtpConfig;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Date;
import java.util.Properties;

public class EmailService {

    public void sendPasswordResetCode(String recipient, String code, int expiresMinutes) throws MessagingException {
        String from = SmtpConfig.getFrom();
        String username = SmtpConfig.getUsername();
        String password = SmtpConfig.getPassword();

        if (from == null || from.isBlank() || username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new MessagingException("SMTP not configured. Set SMTP_USERNAME, SMTP_PASSWORD, and SMTP_FROM.");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", SmtpConfig.getHost());
        props.put("mail.smtp.port", String.valueOf(SmtpConfig.getPort()));
        props.put("mail.smtp.auth", String.valueOf(SmtpConfig.isAuthEnabled()));
        props.put("mail.smtp.starttls.enable", String.valueOf(SmtpConfig.isTlsEnabled()));

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject("MindTrack password reset code");
        message.setSentDate(new Date());
        message.setText(buildBody(code, expiresMinutes));

        try {
            Transport.send(message);
        } catch (AuthenticationFailedException ex) {
            throw new MessagingException("SMTP authentication failed. Check Gmail App Password.", ex);
        }
    }

    private String buildBody(String code, int expiresMinutes) {
        return "Your MindTrack password reset code is: " + code + "\n\n"
                + "This code expires in " + expiresMinutes + " minutes. "
                + "If you did not request a reset, you can ignore this message.";
    }
}
