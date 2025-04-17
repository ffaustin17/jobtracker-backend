//package com.ffaustin.job_tracker.service;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//import java.io.UnsupportedEncodingException;
//
//@Service
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//
//    @Value("${spring.mail.username}")
//    private String sender;
//
//    public EmailService(JavaMailSender mailSender){
//        this.mailSender = mailSender;
//    }
//
//    public void sendVerificationEmail(String to, String token){
//        String link = "http://localhost:8080/api/auth/verify?token="+token;
//        String subject = "Verify Your JobTrackr Account";
//        String content = "<p>Click the link below to verify your account: </p>" +
//                         "<a href=\"" + link + "\">Verify Now</a>";
//
//        sendHtmlEmail(to, subject, content);
//        System.out.println("[MOCK EMAIL] Verification link: " + link);
//    }
//
//    public void sendEmail(String to, String subject ,String body ){
//        sendPlainTextEmail(to, subject, body);
//    }
//
//    private void sendHtmlEmail(String to, String subject, String htmlContent){
//        try{
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setFrom("fabricefaustin17@gmail.com", "JobTrackr Support");
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(htmlContent, true);
//
//            mailSender.send(message);
//        }
//        catch(MessagingException e){
//            throw new RuntimeException("Failed to send HTML email: " + e.getMessage(), e);
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void sendPlainTextEmail(String to, String subject, String text){
//        try{
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
//
//            helper.setFrom("fabricefaustin17@gmail.com", "JobTrackr Support");
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(text, false);
//
//            mailSender.send(message);
//        }
//        catch(MessagingException e){
//            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
//        } catch (UnsupportedEncodingException e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
