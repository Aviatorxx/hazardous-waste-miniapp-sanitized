package org.gsu.hwtttt.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.gsu.hwtttt.common.result.Result;
import org.gsu.hwtttt.entity.CompatibilityCategory;
import org.gsu.hwtttt.entity.CompatibilityChecks;
import org.gsu.hwtttt.service.MatchingService;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Compatibility Check REST API Controller
 * Implements Module 4: Hazardous Waste Compatibility Simulation
 * 
 * @author WenXin
 * @date 2025/01/07
 */
@Slf4j
@RestController
@RequestMapping("/api/compatibility")
@Api(tags = "Compatibility Check Management")
@RequiredArgsConstructor
@Validated
public class CompatibilityController {

    private final MatchingService matchingService;

    // ==================== 3. Compatibility Check APIs ====================

    @PostMapping("/check")
    @ApiOperation("Check compatibility for wastes in session")
    public Result<Map<String, Object>> checkCompatibility(@RequestBody Map<String, Object> request) {
        log.info("Performing compatibility check: {}", request);
        
        if (request.containsKey("sessionId")) {
            Long sessionId = Long.valueOf(request.get("sessionId").toString());
            Map<String, Object> compatibilityResult = matchingService.performCompatibilityCheck(sessionId);
            return Result.success(compatibilityResult);
        } else if (request.containsKey("wasteIds")) {
            @SuppressWarnings("unchecked")
            List<Long> wasteIds = (List<Long>) request.get("wasteIds");
            Map<String, Object> compatibilityResult = matchingService.checkWasteCompatibility(wasteIds);
            return Result.success(compatibilityResult);
        } else {
            return Result.fail("Either sessionId or wasteIds must be provided");
        }
    }

    @GetMapping("/check/{sessionId}")
    @ApiOperation("Get compatibility check results")
    public Result<Map<String, Object>> getCompatibilityCheckResults(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId) {
        log.info("Getting compatibility check results: sessionId={}", sessionId);
        
        List<CompatibilityChecks> checkResults = matchingService.getCompatibilityCheckResults(sessionId);
        Map<String, Object> detailedAnalysis = matchingService.getCompatibilityAnalysis(sessionId);
        
        Map<String, Object> response = Map.of(
            "sessionId", sessionId,
            "checkResults", checkResults,
            "analysis", detailedAnalysis
        );
        
        return Result.success(response);
    }

    @GetMapping("/categories")
    @ApiOperation("Get 41 compatibility categories")
    public Result<List<CompatibilityCategory>> getCompatibilityCategories() {
        log.info("Getting compatibility categories");
        
        List<CompatibilityCategory> categories = matchingService.getCompatibilityCategories();
        return Result.success(categories);
    }

    @GetMapping("/matrix/diagnose")
    @ApiOperation("Diagnose compatibility matrix issues")
    public Result<Map<String, Object>> diagnoseCompatibilityMatrix() {
        log.info("Diagnosing compatibility matrix for duplicate rules");
        
        try {
            Map<String, Object> diagnosticResult = matchingService.diagnoseCompatibilityMatrix();
            return Result.success(diagnosticResult);
        } catch (Exception e) {
            log.error("Error during compatibility matrix diagnosis: {}", e.getMessage(), e);
            return Result.fail("Diagnostic failed: " + e.getMessage());
        }
    }

    @GetMapping("/matrix/full")
    @ApiOperation("Get full compatibility matrix")
    public Result<List<List<Map<String, Object>>>> getFullCompatibilityMatrix() {
        log.info("Getting full compatibility matrix");
        
        try {
            List<List<Map<String, Object>>> matrix = matchingService.getFullCompatibilityMatrix();
            return Result.success(matrix);
        } catch (Exception e) {
            log.error("Error getting full compatibility matrix: {}", e.getMessage(), e);
            return Result.fail("Failed to get matrix: " + e.getMessage());
        }
    }

    @PostMapping("/recheck/{sessionId}")
    @ApiOperation("Re-check compatibility for a session (allows re-checking compatible/incompatible sessions)")
    public Result<Map<String, Object>> recheckCompatibility(
            @ApiParam("Session ID") @PathVariable @NotNull @Min(value = 1, message = "Session ID must be greater than 0") Long sessionId) {
        log.info("Re-checking compatibility for session: {}", sessionId);
        
        try {
            Map<String, Object> compatibilityResult = matchingService.performCompatibilityCheck(sessionId);
            
            // Add additional metadata to indicate this was a recheck
            Map<String, Object> response = new HashMap<>(compatibilityResult);
            response.put("operationType", "RECHECK");
            response.put("recheckTime", java.time.LocalDateTime.now());
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("Error during compatibility re-check for session {}: {}", sessionId, e.getMessage(), e);
            return Result.fail("Re-check failed: " + e.getMessage());
        }
    }
} 