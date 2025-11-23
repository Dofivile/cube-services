package com.example.cube.service.impl;

import com.example.cube.dto.MemberWithContact;
import com.example.cube.model.Cube;
import com.example.cube.model.CubeMember;
import com.example.cube.repository.CubeMemberRepository;
import com.example.cube.repository.CubeRepository;
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

    @Autowired
    private CubeRepository cubeRepository;

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
            sendAdminNotificationEmail(resendApiUrl, cube, winnerName, winnerEmail, payoutAmount, cycleNumber, winner.getMemberId(), winner.getUserId());

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
    private void sendAdminNotificationEmail(String apiUrl, Cube cube, String winnerName, String winnerEmail, BigDecimal payoutAmount, Integer cycleNumber, UUID memberId, UUID userId) {
        try {
            String subject = "🔔 Cube Payout Review — " + cube.getName() + " (Cycle " + cycleNumber + ")";
            String htmlBody = buildAdminEmailHtml(cube, winnerName, winnerEmail, payoutAmount, cycleNumber, memberId, userId);

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
            🎉 Cycle Winner Announcement
          </h1>
        </td></tr>

        <tr><td style="padding:0 32px;background:#ffffff;">
          <div style="height:1px;background:#e5e5e5;margin:16px 0;"></div>
        </td></tr>

        <tr><td style="padding:16px 32px 24px;background:#ffffff;">
          <p style="margin:0 0 8px 0;font-size:13px;color:#666666;text-align:center;text-transform:uppercase;letter-spacing:0.5px;">
            %s
          </p>
          <p style="margin:0 0 24px 0;font-size:15px;line-height:1.6;color:#333333;text-align:center;">
            Congratulations to this cycle's winner! Thank you to all members for staying consistent with your contributions.
          </p>
        </td></tr>

        <tr><td align="center" style="padding:8px 32px 32px;background:#ffffff;">
          <table cellpadding="0" cellspacing="0" border="0" role="presentation" style="
            margin:0 auto;
            padding:24px 32px;
            border-radius:16px;
            background:#f0fdf4;
            border:2px solid #10b981;
          ">
            <tr><td align="center">
              <p style="font-size:11px;color:#047857;letter-spacing:1px;margin:0 0 12px 0;text-transform:uppercase;font-weight:600;">
                🎊 Winner 🎊
              </p>
              <p style="font-size:24px;font-weight:700;color:#000000;margin:0 0 16px 0;">
                %s
              </p>
              <p style="font-size:13px;color:#6b7280;margin:0 0 4px 0;">Payout Amount</p>
              <p style="font-size:32px;font-weight:700;color:#10b981;margin:0 0 12px 0;">
                $%s
              </p>
              <p style="font-size:13px;color:#6b7280;margin:0;">Cycle %d</p>
            </td></tr>
          </table>
        </td></tr>

        <tr><td style="padding:0 32px 24px;background:#ffffff;">
          <div style="background:#fef3c7;border-left:4px solid #f59e0b;padding:12px 16px;border-radius:6px;">
            <p style="margin:0;font-size:13px;color:#92400e;line-height:1.5;">
              <strong>Next Steps:</strong> The payout will be processed soon. You'll receive another notification once it's sent.
            </p>
          </div>
        </td></tr>

        <tr><td style="padding:0 32px 32px;background:#ffffff;">
          <div style="height:1px;background:#e5e5e5;margin:0;"></div>
        </td></tr>

        <tr><td style="padding:0 32px 24px;background:#ffffff;">
          <p style="margin:0;font-size:12px;color:#999999;text-align:center;line-height:1.5;">
            This message was sent automatically by Cube.<br/>
            Keep contributing to unlock your turn to win!
          </p>
        </td></tr>

      </table>

    </td></tr>
  </table>
</body>
</html>
                """,
                logoUrl,
                cube.getName(),
                winnerName,
                payoutAmount,
                cycleNumber
        );
    }

    /**
     * Build HTML email template for admin notification
     */
    private String buildAdminEmailHtml(Cube cube, String winnerName, String winnerEmail, BigDecimal payoutAmount, Integer cycleNumber, UUID memberId, UUID userId) {
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
            ⚠️ Payout Review Required
          </h1>
        </td></tr>

        <tr><td style="padding:0 32px;background:#ffffff;">
          <div style="height:1px;background:#e5e5e5;margin:16px 0;"></div>
        </td></tr>

        <tr><td style="padding:16px 32px 8px;background:#ffffff;">
          <p style="margin:0;font-size:13px;color:#666666;text-transform:uppercase;letter-spacing:0.5px;">
            Cycle Winner Details
          </p>
        </td></tr>

        <tr><td style="padding:8px 32px 24px;background:#ffffff;">
          <table style="width:100%%;border-collapse:collapse;">
            <tr>
              <td style="padding:8px 0;color:#6b7280;font-size:13px;"><strong>Cube:</strong></td>
              <td style="padding:8px 0;color:#000000;font-size:14px;">%s</td>
            </tr>
            <tr>
              <td style="padding:8px 0;color:#6b7280;font-size:13px;"><strong>Winner:</strong></td>
              <td style="padding:8px 0;color:#000000;font-size:14px;font-weight:600;">%s</td>
            </tr>
            <tr>
              <td style="padding:8px 0;color:#6b7280;font-size:13px;"><strong>Winner Email:</strong></td>
              <td style="padding:8px 0;color:#000000;font-size:14px;">%s</td>
            </tr>
            <tr>
              <td style="padding:8px 0;color:#6b7280;font-size:13px;"><strong>User ID:</strong></td>
              <td style="padding:8px 0;color:#000000;font-size:11px;font-family:monospace;">%s</td>
            </tr>
            <tr>
              <td style="padding:8px 0;color:#6b7280;font-size:13px;"><strong>Member ID:</strong></td>
              <td style="padding:8px 0;color:#000000;font-size:11px;font-family:monospace;">%s</td>
            </tr>
            <tr>
              <td style="padding:8px 0;color:#6b7280;font-size:13px;"><strong>Cube ID:</strong></td>
              <td style="padding:8px 0;color:#000000;font-size:11px;font-family:monospace;">%s</td>
            </tr>
            <tr>
              <td style="padding:12px 0 8px 0;color:#6b7280;font-size:13px;border-top:1px solid #e5e5e5;"><strong>Amount:</strong></td>
              <td style="padding:12px 0 8px 0;color:#10b981;font-size:20px;font-weight:700;border-top:1px solid #e5e5e5;">$%s</td>
            </tr>
            <tr>
              <td style="padding:8px 0;color:#6b7280;font-size:13px;"><strong>Cycle:</strong></td>
              <td style="padding:8px 0;color:#000000;font-size:14px;font-weight:600;">%d</td>
            </tr>
          </table>
        </td></tr>

        <tr><td style="padding:0 32px 24px;background:#ffffff;">
          <div style="background:#fef3c7;border-left:4px solid #f59e0b;padding:12px 16px;border-radius:6px;">
            <p style="margin:0;font-size:13px;color:#92400e;line-height:1.5;">
              <strong>Action Required:</strong> Review and process payout in Stripe dashboard. All members have been notified.
            </p>
          </div>
        </td></tr>

        <tr><td style="padding:0 32px 32px;background:#ffffff;">
          <div style="height:1px;background:#e5e5e5;margin:0;"></div>
        </td></tr>

        <tr><td style="padding:0 32px 24px;background:#ffffff;">
          <p style="margin:0;font-size:12px;color:#999999;text-align:center;line-height:1.5;">
            Automated notification from Cube payout system
          </p>
        </td></tr>

      </table>

    </td></tr>
  </table>
</body>
</html>
                """,
                logoUrl,
                cube.getName(),
                winnerName,
                winnerEmail != null ? winnerEmail : "N/A",
                userId,
                memberId,
                cube.getCubeId(),
                payoutAmount,
                cycleNumber
        );
    }

    @Override
    public void checkAndSendCubeReadyEmails(UUID cubeId) {
        try {
            // 1. Get the cube
            Cube cube = cubeRepository.findById(cubeId).orElse(null);
            if (cube == null) {
                System.out.println("⚠️ Cube not found: " + cubeId);
                return;
            }

            // 2. Only check if cube is still in draft status (status_id = 1)
            if (cube.getStatusId() == null || cube.getStatusId() != 1) {
                System.out.println("⚠️ Cube is not in draft status, skipping readiness check");
                return;
            }

            // 3. Check if expected number of members is set
            Integer expectedMembers = cube.getNumberofmembers();
            if (expectedMembers == null) {
                System.out.println("⚠️ Cube has no expected member count set");
                return;
            }

            // 4. Get all members
            List<CubeMember> members = cubeMemberRepository.findByCubeId(cubeId);
            int actualMemberCount = members.size();

            // 5. Check if member count matches expected
            if (actualMemberCount < expectedMembers) {
                System.out.println("ℹ️ Cube not ready: " + actualMemberCount + "/" + expectedMembers + " members joined");
                return;
            }

            // 6. Check if all members have paid (status_id = 2 means "paid")
            boolean allPaid = members.stream()
                    .allMatch(m -> m.getStatusId() != null && m.getStatusId() == 2);
            
            if (!allPaid) {
                long paidCount = members.stream()
                        .filter(m -> m.getStatusId() != null && m.getStatusId() == 2)
                        .count();
                System.out.println("ℹ️ Cube not ready: Only " + paidCount + "/" + actualMemberCount + " members have paid");
                return;
            }

            // 7. Cube is ready! Send emails
            System.out.println("✅ Cube is ready: " + cube.getName() + " - Sending ready emails...");
            
            String resendApiUrl = "https://api.resend.com/emails";
            
            // Get all member contact info
            List<MemberWithContact> membersWithContact = cubeMemberRepository.findMembersWithContactInfo(cubeId);
            
            // Send to all members
            for (MemberWithContact member : membersWithContact) {
                boolean isAdmin = member.getRoleId() != null && member.getRoleId() == 1;
                
                if (isAdmin) {
                    // Send admin email
                    String adminSubject = "🎉 " + cube.getName() + " is ready to start!";
                    String adminHtml = buildCubeReadyAdminEmail(cube, members.size());
                    sendEmail(resendApiUrl, member.getEmail(), adminSubject, adminHtml);
                    System.out.println("✅ Admin ready email sent to: " + member.getEmail());
                } else {
                    // Send member email
                    String memberSubject = "🎉 " + cube.getName() + " is ready!";
                    String memberHtml = buildCubeReadyMemberEmail(cube, members.size());
                    sendEmail(resendApiUrl, member.getEmail(), memberSubject, memberHtml);
                    System.out.println("✅ Member ready email sent to: " + member.getEmail());
                }
            }
            
            // Send to reviewer (admin email from config)
            String reviewerSubject = "🔔 Cube Ready for Review: " + cube.getName();
            String reviewerHtml = buildCubeReadyReviewerEmail(cube, members.size());
            sendEmail(resendApiUrl, adminEmail, reviewerSubject, reviewerHtml);
            System.out.println("✅ Reviewer email sent to: " + adminEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Error checking/sending cube ready emails: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Build email for admin (cube creator/admin member)
     */
    private String buildCubeReadyAdminEmail(Cube cube, int memberCount) {
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
                                        <td style="background: #10b981; padding: 20px;">
                                            <h2 style="color: white; margin: 0;">🎉 Your Cube is Ready!</h2>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 30px;">
                                            <h3 style="margin-top: 0; color: #1f2937;">%s</h3>
                                            <p style="color: #4b5563; font-size: 16px; line-height: 1.6;">
                                                All %d members have joined and completed their payments. Your cube is ready to start!
                                            </p>
                                            
                                            <div style="background: #dbeafe; border-left: 4px solid #3b82f6; padding: 15px; border-radius: 4px; margin: 20px 0;">
                                                <p style="margin: 0; color: #1e40af;"><strong>Action Required:</strong> Please start the cube to begin the first cycle.</p>
                                            </div>
                                            
                                            <div style="text-align: center; margin: 30px 0;">
                                                <a href="your-app-link" style="background: #10b981; color: white; padding: 14px 32px; text-decoration: none; border-radius: 6px; font-weight: 600; display: inline-block;">
                                                    Start Cube Now
                                                </a>
                                            </div>
                                            
                                            <p style="color: #6b7280; font-size: 14px; margin-top: 30px;">
                                                All members are waiting for you to start the cube.
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
                memberCount
        );
    }

    /**
     * Build email for regular members
     */
    private String buildCubeReadyMemberEmail(Cube cube, int memberCount) {
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
                                        <td style="background: #10b981; padding: 20px;">
                                            <h2 style="color: white; margin: 0;">🎉 Cube is Ready!</h2>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 30px;">
                                            <h3 style="margin-top: 0; color: #1f2937;">%s</h3>
                                            <p style="color: #4b5563; font-size: 16px; line-height: 1.6;">
                                                Great news! All %d members have joined and completed their payments.
                                            </p>
                                            
                                            <div style="background: #fef3c7; border-left: 4px solid #f59e0b; padding: 15px; border-radius: 4px; margin: 20px 0;">
                                                <p style="margin: 0; color: #92400e;">
                                                    <strong>Waiting on admin to start the cube.</strong> You'll receive another notification once the cube begins.
                                                </p>
                                            </div>
                                            
                                            <p style="color: #6b7280; font-size: 14px; margin-top: 30px;">
                                                The first cycle will begin as soon as the admin starts the cube. Stay tuned!
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
                memberCount
        );
    }

    /**
     * Build email for reviewer (system admin)
     */
    private String buildCubeReadyReviewerEmail(Cube cube, int memberCount) {
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
                                        <td style="background: #6366f1; padding: 20px;">
                                            <h2 style="color: white; margin: 0;">🔔 Cube Ready for Review</h2>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding: 30px;">
                                            <h3 style="margin-top: 0; color: #1f2937;">Cube Details</h3>
                                            <table style="width: 100%%; border-collapse: collapse;">
                                                <tr><td style="padding: 8px 0; color: #6b7280;"><strong>Cube Name:</strong></td><td style="padding: 8px 0;">%s</td></tr>
                                                <tr><td style="padding: 8px 0; color: #6b7280;"><strong>Members:</strong></td><td style="padding: 8px 0;">%d</td></tr>
                                                <tr><td style="padding: 8px 0; color: #6b7280;"><strong>Status:</strong></td><td style="padding: 8px 0; color: #10b981;"><strong>Ready to Start</strong></td></tr>
                                                <tr><td style="padding: 8px 0; color: #6b7280;"><strong>Cube ID:</strong></td><td style="padding: 8px 0; font-family: monospace; font-size: 12px;">%s</td></tr>
                                            </table>
                                            
                                            <div style="background: #fef3c7; border-left: 4px solid #f59e0b; padding: 15px; border-radius: 4px; margin: 20px 0;">
                                                <p style="margin: 0; color: #92400e;">
                                                    <strong>FYI:</strong> Waiting on admin to start the cube. All payments verified.
                                                </p>
                                            </div>
                                            
                                            <p style="color: #6b7280; font-size: 14px;">
                                                All members have joined and paid. The admin has been notified to start the cube.
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
                memberCount,
                cube.getCubeId()
        );
    }
}