package tn.esprit.skylora.utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private static final String FROM_EMAIL = "barhoumia093@gmail.com";
    private static final String APP_PASSWORD = "mzgvrlhydgjsrjiy";
    private static final String TO_EMAIL = "amal.barhoumi@esprit.tn";

    /**
     * Envoie un email de notification asynchrone pour ne pas bloquer l'interface
     * utilisateur.
     * 
     * @param subject Le sujet de l'email.
     * @param content Le contenu textuel de l'email.
     */
    public static void sendEmail(String subject, String content) {
        // Run in a separate thread to avoid UI freeze
        new Thread(() -> {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(FROM_EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(TO_EMAIL));
                message.setSubject(subject);
                message.setText(content);

                Transport.send(message);
                System.out.println("Email sent successfully!");
            } catch (MessagingException e) {
                System.err.println("Failed to send email: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
}
