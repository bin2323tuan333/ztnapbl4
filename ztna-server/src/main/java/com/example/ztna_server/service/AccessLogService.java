package com.example.ztna_server.service;

import com.example.ztna_server.dto.response.AccessLogResponse;
import com.example.ztna_server.entity.AccessLog;
import com.example.ztna_server.enums.Decision;
import com.example.ztna_server.repository.AccessLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessLogService {
    private final AccessLogRepository accessLogRepository;

    @Transactional
    public void log(UUID userId,
                    String userIp,
                    String userName,
                    UUID resourceId,
                    String resourceName,
                    String resourceHost,
                    UUID connectorId,
                    String connectorName,
                    Decision decision,
                    String reason) {

        AccessLog log = AccessLog.builder()
                .userId(userId)
                .userIp(userIp)
                .userName(userName)
                .resourceId(resourceId)
                .resourceName(resourceName)
                .resourceHost(resourceHost)
                .connectorId(connectorId)
                .connectorName(connectorName)
                .decision(decision)
                .reason(reason)
                .build();

        accessLogRepository.save(log);
    }


//    @Transactional(readOnly = true)
//    public Page<AccessLogResponse> search(String username,
//                                          String resourceName,
//                                          Decision decision,
//                                          Instant from,
//                                          Instant to,
//                                          Pageable pageable) {
//
//        return accessLogRepository
//                .search(username, resourceName, decision, from, to, pageable)
//                .map(this::toResponse);
//    }

//    private AccessLogResponse toResponse(AccessLog log) {
//        return AccessLogResponse.builder()
//                .id(log.getId())
//                .requestId(log.getRequestId())
//                .userId(log.getUserId())
//                .username(log.getUsername())
//                .resourceId(log.getResourceId())
//                .resourceName(log.getResourceName())
//                .connectorId(log.getConnectorId())
//                .path(log.getPath())
//                .method(log.getMethod())
//                .clientIp(log.getClientIp())
//                .decision(log.getDecision())
//                .reason(log.getReason())
//                .createdAt(log.getCreatedAt())
//                .build();
//    }
}