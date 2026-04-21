package org.gsu.hwtttt.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.gsu.hwtttt.common.result.Result;
import org.gsu.hwtttt.dto.request.MatchingRequest;
import org.gsu.hwtttt.dto.response.MatchingResponse;
import org.gsu.hwtttt.entity.MatchingSessions;
import org.gsu.hwtttt.entity.MatchingDetails;
import org.gsu.hwtttt.entity.MatchingResults;
import org.gsu.hwtttt.entity.HazardousWaste;
import org.gsu.hwtttt.service.MatchingService;
import org.gsu.hwtttt.service.MatchingSessionsService;
import org.gsu.hwtttt.service.HazardousWasteService;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Module 4: Compatibility Simulation Controller
 * Unified API endpoints following /api/matching pattern
 * Supports all 4 stages: Import → Compatibility → Calculation → Results
 *
 * @author WenXin
 * @date 2025/01/07
 */
@Slf4j
@RestController
@RequestMapping("/api/matching")
@Api(tags = "Module 4: Compatibility Simulation")
@RequiredArgsConstructor
@Validated
public class MatchingController {

    private final MatchingService matchingService;
    private final MatchingSessionsService matchingSessionsService;
    private final HazardousWasteService hazardousWasteService;

    // ==================== 1. Session Management APIs ====================

    @PostMapping("/sessions")
    @ApiOperation("Create new matching session")
    public Result<Map<String, Object>> createSession(@Valid @RequestBody MatchingRequest request) {
        log.info("Creating new matching session: {}", request.getSessionName());
        
        MatchingSessions session = new MatchingSessions();
        session.setSessionName(request.getSessionName());
        session.setTargetHeatValue(request.getTargetHeatValue());
        session.setTotalAmount(request.getTotalAmount());
        session.setCreateUser(request.getCreateUser());
        
        MatchingSessions createdSession = matchingSessionsService.createSession(session);
        
        Map<String, Object> response = Map.of(
            "sessionId", createdSession.getId(),
            "sessionName", createdSession.getSessionName(),
            "status", createdSession.getStatus(),
            "createTime", createdSession.getCreateTime()
        );
        
        return Result.success("Session created successfully", response);
    }

    @GetMapping("/sessions")
    @ApiOperation("Get user's session list with pagination")
    public Result<List<MatchingSessions>> getUserSessions(
            @ApiParam("Username") @RequestParam(required = false) String username,
            @ApiParam("Page number") @RequestParam(defaultValue = "1") Integer pageNo,
            @ApiParam("Page size") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("Getting user sessions: username={}, pageNo={}, pageSize={}", username, pageNo, pageSize);
        
        List<MatchingSessions> sessions = matchingSessionsService.getSessionHistoryList(username, pageNo, pageSize);
        return Result.success(sessions);
    }

    @GetMapping("/sessions/{sessionId}")
    @ApiOperation("Get session details")
    public Result<Map<String, Object>> getSessionDetails(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId) {
        log.info("Getting session details: sessionId={}", sessionId);
        
        MatchingSessions session = matchingSessionsService.getSessionById(sessionId);
        if (session == null) {
            return Result.fail("Session not found");
        }
        
        // Get session progress
        Map<String, Object> progress = matchingService.getSessionProgress(sessionId);
        
        Map<String, Object> response = Map.of(
            "session", session,
            "progress", progress
        );
        
        return Result.success(response);
    }

    @PutMapping("/sessions/{sessionId}")
    @ApiOperation("Update session information")
    public Result<Boolean> updateSession(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId,
            @Valid @RequestBody MatchingRequest request) {
        log.info("Updating session: sessionId={}", sessionId);
        
        boolean success = matchingSessionsService.updateSession(sessionId, request);
        return Result.success(success);
    }

    @DeleteMapping("/sessions/{sessionId}")
    @ApiOperation("Delete session")
    public Result<Boolean> deleteSession(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId) {
        log.info("Deleting session: sessionId={}", sessionId);
        
        boolean success = matchingSessionsService.deleteSession(sessionId);
        return Result.success(success);
    }

