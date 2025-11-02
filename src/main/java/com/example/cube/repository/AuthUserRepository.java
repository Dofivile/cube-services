package com.example.cube.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public interface AuthUserRepository extends JpaRepository<Object, UUID> {

    /**
     * Fetch user contact information (email, first_name, last_name) for multiple users.
     * Joins Supabase auth.users table with local user_details table.
     * Returns raw Map to avoid creating entity for auth schema.
     */
    @Query(value = """
        SELECT 
            au.id::text as user_id,
            au.email,
            ud.first_name,
            ud.last_name
        FROM auth.users au
        LEFT JOIN public.user_details ud ON au.id = ud.user_id
        WHERE au.id = ANY(:userIds)
        """, nativeQuery = true)
    List<Map<String, Object>> findUserDetailsByUserIds(@Param("userIds") UUID[] userIds);
}