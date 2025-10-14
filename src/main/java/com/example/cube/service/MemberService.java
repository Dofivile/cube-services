package com.example.cube.service;

import com.example.cube.dto.request.InviteMembersRequest;
import com.example.cube.dto.response.InviteMembersResponse;
import java.util.UUID;

public interface MemberService {
    InviteMembersResponse inviteMembers(UUID cubeId, InviteMembersRequest request, UUID invitedBy);
}