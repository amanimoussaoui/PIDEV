package tn.esprit.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class MailService {
    private final Session session;

    public MailService() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        this.session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("tasnimsdiri2001@gmail.com", "pbri ajrq xrxq ajfr");
            }
        });
    }

    public void envoyerEmail(String to, String sujet, String message) {
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("tasnimsdiri2001@gmail.com"));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            msg.setSubject(sujet);
            msg.setText(message);

            // Send in a new thread to avoid blocking the UI
            new Thread(() -> {
                try {
                    Transport.send(msg);
                    System.out.println("Email sent successfully to: " + to);
                } catch (MessagingException e) {
                    System.err.println("Failed to send email to " + to);
                    e.printStackTrace();
                }
            }).start();

        } catch (MessagingException e) {
            System.err.println("Error creating email message");
            e.printStackTrace();
        }
    }
}