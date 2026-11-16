package com.example.ztna_server.repository;

import com.example.ztna_server.entity.Rule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RuleRepository extends JpaRepository<Rule, UUID> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID id);

    @Override
    @EntityGraph(attributePaths = {"users", "resources"})
    List<Rule> findAll();

    @Override
    @EntityGraph(attributePaths = {"users", "resources"})
    Optional<Rule> findById(UUID id);

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM Rule r
        JOIN r.users u
        JOIN r.resources res
        WHERE u.id = :userId
          AND res.id = :resourceId
          AND r.enabled = true
          AND (r.expiresAt IS NULL OR r.expiresAt > :now)
    """)
    boolean hasAccess(@Param("userId") UUID userId, @Param("resourceId") UUID resourceId, @Param("now") Instant now);
}