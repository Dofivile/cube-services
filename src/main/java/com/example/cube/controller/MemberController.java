package com.example.cube.controller;

import com.example.cube.dto.request.InviteMembersRequest;
import com.example.cube.dto.request.VerifyAdminRequest;
import com.example.cube.dto.response.GetCubeMembersResponse;
import com.example.cube.dto.response.InviteMembersResponse;
import com.example.cube.dto.response.VerifyAdminResponse;
import com.example.cube.mapper.MemberMapper;  // ✅ ADD
import com.example.cube.model.CubeMember;
import com.example.cube.repository.CubeMemberRepository;
import com.example.cube.security.AuthenticationService;
import com.example.cube.service.InvitationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final InvitationService invitationService;
    private final AuthenticationService authenticationService;
    private final CubeMemberRepository cubeMemberRepository;
    private final MemberMapper memberMapper;  // ✅ ADD

    @Autowired
    public MemberController(InvitationService invitationService,
                            AuthenticationService authenticationService,
                            CubeMemberRepository cubeMemberRepository,
                            MemberMapper memberMapper) {  // ✅ ADD
        this.invitationService = invitationService;
        this.authenticationService = authenticationService;
        this.cubeMemberRepository = cubeMemberRepository;
        this.memberMapper = memberMapper;  // ✅ ADD
    }

    @PostMapping("/invite")
    public ResponseEntity<InviteMembersResponse> inviteMembers(@Valid @RequestBody InviteMembersRequest request,
                                                               @RequestHeader("Authorization") String authHeader) {
        UUID invitedBy = authenticationService.validateAndExtractUserId(authHeader);
        UUID cubeId = request.getCubeId();

        InviteMembersResponse response = invitationService.inviteMembers(cubeId, request, invitedBy);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/cube/{cubeId}")
    public ResponseEntity<GetCubeMembersResponse> getCubeMembers(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID cubeId) {

        // Validate auth token
        UUID userId = authenticationService.validateAndExtractUserId(authHeader);

        // Get all members for this cube
        List<CubeMember> members = cubeMemberRepository.findByCubeId(cubeId);

        // ✅ REPLACE the entire stream with mapper call
        List<GetCubeMembersResponse.MemberInfo> memberInfoList = members.stream()
                .map(memberMapper::toMemberInfo)
                .toList();

        GetCubeMembersResponse response = new GetCubeMembersResponse(
                cubeId,
                memberInfoList.size(),
                memberInfoList
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-admin")
    public ResponseEntity<VerifyAdminResponse> verifyAdmin(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody VerifyAdminRequest request) {

        // Extract user ID from auth token
        UUID userId = authenticationService.validateAndExtractUserId(authHeader);

        // Query cube_members table by authenticated user_id and cube_id
        Optional<CubeMember> memberOpt = cubeMemberRepository.findByCubeIdAndUserId(
                request.getCubeId(),
                userId
        );

        // Check if member exists and if role_id = 1 (admin)
        boolean isAdmin = memberOpt.isPresent() && memberOpt.get().getRoleId() == 1;

        VerifyAdminResponse response = new VerifyAdminResponse(isAdmin);

        return ResponseEntity.ok(response);
    }
}