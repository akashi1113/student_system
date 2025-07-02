package com.csu.sms.util;

import jakarta.mail.*;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;


public class MailUtil {
    private static String senderMail = "3626183568@qq.com";
    private static String authCode = "hvkdfkrbtfmtcifh";
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

    public static void sendEmail(String htmlContent) {
        // 创建Properties对象
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp"); // 使用的协议
        props.setProperty("mail.smtp.host", emailSMTPHost); // 发件人的SMTP服务器地址
        props.setProperty("mail.smtp.auth", "true"); // 需要请求认证

        // QQ邮箱需要开启SSL加密
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.socketFactory.port", "465");

        // 创建会话
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderMail, authCode);
            }
        });

        try {
            // 创建邮件
            MimeMessage message = creatMimeMessage(session, htmlContent);
            // 发送邮件
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("发送邮件失败", e);
        }
    }
}

