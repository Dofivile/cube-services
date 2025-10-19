package com.example.cube.controller;

import com.example.cube.dto.request.InviteMembersRequest;
import com.example.cube.dto.response.InviteMembersResponse;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.InvitationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final InvitationService invitationService;  // ← Changed from MemberService
    private final AuthenticationService authenticationService;

    @Autowired
    public MemberController(InvitationService invitationService,  // ← Changed
                            AuthenticationService authenticationService) {
        this.invitationService = invitationService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/invite")
    public ResponseEntity<InviteMembersResponse> inviteMembers(@Valid @RequestBody InviteMembersRequest request,
            @RequestHeader("Authorization") String authHeader) {

        UUID invitedBy = authenticationService.validateAndExtractUserId(authHeader);
        UUID cubeId = request.getCubeId();

        InviteMembersResponse response = invitationService.inviteMembers(cubeId, request, invitedBy);

        return ResponseEntity.ok(response);
    }
}