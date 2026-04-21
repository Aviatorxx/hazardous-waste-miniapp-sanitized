package org.gsu.hwtttt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gsu.hwtttt.common.result.Result;
import org.gsu.hwtttt.dto.request.MatchingRequest;
import org.gsu.hwtttt.dto.response.MatchingResponse;
import org.gsu.hwtttt.entity.*;
import org.gsu.hwtttt.service.MatchingService;
import org.gsu.hwtttt.service.MatchingSessionsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit Tests for Matching Controllers
 * Tests controller layer with mocked services
 *
 * @author WenXin
 * @date 2025/01/07
 */
@WebMvcTest(MatchingController.class)
class MatchingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MatchingService matchingService;

    @MockBean
    private MatchingSessionsService matchingSessionsService;

    private MatchingSessions testSession;
    private MatchingDetails testDetail;
    private CompatibilityChecks testCompatibilityCheck;
    private CompatibilityCategory testCategory;

    @BeforeEach
    void setUp() {
        // Setup test data
        testSession = new MatchingSessions();
        testSession.setId(1L);
        testSession.setSessionName("Test Session");
        testSession.setStatus("draft");
        testSession.setCreateTime(LocalDateTime.now());
        testSession.setCreateUser("test_user");

        testDetail = new MatchingDetails();
        testDetail.setId(1L);
        testDetail.setSessionId(1L);
        testDetail.setWasteId(1L);
        testDetail.setPlannedAmount(new BigDecimal("100"));
        testDetail.setActualAmount(new BigDecimal("100"));

        testCompatibilityCheck = new CompatibilityChecks();
        testCompatibilityCheck.setId(1L);
        testCompatibilityCheck.setSessionId(1L);
        testCompatibilityCheck.setWasteId1(1L);
        testCompatibilityCheck.setWasteId2(2L);
        testCompatibilityCheck.setCompatible(true);
        testCompatibilityCheck.setCheckResult("Compatible");

        testCategory = new CompatibilityCategory();
        testCategory.setCategoryCode("A01");
        testCategory.setCategoryNameCn("爆炸性物质");
        testCategory.setCategoryNameEn("Explosive substances");
        testCategory.setIdx(1);
    }

    // ==================== Session Management Tests ====================

    @Test
    @DisplayName("Create Session - Success")
    void testCreateSession_Success() throws Exception {
        // Mock service response
        when(matchingSessionsService.createSession(any(MatchingSessions.class))).thenReturn(testSession);

        MatchingRequest request = new MatchingRequest();
        request.setSessionName("Test Session");
        request.setTargetHeatValue(new BigDecimal("15000"));
        request.setTotalAmount(new BigDecimal("1000"));
        request.setCreateUser("test_user");

        mockMvc.perform(post("/api/matching/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(1L))
                .andExpect(jsonPath("$.data.sessionName").value("Test Session"))
                .andExpect(jsonPath("$.data.status").value("draft"));
    }

    @Test
    @DisplayName("Create Session - Validation Error")
    void testCreateSession_ValidationError() throws Exception {
        MatchingRequest request = new MatchingRequest();
        // Missing required fields

        mockMvc.perform(post("/api/matching/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get User Sessions - Success")
    void testGetUserSessions_Success() throws Exception {
        List<MatchingSessions> sessions = List.of(testSession);
        when(matchingSessionsService.getSessionHistoryList(anyString(), any(Integer.class), any(Integer.class)))
                .thenReturn(sessions);

        mockMvc.perform(get("/api/matching/sessions")
                .param("username", "test_user")
                .param("pageNo", "1")
                .param("pageSize", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].sessionName").value("Test Session"));
    }

    @Test
    @DisplayName("Delete Session - Success")
    void testDeleteSession_Success() throws Exception {
        when(matchingSessionsService.deleteSession(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/matching/sessions/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    // ==================== Waste Management Tests ====================

    @Test
    @DisplayName("Add Waste to Session - Success")
    void testAddWasteToSession_Success() throws Exception {
        Map<String, Object> stockCheck = Map.of("sufficient", true, "remainingStock", 1000);
        Map<String, Object> addResult = Map.of(
            "success", true,
            "message", "危废添加成功",
            "alreadyExists", false,
            "matchingDetailId", 1L,
            "sessionId", 1L,
            "wasteId", 1L,
            "plannedAmount", 100.0
        );
        Result<Map<String, Object>> serviceResult = Result.success("危废添加成功", addResult);
        
        when(matchingService.checkWasteStock(anyLong(), any(BigDecimal.class))).thenReturn(stockCheck);
        when(matchingService.addWasteToSession(anyLong(), anyLong(), any(Double.class))).thenReturn(serviceResult);

        mockMvc.perform(post("/api/matching/sessions/1/wastes")
                .param("wasteId", "1")
                .param("plannedAmount", "100"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.wasteId").value(1))
                .andExpect(jsonPath("$.data.plannedAmount").value(100));
    }

    @Test
    @DisplayName("Add Waste to Session - Insufficient Stock")
    void testAddWasteToSession_InsufficientStock() throws Exception {
        Map<String, Object> stockCheck = Map.of("sufficient", false, "remainingStock", 50);
        when(matchingService.checkWasteStock(anyLong(), any(BigDecimal.class))).thenReturn(stockCheck);

        mockMvc.perform(post("/api/matching/sessions/1/wastes")
                .param("wasteId", "1")
                .param("plannedAmount", "100"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Insufficient stock")));
    }

    @Test
    @DisplayName("Update Waste Amount - Success")
    void testUpdateWasteAmount_Success() throws Exception {
        Map<String, Object> stockCheck = Map.of("sufficient", true, "remainingStock", 1000);
        when(matchingService.checkWasteStock(anyLong(), any(BigDecimal.class))).thenReturn(stockCheck);
        when(matchingService.updateWasteQuantity(anyLong(), anyLong(), any(Double.class))).thenReturn(true);

        mockMvc.perform(put("/api/matching/sessions/1/wastes/1/amount")
                .param("plannedAmount", "150"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("Get Session Wastes - Success")
    void testGetSessionWastes_Success() throws Exception {
        List<MatchingDetails> details = List.of(testDetail);
        when(matchingService.getSessionWastes(1L)).thenReturn(details);

        mockMvc.perform(get("/api/matching/sessions/1/wastes"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("Remove Waste from Session - Success")
    void testRemoveWasteFromSession_Success() throws Exception {
        when(matchingService.removeWasteFromSession(1L, 1L)).thenReturn(true);

        mockMvc.perform(delete("/api/matching/sessions/1/wastes/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    // ==================== Compatibility Check Tests ====================

    @Test
    @DisplayName("Check Compatibility by Session - Success")
    void testCheckCompatibilityBySession_Success() throws Exception {
        Map<String, Object> compatibilityResult = Map.of(
            "compatible", true,
            "message", "All wastes are compatible"
        );
        when(matchingService.performCompatibilityCheck(1L)).thenReturn(compatibilityResult);

        String requestBody = objectMapper.writeValueAsString(Map.of("sessionId", 1L));

        mockMvc.perform(post("/api/compatibility/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.compatible").value(true))
                .andExpect(jsonPath("$.data.message").value("All wastes are compatible"));
    }

    @Test
    @DisplayName("Check Compatibility by Waste IDs - Success")
    void testCheckCompatibilityByWasteIds_Success() throws Exception {
        Map<String, Object> compatibilityResult = Map.of(
            "compatible", false,
            "incompatiblePairs", List.of("Waste A and Waste B are incompatible"),
            "message", "Incompatible waste combinations found"
        );
        when(matchingService.checkWasteCompatibility(any())).thenReturn(compatibilityResult);

        List<Long> wasteIds = List.of(1L, 2L, 3L);
        String requestBody = objectMapper.writeValueAsString(Map.of("wasteIds", wasteIds));

        mockMvc.perform(post("/api/compatibility/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.compatible").value(false))
                .andExpect(jsonPath("$.data.incompatiblePairs").isArray());
    }

    @Test
    @DisplayName("Get Compatibility Check Results - Success")
    void testGetCompatibilityCheckResults_Success() throws Exception {
        List<CompatibilityChecks> checks = List.of(testCompatibilityCheck);
        Map<String, Object> analysis = Map.of(
            "totalPairs", 1,
            "compatiblePairs", 1,
            "compatibilityRate", new BigDecimal("1.00")
        );
        
        when(matchingService.getCompatibilityCheckResults(1L)).thenReturn(checks);
        when(matchingService.getCompatibilityAnalysis(1L)).thenReturn(analysis);

        mockMvc.perform(get("/api/compatibility/check/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(1))
                .andExpect(jsonPath("$.data.checkResults").isArray())
                .andExpect(jsonPath("$.data.analysis").exists());
    }

    @Test
    @DisplayName("Get Compatibility Categories - Success")
    void testGetCompatibilityCategories_Success() throws Exception {
        List<CompatibilityCategory> categories = List.of(testCategory);
        when(matchingService.getCompatibilityCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/compatibility/categories"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].categoryCode").value("A01"));
    }

    // ==================== Calculation Tests ====================

    @Test
    @DisplayName("Execute Matching Calculation - Success")
    void testExecuteMatchingCalculation_Success() throws Exception {
        Map<String, Object> compatibilityResult = Map.of("compatible", true);
        MatchingResponse matchingResponse = new MatchingResponse();
        matchingResponse.setSuccess(true);
        matchingResponse.setMessage("Calculation completed successfully");
        matchingResponse.setSessionId(1L);

        when(matchingService.performCompatibilityCheck(1L)).thenReturn(compatibilityResult);
        when(matchingSessionsService.updateSessionStatus(1L, "calculating")).thenReturn(true);
        when(matchingService.executeMatching(1L)).thenReturn(matchingResponse);
        when(matchingSessionsService.updateSessionStatus(1L, "completed")).thenReturn(true);

        mockMvc.perform(post("/api/matching/calculate/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("success"))
                .andExpect(jsonPath("$.data.sessionId").value(1));
    }

    @Test
    @DisplayName("Execute Matching Calculation - Compatibility Failed")
    void testExecuteMatchingCalculation_CompatibilityFailed() throws Exception {
        Map<String, Object> compatibilityResult = Map.of(
            "compatible", false,
            "reason", "Incompatible waste types detected"
        );
        when(matchingService.performCompatibilityCheck(1L)).thenReturn(compatibilityResult);

        mockMvc.perform(post("/api/matching/calculate/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Compatibility check failed")));
    }

    @Test
    @DisplayName("Get Calculation Status - Success")
    void testGetCalculationStatus_Success() throws Exception {
        when(matchingSessionsService.getSessionById(1L)).thenReturn(testSession);

        mockMvc.perform(get("/api/matching/calculate/status/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("draft"))
                .andExpect(jsonPath("$.data.progress").exists())
                .andExpect(jsonPath("$.data.estimatedTime").exists());
    }

    @Test
    @DisplayName("Get Matching Constraints - Success")
    void testGetMatchingConstraints_Success() throws Exception {
        List<Map<String, Object>> constraints = List.of(
            Map.of("parameterCode", "heatValue", "constraintName", "Heat Value", "minValue", 12500, "maxValue", 16800, "unit", "kJ/kg")
        );
        when(matchingService.getMatchingConstraints()).thenReturn(constraints);

        mockMvc.perform(get("/api/matching/constraints"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].parameterCode").value("heatValue"));
    }

    // ==================== Results Tests ====================

    @Test
    @DisplayName("Get Matching Results - Success")
    void testGetMatchingResults_Success() throws Exception {
        MatchingResponse response = new MatchingResponse();
        response.setSessionId(1L);
        response.setSuccess(true);
        response.setMessage("Matching completed successfully");

        when(matchingService.getSessionDetails(1L)).thenReturn(response);

        mockMvc.perform(get("/api/matching/results/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(1))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    @DisplayName("Get Detailed Analysis - Success")
    void testGetDetailedAnalysis_Success() throws Exception {
        Map<String, Object> wasteRatios = Map.of("waste1", 0.3, "waste2", 0.7);
        Map<String, Object> mixtureProperties = Map.of("heatValue", 14500, "waterContent", 12.5);
        Map<String, Object> constraintChecks = Map.of("allPassed", true, "violations", List.of());

        when(matchingService.getMixingRatios(1L)).thenReturn(wasteRatios);
        when(matchingService.getCalculatedProperties(1L)).thenReturn(mixtureProperties);
        when(matchingService.getConstraintCheckResults(1L)).thenReturn(constraintChecks);

        mockMvc.perform(get("/api/matching/results/1/details"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(1))
                .andExpect(jsonPath("$.data.wasteRatios").exists())
                .andExpect(jsonPath("$.data.mixtureProperties").exists())
                .andExpect(jsonPath("$.data.constraintChecks").exists());
    }

    @Test
    @DisplayName("Get Optimization Suggestions - Success")
    void testGetOptimizationSuggestions_Success() throws Exception {
        Map<String, Object> suggestions = Map.of(
            "suggestions", List.of(
                Map.of("type", "adjust_quantity", "description", "Adjust waste quantities", "priority", "high")
            )
        );
        when(matchingService.getOptimizationSuggestions(1L)).thenReturn(suggestions);

        mockMvc.perform(get("/api/matching/results/1/suggestions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    // ==================== Error Handling Tests ====================

    @Test
    @DisplayName("Invalid Session ID - Error Handling")
    void testInvalidSessionId_ErrorHandling() throws Exception {
        when(matchingSessionsService.getSessionById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/matching/calculate/status/999"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Session not found"));
    }

    @Test
    @DisplayName("Missing Request Body - Validation Error")
    void testMissingRequestBody_ValidationError() throws Exception {
        mockMvc.perform(post("/api/compatibility/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Either sessionId or wasteIds must be provided"));
    }
} 