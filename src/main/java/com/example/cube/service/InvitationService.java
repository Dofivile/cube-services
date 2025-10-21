package com.example.cube.service;

import com.example.cube.dto.request.AddMembersDirectRequest;
import com.example.cube.dto.request.InviteMembersRequest;
import com.example.cube.dto.response.AddMembersDirectResponse;
import com.example.cube.dto.response.InviteMembersResponse;
import java.util.UUID;

public interface InvitationService {

    /**
     * Create and send invitations to multiple emails
     */
    InviteMembersResponse inviteMembers(UUID cubeId, InviteMembersRequest request, UUID invitedBy);

    // We'll add acceptInvitation(), declineInvitation() later
    AddMembersDirectResponse addMembersDirect(UUID cubeId, AddMembersDirectRequest request, UUID addedBy);
}