package com.example.cube.service.impl;

import com.example.cube.dto.request.InviteMembersRequest;
import com.example.cube.dto.response.InviteMembersResponse;
import com.example.cube.model.Cube;
import com.example.cube.model.CubeInvitation;
import com.example.cube.repository.CubeInvitationRepository;
import com.example.cube.repository.CubeMemberRepository;
import com.example.cube.repository.CubeRepository;
import com.example.cube.service.EmailService;
import com.example.cube.service.InvitationService;
import com.example.cube.service.supabass.SupabaseUserLookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class InvitationServiceImpl implements InvitationService {

    private final CubeInvitationRepository invitationRepository;
    private final CubeMemberRepository cubeMemberRepository;
    private final CubeRepository cubeRepository;
    private final SupabaseUserLookupService userLookupService;
    private final EmailService emailService;

    @Autowired
    public InvitationServiceImpl(CubeInvitationRepository invitationRepository,
                                 CubeMemberRepository cubeMemberRepository,
                                 CubeRepository cubeRepository,
                                 SupabaseUserLookupService userLookupService,
                                 EmailService emailService) {
        this.invitationRepository = invitationRepository;
        this.cubeMemberRepository = cubeMemberRepository;
        this.cubeRepository = cubeRepository;
        this.userLookupService = userLookupService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public InviteMembersResponse inviteMembers(UUID cubeId, InviteMembersRequest request, UUID invitedBy) {

        // 1. Validate cube exists
        Cube cube = cubeRepository.findById(cubeId)
                .orElseThrow(() -> new RuntimeException("Cube not found"));

        // 2. Validate inviter has permission
        if (!cubeMemberRepository.existsByCubeIdAndUserId(cubeId, invitedBy)) {
            throw new RuntimeException("You don't have permission to invite members to this cube");
        }

        // 3. Validate cube capacity
        validateCubeCapacity(cube, request.getEmails().size());

        // 4. Process each email invitation
        Map<String, String> results = processInvitations(cubeId, request.getEmails(), request.getRoleId(), invitedBy, cube);

        // 5. Build response
        InviteMembersResponse response = new InviteMembersResponse();
        response.setCubeId(cubeId);
        response.setResults(results);

        long successCount = results.values().stream()
                .filter(s -> "invited".equals(s))
                .count();
        response.setMessage(String.format("Sent %d invitation(s)", successCount));

        return response;
    }

    // ========== Helper Methods ==========

    private void validateCubeCapacity(Cube cube, int newMembersCount) {
        int maxCapacity = cube.getNumberofmembers();
        long currentMemberCount = cubeMemberRepository.countByCubeId(cube.getCubeId());
        long totalAfterInvite = currentMemberCount + newMembersCount;

        if (totalAfterInvite > maxCapacity) {
            throw new RuntimeException(
                    String.format("Cube is full or will exceed capacity. Current: %d, Max: %d, Trying to add: %d",
                            currentMemberCount, maxCapacity, newMembersCount)
            );
        }
    }

    private Map<String, String> processInvitations(UUID cubeId, List<String> emails,
                                                   Integer roleId, UUID invitedBy, Cube cube) {
        Map<String, String> results = new HashMap<>();

        for (String email : emails) {
            try {
                String status = createSingleInvitation(cubeId, email, roleId, invitedBy, cube);
                results.put(email, status);
            } catch (Exception e) {
                results.put(email, "error: " + e.getMessage());
            }
        }

        return results;
    }

    private String createSingleInvitation(UUID cubeId, String email, Integer roleId,
                                          UUID invitedBy, Cube cube) {
        // 1. Check if user with this email is already a member
        String existingUserId = userLookupService.getUserIdByEmail(email);
        if (existingUserId != null) {
            UUID userId = UUID.fromString(existingUserId);
            if (cubeMemberRepository.existsByCubeIdAndUserId(cubeId, userId)) {
                return "already_member";
            }
        }

        // 2. Check if pending invitation already exists
        if (invitationRepository.existsByEmailAndCubeIdAndStatus(email, cubeId, "pending")) {
            return "pending_invitation_exists";
        }

        // 3. Create invitation record
        CubeInvitation invitation = new CubeInvitation();
        invitation.setCubeId(cubeId);
        invitation.setEmail(email);
        invitation.setInvitedBy(invitedBy);
        invitation.setRoleId(roleId);

        // Set inviteeId if user exists in system
        if (existingUserId != null) {
            invitation.setInviteeId(UUID.fromString(existingUserId));
        }
        // Otherwise inviteeId stays null (for unregistered users)

        // Generate secure token
        invitation.setInviteToken(generateInviteToken());

        // Set expiration (48 hours from now)
        invitation.setExpiresAt(LocalDateTime.now().plusHours(48));

        // Save to database
        invitationRepository.save(invitation);

        // 4. Send email
        try {
            emailService.sendInvitationEmail(email, invitation.getInviteToken(),
                    cube.getName(), invitedBy);
            return "invited";
        } catch (Exception e) {
            return "invited_email_failed";
        }
    }

    private String generateInviteToken() {
        // Generate a secure random token
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }
}