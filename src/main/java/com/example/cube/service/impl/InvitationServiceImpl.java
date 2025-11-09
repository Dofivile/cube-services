package com.example.cube.service.impl;

import com.example.cube.dto.request.AddMembersDirectRequest;
import com.example.cube.dto.request.InviteMembersRequest;
import com.example.cube.dto.response.AddMembersDirectResponse;
import com.example.cube.dto.response.InviteMembersResponse;
import com.example.cube.dto.response.JoinCubeResponse;
import com.example.cube.model.Cube;
import com.example.cube.model.CubeInvitation;
import com.example.cube.model.CubeMember;
import com.example.cube.repository.CubeInvitationRepository;
import com.example.cube.repository.CubeMemberRepository;
import com.example.cube.repository.CubeRepository;
import com.example.cube.repository.UserDetailsRepository;
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
    private final UserDetailsRepository userDetailsRepository;

    @Autowired
    public InvitationServiceImpl(CubeInvitationRepository invitationRepository,
                                 CubeMemberRepository cubeMemberRepository,
                                 CubeRepository cubeRepository,
                                 SupabaseUserLookupService userLookupService,
                                 EmailService emailService, UserDetailsRepository userDetailsRepository) {
        this.invitationRepository = invitationRepository;
        this.cubeMemberRepository = cubeMemberRepository;
        this.cubeRepository = cubeRepository;
        this.userLookupService = userLookupService;
        this.emailService = emailService;
        this.userDetailsRepository = userDetailsRepository;
    }

    @Override
    @Transactional
    public InviteMembersResponse inviteMembers(UUID cubeId, InviteMembersRequest request, UUID invitedBy) {

        // 1. Validate cube exists
        Cube cube = cubeRepository.findById(cubeId)
                .orElseThrow(() -> new RuntimeException("Cube not found"));

        // 2. ✅ Validate inviter is an ADMIN
        CubeMember inviterMember = cubeMemberRepository.findByCubeIdAndUserId(cubeId, invitedBy)
                .orElseThrow(() -> new RuntimeException("You are not a member of this cube"));
        
        if (inviterMember.getRoleId() != 1) {
            throw new RuntimeException("Only admins can invite members to this cube");
        }

        // 3. Validate cube capacity
        validateCubeCapacity(cube, request.getEmails().size());

        // 4. Process each email invitation
        Map<String, String> results = processInvitations(cubeId, request.getEmails(), invitedBy, cube);

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


    @Override
    @Transactional
    public AddMembersDirectResponse addMembersDirect(UUID cubeId, AddMembersDirectRequest request, UUID addedBy) {

        // 1. Validate cube exists
        Cube cube = cubeRepository.findById(cubeId)
                .orElseThrow(() -> new RuntimeException("Cube not found"));

        // 2. Validate user has permission
        if (!cubeMemberRepository.existsByCubeIdAndUserId(cubeId, addedBy)) {
            throw new RuntimeException("You don't have permission to add members to this cube");
        }

        // 3. Validate capacity
        int maxCapacity = cube.getNumberofmembers();
        long currentMemberCount = cubeMemberRepository.countByCubeId(cubeId);
        long totalAfterAdd = currentMemberCount + request.getUserIds().size();

        if (totalAfterAdd > maxCapacity) {
            throw new RuntimeException(
                    String.format("Cube capacity exceeded. Current: %d, Max: %d, Trying to add: %d",
                            currentMemberCount, maxCapacity, request.getUserIds().size())
            );
        }

        // 4. Add each user
        Map<String, String> results = new HashMap<>();

        for (UUID userId : request.getUserIds()) {
            try {
                // Check if already a member
                if (cubeMemberRepository.existsByCubeIdAndUserId(cubeId, userId)) {
                    results.put(userId.toString(), "already_member");
                    continue;
                }

                // Check if user exists
                if (!userDetailsRepository.existsById(userId)) {
                    results.put(userId.toString(), "user_not_found");
                    continue;
                }

                // Add member directly
                CubeMember member = new CubeMember();
                member.setCubeId(cubeId);
                member.setUserId(userId);
                // Enforce default member role (2)
                member.setRoleId(2);
                cubeMemberRepository.save(member);

                results.put(userId.toString(), "added");

            } catch (Exception e) {
                results.put(userId.toString(), "error: " + e.getMessage());
            }
        }

        // 5. Build response
        long successCount = results.values().stream().filter(s -> "added".equals(s)).count();

        AddMembersDirectResponse response = new AddMembersDirectResponse();
        response.setCubeId(cubeId);
        response.setResults(results);
        response.setMessage(String.format("Added %d member(s) directly", successCount));

        return response;
    }

    @Override
    @Transactional
    public JoinCubeResponse joinCubeByCode(String invitationCode, UUID userId) {
        // 1. Find cube by invitation code
        Cube cube = cubeRepository.findByInvitationCode(invitationCode.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Invalid invitation code"));

        // 2. Check if already a member
        if (cubeMemberRepository.existsByCubeIdAndUserId(cube.getCubeId(), userId)) {
            return new JoinCubeResponse(
                    true,
                    "You are already a member of this cube",
                    cube.getCubeId(),
                    cube.getName(),
                    cubeMemberRepository.findByCubeIdAndUserId(cube.getCubeId(), userId)
                            .map(CubeMember::getMemberId)
                            .orElse(null)
            );
        }

        // 3. Validate cube capacity
        validateCubeCapacity(cube, 1);

        // 4. Add user as member
        CubeMember member = new CubeMember();
        member.setCubeId(cube.getCubeId());
        member.setUserId(userId);
        member.setRoleId(2); // Regular member role
        cubeMemberRepository.save(member);

        // 5. ✅ Update invitation record (mark as accepted)
        updateInvitationRecordForUser(cube.getCubeId(), userId);

        return new JoinCubeResponse(
                true,
                "Successfully joined the cube",
                cube.getCubeId(),
                cube.getName(),
                member.getMemberId()
        );
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
                                                   UUID invitedBy, Cube cube) {
        Map<String, String> results = new HashMap<>();

        for (String email : emails) {
            try {
                String status = createSingleInvitation(cubeId, email, invitedBy, cube);
                results.put(email, status);
            } catch (Exception e) {
                results.put(email, "error: " + e.getMessage());
            }
        }

        return results;
    }

    private String createSingleInvitation(UUID cubeId, String email,
                                          UUID invitedBy, Cube cube) {
        
        if (invitationRepository.existsByEmailAndCubeIdAndStatusId(email, cubeId, 1)) {
            return "pending_invitation_exists";
        }

        // 3. Create invitation record (for tracking purposes)
        CubeInvitation invitation = new CubeInvitation();
        invitation.setCubeId(cubeId);
        invitation.setEmail(email);
        invitation.setStatusId(1);  // pending
        invitation.setInvitedBy(invitedBy);
        invitation.setRoleId(2);  // Enforce default member role

        // ✅ REMOVED: Don't pre-populate inviteeId - will be set when they join
        // inviteeId stays NULL until user creates account and joins

        // Use cube's invitation code
        invitation.setInviteToken(cube.getInvitationCode());

        // Set expiration (48 hours from now)
        invitation.setExpiresAt(LocalDateTime.now().plusHours(48));

        // Save to database
        invitationRepository.save(invitation);

        // 4. Send email with invitation code
        try {
            emailService.sendInvitationEmail(email, cube.getInvitationCode(),
                    cube.getName(), invitedBy);
            return "invited";
        } catch (Exception e) {
            return "invited_email_failed";
        }
    }

    /**
     * Mark invitation as accepted when user joins via invitation code
     */
    private void updateInvitationRecordForUser(UUID cubeId, UUID userId) {
        try {
            // ✅ Get user's email from Supabase auth.users table
            String email = userLookupService.getEmailByUserId(userId.toString());
            if (email == null) {
                System.out.println("⚠️ Could not find email for user " + userId + " - invitation record not updated");
                return;
            }

            // Find pending invitation(s) for this cube and email
            List<CubeInvitation> invitations = invitationRepository.findByCubeIdAndEmailAndStatusId(
                    cubeId,
                    email,
                    1  // pending status
            );

            if (invitations.isEmpty()) {
                System.out.println("ℹ️ No pending invitation found for " + email + " - user joined directly with code");
                return;
            }

            // Mark all matching invitations as accepted
            for (CubeInvitation invitation : invitations) {
                invitation.setStatusId(2); // accepted
                invitation.setAcceptedAt(LocalDateTime.now());
                invitation.setInviteeId(userId);
                invitationRepository.save(invitation);
            }

            System.out.println("✅ Marked " + invitations.size() + " invitation(s) as accepted for " + email);

        } catch (Exception e) {
            System.err.println("⚠️ Failed to update invitation record: " + e.getMessage());
            // Don't throw - user already joined successfully, this is just record-keeping
        }
    }

}