    @GetMapping("/sessions/{sessionId}/status")
    @ApiOperation("Get session status and progress")
    public Result<Map<String, Object>> getSessionStatus(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId) {
        log.info("Getting session status: sessionId={}", sessionId);
        
        MatchingSessions session = matchingSessionsService.getSessionById(sessionId);
        if (session == null) {
            return Result.fail("Session not found");
        }
        
        Map<String, Object> status = Map.of(
            "sessionId", sessionId,
            "status", session.getStatus(),
            "progress", getProgressPercentage(session.getStatus()),
            "estimatedTime", getEstimatedTime(session.getStatus()),
            "lastUpdateTime", session.getUpdateTime()
        );
        
        return Result.success(status);
    }

    @PostMapping("/sessions/{sessionId}/reset")
    @ApiOperation("Reset session to draft state")
    public Result<Boolean> resetSession(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId,
            @ApiParam("Reset reason") @RequestParam(required = false) String reason) {
        log.info("Resetting session: sessionId={}, reason={}", sessionId, reason);
        
        boolean success = matchingSessionsService.updateSessionStatusWithHistory(sessionId, "draft", 
            reason != null ? reason : "Session reset by user", "system");
        return Result.success(success);
    }

    // ==================== 2. Import Stage APIs ====================

    @GetMapping("/wastes/search")
    @ApiOperation("Search hazardous wastes for import")
    public Result<List<Map<String, Object>>> searchWastes(
            @ApiParam("Search keyword or waste code") @RequestParam @NotBlank(message = "Search keyword cannot be empty") String keyword,
            @ApiParam("Page number") @RequestParam(defaultValue = "1") Integer pageNo,
            @ApiParam("Page size") @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("Searching wastes for import: keyword={}", keyword);
        
        List<Map<String, Object>> availableWastes = matchingService.searchAvailableWastes(keyword);
        return Result.success(availableWastes);
    }

    @GetMapping("/wastes/{wasteId}")
    @ApiOperation("Get waste details for import")
    public Result<HazardousWaste> getWasteDetails(
            @ApiParam("Waste ID") @PathVariable @NotNull @Min(value = 1, message = "Waste ID must be greater than 0") Long wasteId) {
        log.info("Getting waste details: wasteId={}", wasteId);
        
        HazardousWaste waste = hazardousWasteService.getById(wasteId);
        if (waste == null) {
            return Result.fail("Waste not found");
        }
        
        return Result.success(waste);
    }

    @PostMapping("/sessions/{sessionId}/wastes")
    @ApiOperation("Add waste to session")
    public Result<Map<String, Object>> addWasteToSession(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId,
            @ApiParam("Waste ID") @RequestParam @NotNull Long wasteId,
            @ApiParam("Planned amount (kg)") @RequestParam @NotNull BigDecimal plannedAmount) {
        log.info("Adding waste to session: sessionId={}, wasteId={}, plannedAmount={}", sessionId, wasteId, plannedAmount);
        
        // Check stock availability
        Map<String, Object> stockCheck = matchingService.checkWasteStock(wasteId, plannedAmount);
        if (!(Boolean) stockCheck.get("sufficient")) {
            return Result.fail("Insufficient stock! Remaining: " + stockCheck.get("remainingStock") + " kg, Required: " + plannedAmount + " kg");
        }
        
        // Add to session
        Result<Map<String, Object>> serviceResult = matchingService.addWasteToSession(sessionId, wasteId, plannedAmount.doubleValue());
        
        if (serviceResult.getSuccess()) {
            // Update session status to waste_selected
            matchingSessionsService.updateSessionStatusWithHistory(sessionId, "waste_selected", 
                "Added waste to session", "system");
            
            // Enhance response data
            Map<String, Object> responseData = new HashMap<>(serviceResult.getData());
            responseData.put("remainingStock", stockCheck.get("remainingStock"));
            responseData.put("stockSufficient", true);
            
            return Result.success(serviceResult.getMessage(), responseData);
        } else {
            return Result.fail(serviceResult.getMessage());
        }
    }

