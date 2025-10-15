package com.example.cube.service.impl;

import com.example.cube.dto.request.InviteMembersRequest;
import com.example.cube.dto.response.InviteMembersResponse;
import com.example.cube.model.Cube;
import com.example.cube.model.CubeMember;
import com.example.cube.repository.CubeMemberRepository;
import com.example.cube.repository.CubeRepository;
import com.example.cube.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MemberServiceImpl implements MemberService {

    private final CubeMemberRepository cubeMemberRepository;
    private final CubeRepository cubeRepository;

    @Autowired
    public MemberServiceImpl(CubeMemberRepository cubeMemberRepository,
                             CubeRepository cubeRepository) {
        this.cubeMemberRepository = cubeMemberRepository;
        this.cubeRepository = cubeRepository;
    }

    @Override
    @Transactional
    public InviteMembersResponse inviteMembers(UUID cubeId, InviteMembersRequest request, UUID invitedBy) {

        // Validate cube exists and get cube info
        Cube cube = getCube(cubeId);

        // Validate permissions
        validateInviterPermission(cubeId, invitedBy);

        // Validate capacity
        validateCubeCapacity(cube, request.getUserIds().size());

        // Process invitations
        Map<String, String> results = processInvitations(cubeId, request, cube);

        // Build and return response
        return buildInviteResponse(cubeId, results);
    }

    /** ---------- Helper methods ---------- */

    /**
     * Get cube and validate it exists
     */
    private Cube getCube(UUID cubeId) {
        return cubeRepository.findById(cubeId)
                .orElseThrow(() -> new RuntimeException("Cube not found"));
    }

    /**
     * Validate that inviter has permission to invite members
     */
    private void validateInviterPermission(UUID cubeId, UUID invitedBy) {
        boolean hasPermission = cubeMemberRepository.existsByCubeIdAndUserId(cubeId, invitedBy);
        if (!hasPermission) {
            throw new RuntimeException("You don't have permission to invite members to this cube");
        }
    }

    /**
     * Validate that cube has capacity for new members
     */
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

    /**
     * Process all invitation requests
     * Returns a map of userId -> status (success, already_member, cube_full)
     */
    private Map<String, String> processInvitations(UUID cubeId, InviteMembersRequest request, Cube cube) {
        Map<String, String> results = new HashMap<>();
        int maxCapacity = cube.getNumberofmembers();
        long currentCount = cubeMemberRepository.countByCubeId(cubeId);

        for (UUID userId : request.getUserIds()) {
            // Check capacity before each add (in case of partial success)
            if (currentCount >= maxCapacity) {
                results.put(userId.toString(), "cube_full");
                continue;
            }

            String status = processSingleInvitation(cubeId, userId, request.getRoleId());
            results.put(userId.toString(), status);

            // Increment count if member was successfully added
            if ("success".equals(status)) {
                currentCount++;
            }
        }

        return results;
    }

    /**
     * Process a single invitation
     * Returns "success" or "already_member"
     */
    private String processSingleInvitation(UUID cubeId, UUID userId, Integer roleId) {
        // Check if already member
        if (isAlreadyMember(cubeId, userId)) {
            return "already_member";
        }

        // Add new member
        addMemberToCube(cubeId, userId, roleId);
        return "success";
    }

    /**
     * Check if user is already a member of the cube
     */
    private boolean isAlreadyMember(UUID cubeId, UUID userId) {
        return cubeMemberRepository.existsByCubeIdAndUserId(cubeId, userId);
    }

    /**
     * Add a new member to the cube
     */
    private void addMemberToCube(UUID cubeId, UUID userId, Integer roleId) {
        CubeMember member = new CubeMember();
        member.setCubeId(cubeId);
        member.setUserId(userId);
        member.setRoleId(roleId);
        cubeMemberRepository.save(member);
    }

    /**
     * Build the response DTO
     */
    private InviteMembersResponse buildInviteResponse(UUID cubeId, Map<String, String> results) {
        InviteMembersResponse response = new InviteMembersResponse();
        response.setCubeId(cubeId);
        response.setResults(results);
        return response;
    }
}