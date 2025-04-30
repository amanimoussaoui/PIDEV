package tn.esprit.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.*;
import java.io.File;
import java.util.Properties;

public class MailUtil {

    public static void sendWarningEmail(String toEmail, File attachmentFile) {
        final String fromEmail = "f5d2216f063df3"; // <<< replace with your Mailtrap username
        final String password = "454909abb02642"; // <<< replace with your Mailtrap password

        Properties props = new Properties();
        props.put("mail.smtp.host", "sandbox.smtp.mailtrap.io"); // <<< your sandbox host
        props.put("mail.smtp.port", "587"); // usually 587 for Mailtrap
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Tentative de connexion détectée");

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("Attention ! Une tentative de connexion a été détectée sur votre compte. Voici la photo capturée.");

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(attachmentFile);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachmentPart);

            message.setContent(multipart);

            Transport.send(message);

            System.out.println("Email envoyé avec succès!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //////////////////////////////////////////////////////
    public static void sendSimpleEmail(String toEmail, String subject, String body) {
        final String fromEmail = "f5d2216f063df3"; // same as before
        final String password = "454909abb02642";  // same as before

        Properties props = new Properties();
        props.put("mail.smtp.host", "sandbox.smtp.mailtrap.io");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("Simple email envoyé avec succès!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//////////////////////////////////////////////
public static void sendResetCodeEmail(String toEmail, String code) {
    final String fromEmail = "f5d2216f063df3"; // your Mailtrap username
    final String password = "454909abb02642"; // your Mailtrap password

    Properties props = new Properties();
    props.put("mail.smtp.host", "sandbox.smtp.mailtrap.io");
    props.put("mail.smtp.port", "587");
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");

    Session session = Session.getInstance(props, new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(fromEmail, password);
        }
    });

    try {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Code de Réinitialisation du Mot de Passe");

        String emailContent = "Votre code de réinitialisation est : " + code;

        message.setText(emailContent);

        Transport.send(message);

        System.out.println("Reset code email envoyé avec succès!");

    } catch (Exception e) {
        e.printStackTrace();
    }
}

}
