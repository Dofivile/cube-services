package com.example.cube.service.impl;

import com.example.cube.model.Notification;
import com.example.cube.model.NotificationChannel;
import com.example.cube.model.NotificationDelivery;
import com.example.cube.repository.NotificationChannelRepository;
import com.example.cube.repository.NotificationDeliveryRepository;
import com.example.cube.repository.NotificationRepository;
import com.example.cube.service.NotificationDeliveryService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class NotificationDeliveryServiceImpl implements NotificationDeliveryService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final NotificationChannelRepository notificationChannelRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email:no-reply@cubemoney.io}")
    private String fromEmail;

    public NotificationDeliveryServiceImpl(NotificationRepository notificationRepository,
                                           NotificationDeliveryRepository notificationDeliveryRepository,
                                           NotificationChannelRepository notificationChannelRepository) {
        this.notificationRepository = notificationRepository;
        this.notificationDeliveryRepository = notificationDeliveryRepository;
        this.notificationChannelRepository = notificationChannelRepository;
    }

    @Override
    public void deliverNotification(UUID notificationId, String userEmail, String title, String body) {
        // 1. Record in-app delivery (in-app is always successful since we already created the notification)
        recordDeliverySuccess(notificationId, "in_app");

        // 2. Send email
        try {
            sendEmailNotification(userEmail, title, body);
            recordDeliverySuccess(notificationId, "email");
            
            // Update notification status to sent
            updateNotificationStatus(notificationId, "sent");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send email notification: " + e.getMessage());
            recordDeliveryFailure(notificationId, "email", e.getMessage());
            
            // Update notification status to failed (only if email fails)
            updateNotificationStatus(notificationId, "failed");
        }
    }

    @Override
    public void recordDeliverySuccess(UUID notificationId, String channelName) {
        NotificationChannel channel = notificationChannelRepository.findByChannelName(channelName)
                .orElseThrow(() -> new RuntimeException("Channel not found: " + channelName));

        NotificationDelivery delivery = new NotificationDelivery();
        delivery.setNotificationId(notificationId);
        delivery.setChannelId(channel.getChannelId());
        delivery.setDeliveredAt(Instant.now());
        delivery.setError(null);

        notificationDeliveryRepository.save(delivery);
    }

    @Override
    public void recordDeliveryFailure(UUID notificationId, String channelName, String error) {
        NotificationChannel channel = notificationChannelRepository.findByChannelName(channelName)
                .orElseThrow(() -> new RuntimeException("Channel not found: " + channelName));

        NotificationDelivery delivery = new NotificationDelivery();
        delivery.setNotificationId(notificationId);
        delivery.setChannelId(channel.getChannelId());
        delivery.setDeliveredAt(null);
        delivery.setError(error);

        notificationDeliveryRepository.save(delivery);
    }

    /**
     * Send email via Resend API
     */
    private void sendEmailNotification(String toEmail, String title, String body) {
        String resendApiUrl = "https://api.resend.com/emails";

        // Build headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + resendApiKey);

        // Build email body
        JSONObject emailBody = new JSONObject();
        emailBody.put("from", fromEmail);
        emailBody.put("to", toEmail);
        emailBody.put("subject", title);
        emailBody.put("html", buildNotificationEmailHtml(title, body));

        HttpEntity<String> request = new HttpEntity<>(emailBody.toString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    resendApiUrl,
                    request,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Email API returned non-2xx status: " + response.getStatusCode());
            }

            System.out.println("✅ Email notification sent to: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Error sending email notification: " + e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    /**
     * Update notification status (sent or failed)
     */
    private void updateNotificationStatus(UUID notificationId, String status) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setStatus(status);
        notification.setSentAt(Instant.now());
        notificationRepository.save(notification);
    }

    /**
     * Build HTML email template for notifications
     */
    private String buildNotificationEmailHtml(String title, String body) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f5f5f5;">
                <table role="presentation" style="width: 100%%; border-collapse: collapse;">
                    <tr>
                        <td align="center" style="padding: 40px 0;">
                            <table role="presentation" style="width: 600px; border-collapse: collapse; background-color: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                                <!-- Header -->
                                <tr>
                                    <td style="padding: 32px 32px 24px 32px; text-align: center; border-bottom: 1px solid #e5e5e5;">
                                        <h1 style="margin: 0; color: #1a1a1a; font-size: 24px; font-weight: 600;">🔔 Cube Notification</h1>
                                    </td>
                                </tr>
                                
                                <!-- Content -->
                                <tr>
                                    <td style="padding: 32px;">
                                        <h2 style="margin: 0 0 16px 0; color: #1a1a1a; font-size: 20px; font-weight: 600;">%s</h2>
                                        <p style="margin: 0; color: #525252; font-size: 16px; line-height: 1.6;">%s</p>
                                    </td>
                                </tr>
                                
                                <!-- Footer -->
                                <tr>
                                    <td style="padding: 24px 32px 32px 32px; text-align: center; border-top: 1px solid #e5e5e5;">
                                        <p style="margin: 0 0 8px 0; color: #737373; font-size: 14px;">Cube Money</p>
                                        <p style="margin: 0; color: #a3a3a3; font-size: 12px;">You're receiving this because you're a member of a Cube.</p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
            </body>
            </html>
            """, title, body);
    }
}

