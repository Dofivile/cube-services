package com.example.cube.service;

import com.example.cube.model.Cube;
import com.example.cube.model.CubeMember;

import java.math.BigDecimal;
import java.util.UUID;

public interface EmailService {

    void sendInvitationEmail(String email, String inviteToken, String cubeName, UUID invitedBy);

    void sendWinnerNotificationEmails(Cube cube, CubeMember winner, BigDecimal payoutAmount);
}