package com.example.cube.service.impl;

import com.example.cube.dto.request.InviteMembersRequestDTO;
import com.example.cube.dto.response.InviteMembersResponseDTO;
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
    public InviteMembersResponseDTO inviteMembers(UUID cubeId, InviteMembersRequestDTO request, UUID invitedBy) {

        // 1. Validate cube exists
        Cube cube = cubeRepository.findById(cubeId)
                .orElseThrow(() -> new RuntimeException("Cube not found"));

        // 2. Validate inviter has permission
        boolean hasPermission = cubeMemberRepository.existsByCubeIdAndUserId(cubeId, invitedBy);
        if (!hasPermission) {
            throw new RuntimeException("You don't have permission to invite members to this cube");
        }

        // 3. Process invitations
        Map<String, String> results = new HashMap<>();

        for (UUID userId : request.getUserIds()) {
            // Check if already member
            if (cubeMemberRepository.existsByCubeIdAndUserId(cubeId, userId)) {
                results.put(userId.toString(), "already_member");
                continue;
            }

            // Add new member
            CubeMember member = new CubeMember();
            member.setCubeId(cubeId);
            member.setUserId(userId);
            member.setRoleId(request.getRoleId());
            cubeMemberRepository.save(member);

            results.put(userId.toString(), "success");
        }

        // 4. Build response
        InviteMembersResponseDTO response = new InviteMembersResponseDTO();
        response.setCubeId(cubeId);
        response.setResults(results);

        return response;
    }
}