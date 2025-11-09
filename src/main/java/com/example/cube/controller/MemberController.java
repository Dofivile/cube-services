package com.example.cube.controller;

import com.example.cube.dto.request.InviteMembersRequest;
import com.example.cube.dto.request.VerifyAdminRequest;
import com.example.cube.dto.response.GetCubeMembersResponse;
import com.example.cube.dto.response.InviteMembersResponse;
import com.example.cube.dto.response.VerifyAdminResponse;
import com.example.cube.model.Cube;
import com.example.cube.model.CubeMember;
import com.example.cube.repository.CubeMemberRepository;
import com.example.cube.security.AuthenticationService;
import com.example.cube.repository.UserDetailsRepository;
import com.example.cube.model.UserDetails;
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

    private final InvitationService invitationService;  // ← Changed from MemberService
    private final AuthenticationService authenticationService;
    private final CubeMemberRepository cubeMemberRepository;
    private final UserDetailsRepository userDetailsRepository;

    @Autowired
    public MemberController(InvitationService invitationService,  // ← Changed
                            AuthenticationService authenticationService,
                            CubeMemberRepository cubeMemberRepository,
                            UserDetailsRepository userDetailsRepository) {
        this.invitationService = invitationService;
        this.authenticationService = authenticationService;
        this.cubeMemberRepository = cubeMemberRepository;
        this.userDetailsRepository = userDetailsRepository;
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

        // Map to response
        List<GetCubeMembersResponse.MemberInfo> memberInfoList = members.stream()
                .map(member -> {
                    GetCubeMembersResponse.MemberInfo info = new GetCubeMembersResponse.MemberInfo();
                    info.setUserId(member.getUserId());
                    info.setMemberId(member.getMemberId());
                    info.setRoleName(member.getRoleId() == 1 ? "admin" : "member");
                    info.setJoinedAt(member.getJoinedAt());
                    info.setHasReceivedPayout(member.getHasReceivedPayout());
                    info.setPayoutPosition(member.getPayoutPosition());
                    
                    // ✅ ADD: Map payment status
                    info.setStatusId(member.getStatusId());
                    info.setPaymentStatus(member.getStatusId() == 2 ? "paid" : "has not paid");
                    
                    // Populate names from user_details if available
                    UserDetails ud = userDetailsRepository.findById(member.getUserId()).orElse(null);
                    if (ud != null) {
                        info.setFirstName(ud.getFirstName());
                        info.setLastName(ud.getLastName());
                    }
                    return info;
                })
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

        // ✅ Extract user ID from auth token
        UUID userId = authenticationService.validateAndExtractUserId(authHeader);

        // Query cube_members table by authenticated user_id and cube_id
        Optional<CubeMember> memberOpt = cubeMemberRepository.findByCubeIdAndUserId(
                request.getCubeId(),
                userId  // ✅ Use authenticated user's ID
        );

        // Check if member exists and if role_id = 1 (admin)
        boolean isAdmin = memberOpt.isPresent() && memberOpt.get().getRoleId() == 1;

        VerifyAdminResponse response = new VerifyAdminResponse(isAdmin);

        return ResponseEntity.ok(response);
    }
}
