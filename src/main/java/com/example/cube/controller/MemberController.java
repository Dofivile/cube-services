package com.example.cube.controller;

import com.example.cube.dto.request.InviteMembersRequestDTO;
import com.example.cube.dto.response.InviteMembersResponseDTO;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cubes")
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
     * POST /api/cubes/{cubeId}/members/invite
     * Invite members to a cube
     */
    @PostMapping("/{cubeId}/members/invite")
    public ResponseEntity<InviteMembersResponseDTO> inviteMembers(@PathVariable UUID cubeId, @Valid @RequestBody InviteMembersRequestDTO request,
            @RequestHeader("Authorization") String authHeader) {

        UUID invitedBy = authenticationService.validateAndExtractUserId(authHeader);
        InviteMembersResponseDTO response = memberService.inviteMembers(cubeId, request, invitedBy);
        return ResponseEntity.ok(response);
    }
}