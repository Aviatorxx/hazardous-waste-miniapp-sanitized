package org.gsu.hwtttt.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gsu.hwtttt.dto.request.MatchingRequest;
import org.gsu.hwtttt.entity.*;
import org.gsu.hwtttt.mapper.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Tests for Module 4: Hazardous Waste Compatibility Simulation
 * 
 * Tests complete workflow: Create session → Add wastes → Check compatibility → Calculate → View results
 * 
 * @author WenXin
 * @date 2025/01/07
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MatchingModuleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MatchingSessionsMapper matchingSessionsMapper;

    @Autowired
    private HazardousWasteMapper hazardousWasteMapper;

    @Autowired
    private CompatibilityCategoryMapper compatibilityCategoryMapper;

    @Autowired
    private MatchingConstraintsMapper matchingConstraintsMapper;

    @Autowired
    private MatchingDetailsMapper matchingDetailsMapper;

    @Autowired
    private CompatibilityChecksMapper compatibilityChecksMapper;

    @Autowired
    private MatchingResultsMapper matchingResultsMapper;

    private static Long testSessionId;
    private static Long testWasteId1;
    private static Long testWasteId2;
    private static Long testWasteId3;

    @BeforeEach
    @Transactional
    @Rollback(false)
    void setupTestData() {
        if (testWasteId1 == null) {
            setupTestWastes();
            setupCompatibilityCategories();
            setupMatchingConstraints();
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. Create Matching Session - Happy Path")
    void testCreateMatchingSession() throws Exception {
        MatchingRequest request = new MatchingRequest();
        request.setSessionName("Integration Test Session");
        request.setTargetHeatValue(new BigDecimal("15000"));
        request.setTotalAmount(new BigDecimal("1000"));
        request.setCreateUser("test_user");

        long startTime = System.currentTimeMillis();

        MvcResult result = mockMvc.perform(post("/api/matching/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionName").value("Integration Test Session"))
                .andExpect(jsonPath("$.data.status").value("draft"))
                .andReturn();

        long responseTime = System.currentTimeMillis() - startTime;
        assertThat(responseTime).isLessThan(2000); // < 2s requirement

        String responseBody = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        testSessionId = Long.valueOf(data.get("sessionId").toString());

        assertThat(testSessionId).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("2. Add Wastes to Session - Happy Path")
    void testAddWastesToSession() throws Exception {
        // Add first waste
        mockMvc.perform(post("/api/matching/sessions/{sessionId}/wastes", testSessionId)
                .param("wasteId", testWasteId1.toString())
                .param("plannedAmount", "300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(true));

        // Add second waste
        mockMvc.perform(post("/api/matching/sessions/{sessionId}/wastes", testSessionId)
                .param("wasteId", testWasteId2.toString())
                .param("plannedAmount", "400"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify wastes were added
        mockMvc.perform(get("/api/matching/sessions/{sessionId}/wastes", testSessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @Order(3)
    @DisplayName("3. Check Compatibility")
    void testCompatibilityCheck() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of("sessionId", testSessionId));

        long startTime = System.currentTimeMillis();

        mockMvc.perform(post("/api/compatibility/check")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.compatible").exists());

        long responseTime = System.currentTimeMillis() - startTime;
        assertThat(responseTime).isLessThan(2000);
    }

    @Test
    @Order(4)
    @DisplayName("4. Execute Matching Calculation")
    void testExecuteMatchingCalculation() throws Exception {
        long startTime = System.currentTimeMillis();

        MvcResult result = mockMvc.perform(post("/api/matching/calculate/{sessionId}", testSessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").exists())
                .andReturn();

        long responseTime = System.currentTimeMillis() - startTime;
        assertThat(responseTime).isLessThan(10000); // < 10s requirement
    }

    @Test
    @Order(5)
    @DisplayName("5. Get Matching Results")
    void testGetMatchingResults() throws Exception {
        mockMvc.perform(get("/api/matching/results/{sessionId}", testSessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(testSessionId));
    }

    private void setupTestWastes() {
        // Create test hazardous waste 1
        HazardousWaste waste1 = new HazardousWaste();
        waste1.setWasteCode("HW01");
        waste1.setWasteName("Test Organic Solvent");
        waste1.setHeatValueCalPerG(new BigDecimal("4500"));
        waste1.setWaterContentPercent(new BigDecimal("10"));
        waste1.setRemainingStorage(new BigDecimal("10000"));
        waste1.setFlammable(true);
        waste1.setOxidizing(false);
        hazardousWasteMapper.insert(waste1);
        testWasteId1 = waste1.getId();

        // Create test hazardous waste 2
        HazardousWaste waste2 = new HazardousWaste();
        waste2.setWasteCode("HW02");
        waste2.setWasteName("Test Plastic Waste");
        waste2.setHeatValueCalPerG(new BigDecimal("3800"));
        waste2.setWaterContentPercent(new BigDecimal("5"));
        waste2.setRemainingStorage(new BigDecimal("8000"));
        waste2.setFlammable(true);
        waste2.setOxidizing(false);
        hazardousWasteMapper.insert(waste2);
        testWasteId2 = waste2.getId();
    }

    private void setupCompatibilityCategories() {
        for (int i = 1; i <= 3; i++) {
            CompatibilityCategory category = new CompatibilityCategory();
            category.setCategoryCode("T" + String.format("%02d", i));
            category.setCategoryNameCn("测试分类" + i);
            category.setCategoryNameEn("Test Category " + i);
            category.setIdx(i);
            compatibilityCategoryMapper.insert(category);
        }
    }

    private void setupMatchingConstraints() {
        MatchingConstraints heatConstraint = new MatchingConstraints();
        heatConstraint.setConstraintName("Heat Value");
        heatConstraint.setParameterCode("heatValue");
        heatConstraint.setMinValue(new BigDecimal("12500"));
        heatConstraint.setMaxValue(new BigDecimal("16800"));
        heatConstraint.setUnit("kJ/kg");
        heatConstraint.setIsActive(true);
        matchingConstraintsMapper.insert(heatConstraint);
        
        // Add total amount constraint for integration testing
        MatchingConstraints totalAmountConstraint = new MatchingConstraints();
        totalAmountConstraint.setConstraintName("总量控制");
        totalAmountConstraint.setParameterCode("TOTAL_AMOUNT");
        totalAmountConstraint.setMinValue(new BigDecimal("35.000"));
        totalAmountConstraint.setMaxValue(new BigDecimal("50.000"));
        totalAmountConstraint.setUnit("t/d");
        totalAmountConstraint.setConstraintDesc("配伍危废总量控制在35-50吨/天范围内");
        totalAmountConstraint.setIsActive(true);
        matchingConstraintsMapper.insert(totalAmountConstraint);
    }
} 