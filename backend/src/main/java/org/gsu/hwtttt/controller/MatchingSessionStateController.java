package org.gsu.hwtttt.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.gsu.hwtttt.common.result.Result;
import org.gsu.hwtttt.dto.request.StatusTransitionRequest;
import org.gsu.hwtttt.dto.response.StatusHistoryResponse;
import org.gsu.hwtttt.entity.MatchingSessions;
import org.gsu.hwtttt.entity.MatchingSessionHistory;
import org.gsu.hwtttt.service.MatchingSessionStateService;
import org.gsu.hwtttt.service.MatchingSessionsService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 配伍会话状态管理 REST API Controller
 * 实现状态转换、历史查询和状态验证功能
 * 
 * @author WenXin
 * @date 2025/01/07
 */
@Slf4j
@RestController
@RequestMapping("/api/matching/sessions")
@Api(tags = "Matching Session State Management")
@RequiredArgsConstructor
@Validated
public class MatchingSessionStateController {

    private final MatchingSessionStateService stateService;
    private final MatchingSessionsService sessionService;

    // ==================== 状态转换管理 ====================

    @PostMapping("/{sessionId}/status")
    @ApiOperation("Change session status")
    public Result<Map<String, Object>> changeSessionStatus(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId,
            @Valid @RequestBody StatusTransitionRequest request) {
        log.info("Changing session status: sessionId={}, toStatus={}", sessionId, request.getToStatus());

        try {
            boolean success;
            if (Boolean.TRUE.equals(request.getForceChange())) {
                success = stateService.forceSetStatus(sessionId, request.getToStatus(), 
                    request.getChangeReason(), request.getChangeUser());
            } else {
                success = stateService.changeSessionStatus(sessionId, request.getToStatus(), 
                    request.getChangeReason(), request.getChangeUser());
            }

            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("sessionId", sessionId);
                response.put("newStatus", request.getToStatus());
                response.put("changeReason", request.getChangeReason());
                response.put("changeUser", request.getChangeUser());
                response.put("forceChange", request.getForceChange());

                return Result.success("Status changed successfully", response);
            } else {
                return Result.fail("Failed to change status");
            }
        } catch (Exception e) {
            log.error("Status change failed: sessionId={}, toStatus={}", sessionId, request.getToStatus(), e);
            return Result.fail("Status change failed: " + e.getMessage());
        }
    }

    @PostMapping("/{sessionId}/rollback")
    @ApiOperation("Rollback session to previous status")
    public Result<Map<String, Object>> rollbackSessionStatus(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId,
            @ApiParam("Rollback reason") @RequestParam(required = false) String reason,
            @ApiParam("Change user") @RequestParam(required = false) String changeUser) {
        log.info("Rolling back session status: sessionId={}", sessionId);

        try {
            boolean success = stateService.rollbackToPreviousState(sessionId, reason, changeUser);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("sessionId", sessionId);
                response.put("rollbackReason", reason);
                response.put("changeUser", changeUser);

                return Result.success("Status rollback successful", response);
            } else {
                return Result.fail("Rollback failed - no previous state available");
            }
        } catch (Exception e) {
            log.error("Status rollback failed: sessionId={}", sessionId, e);
            return Result.fail("Rollback failed: " + e.getMessage());
        }
    }

    // ==================== 状态历史查询 ====================

    @GetMapping("/{sessionId}/status/history")
    @ApiOperation("Get session status history")
    public Result<StatusHistoryResponse> getSessionStatusHistory(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId) {
        log.info("Getting session status history: sessionId={}", sessionId);

        try {
            MatchingSessions session = sessionService.getSessionById(sessionId);
            if (session == null) {
                return Result.fail("Session not found");
            }

            List<MatchingSessionHistory> history = stateService.getSessionStateHistory(sessionId);
            StatusHistoryResponse response = buildStatusHistoryResponse(session, history);

            return Result.success(response);
        } catch (Exception e) {
            log.error("Get status history failed: sessionId={}", sessionId, e);
            return Result.fail("Get status history failed: " + e.getMessage());
        }
    }

    @GetMapping("/{sessionId}/status/current")
    @ApiOperation("Get current status and valid transitions")
    public Result<StatusHistoryResponse.StatusTransitionInfo> getCurrentStatusInfo(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId) {
        log.info("Getting current status info: sessionId={}", sessionId);

        try {
            MatchingSessions session = sessionService.getSessionById(sessionId);
            if (session == null) {
                return Result.fail("Session not found");
            }

            String currentStatus = session.getStatus();
            Set<String> validTransitions = stateService.getValidTransitions(currentStatus);

            StatusHistoryResponse.StatusTransitionInfo response = new StatusHistoryResponse.StatusTransitionInfo();
            response.setCurrentStatus(currentStatus);
            response.setValidTransitions(new ArrayList<>(validTransitions));
            response.setStatusDescription(getStatusDescription(currentStatus));
            response.setIsFinalState(validTransitions.isEmpty());

            return Result.success(response);
        } catch (Exception e) {
            log.error("Get current status info failed: sessionId={}", sessionId, e);
            return Result.fail("Get current status info failed: " + e.getMessage());
        }
    }

    // ==================== 状态验证和查询 ====================

    @GetMapping("/status/valid-transitions")
    @ApiOperation("Get valid status transitions")
    public Result<Map<String, List<String>>> getValidStatusTransitions() {
        log.info("Getting valid status transitions");

        try {
            Set<String> allStatuses = stateService.getAllValidStatuses();
            Map<String, List<String>> transitions = new HashMap<>();

            for (String status : allStatuses) {
                Set<String> validTransitions = stateService.getValidTransitions(status);
                transitions.put(status, new ArrayList<>(validTransitions));
            }

            return Result.success(transitions);
        } catch (Exception e) {
            log.error("Get valid transitions failed", e);
            return Result.fail("Get valid transitions failed: " + e.getMessage());
        }
    }

    @GetMapping("/status/validate")
    @ApiOperation("Validate status transition")
    public Result<Map<String, Object>> validateStatusTransition(
            @ApiParam("From status") @RequestParam String fromStatus,
            @ApiParam("To status") @RequestParam String toStatus) {
        log.info("Validating status transition: {} -> {}", fromStatus, toStatus);

        try {
            boolean isValid = stateService.isValidStateTransition(fromStatus, toStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("fromStatus", fromStatus);
            response.put("toStatus", toStatus);
            response.put("isValid", isValid);
            
            if (!isValid) {
                Set<String> validTransitions = stateService.getValidTransitions(fromStatus);
                response.put("validTransitions", validTransitions);
                response.put("message", "Invalid transition. Valid transitions: " + validTransitions);
            }

            return Result.success(response);
        } catch (Exception e) {
            log.error("Validate transition failed: {} -> {}", fromStatus, toStatus, e);
            return Result.fail("Validation failed: " + e.getMessage());
        }
    }

    // ==================== 状态统计 ====================

    @GetMapping("/status/statistics")
    @ApiOperation("Get status transition statistics")
    public Result<Map<String, Object>> getStatusStatistics(
            @ApiParam("From status (optional)") @RequestParam(required = false) String fromStatus,
            @ApiParam("To status (optional)") @RequestParam(required = false) String toStatus) {
        log.info("Getting status statistics: fromStatus={}, toStatus={}", fromStatus, toStatus);

        try {
            Map<String, Object> statistics = new HashMap<>();
            
            if (fromStatus != null && toStatus != null) {
                Integer count = stateService.getStateTransitionCount(fromStatus, toStatus);
                statistics.put("transitionCount", count);
                statistics.put("fromStatus", fromStatus);
                statistics.put("toStatus", toStatus);
            } else {
                // 获取所有状态的统计信息
                Set<String> allStatuses = stateService.getAllValidStatuses();
                Map<String, Map<String, Integer>> transitionMatrix = new HashMap<>();
                
                for (String from : allStatuses) {
                    Map<String, Integer> fromTransitions = new HashMap<>();
                    for (String to : allStatuses) {
                        Integer count = stateService.getStateTransitionCount(from, to);
                        if (count > 0) {
                            fromTransitions.put(to, count);
                        }
                    }
                    if (!fromTransitions.isEmpty()) {
                        transitionMatrix.put(from, fromTransitions);
                    }
                }
                
                statistics.put("transitionMatrix", transitionMatrix);
                statistics.put("allStatuses", allStatuses);
            }

            return Result.success(statistics);
        } catch (Exception e) {
            log.error("Get status statistics failed", e);
            return Result.fail("Get statistics failed: " + e.getMessage());
        }
    }

    // ==================== Helper Methods ====================

    private StatusHistoryResponse buildStatusHistoryResponse(MatchingSessions session, List<MatchingSessionHistory> history) {
        StatusHistoryResponse response = new StatusHistoryResponse();
        response.setSessionId(session.getId());
        response.setSessionName(session.getSessionName());
        response.setCurrentStatus(session.getStatus());
        response.setTotalChanges(history.size());

        List<StatusHistoryResponse.StatusChangeDetail> details = new ArrayList<>();
        for (int i = 0; i < history.size(); i++) {
            MatchingSessionHistory h = history.get(i);
            StatusHistoryResponse.StatusChangeDetail detail = new StatusHistoryResponse.StatusChangeDetail();
            detail.setId(h.getId());
            detail.setFromStatus(h.getFromStatus());
            detail.setToStatus(h.getToStatus());
            detail.setChangeReason(h.getChangeReason());
            detail.setChangeTime(h.getChangeTime());
            detail.setChangeUser(h.getChangeUser());

            // 计算状态持续时长
            if (i < history.size() - 1) {
                MatchingSessionHistory next = history.get(i + 1);
                Duration duration = Duration.between(next.getChangeTime(), h.getChangeTime());
                detail.setDurationSeconds(duration.getSeconds());
            }

            details.add(detail);
        }

        response.setStatusHistory(details);
        return response;
    }

    private String getStatusDescription(String status) {
        switch (status) {
            case "draft": return "草稿状态";
            case "waste_selected": return "废物已选择";
            case "compatibility_checking": return "相容性检查中";
            case "compatible": return "相容";
            case "incompatible": return "不相容";
            case "calculating": return "计算中";
            case "calculation_success": return "计算成功";
            case "calculation_failed": return "计算失败";
            case "archived": return "已归档";
            case "completed": return "已完成（旧状态）";
            case "failed": return "失败（旧状态）";
            default: return "未知状态";
        }
    }
} 