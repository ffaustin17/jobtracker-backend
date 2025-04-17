package com.ffaustin.job_tracker.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class SendGridService {

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name}")
    private String fromName;

    public void sendEmail(String to, String subject, String htmlContent){
        Email from = new Email(fromEmail, fromName);
        Email recipient = new Email(to);

        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, recipient, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        try{
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            System.out.println("SendGrid response: " + response.getStatusCode());
            System.out.println(response.getBody());
            System.out.println(response.getHeaders());
        }
        catch(IOException ex){
            System.err.println("SendGrid send failed: " + ex.getMessage());
            throw new RuntimeException("Failed to send email");
        }
    }

    public String buildVerificationEmail(String token){
        try{
            //load the HTML template from resources
            ClassPathResource resource = new ClassPathResource("templates/verification.html");
            String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            //construct the verification link
            String link = "http://localhost:8080/api/auth/verify?token="+token;

            //replace placeholder in HTML template
            return template.replace("{{VERIFICATION_LINK}}", link);
        }
        catch(IOException e){
            throw new RuntimeException("Failed to load email template", e);
        }
    }
}
