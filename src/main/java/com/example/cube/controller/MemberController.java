package com.example.cube.controller;

import com.example.cube.dto.request.InviteMembersRequest;
import com.example.cube.dto.response.InviteMembersResponse;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/members")  // ← CHANGED from /api/cubes
public class MemberController {

    private final MemberService memberService;
    private final AuthenticationService authenticationService;

    @Autowired
    public MemberController(MemberService memberService,
                            AuthenticationService authenticationService) {
        this.memberService = memberService;
        this.authenticationService = authenticationService;
    }

    /**
     * POST /api/members/invite
     * Invite members to a cube
     */
    @PostMapping("/invite")  // ← REMOVED /{cubeId} from path
    public ResponseEntity<InviteMembersResponse> inviteMembers(
            @Valid @RequestBody InviteMembersRequest request,  // ← Removed @PathVariable cubeId
            @RequestHeader("Authorization") String authHeader) {

        UUID invitedBy = authenticationService.validateAndExtractUserId(authHeader);

        // Extract cubeId from request body instead of path
        UUID cubeId = request.getCubeId();

        InviteMembersResponse response = memberService.inviteMembers(cubeId, request, invitedBy);
        return ResponseEntity.ok(response);
    }
}