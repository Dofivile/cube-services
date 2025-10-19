package com.example.cube.service;

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
}