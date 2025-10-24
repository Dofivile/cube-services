package com.example.cube.service.impl;

import com.example.cube.service.EmailService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${cube.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email:no-reply@cubemoney.io}")
    private String fromEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendInvitationEmail(String email, String inviteToken, String cubeName, UUID invitedBy) {

        String invitationLink = frontendUrl + "/invitations/accept?token=" + inviteToken;

        // Resend API endpoint
        String resendApiUrl = "https://api.resend.com/emails";

        // Build headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + resendApiKey);
        final String verifiedSender = fromEmail != null ? fromEmail : "no-reply@cubemoney.io";
        headers.set("From", verifiedSender);

        // Build email body
        JSONObject emailBody = new JSONObject();
        emailBody.put("from", verifiedSender);
        emailBody.put("to", email);
        emailBody.put("subject", "You've been invited to join " + cubeName);
        emailBody.put("html", buildEmailHtml(invitationLink, cubeName));

        HttpEntity<String> request = new HttpEntity<>(emailBody.toString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    resendApiUrl,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Email sent successfully to: " + email);
            } else {
                System.err.println("❌ Failed to send email: " + response.getBody());
                throw new RuntimeException("Email sending failed");
            }
        } catch (Exception e) {
            System.err.println("❌ Error sending email to " + email + ": " + e.getMessage());
            // Don't throw - allow invitation to be created even if email fails
            throw new RuntimeException("Email service error: " + e.getMessage());
        }
    }

    /**
     * Build HTML email template
     */
    private String buildEmailHtml(String invitationLink, String cubeName) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4; padding: 20px;">
                    <tr>
                        <td align="center">
                            <table width="600" cellpadding="0" cellspacing="0" style="background-color: white; border-radius: 8px; padding: 40px;">
                                <tr>
                                    <td>
                                        <h1 style="color: #333; margin: 0 0 20px 0;">You've been invited!</h1>
                                        <p style="color: #666; font-size: 16px; line-height: 1.5; margin: 0 0 20px 0;">
                                            You've been invited to join <strong>%s</strong>.
                                        </p>
                                        <table width="100%%" cellpadding="0" cellspacing="0">
                                            <tr>
                                                <td align="center" style="padding: 20px 0;">
                                                    <a href="%s" 
                                                       style="display: inline-block; background-color: #4CAF50; color: white; 
                                                              padding: 14px 30px; text-decoration: none; border-radius: 5px; 
                                                              font-weight: bold; font-size: 16px;">
                                                        Accept Invitation
                                                    </a>
                                                </td>
                                            </tr>
                                        </table>
                                        <p style="color: #999; font-size: 14px; line-height: 1.5; margin: 20px 0 0 0;">
                                            Or copy and paste this link into your browser:
                                        </p>
                                        <p style="color: #666; font-size: 12px; word-break: break-all; background-color: #f9f9f9; 
                                                  padding: 10px; border-radius: 4px; margin: 10px 0 20px 0;">
                                            %s
                                        </p>
                                        <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0;">
                                        <p style="color: #999; font-size: 12px; text-align: center; margin: 0;">
                                            This invitation will expire in 48 hours.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """, cubeName, invitationLink, invitationLink);
    }
}
