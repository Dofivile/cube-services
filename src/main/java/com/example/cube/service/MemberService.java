package com.example.cube.service;

import com.example.cube.dto.request.InviteMembersRequestDTO;
import com.example.cube.dto.response.InviteMembersResponseDTO;
import java.util.UUID;

public interface MemberService {
    InviteMembersResponseDTO inviteMembers(UUID cubeId, InviteMembersRequestDTO request, UUID invitedBy);
}