package com.example.cube.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class AuthUserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Fetch user contact information (email, first_name, last_name) for multiple users.
     * Joins Supabase auth.users table with local user_details table.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> findUserDetailsByUserIds(UUID[] userIds) {
        String sql = """
            SELECT 
                au.id::text as user_id,
                au.email,
                ud.first_name,
                ud.last_name
            FROM auth.users au
            LEFT JOIN public.user_details ud ON au.id = ud.user_id
            WHERE au.id = ANY(:userIds)
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userIds", userIds);

        return query.getResultList();
    }
}