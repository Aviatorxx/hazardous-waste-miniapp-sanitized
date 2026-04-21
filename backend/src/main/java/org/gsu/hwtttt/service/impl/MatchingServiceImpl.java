package org.gsu.hwtttt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gsu.hwtttt.common.result.Result;
import org.gsu.hwtttt.constant.SystemConstants;
import org.gsu.hwtttt.dto.WasteBlendingData;
import org.gsu.hwtttt.dto.response.MatchingResponse;
import org.gsu.hwtttt.dto.response.SessionSummaryResponse;
import org.gsu.hwtttt.entity.*;
import org.gsu.hwtttt.mapper.*;
import org.gsu.hwtttt.service.MatchingService;
import org.gsu.hwtttt.util.LinearProgrammingUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Module 4: 配伍算法核心Service实现类
 * 支持兼容性模拟全流程
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final MatchingDetailsMapper matchingDetailsMapper;
    private final MatchingResultsMapper matchingResultsMapper;
    private final CompatibilityChecksMapper compatibilityChecksMapper;
    private final MatchingConstraintsMapper matchingConstraintsMapper;
    private final CompatibilityMatrixMapper compatibilityMatrixMapper;
    private final HazardousWasteMapper hazardousWasteMapper;
    private final MatchingSessionsMapper matchingSessionsMapper;
    private final CompatibilityCategoryMapper compatibilityCategoryMapper;
    private final LinearProgrammingUtil linearProgrammingUtil;

    @Override
    @Transactional
    public MatchingResponse executeMatching(Long sessionId) {
        log.info("执行配伍计算，会话ID: {}", sessionId);
        MatchingResponse response = new MatchingResponse();
        response.setSessionId(sessionId);
        response.setCalculationTime(LocalDateTime.now());

        try {
            MatchingSessions session = matchingSessionsMapper.selectById(sessionId);
            if (session == null || session.getDeleted()) {
                response.setSuccess(false);
                response.setMessage("会话不存在或已被删除。");
                return response;
            }

            // 🔧 NEW: Check total amount constraint BEFORE compatibility checking
            Map<String, Object> weightedAverages = calculateWeightedAverages(sessionId);
            List<MatchingConstraints> constraints = matchingConstraintsMapper.selectList(
                new QueryWrapper<MatchingConstraints>()
                    .eq("is_active", true)
                    .eq("parameter_code", "TOTAL_AMOUNT")
            );
            
            // Check total amount constraint first
            if (!constraints.isEmpty()) {
                List<String> totalAmountViolations = checkViolations(weightedAverages, constraints);
                if (!totalAmountViolations.isEmpty()) {
                    log.warn("Total amount constraint violation detected for session {}: {}", sessionId, totalAmountViolations);
                    response.setSuccess(false);
                    response.setMessage("配伍失败：" + String.join("; ", totalAmountViolations));
                    response.setConstraintViolations(totalAmountViolations);
                    saveMatchingResult(sessionId, response);
                    return response;
                }
            }

            Map<String, Object> compatibilityResult = performCompatibilityCheck(sessionId);
            if (!(Boolean) compatibilityResult.get("compatible")) {
                response.setSuccess(false);
                response.setMessage("相容性检查未通过: " + compatibilityResult.get("reason"));
                saveMatchingResult(sessionId, response);
                return response;
            }

            // Check all other constraints after compatibility
            List<MatchingConstraints> allConstraints = matchingConstraintsMapper.selectList(new QueryWrapper<MatchingConstraints>().eq("is_active", true));
            
            log.info("Loaded {} active constraints for validation", allConstraints.size());
            List<String> violations = checkViolations(weightedAverages, allConstraints);

            if (!violations.isEmpty()) {
                log.warn("Constraint violations detected for session {}: {}", sessionId, violations);
                response.setSuccess(false);
                response.setMessage("配伍失败：" + String.join("; ", violations));
                response.setConstraintViolations(violations);
                saveMatchingResult(sessionId, response);
                return response;
            }

            log.info("All constraints satisfied for session: {}", sessionId);
            response.setSuccess(true);
            response.setMessage("配伍计算成功");
            response.setStatus("COMPLETED");
            response.setActualHeatValue((BigDecimal) weightedAverages.get("heatValue"));
            List<MatchingDetails> details = matchingDetailsMapper.selectBySessionId(sessionId);
            response.setWasteDetails(details.stream().map(this::buildWasteDetail).collect(Collectors.toList()));
            response.setIndicators(buildIndicators(weightedAverages));
            
            saveMatchingResult(sessionId, response);
            
            return response;

        } catch (Exception e) {
            log.error("配伍计算异常: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("配伍计算时发生内部错误: " + e.getMessage());
            saveMatchingResult(sessionId, response);
            return response;
        }
    }

    @Override
    public boolean checkCompatibility(Long sessionId) {
        Map<String, Object> result = performCompatibilityCheck(sessionId);
        return (Boolean) result.get("compatible");
    }

    @Override
    public boolean validateConstraints(Long sessionId) {
        Map<String, Object> weightedAverages = calculateWeightedAverages(sessionId);
        List<MatchingConstraints> constraints = matchingConstraintsMapper.selectList(new QueryWrapper<MatchingConstraints>().eq("is_active", true));
        return checkViolations(weightedAverages, constraints).isEmpty();
    }

    private List<String> checkViolations(Map<String, Object> weightedAverages, List<MatchingConstraints> constraints) {
        List<String> violations = new ArrayList<>();
        log.debug("Starting constraint validation with {} constraints", constraints.size());
        log.debug("Weighted averages: {}", weightedAverages);
        
        // 🔧 CRITICAL FIX: Create parameter mapping between database codes and calculation keys
        Map<String, String> parameterMapping = new HashMap<>();
        parameterMapping.put("HEAT_VALUE", "heatValue");
        parameterMapping.put("WATER_CONTENT", "waterContent");
        parameterMapping.put("N_CONTENT", "nContent");
        parameterMapping.put("S_CONTENT", "sContent");
        parameterMapping.put("CL_CONTENT", "clContent");
        parameterMapping.put("F_CONTENT", "fContent");
        parameterMapping.put("HG_CONTENT", "hgContent");
        parameterMapping.put("CD_CONTENT", "cdContent");
        parameterMapping.put("AS_NI_CONTENT", "asNiContent");  // Combined As+Ni for constraint validation
        parameterMapping.put("AS_CONTENT", "asContent");       // Individual As
        parameterMapping.put("NI_CONTENT", "niContent");       // Individual Ni
        parameterMapping.put("PB_CONTENT", "pbContent");
        parameterMapping.put("HEAVY_METALS_TOTAL", "heavyMetalsTotal");
        parameterMapping.put("TOTAL_AMOUNT", "totalAmount");   // Total amount constraint
        parameterMapping.put("P_CONTENT", "pContent");         // Phosphorus content (%)
        parameterMapping.put("PH_VALUE", "phValue");           // pH value
        
        log.info("Parameter mapping configured: {}", parameterMapping);
        
        for (MatchingConstraints constraint : constraints) {
            String paramCode = constraint.getParameterCode();
            
            // 🔧 FIX: Map constraint parameter to weighted averages key
            String mappedKey = parameterMapping.get(paramCode);
            if (mappedKey == null) {
                log.warn("No mapping found for constraint parameter: {}. Available mappings: {}", 
                    paramCode, parameterMapping.keySet());
                log.warn("Available weighted average keys: {}", weightedAverages.keySet());
                continue;
            }
            
            if (weightedAverages.containsKey(mappedKey)) {
                BigDecimal value = (BigDecimal) weightedAverages.get(mappedKey);
                BigDecimal originalValue = value;
                
                // 🔧 FIX: Apply unit conversion for heat value from cal/g to kJ/kg
                if ("HEAT_VALUE".equals(paramCode)) {
                    value = value.multiply(new BigDecimal("4.184")); // cal/g → kJ/kg conversion
                    log.debug("Heat value converted for constraint validation: {} cal/g → {} kJ/kg", 
                        originalValue, value);
                }
                
                log.debug("Validating constraint: {} ({}), mapped key: {}, value: {}, range: {} - {}", 
                    constraint.getConstraintName(), paramCode, mappedKey, value,
                    constraint.getMinValue(), constraint.getMaxValue());
                
                // Check minimum constraint
                if (constraint.getMinValue() != null && value.compareTo(constraint.getMinValue()) < 0) {
                    String unit = constraint.getUnit() != null ? constraint.getUnit() : "";
                    String violation = String.format("%s 的值 %.2f %s 低于最小允许值 %.2f %s", 
                        constraint.getConstraintName(), 
                        value.doubleValue(), 
                        unit,
                        constraint.getMinValue().doubleValue(),
                        unit);
                    violations.add(violation);
                    log.warn("Constraint violation detected: {} = {} < {} (min) for parameter {}", 
                        constraint.getConstraintName(), value, constraint.getMinValue(), paramCode);
                }
                
                // Check maximum constraint  
                if (constraint.getMaxValue() != null && value.compareTo(constraint.getMaxValue()) > 0) {
                    String unit = constraint.getUnit() != null ? constraint.getUnit() : "";
                    String violation = String.format("%s 的值 %.2f %s 高于最大允许值 %.2f %s", 
                        constraint.getConstraintName(), 
                        value.doubleValue(), 
                        unit,
                        constraint.getMaxValue().doubleValue(),
                        unit);
                    violations.add(violation);
                    log.warn("Constraint violation detected: {} = {} > {} (max) for parameter {}", 
                        constraint.getConstraintName(), value, constraint.getMaxValue(), paramCode);
                }
                
                if (constraint.getMinValue() != null || constraint.getMaxValue() != null) {
                    log.debug("Validated constraint: {} = {} (range: {} - {}) ✅", 
                        constraint.getConstraintName(), value, constraint.getMinValue(), constraint.getMaxValue());
                }
                
            } else {
                log.warn("Parameter {} (mapped to '{}') not found in weighted averages. Available keys: {}", 
                    paramCode, mappedKey, weightedAverages.keySet());
                log.warn("This indicates a missing calculation for parameter: {}", constraint.getConstraintName());
            }
        }
        
        log.info("Constraint validation completed. Violations found: {}", violations.size());
        if (!violations.isEmpty()) {
            log.warn("❌ CONSTRAINT VIOLATIONS DETECTED: {}", violations);
        } else {
            log.info("✅ All constraints satisfied - no violations found");
        }
        
        return violations;
    }

    @Override
    @Transactional
    public Result<Map<String, Object>> addWasteToSession(Long sessionId, Long wasteId, Double quantity) {
        try {
            // Validate session exists
            MatchingSessions session = matchingSessionsMapper.selectById(sessionId);
            if (session == null) {
                return Result.error("配伍会话不存在");
            }
            
            // Validate waste exists
            HazardousWaste waste = hazardousWasteMapper.selectById(wasteId);
            if (waste == null) {
                return Result.error("危废信息不存在");
            }
            
            // Check if waste already exists in session (make it idempotent)
            MatchingDetails existing = matchingDetailsMapper.selectBySessionIdAndWasteId(sessionId, wasteId);
            if (existing != null) {
                // Instead of failing, return success with existing data
                log.info("危废已存在于会话中，返回现有记录: sessionId={}, wasteId={}", sessionId, wasteId);
                
                Map<String, Object> result = new HashMap<>();
                result.put("message", "危废已在会话中");
                result.put("alreadyExists", true);
                result.put("matchingDetailId", existing.getId());
                result.put("sessionId", sessionId);
                result.put("wasteId", wasteId);
                result.put("currentAmount", existing.getPlannedAmount());
                result.put("success", true);
                
                return Result.success("危废已添加", result);
            }
            
            // Create new matching detail
            MatchingDetails details = new MatchingDetails();
            details.setSessionId(sessionId);
            details.setWasteId(wasteId);
            details.setPlannedAmount(BigDecimal.valueOf(quantity));
            details.setActualAmount(BigDecimal.valueOf(quantity));
            details.setCreateTime(LocalDateTime.now());
            
            boolean insertSuccess = matchingDetailsMapper.insert(details) > 0;
            
            if (insertSuccess) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "危废添加成功");
                result.put("alreadyExists", false);
                result.put("matchingDetailId", details.getId());
                result.put("sessionId", sessionId);
                result.put("wasteId", wasteId);
                result.put("plannedAmount", quantity);
                
                return Result.success("危废添加成功", result);
            } else {
                return Result.error("数据库插入失败");
            }
            
        } catch (Exception e) {
            log.error("添加危废到会话失败: sessionId={}, wasteId={}", sessionId, wasteId, e);
            return Result.error("系统错误，请重试: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean removeWasteFromSession(Long sessionId, Long wasteId) {
        try {
            return matchingDetailsMapper.deleteBySessionIdAndWasteId(sessionId, wasteId) > 0;
        } catch (Exception e) {
            log.error("从会话移除危废失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean updateWasteQuantity(Long sessionId, Long wasteId, Double quantity) {
        try {
            BigDecimal amount = BigDecimal.valueOf(quantity);
            return matchingDetailsMapper.updateActualAmount(sessionId, wasteId, amount) > 0;
        } catch (Exception e) {
            log.error("更新危废用量失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public MatchingResponse getSessionDetails(Long sessionId) {
        // ✅ FIXED: Return stored results instead of executing calculations
        return getStoredCalculationResults(sessionId);
    }

    /**
     * Get stored calculation results without triggering new calculations
     * This method ONLY retrieves existing results from the database
     * 
     * @param sessionId Session ID
     * @return Stored calculation results
     */
    @Override
    public MatchingResponse getStoredCalculationResults(Long sessionId) {
        log.info("Retrieving stored calculation results for session: {}", sessionId);
        
        // Get session info
        MatchingSessions session = matchingSessionsMapper.selectById(sessionId);
        if (session == null || session.getDeleted()) {
            throw new RuntimeException("Session not found: " + sessionId);
        }

        // Query existing results from matching_results table (most recent first)
        MatchingResults existingResult = matchingResultsMapper.selectOne(
            new QueryWrapper<MatchingResults>()
                .eq("session_id", sessionId)
                .orderByDesc("calculation_time")
                .last("LIMIT 1")
        );

        if (existingResult == null) {
            log.info("No calculation results found for session: {}", sessionId);
            // Return response indicating no results available
            MatchingResponse response = new MatchingResponse();
            response.setSessionId(sessionId);
            response.setSessionName(session.getSessionName());
            response.setSuccess(false);
            response.setMessage("No calculation results found for this session. Please run calculations first.");
            response.setStatus(session.getStatus());
            response.setTargetHeatValue(session.getTargetHeatValue());
            response.setTotalAmount(session.getTotalAmount());
            return response;
        }

        log.info("Found stored calculation results for session: {} with status: {}", sessionId, existingResult.getResultStatus());
        // Build response from existing stored results
        return buildResponseFromStoredResults(session, existingResult);
    }

    /**
     * Build MatchingResponse from stored database results
     * 
     * @param session Session entity
     * @param result Stored matching result
     * @return Complete MatchingResponse
     */
    private MatchingResponse buildResponseFromStoredResults(MatchingSessions session, MatchingResults result) {
        log.debug("Building response from stored results for session: {}", session.getId());
        
        MatchingResponse response = new MatchingResponse();
        
        // Basic session info
        response.setSessionId(session.getId());
        response.setSessionName(session.getSessionName());
        response.setStatus(session.getStatus());
        response.setTargetHeatValue(session.getTargetHeatValue());
        response.setTotalAmount(session.getTotalAmount());
        response.setCalculationTime(result.getCalculationTime());
        
        // Result status mapping
        response.setSuccess(result.isSuccess());
        if (result.isSuccess()) {
            response.setMessage("Calculation results retrieved successfully");
        } else if (result.isFailed()) {
            response.setMessage("Calculation failed: " + result.getFailureReasons());
        } else if (result.isWarning()) {
            response.setMessage("Calculation completed with warnings: " + result.getWarnings());
        }
        
        // Calculated values from stored results
        response.setActualHeatValue(result.getCalculatedHeatValue());
        
        // Build indicators from stored results
        MatchingResponse.MatchingIndicators indicators = new MatchingResponse.MatchingIndicators();
        indicators.setHeatValue(result.getCalculatedHeatValue());
        indicators.setWaterContent(result.getCalculatedWaterContent());
        indicators.setNitrogenContent(result.getCalculatedNContent());
        indicators.setSulfurContent(result.getCalculatedSContent());
        indicators.setChlorineContent(result.getCalculatedClContent());
        indicators.setFluorineContent(result.getCalculatedFContent());
        
        // Mercury is always 0 as per requirements
        indicators.setMercuryContent(BigDecimal.ZERO);
        
        // Heavy metals indicators from stored results
        indicators.setCadmiumContent(result.getCalculatedCdContent());
        indicators.setLeadContent(result.getCalculatedPbContent());
        indicators.setTotalHeavyMetals(result.getCalculatedHeavyMetalsTotal());
        
        // For As+Ni combined, calculate if both As and Ni are available in stored results
        // Note: The entity may not have separate As and Ni fields, so we'll use what's available
        indicators.setArsenicNickelContent(BigDecimal.ZERO); // TODO: Update when As+Ni storage is implemented
        
        response.setIndicators(indicators);
        
        // Parse constraint violations if any
        if (result.getConstraintViolations() != null && !result.getConstraintViolations().isEmpty()) {
            try {
                // Parse JSON string to list (assuming it's stored as JSON)
                ObjectMapper objectMapper = new ObjectMapper();
                List<String> violations = objectMapper.readValue(
                    result.getConstraintViolations(), 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                response.setConstraintViolations(violations);
            } catch (Exception e) {
                log.warn("Failed to parse constraint violations JSON for session {}: {}", session.getId(), e.getMessage());
                // Fallback: treat as single string
                response.setConstraintViolations(List.of(result.getConstraintViolations()));
            }
        }
        
        // Get waste details from session (these don't change after calculation)
        List<MatchingDetails> details = matchingDetailsMapper.selectBySessionId(session.getId());
        if (!details.isEmpty()) {
            response.setWasteDetails(details.stream().map(this::buildWasteDetail).collect(Collectors.toList()));
        }
        
        log.info("Successfully built response from stored results for session: {} (success: {})", 
                 session.getId(), response.getSuccess());
        
        return response;
    }

    @Override
    public MatchingResponse recalculateMatching(Long sessionId) {
        return executeMatching(sessionId);
    }

    @Override
    public Map<String, Object> importWasteToSession(Long sessionId, Long wasteId, Double plannedAmount) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查库存
        Map<String, Object> stockCheck = checkWasteStock(wasteId, BigDecimal.valueOf(plannedAmount));
        if (!(Boolean) stockCheck.get("sufficient")) {
            result.put("success", false);
            result.put("message", "库存不足");
            result.put("remainingStock", stockCheck.get("remainingStock"));
            return result;
        }
        
        // 添加到会话 - 处理新的Result返回类型
        Result<Map<String, Object>> addResult = addWasteToSession(sessionId, wasteId, plannedAmount);
        if (addResult.getSuccess()) {
            result.put("success", true);
            result.put("message", "导入成功");
            result.putAll(addResult.getData()); // 包含详细信息
        } else {
            result.put("success", false);
            result.put("message", "导入失败: " + addResult.getMessage());
        }
        
        return result;
    }

    @Override
    @Transactional
    public Map<String, Object> performCompatibilityCheck(Long sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        // Check if this is a re-check scenario
        MatchingSessions session = matchingSessionsMapper.selectById(sessionId);
        boolean isRecheck = session != null && 
                          (SystemConstants.MatchingStatus.COMPATIBLE.equals(session.getStatus()) || 
                           SystemConstants.MatchingStatus.INCOMPATIBLE.equals(session.getStatus()));
        
        if (isRecheck) {
            log.info("Re-checking compatibility for session: {} (previous status: {})", sessionId, session.getStatus());
        } else {
            log.info("Starting compatibility check for session: {}", sessionId);
        }
        
        try {
            // Clear previous compatibility check results
            int deletedCount = compatibilityChecksMapper.delete(new QueryWrapper<CompatibilityChecks>().eq("session_id", sessionId));
            if (isRecheck && deletedCount > 0) {
                log.info("Cleared {} previous compatibility check results for session: {} (re-check)", deletedCount, sessionId);
            } else {
                log.info("Cleared previous compatibility check results for session: {}", sessionId);
            }

            List<MatchingDetails> details = matchingDetailsMapper.selectBySessionId(sessionId);
            if (details.size() < 2) {
                result.put("compatible", true);
                result.put("message", isRecheck ? "重新检查完成：单个危废无需检查相容性。" : "单个危废无需检查相容性。");
                result.put("totalPairs", 0);
                result.put("compatiblePairs", 0);
                result.put("incompatiblePairs", 0);
                result.put("isRecheck", isRecheck);
                return result;
            }

            List<Long> wasteIds = details.stream().map(MatchingDetails::getWasteId).collect(Collectors.toList());
            List<HazardousWaste> wastes = hazardousWasteMapper.selectBatchIds(wasteIds);
            Map<Long, HazardousWaste> wasteMap = wastes.stream().collect(Collectors.toMap(HazardousWaste::getId, w -> w));

            boolean overallCompatible = true;
            StringBuilder reasons = new StringBuilder();
            int totalPairs = 0;
            int compatiblePairs = 0;
            int incompatiblePairs = 0;
            int errorPairs = 0;
            List<Map<String, Object>> pairResults = new ArrayList<>();

            for (int i = 0; i < details.size(); i++) {
                for (int j = i + 1; j < details.size(); j++) {
                    HazardousWaste waste1 = wasteMap.get(details.get(i).getWasteId());
                    HazardousWaste waste2 = wasteMap.get(details.get(j).getWasteId());
                    
                    log.info("Checking compatibility between {} ({}) and {} ({})", 
                        waste1.getWasteName(), waste1.getCompatibilityCategoryCode(),
                        waste2.getWasteName(), waste2.getCompatibilityCategoryCode());
                    
                    try {
                        CompatibilityChecks check = checkSinglePairCompatibility(sessionId, waste1, waste2);
                        totalPairs++;
                        
                        if (check.getCompatible()) {
                            compatiblePairs++;
                        } else {
                            overallCompatible = false;
                            incompatiblePairs++;
                            reasons.append(String.format("危废 %s 与 %s 不相容: %s. ", 
                                waste1.getWasteName(), waste2.getWasteName(), check.getConflictReason()));
                        }
                        
                        // Add pair result for detailed analysis
                        Map<String, Object> pairResult = new HashMap<>();
                        pairResult.put("waste1Id", waste1.getId());
                        pairResult.put("waste1Name", waste1.getWasteName());
                        pairResult.put("waste1Category", waste1.getCompatibilityCategoryCode());
                        pairResult.put("waste2Id", waste2.getId());
                        pairResult.put("waste2Name", waste2.getWasteName());
                        pairResult.put("waste2Category", waste2.getCompatibilityCategoryCode());
                        pairResult.put("compatible", check.getCompatible());
                        pairResult.put("riskLevel", check.getRiskLevel());
                        pairResult.put("reason", check.getConflictReason());
                        pairResult.put("checkType", check.getCheckType());
                        pairResults.add(pairResult);
                        
                    } catch (Exception e) {
                        log.error("Error checking compatibility for pair {} - {}: {}", 
                            waste1.getWasteName(), waste2.getWasteName(), e.getMessage(), e);
                        
                        // Count error pairs but don't fail the entire process
                        totalPairs++;
                        errorPairs++;
                        overallCompatible = false; // Treat errors as incompatible for safety
                        
                        reasons.append(String.format("危废 %s 与 %s 检查失败: %s. ", 
                            waste1.getWasteName(), waste2.getWasteName(), e.getMessage()));
                        
                        // Add error result
                        Map<String, Object> pairResult = new HashMap<>();
                        pairResult.put("waste1Id", waste1.getId());
                        pairResult.put("waste1Name", waste1.getWasteName());
                        pairResult.put("waste1Category", waste1.getCompatibilityCategoryCode());
                        pairResult.put("waste2Id", waste2.getId());
                        pairResult.put("waste2Name", waste2.getWasteName());
                        pairResult.put("waste2Category", waste2.getCompatibilityCategoryCode());
                        pairResult.put("compatible", false);
                        pairResult.put("riskLevel", "HIGH");
                        pairResult.put("reason", "检查失败: " + e.getMessage());
                        pairResult.put("checkType", "ERROR");
                        pairResult.put("error", true);
                        pairResults.add(pairResult);
                    }
                }
            }

            result.put("compatible", overallCompatible);
            result.put("totalPairs", totalPairs);
            result.put("compatiblePairs", compatiblePairs);
            result.put("incompatiblePairs", incompatiblePairs);
            result.put("errorPairs", errorPairs);
            result.put("pairResults", pairResults);
            result.put("isRecheck", isRecheck);
            
            if (!overallCompatible) {
                result.put("reason", reasons.toString());
                if (errorPairs > 0) {
                    result.put("warning", String.format("有 %d 对危废检查时发生错误，已标记为不相容", errorPairs));
                }
                if (isRecheck) {
                    result.put("message", "重新检查完成：存在不相容的危废组合。");
                }
            } else {
                result.put("message", isRecheck ? "重新检查完成：所有危废组合均相容。" : "所有危废组合均相容。");
            }
            
            String logMessage = isRecheck ? 
                "Compatibility re-check completed for session: {}. Overall compatible: {}, Total pairs: {}, Compatible: {}, Incompatible: {}, Errors: {}" :
                "Compatibility check completed for session: {}. Overall compatible: {}, Total pairs: {}, Compatible: {}, Incompatible: {}, Errors: {}";
            log.info(logMessage, sessionId, overallCompatible, totalPairs, compatiblePairs, incompatiblePairs, errorPairs);
            
        } catch (Exception e) {
            log.error("Critical error during compatibility check for session {}: {}", sessionId, e.getMessage(), e);
            result.put("compatible", false);
            result.put("error", true);
            result.put("message", "相容性检查过程中发生严重错误: " + e.getMessage());
            result.put("reason", "系统错误: " + e.getMessage());
            
            // Don't re-throw the exception to allow graceful degradation
            // Instead, log the error and return failure result
        }
        
        return result;
    }

    private CompatibilityChecks checkSinglePairCompatibility(Long sessionId, HazardousWaste waste1, HazardousWaste waste2) {
        String category1 = waste1.getCompatibilityCategoryCode();
        String category2 = waste2.getCompatibilityCategoryCode();

        // Use safe matrix lookup to handle bidirectional duplicates
        CompatibilityMatrix rule = null;
        try {
            rule = compatibilityMatrixMapper.findRuleSafely(category1, category2);
            log.debug("Matrix rule lookup for categories {}-{}: {}", category1, category2, 
                rule != null ? "found (compatible=" + rule.getCompatible() + ")" : "not found");
        } catch (Exception e) {
            log.warn("Error querying compatibility matrix for categories {}-{}: {}. Using fallback logic.", 
                category1, category2, e.getMessage());
            rule = null; // Will trigger fallback logic
        }

        CompatibilityChecks check = new CompatibilityChecks();
        check.setSessionId(sessionId);
        check.setWasteId1(waste1.getId());
        check.setWasteId2(waste2.getId());
        check.setCheckTime(LocalDateTime.now());
        
        // Set new fields for waste combination, check type, and check result
        try {
            // Create JSON representation of waste combination
            Map<String, Object> wasteCombination = new HashMap<>();
            wasteCombination.put("waste1", Map.of(
                "id", waste1.getId(),
                "code", waste1.getWasteCode(),
                "name", waste1.getWasteName(),
                "category", category1
            ));
            wasteCombination.put("waste2", Map.of(
                "id", waste2.getId(),
                "code", waste2.getWasteCode(),
                "name", waste2.getWasteName(),
                "category", category2
            ));
            
            // Convert to JSON string for storage
            ObjectMapper objectMapper = new ObjectMapper();
            check.setWastesCombination(objectMapper.writeValueAsString(wasteCombination));
            
            // Set check type - automatic for matrix-based, manual for fallback
            check.setCheckType(rule != null ? "AUTOMATIC" : "MANUAL");
            
        } catch (Exception e) {
            log.warn("Failed to set waste combination JSON: {}", e.getMessage());
            // Fallback to simple string representation
            check.setWastesCombination(String.format("[%s:%s] + [%s:%s]", 
                waste1.getWasteCode(), waste1.getWasteName(),
                waste2.getWasteCode(), waste2.getWasteName()));
            check.setCheckType("MANUAL");
        }

        if (rule != null) {
            check.setCompatible(rule.getCompatible());
            check.setRiskLevel(rule.getRiskLevel());
            check.setConflictReason(rule.getIncompatibleReason());
            check.setSafetyNotes(rule.getSafetyNotes());
            
            // Set detailed check result as JSON
            try {
                Map<String, Object> checkResult = new HashMap<>();
                checkResult.put("ruleId", rule.getId());
                checkResult.put("compatible", rule.getCompatible());
                checkResult.put("riskLevel", rule.getRiskLevel());
                checkResult.put("conflictReason", rule.getIncompatibleReason());
                checkResult.put("safetyNotes", rule.getSafetyNotes());
                checkResult.put("checkMethod", "MATRIX_BASED");
                checkResult.put("timestamp", LocalDateTime.now().toString());
                
                ObjectMapper objectMapper = new ObjectMapper();
                check.setCheckResult(objectMapper.writeValueAsString(checkResult));
            } catch (Exception e) {
                log.warn("Failed to set check result JSON: {}", e.getMessage());
                check.setCheckResult(String.format("{\"compatible\":%s,\"riskLevel\":\"%s\",\"method\":\"MATRIX_BASED\"}", 
                    rule.getCompatible(), rule.getRiskLevel()));
            }
        } else {
            // Fallback to rule-based checks
            boolean compatible = !((Boolean.TRUE.equals(waste1.getOxidizing()) && Boolean.TRUE.equals(waste2.getFlammable())) ||
                                 (Boolean.TRUE.equals(waste1.getFlammable()) && Boolean.TRUE.equals(waste2.getOxidizing())));
            check.setCompatible(compatible);
            if (!compatible) {
                check.setRiskLevel("HIGH");
                check.setConflictReason("氧化性危废与易燃性危废不相容");
            } else {
                check.setRiskLevel("LOW");
                check.setConflictReason("无直接冲突");
            }
            
            // Set detailed check result for rule-based check
            try {
                Map<String, Object> checkResult = new HashMap<>();
                checkResult.put("compatible", compatible);
                checkResult.put("riskLevel", check.getRiskLevel());
                checkResult.put("conflictReason", check.getConflictReason());
                checkResult.put("checkMethod", "RULE_BASED");
                checkResult.put("properties", Map.of(
                    "waste1_oxidizing", Boolean.TRUE.equals(waste1.getOxidizing()),
                    "waste1_flammable", Boolean.TRUE.equals(waste1.getFlammable()),
                    "waste2_oxidizing", Boolean.TRUE.equals(waste2.getOxidizing()),
                    "waste2_flammable", Boolean.TRUE.equals(waste2.getFlammable())
                ));
                checkResult.put("timestamp", LocalDateTime.now().toString());
                
                ObjectMapper objectMapper = new ObjectMapper();
                check.setCheckResult(objectMapper.writeValueAsString(checkResult));
            } catch (Exception e) {
                log.warn("Failed to set check result JSON: {}", e.getMessage());
                check.setCheckResult(String.format("{\"compatible\":%s,\"riskLevel\":\"%s\",\"method\":\"RULE_BASED\"}", 
                    compatible, check.getRiskLevel()));
            }
        }
        
        // Insert compatibility check result into database with improved error handling
        try {
            log.debug("Inserting compatibility check: sessionId={}, waste1={}, waste2={}, compatible={}, riskLevel={}", 
                sessionId, waste1.getId(), waste2.getId(), check.getCompatible(), check.getRiskLevel());
            
            int insertResult = compatibilityChecksMapper.insert(check);
            if (insertResult <= 0) {
                log.error("Failed to insert compatibility check record: insertResult={}", insertResult);
                throw new RuntimeException("Failed to save compatibility check result");
            }
            
            log.debug("Successfully inserted compatibility check with ID: {}", check.getId());
            
        } catch (Exception e) {
            log.error("Error inserting compatibility check for session {} (waste1: {}, waste2: {}): {}", 
                sessionId, waste1.getId(), waste2.getId(), e.getMessage(), e);
            
            // Don't throw the exception immediately - try to continue with the session
            // Set a flag to indicate the save failed but still return the check result
            log.warn("Continuing compatibility check process despite database save failure");
        }
        
        return check;
    }

    @Override
    public Map<String, Object> performMatching(Long sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
        // 先检查相容性
        Map<String, Object> compatibilityResult = performCompatibilityCheck(sessionId);
        if (!(Boolean) compatibilityResult.get("compatible")) {
            result.put("success", false);
            result.put("message", "相容性检查未通过");
            result.put("reason", compatibilityResult.get("reason"));
            return result;
        }
        
            // 🔥 使用线性规划算法进行真正的优化计算
            LinearProgrammingUtil.SolutionResult optimizationResult = optimizeWasteMixing(sessionId);
            
            if (optimizationResult.isFeasible()) {
                // 更新优化后的数量到数据库
                updateOptimizedAmounts(sessionId, optimizationResult.getWasteQuantities());
                
                result.put("success", true);
                result.put("message", "配伍优化计算成功");
                result.put("status", "COMPLETED");
                result.put("optimizedQuantities", optimizationResult.getWasteQuantities());
                result.put("totalQuantity", optimizationResult.getTotalQuantity());
                result.put("mixtureProperties", optimizationResult.getMixtureProperties());
                result.put("solutionTime", optimizationResult.getSolutionTime());
                result.put("constraintViolations", optimizationResult.getViolations());
            } else {
                result.put("success", false);
                result.put("message", "无法找到满足所有约束的可行解");
                result.put("violations", optimizationResult.getViolations());
                result.put("errorMessage", optimizationResult.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("执行配伍计算失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "计算失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getMatchingHistory(Long sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取配伍结果历史
            QueryWrapper<MatchingResults> wrapper = new QueryWrapper<>();
            wrapper.eq("session_id", sessionId).orderByDesc("create_time");
            List<MatchingResults> history = matchingResultsMapper.selectList(wrapper);
            
            result.put("total", history.size());
            result.put("history", history);
            result.put("success", true);
            
        } catch (Exception e) {
            log.error("获取配伍历史失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "获取配伍历史失败");
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getConstraintCheckResults(Long sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取所有约束配置
            QueryWrapper<MatchingConstraints> wrapper = new QueryWrapper<>();
            wrapper.eq("is_active", true);
            List<MatchingConstraints> constraints = matchingConstraintsMapper.selectList(wrapper);
            
            // 获取会话的加权平均值
            Map<String, Object> averages = calculateWeightedAverages(sessionId);
            
            List<Map<String, Object>> checkResults = new ArrayList<>();
            boolean allPassed = true;
            
            for (MatchingConstraints constraint : constraints) {
                Map<String, Object> checkResult = new HashMap<>();
                checkResult.put("constraintName", constraint.getConstraintName());
                checkResult.put("parameterCode", constraint.getParameterCode());
                
                // 获取实际值
                BigDecimal actualValue = (BigDecimal) averages.get(constraint.getParameterCode());
                if (actualValue == null) {
                    actualValue = BigDecimal.ZERO;
                }
                
                // 检查是否在范围内
                boolean passed = true;
                if (constraint.getMinValue() != null && actualValue.compareTo(constraint.getMinValue()) < 0) {
                    passed = false;
                }
                if (constraint.getMaxValue() != null && actualValue.compareTo(constraint.getMaxValue()) > 0) {
                    passed = false;
                }
                
                checkResult.put("passed", passed);
                checkResult.put("actualValue", actualValue);
                checkResult.put("minValue", constraint.getMinValue());
                checkResult.put("maxValue", constraint.getMaxValue());
                checkResult.put("unit", constraint.getUnit());
                
                checkResults.add(checkResult);
                
                if (!passed) {
                    allPassed = false;
                }
            }
            
            result.put("allPassed", allPassed);
            result.put("constraints", checkResults);
            result.put("success", true);
            
        } catch (Exception e) {
            log.error("约束检查失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("allPassed", false);
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getRiskAssessment(Long sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取相容性检查结果
            Map<String, Object> compatibility = performCompatibilityCheck(sessionId);
            
            // 获取约束检查结果
            Map<String, Object> constraints = getConstraintCheckResults(sessionId);
            
            // 风险等级评估
            String riskLevel = "LOW";
            if (!(Boolean) compatibility.get("compatible")) {
                riskLevel = "HIGH";
            } else if (!(Boolean) constraints.get("allPassed")) {
                riskLevel = "MEDIUM";
            }
            
            result.put("riskLevel", riskLevel);
            result.put("compatibilityRisk", !(Boolean) compatibility.get("compatible"));
            result.put("constraintRisk", !(Boolean) constraints.get("allPassed"));
            result.put("riskFactors", buildRiskFactors(compatibility, constraints));
            result.put("recommendations", buildRecommendations(riskLevel));
            
        } catch (Exception e) {
            log.error("风险评估失败: {}", e.getMessage(), e);
            result.put("riskLevel", "UNKNOWN");
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getFailureAnalysis(Long sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> failureReasons = new ArrayList<>();
            
            // 检查相容性失败原因
            Map<String, Object> compatibility = performCompatibilityCheck(sessionId);
            if (!(Boolean) compatibility.get("compatible")) {
                failureReasons.add("相容性检查失败: " + compatibility.get("reason"));
            }
            
            // 检查约束失败原因
            Map<String, Object> constraints = getConstraintCheckResults(sessionId);
            if (!(Boolean) constraints.get("allPassed")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> constraintsList = (List<Map<String, Object>>) constraints.get("constraints");
                for (Map<String, Object> constraint : constraintsList) {
                    if (!(Boolean) constraint.get("passed")) {
                        failureReasons.add("约束检查失败: " + constraint.get("constraintName"));
                    }
                }
            }
            
            result.put("hasFailures", !failureReasons.isEmpty());
            result.put("failureReasons", failureReasons);
            result.put("suggestions", buildFailureSuggestions(failureReasons));
            
        } catch (Exception e) {
            log.error("失败分析出错: {}", e.getMessage(), e);
            result.put("hasFailures", true);
            result.put("failureReasons", Collections.singletonList("分析过程出错"));
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getOptimizationSuggestions(Long sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> suggestions = new ArrayList<>();
            
            // 基于约束检查结果提供建议
            Map<String, Object> constraints = getConstraintCheckResults(sessionId);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> constraintsList = (List<Map<String, Object>>) constraints.get("constraints");
            
            for (Map<String, Object> constraint : constraintsList) {
                if (!(Boolean) constraint.get("passed")) {
                    suggestions.add("调整 " + constraint.get("constraintName") + " 至合理范围");
                }
            }
            
            if (suggestions.isEmpty()) {
                suggestions.add("当前配伍方案良好，无需优化");
            }
            
            result.put("suggestions", suggestions);
            result.put("optimizationScore", calculateOptimizationScore(sessionId));
            
        } catch (Exception e) {
            log.error("优化建议生成失败: {}", e.getMessage(), e);
            result.put("suggestions", Collections.singletonList("无法生成优化建议"));
        }
        
        return result;
    }

    @Override
    public Map<String, Object> calculateWeightedAverages(Long sessionId) {
        Map<String, Object> averages = new HashMap<>();
        List<MatchingDetails> details = matchingDetailsMapper.selectBySessionId(sessionId);
        if (details.isEmpty()) {
            log.warn("No matching details found for session: {}", sessionId);
            return averages;
        }

        BigDecimal totalAmount = details.stream()
            .map(MatchingDetails::getPlannedAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Total amount is zero for session: {}", sessionId);
            return averages;
        }
        
        log.info("Calculating weighted averages for session: {} with total amount: {} kg", sessionId, totalAmount);
        
        // Initialize totals for weighted averages
        BigDecimal totalHeatValue = BigDecimal.ZERO;
        BigDecimal totalWaterContent = BigDecimal.ZERO;
        BigDecimal totalNContent = BigDecimal.ZERO;
        BigDecimal totalSContent = BigDecimal.ZERO;
        BigDecimal totalClContent = BigDecimal.ZERO;
        BigDecimal totalFContent = BigDecimal.ZERO;
        BigDecimal totalPContent = BigDecimal.ZERO;   // Phosphorus (P) - %
        BigDecimal totalPhValue = BigDecimal.ZERO;    // pH value
        BigDecimal totalHgContent = BigDecimal.ZERO;  // Mercury (always 0 as per requirements)
        BigDecimal totalCdContent = BigDecimal.ZERO;
        BigDecimal totalAsContent = BigDecimal.ZERO;
        BigDecimal totalNiContent = BigDecimal.ZERO;  // Nickel
        BigDecimal totalPbContent = BigDecimal.ZERO;
        // Heavy metals for Cr+Sn+Sb+Cu+Mn calculation
        BigDecimal totalCrContent = BigDecimal.ZERO;
        BigDecimal totalSnContent = BigDecimal.ZERO;
        BigDecimal totalSbContent = BigDecimal.ZERO;
        BigDecimal totalCuContent = BigDecimal.ZERO;
        BigDecimal totalMnContent = BigDecimal.ZERO;

        // Calculate weighted sums: Σ(Qᵢ × Dᵢ)
        for (MatchingDetails detail : details) {
            HazardousWaste waste = hazardousWasteMapper.selectById(detail.getWasteId());
            if (waste != null) {
                BigDecimal amount = detail.getPlannedAmount();
                
                // Heat value (Q) - cal/g
                totalHeatValue = totalHeatValue.add(
                    amount.multiply(Optional.ofNullable(waste.getHeatValueCalPerG()).orElse(BigDecimal.ZERO))
                );
                
                // Water content (M) - %
                totalWaterContent = totalWaterContent.add(
                    amount.multiply(Optional.ofNullable(waste.getWaterContentPercent()).orElse(BigDecimal.ZERO))
                );
                
                // Nitrogen content (N) - %
                totalNContent = totalNContent.add(
                    amount.multiply(Optional.ofNullable(waste.getNPercent()).orElse(BigDecimal.ZERO))
                );
                
                // Sulfur content (S) - %
                totalSContent = totalSContent.add(
                    amount.multiply(Optional.ofNullable(waste.getSPercent()).orElse(BigDecimal.ZERO))
                );
                
                // Chlorine content (Cl) - %
                totalClContent = totalClContent.add(
                    amount.multiply(Optional.ofNullable(waste.getClPercent()).orElse(BigDecimal.ZERO))
                );
                
                // Fluorine content (F) - %
                totalFContent = totalFContent.add(
                    amount.multiply(Optional.ofNullable(waste.getFPercent()).orElse(BigDecimal.ZERO))
                );

                // Phosphorus content (P) - %
                totalPContent = totalPContent.add(
                    amount.multiply(Optional.ofNullable(waste.getPPercent()).orElse(BigDecimal.ZERO))
                );

                // pH value
                totalPhValue = totalPhValue.add(
                    amount.multiply(Optional.ofNullable(waste.getPh()).orElse(BigDecimal.ZERO))
                );
                
                // Heavy metals - mg/L
                // Note: Mercury (Hg) field is not available in HazardousWaste entity
                // totalHgContent would need to be added when the field is available
                
                totalCdContent = totalCdContent.add(
                    amount.multiply(Optional.ofNullable(waste.getCdMgPerL()).orElse(BigDecimal.ZERO))
                );
                
                totalAsContent = totalAsContent.add(
                    amount.multiply(Optional.ofNullable(waste.getAsMgPerL()).orElse(BigDecimal.ZERO))
                );
                
                // For AS_NI_CONTENT constraint, we combine As and Ni
                totalNiContent = totalNiContent.add(
                    amount.multiply(Optional.ofNullable(waste.getNiMgPerL()).orElse(BigDecimal.ZERO))
                );
                
                totalPbContent = totalPbContent.add(
                    amount.multiply(Optional.ofNullable(waste.getPbMgPerL()).orElse(BigDecimal.ZERO))
                );
                
                // Heavy metals for Cr+Sn+Sb+Cu+Mn calculation
                totalCrContent = totalCrContent.add(
                    amount.multiply(Optional.ofNullable(waste.getCrMgPerL()).orElse(BigDecimal.ZERO))
                );
                totalSnContent = totalSnContent.add(
                    amount.multiply(Optional.ofNullable(waste.getSnMgPerL()).orElse(BigDecimal.ZERO))
                );
                totalSbContent = totalSbContent.add(
                    amount.multiply(Optional.ofNullable(waste.getSbMgPerL()).orElse(BigDecimal.ZERO))
                );
                totalCuContent = totalCuContent.add(
                    amount.multiply(Optional.ofNullable(waste.getCuMgPerL()).orElse(BigDecimal.ZERO))
                );
                totalMnContent = totalMnContent.add(
                    amount.multiply(Optional.ofNullable(waste.getMnMgPerL()).orElse(BigDecimal.ZERO))
                );
                
                log.debug("Processed waste {}: amount={}, heatValue={}, waterContent={}", 
                    waste.getWasteCode(), amount, waste.getHeatValueCalPerG(), waste.getWaterContentPercent());
            }
        }
        
        // Calculate weighted averages: (Σ Qᵢ × Dᵢ) / D₀
        RoundingMode roundingMode = RoundingMode.HALF_UP;
        
        // 🔧 FIX: Use consistent parameter names that match constraint mapping
        averages.put("heatValue", totalHeatValue.divide(totalAmount, 2, roundingMode));
        averages.put("waterContent", totalWaterContent.divide(totalAmount, 3, roundingMode));
        averages.put("nContent", totalNContent.divide(totalAmount, 4, roundingMode));
        averages.put("sContent", totalSContent.divide(totalAmount, 4, roundingMode));
        averages.put("clContent", totalClContent.divide(totalAmount, 4, roundingMode));
        averages.put("fContent", totalFContent.divide(totalAmount, 4, roundingMode));
        averages.put("pContent", totalPContent.divide(totalAmount, 4, roundingMode));
        averages.put("phValue", totalPhValue.divide(totalAmount, 4, roundingMode));
        averages.put("hgContent", BigDecimal.ZERO); // Mercury always 0 as per requirements
        averages.put("cdContent", totalCdContent.divide(totalAmount, 2, roundingMode));
        averages.put("asContent", totalAsContent.divide(totalAmount, 2, roundingMode)); // As alone for separate tracking
        averages.put("niContent", totalNiContent.divide(totalAmount, 2, roundingMode)); // Ni alone for separate tracking
        averages.put("pbContent", totalPbContent.divide(totalAmount, 2, roundingMode));
        
        // For AS_NI_CONTENT constraint: combine As + Ni
        BigDecimal combinedAsNi = (totalAsContent.add(totalNiContent)).divide(totalAmount, 2, roundingMode);
        averages.put("asNiContent", combinedAsNi); // Store As+Ni combined value
        
        // Calculate individual heavy metals averages for Cr+Sn+Sb+Cu+Mn
        averages.put("crContent", totalCrContent.divide(totalAmount, 2, roundingMode));
        averages.put("snContent", totalSnContent.divide(totalAmount, 2, roundingMode));
        averages.put("sbContent", totalSbContent.divide(totalAmount, 2, roundingMode));
        averages.put("cuContent", totalCuContent.divide(totalAmount, 2, roundingMode));
        averages.put("mnContent", totalMnContent.divide(totalAmount, 2, roundingMode));
        
        // Calculate total heavy metals (Cr+Sn+Sb+Cu+Mn) as per specification
        BigDecimal heavyMetalsTotal = (totalCrContent.add(totalSnContent)
            .add(totalSbContent).add(totalCuContent).add(totalMnContent))
            .divide(totalAmount, 2, roundingMode);
        averages.put("heavyMetalsTotal", heavyMetalsTotal);
        
        // Add total amount for constraint validation (convert from kg to t/d for validation)
        BigDecimal totalAmountTonnesPerDay = totalAmount.divide(new BigDecimal("1000"), 3, roundingMode);
        averages.put("totalAmount", totalAmountTonnesPerDay);
        
        log.info("Calculated weighted averages for session {}: {}", sessionId, averages);
        
        // Log important values for debugging
        if (averages.containsKey("heatValue")) {
            BigDecimal heatValue = (BigDecimal) averages.get("heatValue");
            BigDecimal heatValueKj = heatValue.multiply(new BigDecimal("4.184"));
            log.info("Heat value: {} cal/g = {} kJ/kg", heatValue, heatValueKj);
        }

        return averages;
    }

    // ==================== 控制器所需的新增方法实现 ====================

    @Override
    public List<Map<String, Object>> searchAvailableWastes(String keyword) {
        try {
            QueryWrapper<HazardousWaste> wrapper = new QueryWrapper<>();
            wrapper.and(w -> w.like("waste_name", keyword)
                            .or().like("waste_code", keyword)
                            .or().like("harmful_components", keyword))
                   .eq("deleted", false)
                   .gt("remaining_storage", 0);
            
            List<HazardousWaste> wastes = hazardousWasteMapper.selectList(wrapper);
            
            return wastes.stream().map(waste -> {
                Map<String, Object> wasteInfo = new HashMap<>();
                wasteInfo.put("id", waste.getId());
                wasteInfo.put("wasteCode", waste.getWasteCode());
                wasteInfo.put("wasteName", waste.getWasteName());
                wasteInfo.put("remainingStorage", waste.getRemainingStorage());
                wasteInfo.put("heatValue", waste.getHeatValueCalPerG());
                wasteInfo.put("waterContent", waste.getWaterContentPercent());
                wasteInfo.put("compatibilityCategory", waste.getCompatibilityCategoryCode());

                // Storage priority reminder (>= 365 days since inbound_time)
                Integer storageDays = null;
                Boolean priorityRecommend = false;
                if (waste.getInboundTime() != null) {
                    try {
                        long days = java.time.temporal.ChronoUnit.DAYS.between(
                            waste.getInboundTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(),
                            LocalDate.now()
                        );
                        storageDays = (int) Math.max(days, 0);
                        priorityRecommend = storageDays >= 365;
                    } catch (Exception ignored) {
                        // Keep null if date conversion fails
                    }
                }
                wasteInfo.put("storageDays", storageDays);
                wasteInfo.put("priorityRecommend", priorityRecommend);
                wasteInfo.put("priorityThresholdDays", 365);

                return wasteInfo;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("搜索可用危废失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Object> checkWasteStock(Long wasteId, BigDecimal requiredAmount) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            HazardousWaste waste = hazardousWasteMapper.selectById(wasteId);
            if (waste == null) {
                result.put("sufficient", false);
                result.put("remainingStock", BigDecimal.ZERO);
                result.put("message", "危废不存在");
                return result;
            }
            
            BigDecimal remainingStock = waste.getRemainingStorage() != null ? waste.getRemainingStorage() : BigDecimal.ZERO;
            boolean sufficient = remainingStock.compareTo(requiredAmount) >= 0;
            
            result.put("sufficient", sufficient);
            result.put("remainingStock", remainingStock);
            result.put("requiredAmount", requiredAmount);
            result.put("message", sufficient ? "库存充足" : "库存不足");
            
        } catch (Exception e) {
            log.error("检查危废库存失败: {}", e.getMessage(), e);
            result.put("sufficient", false);
            result.put("remainingStock", BigDecimal.ZERO);
            result.put("message", "检查失败");
        }
        
        return result;
    }

    @Override
    public List<MatchingDetails> getSessionWastes(Long sessionId) {
        try {
            return matchingDetailsMapper.selectBySessionId(sessionId);
        } catch (Exception e) {
            log.error("获取会话危废列表失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<List<Map<String, Object>>> getCompatibilityMatrix(Long sessionId) {
        try {
            List<MatchingDetails> sessionWastes = matchingDetailsMapper.selectBySessionId(sessionId);
            List<Long> wasteIds = sessionWastes.stream()
                .map(MatchingDetails::getWasteId)
                .collect(Collectors.toList());
            
            List<HazardousWaste> wastes = hazardousWasteMapper.selectBatchIds(wasteIds);
            
            List<List<Map<String, Object>>> matrix = new ArrayList<>();
            
            for (HazardousWaste waste1 : wastes) {
                List<Map<String, Object>> row = new ArrayList<>();
                for (HazardousWaste waste2 : wastes) {
                    Map<String, Object> cell = new HashMap<>();
                    cell.put("waste1Id", waste1.getId());
                    cell.put("waste2Id", waste2.getId());
                    cell.put("compatible", checkWasteCompatibility(waste1, waste2));
                    cell.put("riskLevel", calculateRiskLevel(waste1, waste2));
                    row.add(cell);
                }
                matrix.add(row);
            }
            
            return matrix;
            
        } catch (Exception e) {
            log.error("获取相容性矩阵失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<CompatibilityChecks> getCompatibilityCheckResults(Long sessionId) {
        try {
            log.debug("Retrieving compatibility check results for session: {}", sessionId);
            QueryWrapper<CompatibilityChecks> wrapper = new QueryWrapper<>();
            wrapper.eq("session_id", sessionId).orderByDesc("create_time");
            List<CompatibilityChecks> results = compatibilityChecksMapper.selectList(wrapper);
            
            log.info("Found {} compatibility check results for session: {}", results.size(), sessionId);
            
            if (results.isEmpty()) {
                log.warn("No compatibility check results found for session: {}. This might indicate that compatibility check was not performed or failed to save results.", sessionId);
            }
            
            return results;
        } catch (Exception e) {
            log.error("获取相容性检查结果失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Object> getCalculatedProperties(Long sessionId) {
        return calculateWeightedAverages(sessionId);
    }

    @Override
    public Map<String, Object> getMixingRatios(Long sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<MatchingDetails> sessionWastes = matchingDetailsMapper.selectBySessionId(sessionId);
            
            BigDecimal totalAmount = sessionWastes.stream()
                .map(MatchingDetails::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            List<Map<String, Object>> ratios = sessionWastes.stream().map(detail -> {
                Map<String, Object> ratio = new HashMap<>();
                ratio.put("wasteId", detail.getWasteId());
                ratio.put("actualAmount", detail.getActualAmount());
                
                if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal percentage = detail.getActualAmount()
                        .divide(totalAmount, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                    ratio.put("percentage", percentage);
                } else {
                    ratio.put("percentage", BigDecimal.ZERO);
                }
                
                return ratio;
            }).collect(Collectors.toList());
            
            result.put("totalAmount", totalAmount);
            result.put("ratios", ratios);
            
        } catch (Exception e) {
            log.error("获取混配比例失败: {}", e.getMessage(), e);
            result.put("totalAmount", BigDecimal.ZERO);
            result.put("ratios", Collections.emptyList());
        }
        
        return result;
    }

    @Override
    public List<CompatibilityChecks> getCompatibilityHistory(Long sessionId) {
        return getCompatibilityCheckResults(sessionId);
    }

    @Override
    public Map<String, Object> getSuccessStatistics(Integer days) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);
            
            // 查询时间范围内的会话数
            QueryWrapper<MatchingSessions> sessionWrapper = new QueryWrapper<>();
            sessionWrapper.ge("create_time", startTime);
            Long totalSessionsCount = matchingSessionsMapper.selectCount(sessionWrapper);
            int totalSessions = totalSessionsCount.intValue();
            
            // 查询成功的配伍结果数
            QueryWrapper<MatchingResults> resultWrapper = new QueryWrapper<>();
            resultWrapper.ge("calculation_time", startTime).eq("result_status", "success");
            Long successfulMatchingCount = matchingResultsMapper.selectCount(resultWrapper);
            int successfulMatching = successfulMatchingCount.intValue();
            
            // 查询成功的相容性检查数
            QueryWrapper<CompatibilityChecks> compatibilityWrapper = new QueryWrapper<>();
            compatibilityWrapper.ge("create_time", startTime).eq("check_result", "PASS");
            Long successfulCompatibilityCount = compatibilityChecksMapper.selectCount(compatibilityWrapper);
            int successfulCompatibility = successfulCompatibilityCount.intValue();
            
            result.put("period", days + "天");
            result.put("totalSessions", totalSessions);
            result.put("successfulMatching", successfulMatching);
            result.put("successfulCompatibility", successfulCompatibility);
            result.put("matchingSuccessRate", totalSessions > 0 ? 
                BigDecimal.valueOf(successfulMatching).divide(BigDecimal.valueOf(totalSessions), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            result.put("compatibilitySuccessRate", totalSessions > 0 ? 
                BigDecimal.valueOf(successfulCompatibility).divide(BigDecimal.valueOf(totalSessions), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            
        } catch (Exception e) {
            log.error("获取成功率统计失败: {}", e.getMessage(), e);
            result.put("error", "统计失败");
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getFailureReasons(Integer days) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);
            
            // 查询失败的相容性检查
            QueryWrapper<CompatibilityChecks> compatibilityWrapper = new QueryWrapper<>();
            compatibilityWrapper.ge("create_time", startTime).eq("check_result", "FAIL");
            List<CompatibilityChecks> failedChecks = compatibilityChecksMapper.selectList(compatibilityWrapper);
            
            // 统计失败原因
            Map<String, Integer> reasonCounts = new HashMap<>();
            for (CompatibilityChecks check : failedChecks) {
                String reason = check.getWastesCombination() != null ? check.getWastesCombination() : "未知原因";
                reasonCounts.put(reason, reasonCounts.getOrDefault(reason, 0) + 1);
            }
            
            result.put("period", days + "天");
            result.put("totalFailures", failedChecks.size());
            result.put("reasonStatistics", reasonCounts);
            result.put("commonReasons", reasonCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));
            
        } catch (Exception e) {
            log.error("获取失败原因统计失败: {}", e.getMessage(), e);
            result.put("error", "统计失败");
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getSessionProgress(Long sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查会话是否存在
            MatchingSessions session = matchingSessionsMapper.selectById(sessionId);
            if (session == null) {
                result.put("exists", false);
                return result;
            }
            
            // 检查各个阶段的完成情况
            List<MatchingDetails> wastes = matchingDetailsMapper.selectBySessionId(sessionId);
            boolean hasWastes = !wastes.isEmpty();
            
            QueryWrapper<CompatibilityChecks> compatibilityWrapper = new QueryWrapper<>();
            compatibilityWrapper.eq("session_id", sessionId);
            boolean hasCompatibilityCheck = compatibilityChecksMapper.selectCount(compatibilityWrapper) > 0;
            
            QueryWrapper<MatchingResults> resultWrapper = new QueryWrapper<>();
            resultWrapper.eq("session_id", sessionId);
            boolean hasMatchingResults = matchingResultsMapper.selectCount(resultWrapper) > 0;
            
            result.put("exists", true);
            result.put("sessionName", session.getSessionName());
            result.put("steps", Map.of(
                "wasteImport", Map.of("completed", hasWastes, "count", wastes.size()),
                "compatibilityCheck", Map.of("completed", hasCompatibilityCheck),
                "matching", Map.of("completed", hasMatchingResults)
            ));
            
            // 计算总体进度
            int completedSteps = 0;
            if (hasWastes) completedSteps++;
            if (hasCompatibilityCheck) completedSteps++;
            if (hasMatchingResults) completedSteps++;
            
            result.put("overallProgress", BigDecimal.valueOf(completedSteps).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP));
            
        } catch (Exception e) {
            log.error("获取会话进度失败: {}", e.getMessage(), e);
            result.put("error", "获取进度失败");
        }
        
        return result;
    }

    @Override
    public List<org.gsu.hwtttt.entity.CompatibilityCategory> getCompatibilityCategories() {
        try {
            return compatibilityCategoryMapper.selectList(new QueryWrapper<org.gsu.hwtttt.entity.CompatibilityCategory>().orderByAsc("idx"));
        } catch (Exception e) {
            log.error("获取相容性分类失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> checkWasteCompatibility(List<Long> wasteIds) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<HazardousWaste> wastes = hazardousWasteMapper.selectBatchIds(wasteIds);
            
            boolean compatible = true;
            List<String> incompatiblePairs = new ArrayList<>();
            
            for (int i = 0; i < wastes.size(); i++) {
                for (int j = i + 1; j < wastes.size(); j++) {
                    HazardousWaste waste1 = wastes.get(i);
                    HazardousWaste waste2 = wastes.get(j);
                    
                    if (!checkWasteCompatibility(waste1, waste2)) {
                        compatible = false;
                        incompatiblePairs.add(waste1.getWasteName() + " 与 " + waste2.getWasteName() + " 不相容");
                    }
                }
            }
            
            result.put("compatible", compatible);
            result.put("incompatiblePairs", incompatiblePairs);
            result.put("message", compatible ? "所有危废相容" : "存在不相容的危废组合");
            
        } catch (Exception e) {
            log.error("检查危废相容性失败: {}", e.getMessage(), e);
            result.put("compatible", false);
            result.put("message", "检查失败");
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getCompatibilityAnalysis(Long sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<CompatibilityChecks> checks = getCompatibilityCheckResults(sessionId);
            
            long totalPairs = checks.size();
            long compatiblePairs = checks.stream().mapToLong(check -> 
                Boolean.TRUE.equals(check.getCompatible()) ? 1 : 0).sum();
            
            result.put("totalPairs", totalPairs);
            result.put("compatiblePairs", compatiblePairs);
            result.put("incompatiblePairs", totalPairs - compatiblePairs);
            result.put("compatibilityRate", totalPairs > 0 ? 
                BigDecimal.valueOf(compatiblePairs).divide(BigDecimal.valueOf(totalPairs), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            
            List<String> riskFactors = checks.stream()
                .filter(check -> !Boolean.TRUE.equals(check.getCompatible()))
                .map(CompatibilityChecks::getCheckResult)
                .collect(Collectors.toList());
            
            result.put("riskFactors", riskFactors);
            
        } catch (Exception e) {
            log.error("获取相容性分析失败: {}", e.getMessage(), e);
            result.put("error", "分析失败");
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getMatchingConstraints() {
        List<Map<String, Object>> constraints = new ArrayList<>();
        
        try {
            List<MatchingConstraints> constraintList = matchingConstraintsMapper.selectList(null);
            for (MatchingConstraints constraint : constraintList) {
                Map<String, Object> constraintMap = new HashMap<>();
                constraintMap.put("id", constraint.getId());
                constraintMap.put("constraintName", constraint.getConstraintName());
                constraintMap.put("parameterCode", constraint.getParameterCode());
                constraintMap.put("minValue", constraint.getMinValue());
                constraintMap.put("maxValue", constraint.getMaxValue());
                constraintMap.put("unit", constraint.getUnit());
                constraintMap.put("description", constraint.getConstraintDesc());
                constraintMap.put("isActive", constraint.getIsActive());
                constraintMap.put("sortOrder", constraint.getSortOrder());
                constraints.add(constraintMap);
            }
        } catch (Exception e) {
            log.error("获取配伍约束失败: {}", e.getMessage(), e);
        }
        
        return constraints;
    }

    /**
     * 诊断相容性矩阵问题
     * 用于识别和记录可能导致TooManyResultsException的重复规则
     *
     * @return 诊断结果
     */
    public Map<String, Object> diagnoseCompatibilityMatrix() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查找重复规则
            List<CompatibilityMatrixMapper.DuplicateRuleStatistics> duplicates = 
                compatibilityMatrixMapper.findDuplicateRules();
            
            result.put("duplicateRulesCount", duplicates.size());
            result.put("duplicateRules", duplicates);
            
            // 统计总规则数
            long totalRules = compatibilityMatrixMapper.selectCount(null);
            result.put("totalRules", totalRules);
            
            // 获取统计信息
            CompatibilityMatrixMapper.CompatibilityStatistics stats = 
                compatibilityMatrixMapper.getCompatibilityStatistics();
            result.put("statistics", stats);
            
            // 记录诊断信息
            if (!duplicates.isEmpty()) {
                log.warn("发现 {} 组重复的相容性规则，可能导致TooManyResultsException", duplicates.size());
                for (CompatibilityMatrixMapper.DuplicateRuleStatistics duplicate : duplicates) {
                    log.warn("重复规则: {} - 数量: {}, ID范围: {}-{}", 
                        duplicate.getCategoryPair(), duplicate.getRuleCount(),
                        duplicate.getMinId(), duplicate.getMaxId());
                }
                result.put("hasIssues", true);
                result.put("recommendation", "建议清理重复的双向规则，保留最新的记录");
            } else {
                result.put("hasIssues", false);
                result.put("message", "相容性矩阵数据正常，无重复规则");
            }
            
        } catch (Exception e) {
            log.error("诊断相容性矩阵时发生错误: {}", e.getMessage(), e);
            result.put("error", true);
            result.put("message", "诊断失败: " + e.getMessage());
        }
        
        return result;
    }

    // ==================== 私有辅助方法 ====================

    private MatchingResponse.WasteDetail buildWasteDetail(MatchingDetails detail) {
        MatchingResponse.WasteDetail wasteDetail = new MatchingResponse.WasteDetail();
        wasteDetail.setWasteId(detail.getWasteId());
        wasteDetail.setQuantity(detail.getActualAmount());
        
        // 获取危废信息
        HazardousWaste waste = hazardousWasteMapper.selectById(detail.getWasteId());
        if (waste != null) {
            wasteDetail.setWasteCode(waste.getWasteCode());
            wasteDetail.setWasteName(waste.getWasteName());
            wasteDetail.setHeatValue(waste.getHeatValueCalPerG());
            wasteDetail.setWaterContent(waste.getWaterContentPercent());
        }
        
        return wasteDetail;
    }

    private MatchingResponse.MatchingIndicators buildIndicators(Map<String, Object> weightedAverages) {
        MatchingResponse.MatchingIndicators indicators = new MatchingResponse.MatchingIndicators();
        
        // Set all 11 required indicators
        indicators.setHeatValue((BigDecimal) weightedAverages.get("heatValue"));
        indicators.setWaterContent((BigDecimal) weightedAverages.get("waterContent"));
        indicators.setNitrogenContent((BigDecimal) weightedAverages.get("nContent"));
        indicators.setSulfurContent((BigDecimal) weightedAverages.get("sContent"));
        indicators.setChlorineContent((BigDecimal) weightedAverages.get("clContent"));
        indicators.setFluorineContent((BigDecimal) weightedAverages.get("fContent"));
        
        // Mercury is always 0 as per requirements
        indicators.setMercuryContent(BigDecimal.ZERO);
        
        // Heavy metals indicators
        indicators.setCadmiumContent((BigDecimal) weightedAverages.get("cdContent"));
        indicators.setArsenicNickelContent((BigDecimal) weightedAverages.get("asNiContent")); // Combined As+Ni
        indicators.setLeadContent((BigDecimal) weightedAverages.get("pbContent"));
        indicators.setTotalHeavyMetals((BigDecimal) weightedAverages.get("heavyMetalsTotal")); // Cr+Sn+Sb+Cu+Mn
        
        // Keep ash content for backward compatibility if needed
        indicators.setAshContent((BigDecimal) weightedAverages.get("ashContent"));
        
        return indicators;
    }

    private void saveMatchingResult(Long sessionId, MatchingResponse response) {
        try {
            MatchingResults result = new MatchingResults();
            result.setSessionId(sessionId);
            result.setCalculationTime(LocalDateTime.now());
            result.setCalculatedHeatValue(response.getActualHeatValue());
            
            // Store all calculated indicators if available
            if (response.getIndicators() != null) {
                MatchingResponse.MatchingIndicators indicators = response.getIndicators();
                result.setCalculatedWaterContent(indicators.getWaterContent());
                result.setCalculatedNContent(indicators.getNitrogenContent());
                result.setCalculatedSContent(indicators.getSulfurContent());
                result.setCalculatedClContent(indicators.getChlorineContent());
                result.setCalculatedFContent(indicators.getFluorineContent());
                result.setCalculatedHgContent(BigDecimal.ZERO); // Mercury always 0
                result.setCalculatedCdContent(indicators.getCadmiumContent());
                result.setCalculatedPbContent(indicators.getLeadContent());
                result.setCalculatedHeavyMetalsTotal(indicators.getTotalHeavyMetals());
            }
            
            // 根据响应状态设置结果状态
            if (Boolean.TRUE.equals(response.getSuccess())) {
                result.setStatusSuccess();
                log.info("Saving successful calculation result for session: {}", sessionId);
            } else {
                result.setStatusFailed();
                result.setFailureReasons(response.getMessage());
                
                // 🔧 FIX: Properly store constraint violations as JSON
                if (response.getConstraintViolations() != null && !response.getConstraintViolations().isEmpty()) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String violationsJson = mapper.writeValueAsString(response.getConstraintViolations());
                        result.setConstraintViolations(violationsJson);
                        log.warn("Constraint violations detected for session {}: {}", sessionId, response.getConstraintViolations());
                    } catch (Exception e) {
                        log.error("Failed to serialize constraint violations: {}", e.getMessage());
                        result.setConstraintViolations(String.join("; ", response.getConstraintViolations()));
                    }
                }
                
                log.error("Saving failed calculation result for session: {} - {}", sessionId, response.getMessage());
            }
            
            matchingResultsMapper.insert(result);
            log.info("Successfully saved matching result for session: {}", sessionId);
            
        } catch (Exception e) {
            log.error("Failed to save matching result for session {}: {}", sessionId, e.getMessage(), e);
            throw new RuntimeException("Failed to save calculation results", e);
        }
    }

    private List<String> buildRiskFactors(Map<String, Object> compatibility, Map<String, Object> constraints) {
        List<String> factors = new ArrayList<>();
        
        if (!(Boolean) compatibility.get("compatible")) {
            factors.add("相容性风险: " + compatibility.get("reason"));
        }
        
        if (!(Boolean) constraints.get("allPassed")) {
            factors.add("约束条件风险: 部分指标超出合理范围");
        }
        
        return factors;
    }

    private List<String> buildRecommendations(String riskLevel) {
        List<String> recommendations = new ArrayList<>();
        
        switch (riskLevel) {
            case "HIGH":
                recommendations.add("建议重新选择危废组合");
                recommendations.add("加强安全防护措施");
                break;
            case "MEDIUM":
                recommendations.add("调整危废配比");
                recommendations.add("优化工艺参数");
                break;
            case "LOW":
                recommendations.add("当前方案可行");
                recommendations.add("继续监控关键指标");
                break;
        }
        
        return recommendations;
    }

    private List<String> buildFailureSuggestions(List<String> failureReasons) {
        List<String> suggestions = new ArrayList<>();
        
        for (String reason : failureReasons) {
            if (reason.contains("相容性")) {
                suggestions.add("重新选择相容的危废类型");
            } else if (reason.contains("约束")) {
                suggestions.add("调整危废用量或配比");
            } else {
                suggestions.add("检查输入数据的完整性");
            }
        }
        
        return suggestions;
    }

    private BigDecimal calculateOptimizationScore(Long sessionId) {
        try {
            Map<String, Object> constraints = getConstraintCheckResults(sessionId);
            if ((Boolean) constraints.get("allPassed")) {
                return BigDecimal.valueOf(95); // 高分
            } else {
                return BigDecimal.valueOf(60); // 中等分
            }
        } catch (Exception e) {
            return BigDecimal.valueOf(50); // 低分
        }
    }

    private boolean checkWasteCompatibility(HazardousWaste waste1, HazardousWaste waste2) {
        if (waste1.getId().equals(waste2.getId())) {
            return true; // 同一个危废自然相容
        }
        
        // 氧化性 + 易燃性 = 不相容
        if ((Boolean.TRUE.equals(waste1.getOxidizing()) && Boolean.TRUE.equals(waste2.getFlammable())) ||
            (Boolean.TRUE.equals(waste1.getFlammable()) && Boolean.TRUE.equals(waste2.getOxidizing()))) {
            return false;
        }
        
        // 毒性 + 卤化烃 = 不相容
        if ((Boolean.TRUE.equals(waste1.getToxic()) && Boolean.TRUE.equals(waste2.getHalogenatedHydrocarbon())) ||
            (Boolean.TRUE.equals(waste1.getHalogenatedHydrocarbon()) && Boolean.TRUE.equals(waste2.getToxic()))) {
            return false;
        }
        
        return true; // 默认相容
    }

    private String calculateRiskLevel(HazardousWaste waste1, HazardousWaste waste2) {
        if (!checkWasteCompatibility(waste1, waste2)) {
            return "HIGH";
        }
        
        // 检查是否有危险特性
        boolean hasDangerousProperty = 
            Boolean.TRUE.equals(waste1.getToxic()) || Boolean.TRUE.equals(waste2.getToxic()) ||
            Boolean.TRUE.equals(waste1.getCorrosive()) || Boolean.TRUE.equals(waste2.getCorrosive()) ||
            Boolean.TRUE.equals(waste1.getReactive()) || Boolean.TRUE.equals(waste2.getReactive());
        
        return hasDangerousProperty ? "MEDIUM" : "LOW";
    }

    /**
     * 🔥 核心线性规划优化方法
     * 实现真正的数学优化算法：D = [D₁, D₂, D₃...Dₙ] 
     * 约束条件：D×Aᵢ/D₀ ≤ Limitᵢ
     */
    private LinearProgrammingUtil.SolutionResult optimizeWasteMixing(Long sessionId) {
        try {
            // 1. 获取会话中的危废数据
            List<MatchingDetails> sessionWastes = matchingDetailsMapper.selectBySessionId(sessionId);
            if (sessionWastes.isEmpty()) {
                throw new RuntimeException("会话中无危废数据");
            }

            // 2. 构建系数矩阵 A = [Q₁ M₁ N₁ S₁...] 
            List<LinearProgrammingUtil.WasteData> wasteDataList = new ArrayList<>();
            BigDecimal totalPlannedCapacity = BigDecimal.ZERO;
            
            for (MatchingDetails detail : sessionWastes) {
                HazardousWaste waste = hazardousWasteMapper.selectById(detail.getWasteId());
                if (waste != null) {
                    LinearProgrammingUtil.WasteData wasteData = new LinearProgrammingUtil.WasteData();
                    wasteData.setWasteId(waste.getId());
                    wasteData.setMaxQuantity(waste.getRemainingStorage() != null ? waste.getRemainingStorage() : new BigDecimal("1000"));
                    
                    // 系数矩阵列：Q (热值), M (水分), N, S, Cl, F, 重金属等
                    wasteData.setHeatValue(waste.getHeatValueCalPerG() != null ? waste.getHeatValueCalPerG() : BigDecimal.ZERO);
                    wasteData.setWaterContent(waste.getWaterContentPercent() != null ? waste.getWaterContentPercent() : BigDecimal.ZERO);
                    wasteData.setNContent(waste.getNPercent() != null ? waste.getNPercent() : BigDecimal.ZERO);
                    wasteData.setSContent(waste.getSPercent() != null ? waste.getSPercent() : BigDecimal.ZERO);
                    wasteData.setClContent(waste.getClPercent() != null ? waste.getClPercent() : BigDecimal.ZERO);
                    wasteData.setFContent(waste.getFPercent() != null ? waste.getFPercent() : BigDecimal.ZERO);
                    wasteData.setCdContent(waste.getCdMgPerL() != null ? waste.getCdMgPerL() : BigDecimal.ZERO);
                    wasteData.setAsContent(waste.getAsMgPerL() != null ? waste.getAsMgPerL() : BigDecimal.ZERO);
                    wasteData.setPbContent(waste.getPbMgPerL() != null ? waste.getPbMgPerL() : BigDecimal.ZERO);
                    
                    // 计算组合重金属含量 Cr+Sn+Sb+Cu+Mn
                    BigDecimal heavyMetalTotal = BigDecimal.ZERO;
                    if (waste.getCrMgPerL() != null) heavyMetalTotal = heavyMetalTotal.add(waste.getCrMgPerL());
                    if (waste.getSnMgPerL() != null) heavyMetalTotal = heavyMetalTotal.add(waste.getSnMgPerL());
                    if (waste.getSbMgPerL() != null) heavyMetalTotal = heavyMetalTotal.add(waste.getSbMgPerL());
                    if (waste.getCuMgPerL() != null) heavyMetalTotal = heavyMetalTotal.add(waste.getCuMgPerL());
                    if (waste.getMnMgPerL() != null) heavyMetalTotal = heavyMetalTotal.add(waste.getMnMgPerL());
                    wasteData.setCrContent(heavyMetalTotal); // 临时存储总重金属含量
                    
                    wasteDataList.add(wasteData);
                    totalPlannedCapacity = totalPlannedCapacity.add(detail.getPlannedAmount());
                }
            }

            // 3. 设置控制参数 - 完全按照数学模型规范
            LinearProgrammingUtil.ControlParameters parameters = new LinearProgrammingUtil.ControlParameters();
            parameters.setHeatValueMin(new BigDecimal("12500"));  // kJ/kg
            parameters.setHeatValueMax(new BigDecimal("16800"));  // kJ/kg
            parameters.setWaterContentMax(new BigDecimal("45"));  // %
            parameters.setNContentMax(new BigDecimal("2"));       // %
            parameters.setSContentMax(new BigDecimal("3"));       // %
            parameters.setClContentMax(new BigDecimal("1.5"));    // %
            parameters.setFContentMax(new BigDecimal("1"));       // %
            parameters.setCdContentMax(new BigDecimal("1"));      // mg/kg
            parameters.setAsNiContentMax(new BigDecimal("95"));   // As+Ni mg/kg
            parameters.setPbContentMax(new BigDecimal("70"));     // mg/kg
            parameters.setHeavyMetalContentMax(new BigDecimal("800")); // Cr+Sn+Sb+Cu+Mn mg/kg

            // 4. 调用线性规划求解器
            return linearProgrammingUtil.solve(wasteDataList, totalPlannedCapacity, parameters);
            
        } catch (Exception e) {
            log.error("线性规划优化失败: {}", e.getMessage(), e);
            LinearProgrammingUtil.SolutionResult errorResult = new LinearProgrammingUtil.SolutionResult();
            errorResult.setFeasible(false);
            errorResult.setErrorMessage("优化计算失败: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 更新优化后的用量到数据库
     * 将线性规划求解的决策变量 D₁, D₂...Dₙ 保存到数据库
     */
    @Transactional
    public void updateOptimizedAmounts(Long sessionId, Map<Long, BigDecimal> optimizedQuantities) {
        try {
            for (Map.Entry<Long, BigDecimal> entry : optimizedQuantities.entrySet()) {
                Long wasteId = entry.getKey();
                BigDecimal optimizedAmount = entry.getValue();
                
                // 更新实际用量为优化后的量
                matchingDetailsMapper.updateActualAmount(sessionId, wasteId, optimizedAmount);
                
                log.info("更新危废 {} 的优化用量为: {}kg", wasteId, optimizedAmount);
            }
        } catch (Exception e) {
            log.error("更新优化用量失败: {}", e.getMessage(), e);
            throw new RuntimeException("更新优化用量失败", e);
        }
    }

    @Override
    public Map<String, Object> validateSessionWastes(Long sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<MatchingDetails> sessionWastes = matchingDetailsMapper.selectBySessionId(sessionId);
            
            if (sessionWastes.isEmpty()) {
                result.put("valid", false);
                result.put("message", "No wastes in session");
                return result;
            }
            
            // Check stock availability for all wastes
            List<String> stockIssues = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            for (MatchingDetails detail : sessionWastes) {
                totalAmount = totalAmount.add(detail.getActualAmount());
                
                HazardousWaste waste = hazardousWasteMapper.selectById(detail.getWasteId());
                if (waste != null && waste.getRemainingStorage().compareTo(detail.getActualAmount()) < 0) {
                    stockIssues.add("Waste ID " + detail.getWasteId() + ": required " + 
                        detail.getActualAmount() + " kg, available " + waste.getRemainingStorage() + " kg");
                }
            }
            
            result.put("valid", stockIssues.isEmpty());
            result.put("totalAmount", totalAmount);
            result.put("wasteCount", sessionWastes.size());
            result.put("stockIssues", stockIssues);
            result.put("message", stockIssues.isEmpty() ? "All validations passed" : "Stock issues found");
            
        } catch (Exception e) {
            log.error("Session waste validation failed: {}", e.getMessage(), e);
            result.put("valid", false);
            result.put("message", "Validation failed: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getCompatibilityCheckResult(Long sessionId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Get the latest compatibility check results
            QueryWrapper<CompatibilityChecks> wrapper = new QueryWrapper<>();
            wrapper.eq("session_id", sessionId)
                   .orderByDesc("create_time")
                   .last("LIMIT 1");
            
            CompatibilityChecks latestCheck = compatibilityChecksMapper.selectOne(wrapper);
            
            if (latestCheck == null) {
                result.put("checked", false);
                result.put("message", "No compatibility check performed yet");
                return result;
            }
            
            result.put("checked", true);
            result.put("compatible", latestCheck.getCompatible());
            result.put("checkResult", latestCheck.getCheckResult());
            result.put("riskLevel", latestCheck.getRiskLevel());
            result.put("checkTime", latestCheck.getCreateTime());
            result.put("details", getCompatibilityCheckResults(sessionId));
            
        } catch (Exception e) {
            log.error("Failed to get compatibility check result: {}", e.getMessage(), e);
            result.put("checked", false);
            result.put("message", "Failed to get check result: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> exportSessionResults(Long sessionId, String format) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            MatchingSessions session = matchingSessionsMapper.selectById(sessionId);
            if (session == null) {
                result.put("success", false);
                result.put("message", "Session not found");
                return result;
            }
            
            // Get all session data
            List<MatchingDetails> wastes = getSessionWastes(sessionId);
            Map<String, Object> properties = getCalculatedProperties(sessionId);
            Map<String, Object> ratios = getMixingRatios(sessionId);
            List<CompatibilityChecks> compatibility = getCompatibilityCheckResults(sessionId);
            
            Map<String, Object> exportData = Map.of(
                "session", session,
                "wastes", wastes,
                "properties", properties,
                "ratios", ratios,
                "compatibility", compatibility,
                "exportTime", java.time.LocalDateTime.now(),
                "format", format
            );
            
            result.put("success", true);
            result.put("data", exportData);
            result.put("fileName", "session_" + sessionId + "_" + System.currentTimeMillis() + "." + format);
            
        } catch (Exception e) {
            log.error("Failed to export session results: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Export failed: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public boolean updateMatchingConstraints(List<Map<String, Object>> constraints) {
        try {
            // Update constraint parameters in database
            for (Map<String, Object> constraint : constraints) {
                String paramName = (String) constraint.get("paramName");
                Object paramValue = constraint.get("paramValue");
                Boolean isActive = (Boolean) constraint.getOrDefault("isActive", true);
                
                // Update existing constraint or insert new one
                QueryWrapper<MatchingConstraints> wrapper = new QueryWrapper<>();
                wrapper.eq("constraint_name", paramName);
                MatchingConstraints existingConstraint = matchingConstraintsMapper.selectOne(wrapper);
                
                if (existingConstraint != null) {
                    existingConstraint.setMaxValue(new BigDecimal(paramValue.toString()));
                    existingConstraint.setIsActive(isActive);
                    matchingConstraintsMapper.updateById(existingConstraint);
                } else {
                    MatchingConstraints newConstraint = new MatchingConstraints();
                    newConstraint.setConstraintName(paramName);
                    newConstraint.setMaxValue(new BigDecimal(paramValue.toString()));
                    newConstraint.setIsActive(isActive);
                    matchingConstraintsMapper.insert(newConstraint);
                }
            }
            
            log.info("Updated {} matching constraints", constraints.size());
            return true;
            
        } catch (Exception e) {
            log.error("Failed to update matching constraints: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<List<Map<String, Object>>> getFullCompatibilityMatrix() {
        List<List<Map<String, Object>>> matrix = new ArrayList<>();
        
        try {
            // Get all compatibility categories
            QueryWrapper<CompatibilityCategory> wrapper = new QueryWrapper<>();
            wrapper.orderByAsc("idx");
            List<CompatibilityCategory> categories = compatibilityCategoryMapper.selectList(wrapper);
            
            for (CompatibilityCategory cat1 : categories) {
                List<Map<String, Object>> row = new ArrayList<>();
                for (CompatibilityCategory cat2 : categories) {
                    Map<String, Object> cell = new HashMap<>();
                    cell.put("category1", cat1.getCategoryCode());
                    cell.put("category2", cat2.getCategoryCode());
                    cell.put("category1Name", cat1.getCategoryNameCn());
                    cell.put("category2Name", cat2.getCategoryNameCn());
                    
                    // Check compatibility using CompatibilityUtil
                    boolean compatible = org.gsu.hwtttt.util.CompatibilityUtil.isCompatible(
                        cat1.getIdx(), cat2.getIdx());
                    cell.put("compatible", compatible);
                    
                    if (!compatible) {
                        // Get incompatibility matrix data
                        QueryWrapper<CompatibilityMatrix> matrixWrapper = new QueryWrapper<>();
                        matrixWrapper.eq("category_1", cat1.getCategoryCode())
                                   .eq("category_2", cat2.getCategoryCode());
                        CompatibilityMatrix matrixEntry = compatibilityMatrixMapper.selectOne(matrixWrapper);
                        
                        if (matrixEntry != null) {
                            cell.put("incompatibleReason", matrixEntry.getIncompatibleReason());
                            cell.put("riskLevel", matrixEntry.getRiskLevel());
                        }
                    }
                    
                    row.add(cell);
                }
                matrix.add(row);
            }
            
        } catch (Exception e) {
            log.error("Failed to get full compatibility matrix: {}", e.getMessage(), e);
        }
        
        return matrix;
    }

    @Override
    public SessionSummaryResponse getSessionSummary(Long sessionId) {
        log.info("Getting session summary for sessionId: {}", sessionId);
        
        try {
            // 1. Get session basic info
            MatchingSessions session = matchingSessionsMapper.selectById(sessionId);
            if (session == null) {
                throw new RuntimeException("Session not found: " + sessionId);
            }
            
            // 2. Get wastes in session
            List<MatchingDetails> sessionWastes = matchingDetailsMapper.selectBySessionId(sessionId);
            
            // 3. Build response
            SessionSummaryResponse response = new SessionSummaryResponse();
            response.setSessionId(sessionId);
            response.setSessionName(session.getSessionName());
            response.setStatus(session.getStatus());
            response.setTargetHeatValue(session.getTargetHeatValue());
            response.setLastUpdateTime(session.getUpdateTime());
            response.setWasteCount(sessionWastes.size());
            
            // 4. Calculate total planned amount
            BigDecimal totalAmount = sessionWastes.stream()
                    .map(MatchingDetails::getPlannedAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            response.setTotalAmount(totalAmount);
            
            // 5. Build waste summary items
            List<SessionSummaryResponse.WasteSummaryItem> wasteSummaryItems = new ArrayList<>();
            for (MatchingDetails detail : sessionWastes) {
                HazardousWaste waste = hazardousWasteMapper.selectById(detail.getWasteId());
                if (waste != null) {
                    SessionSummaryResponse.WasteSummaryItem item = new SessionSummaryResponse.WasteSummaryItem();
                    item.setWasteId(waste.getId());
                    item.setWasteCode(waste.getWasteCode());
                    item.setWasteName(waste.getWasteName());
                    item.setSourceUnit(waste.getSourceUnit());
                    item.setPlannedAmount(detail.getPlannedAmount());
                    item.setRemainingStorage(waste.getRemainingStorage());
                    item.setStockSufficient(detail.getPlannedAmount().compareTo(waste.getRemainingStorage()) <= 0);
                    
                    // Convert heat value from cal/g to kJ/kg (multiply by 4.184)
                    if (waste.getHeatValueCalPerG() != null) {
                        item.setHeatValueKjPerKg(waste.getHeatValueCalPerG().multiply(new BigDecimal("4.184")));
                    }
                    
                    item.setWaterContentPercent(waste.getWaterContentPercent());
                    // Convert compatibility category code to integer if needed
                    if (waste.getCompatibilityCategoryCode() != null) {
                        try {
                            item.setCompatibilityCategoryCode(Integer.parseInt(waste.getCompatibilityCategoryCode()));
                        } catch (NumberFormatException e) {
                            item.setCompatibilityCategoryCode(null);
                        }
                    }
                    item.setNitrogenContent(waste.getNPercent());
                    item.setSulfurContent(waste.getSPercent());
                    item.setChlorineContent(waste.getClPercent());
                    item.setFluorineContent(waste.getFPercent());
                    // Set heavy metals content (mg/L values)
                    item.setMercuryContent(null); // Mercury field not available in current entity
                    item.setCadmiumContent(waste.getCdMgPerL());
                    item.setLeadContent(waste.getPbMgPerL());
                    
                    wasteSummaryItems.add(item);
                }
            }
            response.setWastes(wasteSummaryItems);
            
            // 6. Build compatibility summary
            SessionSummaryResponse.CompatibilitySummary compatibilitySummary = new SessionSummaryResponse.CompatibilitySummary();
            
            // Check if compatibility check has been performed
            QueryWrapper<CompatibilityChecks> checksWrapper = new QueryWrapper<>();
            checksWrapper.eq("session_id", sessionId);
            List<CompatibilityChecks> compatibilityChecks = compatibilityChecksMapper.selectList(checksWrapper);
            compatibilitySummary.setChecked(!compatibilityChecks.isEmpty());
            
            if (!compatibilityChecks.isEmpty()) {
                boolean allCompatible = compatibilityChecks.stream().allMatch(CompatibilityChecks::getCompatible);
                compatibilitySummary.setCompatible(allCompatible);
                
                List<CompatibilityChecks> incompatiblePairs = compatibilityChecks.stream()
                        .filter(check -> !check.getCompatible())
                        .collect(Collectors.toList());
                
                compatibilitySummary.setIncompatiblePairs(incompatiblePairs.size());
                
                // Extract risk factors from incompatible pairs
                Set<String> riskFactors = new HashSet<>();
                List<SessionSummaryResponse.IncompatiblePairDetail> pairDetails = new ArrayList<>();
                
                for (CompatibilityChecks check : incompatiblePairs) {
                    HazardousWaste waste1 = hazardousWasteMapper.selectById(check.getWasteId1());
                    HazardousWaste waste2 = hazardousWasteMapper.selectById(check.getWasteId2());
                    
                    if (waste1 != null && waste2 != null) {
                        SessionSummaryResponse.IncompatiblePairDetail pairDetail = new SessionSummaryResponse.IncompatiblePairDetail();
                        pairDetail.setWasteId1(waste1.getId());
                        pairDetail.setWasteCode1(waste1.getWasteCode());
                        pairDetail.setWasteId2(waste2.getId());
                        pairDetail.setWasteCode2(waste2.getWasteCode());
                        
                        // Parse risk codes from conflict reason
                        String conflictReason = check.getConflictReason();
                        List<String> riskCodes = parseRiskCodes(conflictReason);
                        pairDetail.setRiskCodes(riskCodes);
                        pairDetail.setRiskDescription(conflictReason);
                        
                        riskFactors.addAll(riskCodes);
                        pairDetails.add(pairDetail);
                    }
                }
                
                compatibilitySummary.setRiskFactors(new ArrayList<>(riskFactors));
                compatibilitySummary.setIncompatiblePairDetails(pairDetails);
            } else {
                compatibilitySummary.setCompatible(null);
                compatibilitySummary.setIncompatiblePairs(0);
                compatibilitySummary.setRiskFactors(new ArrayList<>());
                compatibilitySummary.setIncompatiblePairDetails(new ArrayList<>());
            }
            
            response.setCompatibility(compatibilitySummary);
            
            // 7. Build calculation readiness
            SessionSummaryResponse.CalculationReadiness readiness = new SessionSummaryResponse.CalculationReadiness();
            List<String> blockers = new ArrayList<>();
            
            // Check stock sufficiency
            boolean stockSufficient = wasteSummaryItems.stream().allMatch(SessionSummaryResponse.WasteSummaryItem::getStockSufficient);
            readiness.setStockSufficient(stockSufficient);
            if (!stockSufficient) {
                blockers.add("Insufficient stock for some wastes");
            }
            
            // Check compatibility
            boolean compatibilityPassed = compatibilitySummary.getChecked() != null && compatibilitySummary.getChecked() 
                    && compatibilitySummary.getCompatible() != null && compatibilitySummary.getCompatible();
            readiness.setCompatibilityPassed(compatibilityPassed);
            if (!compatibilityPassed) {
                if (compatibilitySummary.getChecked() == null || !compatibilitySummary.getChecked()) {
                    blockers.add("Compatibility check not performed");
                } else {
                    blockers.add("Compatibility check failed");
                }
            }
            
            // Check session status
            boolean statusReady = Arrays.asList("compatible", "ready_for_calculation").contains(session.getStatus());
            readiness.setStatusReady(statusReady);
            if (!statusReady) {
                blockers.add("Session status does not allow calculation: " + session.getStatus());
            }
            
            // Check total amount
            boolean totalAmountSufficient = totalAmount.compareTo(new BigDecimal("1000")) >= 0; // Minimum 1000 kg
            readiness.setTotalAmountSufficient(totalAmountSufficient);
            if (!totalAmountSufficient) {
                blockers.add("Total amount too small (minimum 1000 kg required)");
            }
            
            // Calculate preliminary heat value estimate
            if (!wasteSummaryItems.isEmpty()) {
                BigDecimal weightedHeatValue = BigDecimal.ZERO;
                BigDecimal totalWeight = BigDecimal.ZERO;
                
                for (SessionSummaryResponse.WasteSummaryItem item : wasteSummaryItems) {
                    if (item.getHeatValueKjPerKg() != null && item.getPlannedAmount() != null) {
                        BigDecimal contribution = item.getHeatValueKjPerKg().multiply(item.getPlannedAmount());
                        weightedHeatValue = weightedHeatValue.add(contribution);
                        totalWeight = totalWeight.add(item.getPlannedAmount());
                    }
                }
                
                if (totalWeight.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal estimatedHeatValue = weightedHeatValue.divide(totalWeight, 2, RoundingMode.HALF_UP);
                    readiness.setEstimatedHeatValue(estimatedHeatValue);
                }
            }
            
            readiness.setBlockers(blockers);
            readiness.setReady(blockers.isEmpty());
            
            response.setReadiness(readiness);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error getting session summary for sessionId: {}", sessionId, e);
            throw new RuntimeException("Failed to get session summary: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parse risk codes from incompatible reason string
     */
    private List<String> parseRiskCodes(String incompatibleReason) {
        List<String> riskCodes = new ArrayList<>();
        if (incompatibleReason != null) {
            String[] codes = {"H", "F", "G", "GT", "E", "P", "S", "U"};
            for (String code : codes) {
                if (incompatibleReason.contains(code)) {
                    riskCodes.add(code);
                }
            }
        }
        return riskCodes;
    }
} 