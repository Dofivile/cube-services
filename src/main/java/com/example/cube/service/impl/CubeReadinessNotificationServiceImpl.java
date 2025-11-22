package com.example.cube.service.impl;

import com.example.cube.model.Cube;
import com.example.cube.model.CubeMember;
import com.example.cube.repository.CubeMemberRepository;
import com.example.cube.repository.CubeRepository;
import com.example.cube.repository.NotificationRepository;
import com.example.cube.service.CubeReadinessNotificationService;
import com.example.cube.service.NotificationDeliveryService;
import com.example.cube.service.NotificationService;
import com.example.cube.service.supabass.SupabaseUserLookupService;
import com.example.cube.dto.response.NotificationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service to evaluate when a cube is ready to start and send notifications.
 * A cube is ready when all members have joined and paid.
 */
@Service
@Transactional
public class CubeReadinessNotificationServiceImpl implements CubeReadinessNotificationService {

    private final CubeRepository cubeRepository;
    private final CubeMemberRepository cubeMemberRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final NotificationDeliveryService notificationDeliveryService;
    private final SupabaseUserLookupService supabaseUserLookupService;

    // Notification type keys
    private static final String TYPE_CUBE_READY_ADMIN = "cube_ready_admin";
    private static final String TYPE_CUBE_READY_MEMBER = "cube_ready_member";

    public CubeReadinessNotificationServiceImpl(CubeRepository cubeRepository,
                                                CubeMemberRepository cubeMemberRepository,
                                                NotificationRepository notificationRepository,
                                                NotificationService notificationService,
                                                NotificationDeliveryService notificationDeliveryService,
                                                SupabaseUserLookupService supabaseUserLookupService) {
        this.cubeRepository = cubeRepository;
        this.cubeMemberRepository = cubeMemberRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.notificationDeliveryService = notificationDeliveryService;
        this.supabaseUserLookupService = supabaseUserLookupService;
    }

    @Override
    public void checkAndNotifyIfReady(UUID cubeId) {
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

        // 4. Check if all members have joined
        long memberCount = cubeMemberRepository.countByCubeId(cubeId);
        if (memberCount < expectedMembers) {
            System.out.println("ℹ️ Cube not ready: " + memberCount + "/" + expectedMembers + " members joined");
            return;
        }

        // 5. Get all members and check if all have paid
        List<CubeMember> members = cubeMemberRepository.findByCubeId(cubeId);
        boolean allPaid = members.stream()
                .allMatch(m -> m.getStatusId() != null && m.getStatusId() == 2); // status_id = 2 means "active/paid"
        
        if (!allPaid) {
            System.out.println("ℹ️ Cube not ready: Not all members have paid yet");
            return;
        }

        // 6. Cube is ready! Send notifications
        System.out.println("✅ Cube is ready: " + cube.getName() + " - Sending notifications...");
        
        sendAdminNotifications(cube, members);
        sendMemberNotifications(cube, members);
    }

    /**
     * Send notifications to all admins
     */
    private void sendAdminNotifications(Cube cube, List<CubeMember> members) {
        // Check if we already sent admin notifications for this cube (idempotency)
        // We use typeId lookup - need to get the typeId first
        Integer typeId = getTypeIdFromKey(TYPE_CUBE_READY_ADMIN);
        if (typeId != null && notificationRepository.existsByCubeIdAndTypeId(cube.getCubeId(), typeId)) {
            System.out.println("ℹ️ Admin notifications already sent for this cube");
            return;
        }

        // Collect all admin user IDs
        Set<UUID> adminUserIds = new HashSet<>();
        
        // 1. Cube creator is always an admin
        if (cube.getUser_id() != null) {
            adminUserIds.add(cube.getUser_id());
        }
        
        // 2. Members with role_id = 1 are also admins
        members.stream()
                .filter(m -> m.getRoleId() != null && m.getRoleId() == 1)
                .map(CubeMember::getUserId)
                .forEach(adminUserIds::add);

        // Send notification to each admin
        String title = "Cube is ready to start";
        String body = "All members have joined and paid for \"" + cube.getName() + "\". Click Start to begin the cube.";

        for (UUID adminId : adminUserIds) {
            try {
                // Create in-app notification
                NotificationResponse notification = notificationService.createNotification(
                        adminId,
                        cube.getCubeId(),
                        TYPE_CUBE_READY_ADMIN,
                        title,
                        body
                );

                // Get admin email and deliver via email + in-app
                String adminEmail = supabaseUserLookupService.getEmailByUserId(adminId.toString());
                if (adminEmail != null) {
                    notificationDeliveryService.deliverNotification(
                            notification.getNotificationId(),
                            adminEmail,
                            title,
                            body
                    );
                    System.out.println("✅ Admin notification sent to: " + adminEmail);
                } else {
                    System.err.println("⚠️ Could not find email for admin: " + adminId);
                    // Still record in-app delivery
                    notificationDeliveryService.recordDeliverySuccess(notification.getNotificationId(), "in_app");
                }
            } catch (Exception e) {
                System.err.println("❌ Failed to send admin notification to " + adminId + ": " + e.getMessage());
            }
        }
    }

    /**
     * Send notifications to all members
     */
    private void sendMemberNotifications(Cube cube, List<CubeMember> members) {
        // Check if we already sent member notifications for this cube (idempotency)
        Integer typeId = getTypeIdFromKey(TYPE_CUBE_READY_MEMBER);
        if (typeId != null && notificationRepository.existsByCubeIdAndTypeId(cube.getCubeId(), typeId)) {
            System.out.println("ℹ️ Member notifications already sent for this cube");
            return;
        }

        String title = "Everything is ready";
        String body = "Your cube \"" + cube.getName() + "\" is ready to start. Waiting on admin to begin.";

        for (CubeMember member : members) {
            try {
                // Create in-app notification
                NotificationResponse notification = notificationService.createNotification(
                        member.getUserId(),
                        cube.getCubeId(),
                        TYPE_CUBE_READY_MEMBER,
                        title,
                        body
                );

                // Get member email and deliver via email + in-app
                String memberEmail = supabaseUserLookupService.getEmailByUserId(member.getUserId().toString());
                if (memberEmail != null) {
                    notificationDeliveryService.deliverNotification(
                            notification.getNotificationId(),
                            memberEmail,
                            title,
                            body
                    );
                    System.out.println("✅ Member notification sent to: " + memberEmail);
                } else {
                    System.err.println("⚠️ Could not find email for member: " + member.getUserId());
                    // Still record in-app delivery
                    notificationDeliveryService.recordDeliverySuccess(notification.getNotificationId(), "in_app");
                }
            } catch (Exception e) {
                System.err.println("❌ Failed to send member notification to " + member.getUserId() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Helper to get notification type ID from key (for idempotency check)
     */
    private Integer getTypeIdFromKey(String typeKey) {
        // We need to look up based on created notifications
        // For idempotency, we'll check by both cubeId and typeId
        // Since we're checking existsByCubeIdAndTypeId, we need the typeId
        // This is a simple workaround - in production you might cache these
        if (TYPE_CUBE_READY_ADMIN.equals(typeKey)) {
            return 1; // Based on your earlier schema
        } else if (TYPE_CUBE_READY_MEMBER.equals(typeKey)) {
            return 2;
        }
        return null;
    }
}

