package com.example.cube.service.impl;

import com.example.cube.dto.MemberWithContact;
import com.example.cube.model.Cube;
import com.example.cube.model.CubeMember;
import com.example.cube.repository.CubeMemberRepository;
import com.example.cube.service.EmailService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${cube.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email:no-reply@cubemoney.io}")
    private String fromEmail;

    @Value("${admin.email}")
    private String adminEmail;

    @Autowired
    private CubeMemberRepository cubeMemberRepository;

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

        // Build email body
        JSONObject emailBody = new JSONObject();
        emailBody.put("from", verifiedSender);
        emailBody.put("to", email);
        emailBody.put("subject", "You've been invited to join " + cubeName);
        emailBody.put("html", buildInvitationEmailHtml(invitationLink, cubeName));

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
            throw new RuntimeException("Email service error: " + e.getMessage());
        }
    }

    @Override
    public void sendWinnerNotificationEmails(Cube cube, CubeMember winner, BigDecimal payoutAmount) {
        try {
            // 1. Get all members with contact info in ONE query ✅
            List<MemberWithContact> members = cubeMemberRepository.findMembersWithContactInfo(cube.getCubeId());

            // 2. Find winner info
            MemberWithContact winnerInfo = members.stream()
                    .filter(m -> m.getUserId().equals(winner.getUserId()))
                    .findFirst()
                    .orElse(null);

            String winnerName = winnerInfo != null ? winnerInfo.getFullName() : "Unknown";
            String winnerEmail = winnerInfo != null ? winnerInfo.getEmail() : null;

            String subject = "🎉 " + cube.getName() + " — Cycle " + cube.getCurrentCycle() + " Winner!";
            String htmlBody = buildWinnerEmailHtml(cube, winnerName, payoutAmount);

            // 3. Send emails to all members
            int emailsSent = 0;
            String resendApiUrl = "https://api.resend.com/emails";

            for (MemberWithContact member : members) {
                String email = member.getEmail();

                if (email == null || email.isBlank()) {
                    System.out.println("⚠️ Skipping member " + member.getMemberId() + " - no email found");
                    continue;
                }

                try {
                    sendEmail(resendApiUrl, email, subject, htmlBody);
                    emailsSent++;
                } catch (Exception e) {
                    System.err.println("❌ Failed to send email to " + email + ": " + e.getMessage());
                }
            }

            // 4. Send admin notification
            sendAdminNotificationEmail(resendApiUrl, cube, winnerName, winnerEmail, payoutAmount);

            System.out.println("✅ Winner emails sent for cube " + cube.getName() +
                    " (" + emailsSent + "/" + members.size() + " members notified)");

        } catch (Exception e) {
            System.err.println("❌ Failed to send winner emails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to send email via Resend API
     */
    private void sendEmail(String apiUrl, String to, String subject, String html) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + resendApiKey);

        JSONObject emailBody = new JSONObject();
        emailBody.put("from", fromEmail);
        emailBody.put("to", to);
        emailBody.put("subject", subject);
        emailBody.put("html", html);

        HttpEntity<String> request = new HttpEntity<>(emailBody.toString(), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to send email: " + response.getBody());
        }
    }

    /**
     * Send admin notification for payout review
     */
    private void sendAdminNotificationEmail(String apiUrl, Cube cube, String winnerName, String winnerEmail, BigDecimal payoutAmount) {
        try {
            String subject = "🔔 Cube Payout Review — " + cube.getName() + " (Cycle " + cube.getCurrentCycle() + ")";
            String htmlBody = buildAdminEmailHtml(cube, winnerName, winnerEmail, payoutAmount);

            sendEmail(apiUrl, adminEmail, subject, htmlBody);
            System.out.println("✅ Admin notification sent for cube " + cube.getName());

        } catch (Exception e) {
            System.err.println("❌ Failed to send admin notification: " + e.getMessage());
        }
    }

    /**
     * Build HTML email template for invitation
     */
    private String buildInvitationEmailHtml(String invitationLink, String cubeName) {
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

    /**
     * Build HTML email template for winner notification (sent to all members)
     */
    private String buildWinnerEmailHtml(Cube cube, String winnerName, BigDecimal payoutAmount) {
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
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: white; border-radius: 8px; overflow: hidden;">
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center;">
                                            <h1 style="color: white; margin: 0; font-size: 28px;">🎉 Cycle Winner Announcement!</h1>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 40px;">
                                            <h2 style="color: #1f2937; margin-top: 0;">%s</h2>
                                            
                                            <div style="background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #667eea;">
                                                <p style="margin: 5px 0; font-size: 16px;"><strong>Winner:</strong> %s</p>
                                                <p style="margin: 5px 0; font-size: 16px;"><strong>Payout Amount:</strong> <span style="color: #059669; font-size: 20px;">$%s</span></p>
                                                <p style="margin: 5px 0; font-size: 16px;"><strong>Cycle:</strong> %d</p>
                                            </div>
                                            
                                            <p style="color: #4b5563; line-height: 1.6;">
                                                Congratulations to the winner! 🎊 Thank you to all members for staying consistent with your contributions. 
                                                The payout will be processed soon.
                                            </p>
                                            
                                            <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 30px 0;">
                                            
                                            <p style="color: #9ca3af; font-size: 12px; text-align: center; margin: 0;">
                                                This message was sent automatically by Cube's payout system.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """,
                cube.getName(),
                winnerName,
                payoutAmount,
                cube.getCurrentCycle()
        );
    }

    /**
     * Build HTML email template for admin notification
     */
    private String buildAdminEmailHtml(Cube cube, String winnerName, String winnerEmail, BigDecimal payoutAmount) {
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
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: white; border-radius: 8px; overflow: hidden;">
                                    <tr>
                                        <td style="background: #dc2626; padding: 20px;">
                                            <h2 style="color: white; margin: 0;">⚠️ Payout Review Required</h2>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 30px;">
                                            <div style="background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px;">
                                                <h3 style="margin-top: 0; color: #1f2937;">Cycle Winner Details</h3>
                                                <table style="width: 100%%; border-collapse: collapse;">
                                                    <tr><td style="padding: 8px 0; color: #6b7280;"><strong>Cube:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                                                    <tr><td style="padding: 8px 0; color: #6b7280;"><strong>Winner:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                                                    <tr><td style="padding: 8px 0; color: #6b7280;"><strong>Winner Email:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                                                    <tr><td style="padding: 8px 0; color: #6b7280;"><strong>Amount:</strong></td><td style="padding: 8px 0; color: #059669; font-size: 18px;"><strong>$%s</strong></td></tr>
                                                    <tr><td style="padding: 8px 0; color: #6b7280;"><strong>Cycle:</strong></td><td style="padding: 8px 0;">%d</td></tr>
                                                    <tr><td style="padding: 8px 0; color: #6b7280;"><strong>Cube ID:</strong></td><td style="padding: 8px 0; font-family: monospace; font-size: 12px;">%s</td></tr>
                                                </table>
                                            </div>
                                            
                                            <div style="background: #fef3c7; border-left: 4px solid #f59e0b; padding: 15px; border-radius: 4px; margin: 20px 0;">
                                                <p style="margin: 0; color: #92400e;"><strong>Action Required:</strong> Review and process payout in Stripe dashboard.</p>
                                            </div>
                                            
                                            <p style="color: #6b7280; font-size: 14px;">
                                                All members have been notified of this winner selection. Please process the payout at your earliest convenience.
                                            </p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """,
                cube.getName(),
                winnerName,
                winnerEmail != null ? winnerEmail : "N/A",
                payoutAmount,
                cube.getCurrentCycle(),
                cube.getCubeId()
        );
    }
}