    @GetMapping("/sessions/{sessionId}/wastes")
    @ApiOperation("Get wastes in session")
    public Result<List<MatchingDetails>> getSessionWastes(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId) {
        log.info("Getting wastes in session: sessionId={}", sessionId);
        
        List<MatchingDetails> sessionWastes = matchingService.getSessionWastes(sessionId);
        return Result.success(sessionWastes);
    }

    @PutMapping("/sessions/{sessionId}/wastes/{wasteId}")
    @ApiOperation("Update waste quantity in session")
    public Result<Boolean> updateWasteQuantity(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId,
            @ApiParam("Waste ID") @PathVariable @NotNull Long wasteId,
            @ApiParam("New planned amount (kg)") @RequestParam @NotNull BigDecimal plannedAmount) {
        log.info("Updating waste quantity: sessionId={}, wasteId={}, plannedAmount={}", sessionId, wasteId, plannedAmount);
        
        // Check stock availability
        Map<String, Object> stockCheck = matchingService.checkWasteStock(wasteId, plannedAmount);
        if (!(Boolean) stockCheck.get("sufficient")) {
            return Result.fail("Insufficient stock! Remaining: " + stockCheck.get("remainingStock") + " kg");
        }
        
        boolean success = matchingService.updateWasteQuantity(sessionId, wasteId, plannedAmount.doubleValue());
        return Result.success(success);
    }

    @DeleteMapping("/sessions/{sessionId}/wastes/{wasteId}")
    @ApiOperation("Remove waste from session")
    public Result<Boolean> removeWasteFromSession(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId,
            @ApiParam("Waste ID") @PathVariable @NotNull Long wasteId) {
        log.info("Removing waste from session: sessionId={}, wasteId={}", sessionId, wasteId);
        
        boolean success = matchingService.removeWasteFromSession(sessionId, wasteId);
        return Result.success(success);
    }

    @PostMapping("/sessions/{sessionId}/wastes/validate")
    @ApiOperation("Validate waste quantities in session")
    public Result<Map<String, Object>> validateWasteQuantities(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId) {
        log.info("Validating waste quantities: sessionId={}", sessionId);
        
        Map<String, Object> validation = matchingService.validateSessionWastes(sessionId);
        return Result.success(validation);
    }

    @GetMapping("/sessions/{sessionId}/summary")
    @ApiOperation("Get comprehensive session summary")
    public Result<org.gsu.hwtttt.dto.response.SessionSummaryResponse> getSessionSummary(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId) {
        log.info("Getting session summary: sessionId={}", sessionId);
        
        org.gsu.hwtttt.dto.response.SessionSummaryResponse summary = matchingService.getSessionSummary(sessionId);
        return Result.success(summary);
    }

    // ==================== 3. Compatibility Check APIs ====================

    @PostMapping("/sessions/{sessionId}/compatibility/check")
    @ApiOperation("Start compatibility check for session wastes")
    public Result<Map<String, Object>> startCompatibilityCheck(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId) {
        log.info("Starting compatibility check: sessionId={}", sessionId);
        
        // Update session status to compatibility_checking
        matchingSessionsService.updateSessionStatusWithHistory(sessionId, "compatibility_checking", 
            "Starting compatibility check", "system");
        
        try {
            Map<String, Object> compatibilityResult = matchingService.performCompatibilityCheck(sessionId);
            
            // Update session status based on result
            String status = (Boolean) compatibilityResult.get("compatible") ? "compatible" : "incompatible";
            String reason = (Boolean) compatibilityResult.get("compatible") ? 
                "Compatibility check passed" : "Compatibility check failed: " + compatibilityResult.get("reason");
            matchingSessionsService.updateSessionStatusWithHistory(sessionId, status, reason, "system");
            
            return Result.success(compatibilityResult);
        } catch (Exception e) {
            log.error("Compatibility check failed: {}", e.getMessage(), e);
            matchingSessionsService.updateSessionStatusWithHistory(sessionId, "incompatible", 
                "Compatibility check failed due to error: " + e.getMessage(), "system");
            return Result.fail("Compatibility check failed: " + e.getMessage());
        }
    }

