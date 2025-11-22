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

    @Value("${cube.email.logo.url}")
    private String logoUrl;

    @Autowired
    private CubeMemberRepository cubeMemberRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendInvitationEmail(String email, String invitationCode, String cubeName, UUID invitedBy) {
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
        emailBody.put("html", buildInvitationEmailHtml(invitationCode, cubeName));

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
    public void sendWinnerNotificationEmails(Cube cube, CubeMember winner, BigDecimal payoutAmount, Integer cycleNumber) {
        try {
            // 1. Get all members with contact info in ONE query ✅
            List<MemberWithContact> members = cubeMemberRepository.findMembersWithContactInfo(cube.getCubeId());

            // 2. Find winner info
            MemberWithContact winnerInfo = members.stream()
                    .filter(m -> m.getUserId().equals(winner.getUserId()))
                    .findFirst()
                    .orElse(null);

            String winnerEmail = winnerInfo != null ? winnerInfo.getEmail() : null;
            String fullName = winnerInfo != null ? winnerInfo.getFullName() : "";

            // Use name if available, otherwise use email, otherwise use User ID
            String winnerName;
            if (fullName != null && !fullName.trim().isEmpty()) {
                winnerName = fullName;
            } else if (winnerEmail != null && !winnerEmail.isEmpty()) {
                winnerName = winnerEmail;
            } else {
                winnerName = "User " + winner.getUserId().toString().substring(0, 8);
            }

            String subject = "🎉 " + cube.getName() + " — Cycle " + cycleNumber + " Winner!";
            String htmlBody = buildWinnerEmailHtml(cube, winnerName, payoutAmount, cycleNumber);

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
            sendAdminNotificationEmail(resendApiUrl, cube, winnerName, winnerEmail, payoutAmount, cycleNumber);

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
    private void sendAdminNotificationEmail(String apiUrl, Cube cube, String winnerName, String winnerEmail, BigDecimal payoutAmount, Integer cycleNumber) {
        try {
            String subject = "🔔 Cube Payout Review — " + cube.getName() + " (Cycle " + cycleNumber + ")";
            String htmlBody = buildAdminEmailHtml(cube, winnerName, winnerEmail, payoutAmount, cycleNumber);

            sendEmail(apiUrl, adminEmail, subject, htmlBody);
            System.out.println("✅ Admin notification sent for cube " + cube.getName());

        } catch (Exception e) {
            System.err.println("❌ Failed to send admin notification: " + e.getMessage());
        }
    }

    /**
     * Build HTML email template for invitation with new modern design
     */
    private String buildInvitationEmailHtml(String invitationCode, String cubeName) {
        return String.format("""
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <meta name="color-scheme" content="light only"/>
  <meta name="supported-color-schemes" content="light"/>
</head>
<body style="margin:0;padding:0;background:#ffffff;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;">
  <table width="100%%" cellpadding="0" cellspacing="0" border="0" role="presentation" style="padding:32px;background:#ffffff;">
    <tr><td align="center">

      <table width="560" cellpadding="0" cellspacing="0" border="0" role="presentation" style="
        max-width:560px;
        border-radius:24px;
        border:1px solid #e5e5e5;
        background:#ffffff;
        box-shadow:0 1px 3px rgba(0,0,0,0.1);
        overflow:hidden;
      ">

        <tr><td align="center" style="padding:40px 32px 24px;background:#f9f9f9;">
          <img src="%s" width="64" height="64" alt="Cube" style="display:block;margin:0 auto;">
        </td></tr>

        <tr><td align="center" style="padding:24px 32px 16px;background:#ffffff;">
          <h1 style="font-size:24px;font-weight:600;font-family:'Playfair Display','Georgia',serif;color:#000000;margin:0;line-height:1.3;">
            You've been invited to join<br/>%s
          </h1>
        </td></tr>

        <tr><td style="padding:0 32px;background:#ffffff;">
          <div style="height:1px;background:#e5e5e5;margin:16px 0;"></div>
        </td></tr>

        <tr><td style="padding:16px 32px 24px;background:#ffffff;">
          <p style="margin:0;font-size:15px;line-height:1.6;color:#333333;text-align:center;">
            Cube gives your group access to capital faster than traditional saving — structured, predictable, and designed for growth.
          </p>
        </td></tr>

        <tr><td align="center" style="padding:8px 32px 24px;background:#ffffff;">
          <table cellpadding="0" cellspacing="0" border="0" role="presentation" style="
            margin:0 auto;
            padding:20px 32px;
            border-radius:12px;
            background:#f5f5f5;
            border:2px dashed #666666;
          ">
            <tr><td align="center">
              <p style="font-size:11px;color:#666666;letter-spacing:1px;margin:0 0 8px 0;text-transform:uppercase;font-weight:600;">
                Invitation Code
              </p>
              <p style="font-size:28px;font-weight:700;letter-spacing:4px;font-family:'Courier New',Courier,monospace;color:#000000;margin:0;">
                %s
              </p>
            </td></tr>
          </table>
        </td></tr>

        <tr><td style="padding:0 32px 32px;background:#ffffff;">
          <p style="margin:0 0 12px;font-size:15px;font-weight:700;color:#000000;">How to join:</p>
          <ol style="margin:0;padding-left:20px;font-size:14px;color:#333333;line-height:1.8;">
            <li style="margin-bottom:8px;">Download Cube from the <strong>App Store</strong> or visit <strong>cubemoney.io</strong></li>
            <li style="margin-bottom:8px;">Sign in or create an account</li>
            <li style="margin-bottom:8px;">Enter the code above</li>
            <li>Secure your spot & begin your cycle</li>
          </ol>
        </td></tr>

        <tr><td style="padding:16px 32px 32px;background:#ffffff;">
          <div style="height:1px;background:#e5e5e5;margin:0 0 16px 0;"></div>
          <p style="margin:0;text-align:center;font-size:12px;color:#666666;">
            Valid for <strong>48 hours</strong> · Do not forward
          </p>
        </td></tr>

      </table>
    </td></tr>
  </table>
</body>
</html>
""", logoUrl, cubeName, invitationCode);
    }

    /**
     * Build HTML email template for winner notification (sent to all members)
     */
    private String buildWinnerEmailHtml(Cube cube, String winnerName, BigDecimal payoutAmount, Integer cycleNumber) {
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
                                            
                                            <div style="background: #f0fdf4; padding: 25px; border-radius: 12px; margin: 20px 0; border: 2px solid #059669; text-align: center;">
                                                <p style="margin: 0 0 10px 0; font-size: 14px; color: #047857; text-transform: uppercase; letter-spacing: 1px; font-weight: bold;">🎊 This Cycle's Winner 🎊</p>
                                                <p style="margin: 5px 0; font-size: 24px; font-weight: bold; color: #1f2937;">%s</p>
                                                <p style="margin: 15px 0 5px 0; font-size: 16px; color: #6b7280;"><strong>Payout Amount:</strong></p>
                                                <p style="margin: 0; color: #059669; font-size: 32px; font-weight: bold;">$%s</p>
                                                <p style="margin: 15px 0 0 0; font-size: 14px; color: #6b7280;"><strong>Cycle:</strong> %d</p>
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
                cycleNumber
        );
    }

    /**
     * Build HTML email template for admin notification
     */
    private String buildAdminEmailHtml(Cube cube, String winnerName, String winnerEmail, BigDecimal payoutAmount, Integer cycleNumber) {
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
                cycleNumber,
                cube.getCubeId()
        );
    }
}