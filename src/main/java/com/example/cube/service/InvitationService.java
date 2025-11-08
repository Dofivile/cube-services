package com.example.cube.service;

import com.example.cube.dto.request.AddMembersDirectRequest;
import com.example.cube.dto.request.InviteMembersRequest;
import com.example.cube.dto.request.JoinCubeRequest;
import com.example.cube.dto.response.AddMembersDirectResponse;
import com.example.cube.dto.response.InviteMembersResponse;
import com.example.cube.dto.response.JoinCubeResponse;
import java.util.UUID;

public interface InvitationService {

    /**
     * Create and send invitations to multiple emails
     */
    InviteMembersResponse inviteMembers(UUID cubeId, InviteMembersRequest request, UUID invitedBy);

    // Accept an invitation using a token for the acting user
    com.example.cube.dto.response.AcceptInvitationResponse acceptInvitation(String inviteToken, UUID actingUserId);

    // We'll add declineInvitation() later
    AddMembersDirectResponse addMembersDirect(UUID cubeId, AddMembersDirectRequest request, UUID addedBy);

    JoinCubeResponse joinCubeByCode(String invitationCode, UUID userId);
}