    @GetMapping("/sessions/{sessionId}/compatibility/status")
    @ApiOperation("Get compatibility check status")
    public Result<Map<String, Object>> getCompatibilityCheckStatus(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId) {
        log.info("Getting compatibility check status: sessionId={}", sessionId);
        
        MatchingSessions session = matchingSessionsService.getSessionById(sessionId);
        if (session == null) {
            return Result.fail("Session not found");
        }
        
        Map<String, Object> status = Map.of(
            "sessionId", sessionId,
            "status", session.getStatus(),
            "isChecking", "compatibility_checking".equals(session.getStatus()),
            "isCompatible", "compatible".equals(session.getStatus()),
            "isIncompatible", "incompatible".equals(session.getStatus())
        );
        
        return Result.success(status);
    }

    @GetMapping("/sessions/{sessionId}/compatibility/result")
    @ApiOperation("Get compatibility check result")
    public Result<Map<String, Object>> getCompatibilityCheckResult(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId) {
        log.info("Getting compatibility check result: sessionId={}", sessionId);
        
        Map<String, Object> result = matchingService.getCompatibilityCheckResult(sessionId);
        return Result.success(result);
    }

    @GetMapping("/sessions/{sessionId}/compatibility/details")
    @ApiOperation("Get detailed compatibility analysis")
    public Result<Map<String, Object>> getCompatibilityDetails(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId) {
        log.info("Getting compatibility details: sessionId={}", sessionId);
        
        Map<String, Object> details = Map.of(
            "matrix", matchingService.getCompatibilityMatrix(sessionId),
            "analysis", matchingService.getCompatibilityAnalysis(sessionId),
            "riskAssessment", matchingService.getRiskAssessment(sessionId)
        );
        
        return Result.success(details);
    }

    // ==================== 4. Calculation APIs ====================

    @PostMapping("/sessions/{sessionId}/calculate")
    @ApiOperation("Start matching calculation using linear programming")
    public Result<Map<String, Object>> startMatchingCalculation(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId) {
        log.info("Starting matching calculation: sessionId={}", sessionId);
        
        // Check if session exists
        MatchingSessions session = matchingSessionsService.getSessionById(sessionId);
        if (session == null) {
            return Result.fail("Session not found");
        }
        
        // Allow calculation from multiple states for better user experience
        String currentStatus = session.getStatus();
        if (!isValidCalculationState(currentStatus)) {
            return Result.fail("Cannot start calculation from current status: " + currentStatus + 
                ". Calculation is allowed from: compatible, calculation_failed, incompatible (with manual override)");
        }
        
        // Handle retry scenarios with appropriate logging
        if ("calculation_failed".equals(currentStatus)) {
            log.info("Retrying calculation for session {} (previous attempt failed)", sessionId);
        } else if ("incompatible".equals(currentStatus)) {
            log.info("Starting calculation for session {} with manual override (incompatible wastes)", sessionId);
        }
        
        // Update session status to calculating
        String reason = buildCalculationStartReason(currentStatus);
        matchingSessionsService.updateSessionStatusWithHistory(sessionId, "calculating", reason, "system");
        
        try {
            // Execute matching calculation
            MatchingResponse response = matchingService.executeMatching(sessionId);
            
            // Update session status based on result
            String status = response.getSuccess() ? "calculation_success" : "calculation_failed";
            String statusReason = response.getSuccess() ? 
                "Calculation completed successfully" : "Calculation failed: " + response.getMessage();
            matchingSessionsService.updateSessionStatusWithHistory(sessionId, status, statusReason, "system");
            
            Map<String, Object> result = Map.of(
                "status", response.getSuccess() ? "success" : "failed",
                "message", response.getMessage(),
                "sessionId", sessionId,
                "calculationTime", response.getCalculationTime(),
                "isRetry", "calculation_failed".equals(currentStatus),
                "previousStatus", currentStatus
            );
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("Matching calculation failed: {}", e.getMessage(), e);
            matchingSessionsService.updateSessionStatusWithHistory(sessionId, "calculation_failed", 
                "Calculation failed due to exception: " + e.getMessage(), "system");
            return Result.fail("Calculation failed: " + e.getMessage());
        }
    }

