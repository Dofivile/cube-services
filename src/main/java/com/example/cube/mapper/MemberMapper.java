package com.example.cube.mapper;

import com.example.cube.dto.response.GetCubeMembersResponse;
import com.example.cube.model.CubeMember;
import com.example.cube.model.UserDetails;
import com.example.cube.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handles conversions between CubeMember entities and response DTOs.
 */
@Component
public class MemberMapper {

    private final UserDetailsRepository userDetailsRepository;

    @Autowired
    public MemberMapper(UserDetailsRepository userDetailsRepository) {
        this.userDetailsRepository = userDetailsRepository;
    }

    /**
     * Converts a CubeMember entity to MemberInfo DTO
     */
    public GetCubeMembersResponse.MemberInfo toMemberInfo(CubeMember member) {
        GetCubeMembersResponse.MemberInfo info = new GetCubeMembersResponse.MemberInfo();

        info.setUserId(member.getUserId());
        info.setMemberId(member.getMemberId());
        info.setRoleName(member.getRoleId() == 1 ? "admin" : "member");
        info.setJoinedAt(member.getJoinedAt());
        info.setHasReceivedPayout(member.getHasReceivedPayout());
        info.setPayoutPosition(member.getPayoutPosition());

        // Map payment status
        info.setStatusId(member.getStatusId());
        info.setPaymentStatus(member.getStatusId() == 2 ? "Paid" : "Has Not Paid");

        // Populate names from user_details if available
        UserDetails ud = userDetailsRepository.findById(member.getUserId()).orElse(null);
        if (ud != null) {
            info.setFirstName(ud.getFirstName());
            info.setLastName(ud.getLastName());
        }

        return info;
    }
}