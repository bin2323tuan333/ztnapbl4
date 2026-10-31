package com.example.ztna_server.mapper;

import com.example.ztna_server.dto.internal.ResourceInfo;
import com.example.ztna_server.dto.internal.UserInfo;
import com.example.ztna_server.dto.request.rule.RuleCreationRequest;
import com.example.ztna_server.dto.response.rule.RuleDetailResponse;
import com.example.ztna_server.dto.response.rule.RuleSummaryResponse;
import com.example.ztna_server.entity.Resource;
import com.example.ztna_server.entity.Rule;
import com.example.ztna_server.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RuleMapper {

    @Mapping(target = "userCount", expression = "java(rule.getUsers() != null ? rule.getUsers().size() : 0)")
    @Mapping(target = "resourceCount", expression = "java(rule.getResources() != null ? rule.getResources().size() : 0)")
    RuleSummaryResponse toSummaryResponse(Rule rule);

    RuleDetailResponse toDetailResponse(Rule rule);

    UserInfo toUserInfo(User user);
    ResourceInfo toResourceInfo(Resource resource);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "resources", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Rule toEntity(RuleCreationRequest request);
}