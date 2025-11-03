package com.example.cube.service;

import com.example.cube.model.Cube;
import com.example.cube.model.CubeMember;

import java.math.BigDecimal;
import java.util.UUID;

public interface EmailService {

    /**
     * Send invitation email to the invitee
     *
     * @param email The recipient's email address
     * @param inviteToken The unique invitation token
     * @param cubeName The name of the cube they're being invited to
     * @param invitedBy The UUID of the user who sent the invitation
     */
    void sendInvitationEmail(String email, String inviteToken, String cubeName, UUID invitedBy);

    /**
     * Send winner notification emails to all cube members and admin
     *
     * @param cube The cube where winner was selected
     * @param winner The winning member
     * @param payoutAmount The payout amount for this cycle
     */
    void sendWinnerNotificationEmails(Cube cube, CubeMember winner, BigDecimal payoutAmount);
}