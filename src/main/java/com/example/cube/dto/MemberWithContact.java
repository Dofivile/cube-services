// Create: src/main/java/com/example/cube/dto/MemberWithContact.java
package com.example.cube.dto;

import java.util.UUID;

public interface MemberWithContact {
    UUID getMemberId();
    UUID getCubeId();
    UUID getUserId();
    Integer getRoleId();
    Boolean getHasPaid();
    String getEmail();
    String getFirstName();
    String getLastName();

    // Computed property
    default String getFullName() {
        String first = getFirstName() != null ? getFirstName() : "";
        String last = getLastName() != null ? getLastName() : "";
        return (first + " " + last).trim();
    }
}