package tn.esprit.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {
    private final String senderEmail = "amounatahfouna443@gmail.com";
    private final String senderPassword = "ofbi ccyl yova ixnp"; // mot de passe d'application

    public void sendAcceptanceEmail(String recipientEmail, String nomUtilisateur, String localisation, double prix) {
        if (recipientEmail == null || nomUtilisateur == null || localisation == null) {
            throw new IllegalArgumentException("Les paramètres ne peuvent pas être null");
        }

        String subject = "Candidature acceptée";
        String content = String.format(
                "Bonjour %s, félicitations! Votre candidature pour le terrain à %s avec le prix %.2f DT a été acceptée. Veuillez nous contacter pour faire un contrat.",
                nomUtilisateur, localisation, prix
        );
        sendEmail(recipientEmail, subject, content);
    }

    public void sendRejectionEmail(String recipientEmail, String nomUtilisateur, String localisation, double prix) {
        String subject = "Candidature refusée";
        String content = String.format(
                "Bonjour %s, désolé, votre candidature pour le terrain à %s avec le prix %.2f DT a été refusée.",
                nomUtilisateur, localisation, prix
        );
        sendEmail(recipientEmail, subject, content);
    }

    public void sendEmail(String recipientEmail, String subject, String content) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
            System.out.println("Email envoyé avec succès à: " + recipientEmail);
        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}