    @GetMapping("/sessions/{sessionId}/calculate/status")
    @ApiOperation("Get calculation progress")
    public Result<Map<String, Object>> getCalculationStatus(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId) {
        log.info("Getting calculation status: sessionId={}", sessionId);
        
        MatchingSessions session = matchingSessionsService.getSessionById(sessionId);
        if (session == null) {
            return Result.fail("Session not found");
        }
        
        Map<String, Object> status = Map.of(
            "sessionId", sessionId,
            "status", session.getStatus(),
            "progress", getProgressPercentage(session.getStatus()),
            "estimatedTime", getEstimatedTime(session.getStatus()),
            "isCalculating", "calculating".equals(session.getStatus()),
            "isCompleted", "calculation_success".equals(session.getStatus()),
            "isFailed", "calculation_failed".equals(session.getStatus()),
            "canRetry", "calculation_failed".equals(session.getStatus()) || "incompatible".equals(session.getStatus())
        );
        
        return Result.success(status);
    }

    @PostMapping("/sessions/{sessionId}/calculate/retry")
    @ApiOperation("Retry calculation after failure with cleanup")
    public Result<Map<String, Object>> retryCalculation(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId,
            @ApiParam("Retry reason") @RequestParam(required = false) String reason) {
        log.info("Retrying calculation for session: sessionId={}, reason={}", sessionId, reason);
        
        MatchingSessions session = matchingSessionsService.getSessionById(sessionId);
        if (session == null) {
            return Result.fail("Session not found");
        }
        
        // Check if retry is allowed from current status
        String currentStatus = session.getStatus();
        if (!"calculation_failed".equals(currentStatus) && !"incompatible".equals(currentStatus)) {
            return Result.fail("Cannot retry calculation from current status: " + currentStatus + 
                ". Retry is allowed from: calculation_failed, incompatible");
        }
        
        try {
            // Clear any previous calculation results for clean retry
            // This is handled automatically by the matching service during execution
            
            String retryReason = reason != null ? reason : "User requested calculation retry";
            String auditReason = String.format("Retrying calculation - %s (previous status: %s)", retryReason, currentStatus);
            
            // Update to calculating status
            matchingSessionsService.updateSessionStatusWithHistory(sessionId, "calculating", auditReason, "system");
            
            // Execute matching calculation
            MatchingResponse response = matchingService.executeMatching(sessionId);
            
            // Update session status based on result
            String status = response.getSuccess() ? "calculation_success" : "calculation_failed";
            String statusReason = response.getSuccess() ? 
                "Retry calculation completed successfully" : "Retry calculation failed: " + response.getMessage();
            matchingSessionsService.updateSessionStatusWithHistory(sessionId, status, statusReason, "system");
            
            Map<String, Object> result = Map.of(
                "status", response.getSuccess() ? "success" : "failed",
                "message", response.getMessage(),
                "sessionId", sessionId,
                "calculationTime", response.getCalculationTime(),
                "isRetry", true,
                "previousStatus", currentStatus,
                "retryReason", retryReason
            );
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("Retry calculation failed: {}", e.getMessage(), e);
            matchingSessionsService.updateSessionStatusWithHistory(sessionId, "calculation_failed", 
                "Retry calculation failed due to exception: " + e.getMessage(), "system");
            return Result.fail("Retry calculation failed: " + e.getMessage());
        }
    }

