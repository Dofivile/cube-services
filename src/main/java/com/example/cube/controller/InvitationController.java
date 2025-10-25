package com.example.cube.controller;

import com.example.cube.dto.request.AcceptInvitationRequest;
import com.example.cube.dto.response.AcceptInvitationResponse;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.InvitationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/invitations")
public class InvitationController {

    private final InvitationService invitationService;
    private final AuthenticationService authenticationService;

    @Autowired
    public InvitationController(InvitationService invitationService,
                                AuthenticationService authenticationService) {
        this.invitationService = invitationService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/accept")
    public ResponseEntity<AcceptInvitationResponse> acceptInvitation(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody AcceptInvitationRequest request) {

        UUID userId = authenticationService.validateAndExtractUserId(authHeader);
        AcceptInvitationResponse response = invitationService.acceptInvitation(request.getInviteToken(), userId);
        return ResponseEntity.ok(response);
    }
}

