package org.gsu.hwtttt.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gsu.hwtttt.dto.request.MatchingRequest;
import org.gsu.hwtttt.entity.*;
import org.gsu.hwtttt.service.MatchingService;
import org.gsu.hwtttt.service.MatchingSessionsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API Response Format Tests for Module 4
 * Verifies HTTP status codes, response format, error messages, and data completeness
 * 
 * @author WenXin
 * @date 2025/01/07
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class ApiResponseTest {

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
    private CompatibilityCategory testCategory;

    @BeforeEach
    void setUp() {
        // Setup test data
        testSession = new MatchingSessions();
        testSession.setId(1L);
        testSession.setSessionName("API Test Session");
        testSession.setStatus("draft");
        testSession.setCreateTime(LocalDateTime.now());
        testSession.setCreateUser("api_test_user");

        testDetail = new MatchingDetails();
        testDetail.setId(1L);
        testDetail.setSessionId(1L);
        testDetail.setWasteId(1L);
        testDetail.setPlannedAmount(new BigDecimal("100"));
        testDetail.setActualAmount(new BigDecimal("100"));

        testCategory = new CompatibilityCategory();
        testCategory.setCategoryCode("A01");
        testCategory.setCategoryNameCn("爆炸性物质");
        testCategory.setCategoryNameEn("Explosive substances");
        testCategory.setIdx(1);
    }

    // ==================== Response Format Tests ====================

    @Test
    @DisplayName("Response Format - Create Session Success")
    void testResponseFormatCreateSessionSuccess() throws Exception {
        // Mock service response
        when(matchingSessionsService.createSession(any(MatchingSessions.class))).thenReturn(testSession);

        MatchingRequest request = new MatchingRequest();
        request.setSessionName("API Format Test Session");
        request.setTargetHeatValue(new BigDecimal("15000"));
        request.setTotalAmount(new BigDecimal("1000"));
        request.setCreateUser("format_test_user");

        MvcResult result = mockMvc.perform(post("/api/matching/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").exists())
                .andExpect(jsonPath("$.success").isBoolean())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.sessionId").exists())
                .andExpect(jsonPath("$.data.sessionId").isNumber())
                .andExpect(jsonPath("$.data.sessionName").exists())
                .andExpect(jsonPath("$.data.sessionName").isString())
                .andExpect(jsonPath("$.data.status").exists())
                .andExpect(jsonPath("$.data.status").isString())
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        // Verify response structure
        String responseBody = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        
        assertThat(response).containsKeys("success", "message", "data", "timestamp");
        assertThat(response.get("success")).isEqualTo(true);
        assertThat(response.get("data")).isInstanceOf(Map.class);
    }

    @Test
    @DisplayName("Response Format - Validation Error")
    void testResponseFormatValidationError() throws Exception {
        MatchingRequest invalidRequest = new MatchingRequest();
        // Missing required fields

        mockMvc.perform(post("/api/matching/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Response Format - Get Sessions List")
    void testResponseFormatGetSessionsList() throws Exception {
        List<MatchingSessions> sessions = List.of(testSession);
        when(matchingSessionsService.getSessionHistoryList(any(), any(), any())).thenReturn(sessions);

        mockMvc.perform(get("/api/matching/sessions")
                .param("username", "api_test_user")
                .param("pageNo", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].sessionName").isString())
                .andExpect(jsonPath("$.data[0].status").isString())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Response Format - Compatibility Check")
    void testResponseFormatCompatibilityCheck() throws Exception {
        Map<String, Object> compatibilityResult = Map.of(
            "compatible", true,
            "message", "All wastes are compatible",
            "analysis", Map.of("totalPairs", 1, "compatiblePairs", 1)
        );
        when(matchingService.performCompatibilityCheck(anyLong())).thenReturn(compatibilityResult);

        String requestBody = objectMapper.writeValueAsString(Map.of("sessionId", 1L));

        mockMvc.perform(post("/api/compatibility/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.compatible").isBoolean())
                .andExpect(jsonPath("$.data.message").isString())
                .andExpect(jsonPath("$.data.analysis").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Response Format - Get Compatibility Categories")
    void testResponseFormatGetCompatibilityCategories() throws Exception {
        List<CompatibilityCategory> categories = List.of(testCategory);
        when(matchingService.getCompatibilityCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/compatibility/categories"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].categoryCode").isString())
                .andExpect(jsonPath("$.data[0].categoryNameCn").isString())
                .andExpect(jsonPath("$.data[0].categoryNameEn").isString())
                .andExpect(jsonPath("$.data[0].idx").isNumber())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ==================== HTTP Status Code Tests ====================

    @Test
    @DisplayName("HTTP Status - 200 OK for Valid Requests")
    void testHttpStatus200Ok() throws Exception {
        when(matchingSessionsService.getSessionById(1L)).thenReturn(testSession);

        mockMvc.perform(get("/api/matching/calculate/status/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("HTTP Status - 400 Bad Request for Invalid Input")
    void testHttpStatus400BadRequest() throws Exception {
        mockMvc.perform(post("/api/matching/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")) // Empty request body
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("HTTP Status - 404 Not Found for Invalid Endpoints")
    void testHttpStatus404NotFound() throws Exception {
        mockMvc.perform(get("/api/matching/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("HTTP Status - 405 Method Not Allowed")
    void testHttpStatus405MethodNotAllowed() throws Exception {
        mockMvc.perform(put("/api/compatibility/categories")) // GET-only endpoint
                .andExpect(status().isMethodNotAllowed());
    }

    // ==================== Error Message Tests ====================

    @Test
    @DisplayName("Error Messages - Missing Required Fields")
    void testErrorMessagesMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/compatibility/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value(containsString("sessionId or wasteIds")));
    }

    @Test
    @DisplayName("Error Messages - Invalid Session ID")
    void testErrorMessagesInvalidSessionId() throws Exception {
        when(matchingSessionsService.getSessionById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/matching/calculate/status/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.message").value(containsString("Session not found")));
    }

    @Test
    @DisplayName("Error Messages - Service Exception Handling")
    void testErrorMessagesServiceException() throws Exception {
        when(matchingService.performCompatibilityCheck(anyLong())).thenThrow(new RuntimeException("Service error"));

        String requestBody = objectMapper.writeValueAsString(Map.of("sessionId", 1L));

        mockMvc.perform(post("/api/compatibility/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.error").exists());
    }

    // ==================== Data Completeness Tests ====================

    @Test
    @DisplayName("Data Completeness - Session Details")
    void testDataCompletenessSessionDetails() throws Exception {
        when(matchingSessionsService.createSession(any())).thenReturn(testSession);

        MatchingRequest request = new MatchingRequest();
        request.setSessionName("Completeness Test");
        request.setTargetHeatValue(new BigDecimal("15000"));
        request.setTotalAmount(new BigDecimal("1000"));
        request.setCreateUser("completeness_user");

        mockMvc.perform(post("/api/matching/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sessionId").exists())
                .andExpect(jsonPath("$.data.sessionName").exists())
                .andExpect(jsonPath("$.data.status").exists())
                .andExpect(jsonPath("$.data.createTime").exists())
                .andExpect(jsonPath("$.data.createUser").exists());
    }

    @Test
    @DisplayName("Data Completeness - Calculation Status")
    void testDataCompletenessCalculationStatus() throws Exception {
        when(matchingSessionsService.getSessionById(1L)).thenReturn(testSession);

        mockMvc.perform(get("/api/matching/calculate/status/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").exists())
                .andExpect(jsonPath("$.data.progress").exists())
                .andExpect(jsonPath("$.data.estimatedTime").exists())
                .andExpect(jsonPath("$.data.sessionId").exists());
    }

    @Test
    @DisplayName("Data Completeness - Compatibility Categories")
    void testDataCompletenessCompatibilityCategories() throws Exception {
        List<CompatibilityCategory> categories = List.of(testCategory);
        when(matchingService.getCompatibilityCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/compatibility/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].categoryCode").exists())
                .andExpect(jsonPath("$.data[0].categoryNameCn").exists())
                .andExpect(jsonPath("$.data[0].categoryNameEn").exists())
                .andExpect(jsonPath("$.data[0].idx").exists());
    }

    // ==================== Performance Response Tests ====================

    @Test
    @DisplayName("Performance - Response Time for Simple Queries")
    void testPerformanceResponseTimeSimpleQueries() throws Exception {
        when(matchingService.getCompatibilityCategories()).thenReturn(List.of(testCategory));

        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/api/compatibility/categories"))
                .andExpect(status().isOk());
        
        long responseTime = System.currentTimeMillis() - startTime;
        assertThat(responseTime).isLessThan(2000); // < 2s requirement
    }

    @Test
    @DisplayName("Performance - Response Time for Compatibility Check")
    void testPerformanceResponseTimeCompatibilityCheck() throws Exception {
        Map<String, Object> result = Map.of("compatible", true, "message", "Compatible");
        when(matchingService.performCompatibilityCheck(anyLong())).thenReturn(result);

        String requestBody = objectMapper.writeValueAsString(Map.of("sessionId", 1L));
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(post("/api/compatibility/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
        
        long responseTime = System.currentTimeMillis() - startTime;
        assertThat(responseTime).isLessThan(2000); // < 2s requirement
    }

    // ==================== Content Type Tests ====================

    @Test
    @DisplayName("Content Type - JSON Request and Response")
    void testContentTypeJsonRequestResponse() throws Exception {
        when(matchingSessionsService.createSession(any())).thenReturn(testSession);

        MatchingRequest request = new MatchingRequest();
        request.setSessionName("Content Type Test");
        request.setCreateUser("content_test_user");

        mockMvc.perform(post("/api/matching/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Content-Type", containsString("application/json")));
    }

    @Test
    @DisplayName("Content Type - Unsupported Media Type")
    void testContentTypeUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/api/matching/sessions")
                .contentType(MediaType.APPLICATION_XML)
                .content("<request><sessionName>Test</sessionName></request>"))
                .andExpect(status().isUnsupportedMediaType());
    }
} 