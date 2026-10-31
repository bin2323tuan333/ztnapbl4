package com.example.ztna_server.repository;

import com.example.ztna_server.entity.Rule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}