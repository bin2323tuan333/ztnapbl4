package com.example.ztna_server.controller;

import com.example.ztna_server.dto.request.rule.RuleCreationRequest;
import com.example.ztna_server.dto.request.rule.RuleResourceAssignmentRequest;
import com.example.ztna_server.dto.request.rule.RuleUserAssignmentRequest;
import com.example.ztna_server.dto.response.rule.RuleDetailResponse;
import com.example.ztna_server.dto.response.rule.RuleSummaryResponse;
import com.example.ztna_server.service.RuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/rules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RuleController {

    private final RuleService ruleService;

    @GetMapping
    public ResponseEntity<List<RuleSummaryResponse>> getAllRules() {
        return ResponseEntity.ok(ruleService.getAllRules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RuleDetailResponse> getRuleDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(ruleService.getRuleDetail(id));
    }

    @PostMapping
    public ResponseEntity<RuleDetailResponse> createRule(@Valid @RequestBody RuleCreationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ruleService.createRule(request));
    }

    @PutMapping("/{id}/users")
    public ResponseEntity<Void> updateUsers(
            @PathVariable UUID id,
            @RequestBody RuleUserAssignmentRequest request) {
        ruleService.updateRuleUsers(id, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/resources")
    public ResponseEntity<Void> updateResources(
            @PathVariable UUID id,
            @RequestBody RuleResourceAssignmentRequest request) {
        ruleService.updateRuleResources(id, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleStatus(@PathVariable UUID id) {
        ruleService.toggleStatus(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID id) {
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}