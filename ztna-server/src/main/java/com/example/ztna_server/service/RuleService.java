package com.example.ztna_server.service;

import com.example.ztna_server.dto.request.rule.RuleCreationRequest;
import com.example.ztna_server.dto.request.rule.RuleResourceAssignmentRequest;
import com.example.ztna_server.dto.request.rule.RuleUserAssignmentRequest;
import com.example.ztna_server.dto.response.rule.RuleDetailResponse;
import com.example.ztna_server.dto.response.rule.RuleSummaryResponse;
import com.example.ztna_server.entity.Resource;
import com.example.ztna_server.entity.Rule;
import com.example.ztna_server.entity.User;
import com.example.ztna_server.mapper.RuleMapper;
import com.example.ztna_server.repository.ResourceRepository;
import com.example.ztna_server.repository.RuleRepository;
import com.example.ztna_server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final RuleRepository ruleRepository;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final RuleMapper ruleMapper;

    @Transactional(readOnly = true)
    public List<RuleSummaryResponse> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(ruleMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RuleDetailResponse getRuleDetail(UUID id) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule không tồn tại"));
        return ruleMapper.toDetailResponse(rule);
    }

    @Transactional
    public RuleDetailResponse createRule(RuleCreationRequest request) {
        if (ruleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên Rule đã tồn tại!");
        }

        Rule rule = ruleMapper.toEntity(request);
        rule.setEnabled(true);
        rule.setCreatedAt(Instant.now());
        rule.setUpdatedAt(Instant.now());

        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            rule.setUsers(new HashSet<>(userRepository.findAllById(request.getUserIds())));
        }
        if (request.getResourceIds() != null && !request.getResourceIds().isEmpty()) {
            rule.setResources(new HashSet<>(resourceRepository.findAllById(request.getResourceIds())));
        }

        return ruleMapper.toDetailResponse(ruleRepository.save(rule));
    }

    @Transactional
    public void updateRuleUsers(UUID id, RuleUserAssignmentRequest request) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule không tồn tại"));

        Set<User> newUsers = (request.getUserIds() == null || request.getUserIds().isEmpty())
                ? new HashSet<>()
                : new HashSet<>(userRepository.findAllById(request.getUserIds()));

        rule.setUsers(newUsers);
        rule.setUpdatedAt(Instant.now());
        ruleRepository.save(rule);
    }

    @Transactional
    public void updateRuleResources(UUID id, RuleResourceAssignmentRequest request) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule không tồn tại"));

        Set<Resource> newResources = (request.getResourceIds() == null || request.getResourceIds().isEmpty())
                ? new HashSet<>()
                : new HashSet<>(resourceRepository.findAllById(request.getResourceIds()));

        rule.setResources(newResources);
        rule.setUpdatedAt(Instant.now());
        ruleRepository.save(rule);
    }

    @Transactional
    public void toggleStatus(UUID id) {
        Rule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rule không tồn tại"));

        rule.setEnabled(!rule.isEnabled());
        rule.setUpdatedAt(Instant.now());
        ruleRepository.save(rule);
    }

    @Transactional
    public void deleteRule(UUID id) {
        if (!ruleRepository.existsById(id)) {
            throw new RuntimeException("Rule không tồn tại");
        }
        ruleRepository.deleteById(id);
    }
}