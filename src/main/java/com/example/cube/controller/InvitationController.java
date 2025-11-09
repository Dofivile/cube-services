package com.example.cube.controller;

import com.example.cube.dto.request.JoinCubeRequest;
import com.example.cube.dto.response.JoinCubeResponse;
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

    @PostMapping("/join")
    public ResponseEntity<JoinCubeResponse> joinCubeByCode(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody JoinCubeRequest request) {

        UUID userId = authenticationService.validateAndExtractUserId(authHeader);
        JoinCubeResponse response = invitationService.joinCubeByCode(request.getInvitationCode(), userId);
        return ResponseEntity.ok(response);
    }
}

