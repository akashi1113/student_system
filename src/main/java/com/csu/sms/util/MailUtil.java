package com.csu.sms.util;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.Date;


public class MailUtil {
    private static String senderMail = "1428188606@qq.com";
    private static String authCode = "llnxcjgpxxmoiedd";
    private static String emailSMTPHost = "smtp.qq.com";
    private static String receiverMail = "";
    private static String receiverName = "";

    public static String getSenderMail() {
        return senderMail;
    }

    public static void setSenderMail(String senderMail) {
        MailUtil.senderMail = senderMail;
    }

    public static String getAuthCode() {
        return authCode;
    }

    public static String getEmailSMTPHost() {
        return emailSMTPHost;
    }

    public static void setEmailSMTPHost(String emailSMTPHost) {
        MailUtil.emailSMTPHost = emailSMTPHost;
    }

    public static void setAuthCode(String authCode) {
        MailUtil.authCode = authCode;
    }

    public static String getReceiverMail() {
        return receiverMail;
    }
    public static String getReceiverName() {
        return receiverName;
    }

    public static void setReceiverMail(String receiverMail) {
        MailUtil.receiverMail = receiverMail;
    }

    public static void setReceiverName(String receiverName) {
        MailUtil.receiverName = receiverName;
    }

    public static MimeMessage creatMimeMessage(Session session, String html) {
        MimeMessage message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(senderMail, "Cloud Bakery", "UTF-8"));
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiverMail, receiverName, "UTF-8"));
            message.setSubject("Verify your email","UTF-8");
            message.setContent(html,"text/html;charset=UTF-8");
            message.setSentDate(new Date());
            message.saveChanges();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return message;
    }
}

