package com.example.cube.service.impl;

import com.example.cube.dto.UserInfoDTO;
import com.example.cube.repository.AuthUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private AuthUserRepository authUserRepository;

    /**
     * Fetch user information (email, first name, last name) for multiple users
     */
    public List<UserInfoDTO> getUserInfoWithEmails(UUID[] userIds) {
        return authUserRepository.findUserDetailsByUserIds(userIds)
                .stream()
                .map(row -> new UserInfoDTO(
                        UUID.fromString((String) row.get("user_id")),
                        (String) row.get("email"),
                        (String) row.get("first_name"),
                        (String) row.get("last_name")
                ))
                .collect(Collectors.toList());
    }

    /**
     * Get user info as a map with userId as key
     */
    public Map<UUID, UserInfoDTO> getUserInfoMap(UUID[] userIds) {
        return getUserInfoWithEmails(userIds).stream()
                .collect(Collectors.toMap(UserInfoDTO::getUserId, userInfo -> userInfo));
    }
}