    @GetMapping("/sessions/{sessionId}/results")
    @ApiOperation("Get stored calculation results (READ-ONLY - does not trigger new calculations)")
    public Result<MatchingResponse> getCalculationResults(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId) {
        log.info("Retrieving stored calculation results (READ-ONLY): sessionId={}", sessionId);
        
        try {
            MatchingResponse response = matchingService.getSessionDetails(sessionId);
            
            if (response.getSuccess()) {
                log.info("Successfully retrieved stored results for session: {} (calculated at: {})", 
                         sessionId, response.getCalculationTime());
            } else {
                log.info("No stored results found for session: {} - {}", sessionId, response.getMessage());
            }
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("Failed to retrieve stored results for session: {} - {}", sessionId, e.getMessage());
            return Result.fail("Failed to retrieve calculation results: " + e.getMessage());
        }
    }

    @GetMapping("/sessions/{sessionId}/results/export")
    @ApiOperation("Export calculation results")
    public Result<Map<String, Object>> exportCalculationResults(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId,
            @ApiParam("Export format") @RequestParam(defaultValue = "json") String format) {
        log.info("Exporting calculation results: sessionId={}, format={}", sessionId, format);
        
        Map<String, Object> exportData = matchingService.exportSessionResults(sessionId, format);
        return Result.success(exportData);
    }

    // ==================== 5. Configuration APIs ====================

    @GetMapping("/constraints")
    @ApiOperation("Get matching constraint parameters")
    public Result<List<Map<String, Object>>> getMatchingConstraints() {
        log.info("Getting matching constraints");
        
        List<Map<String, Object>> constraints = matchingService.getMatchingConstraints();
        return Result.success(constraints);
    }

    @PutMapping("/constraints")
    @ApiOperation("Update matching constraints")
    public Result<Boolean> updateMatchingConstraints(
            @RequestBody List<Map<String, Object>> constraints) {
        log.info("Updating matching constraints");
        
        boolean success = matchingService.updateMatchingConstraints(constraints);
        return Result.success(success);
    }

    @GetMapping("/compatibility/matrix")
    @ApiOperation("Get 40×40 compatibility matrix")
    public Result<List<List<Map<String, Object>>>> getCompatibilityMatrix() {
        log.info("Getting compatibility matrix");
        
        List<List<Map<String, Object>>> matrix = matchingService.getFullCompatibilityMatrix();
        return Result.success(matrix);
    }

    // ==================== Helper Methods ====================

    private int getProgressPercentage(String status) {
        switch (status) {
            case "draft": return 0;
            case "waste_selected": return 25;
            case "compatibility_checking": return 40;
            case "compatible": return 50;
            case "incompatible": return 35;
            case "calculating": return 75;
            case "calculation_success": return 100;
            case "calculation_failed": return 80;
            case "archived": return 100;
            default: return 0;
        }
    }

    private String getEstimatedTime(String status) {
        switch (status) {
            case "compatibility_checking": return "5-10 seconds";
            case "calculating": return "30-60 seconds";
            default: return "N/A";
        }
    }

    /**
     * Check if the current session status allows starting calculation
     * 
     * @param status Current session status
     * @return true if calculation can be started from this status
     */
    private boolean isValidCalculationState(String status) {
        return "compatible".equals(status) ||           // Normal flow
               "calculation_failed".equals(status) ||   // Retry after failure
               "incompatible".equals(status);           // Manual override (advanced users)
    }
    
    /**
     * Build appropriate reason message for calculation start based on current status
     * 
     * @param currentStatus Current session status
     * @return Reason message for audit trail
     */
    private String buildCalculationStartReason(String currentStatus) {
        switch (currentStatus) {
            case "compatible":
                return "Starting matching calculation";
            case "calculation_failed":
                return "Retrying matching calculation after previous failure";
            case "incompatible":
                return "Starting calculation with manual override (incompatible wastes)";
            default:
                return "Starting matching calculation from status: " + currentStatus;
        }
    }
} 