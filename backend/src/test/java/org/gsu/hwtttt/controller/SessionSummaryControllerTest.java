package org.gsu.hwtttt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gsu.hwtttt.dto.response.SessionSummaryResponse;
import org.gsu.hwtttt.service.MatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Session Summary Controller Test
 * Tests the new getSessionSummary endpoint
 *
 * @author WenXin
 * @date 2025/01/07
 */
@WebMvcTest(MatchingController.class)
public class SessionSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchingService matchingService;

    @MockBean
    private org.gsu.hwtttt.service.MatchingSessionsService matchingSessionsService;

    @MockBean
    private org.gsu.hwtttt.service.HazardousWasteService hazardousWasteService;

    @Autowired
    private ObjectMapper objectMapper;

    private SessionSummaryResponse mockSessionSummary;

    @BeforeEach
    void setUp() {
        // Create mock session summary response
        mockSessionSummary = new SessionSummaryResponse();
        mockSessionSummary.setSessionId(1L);
        mockSessionSummary.setSessionName("Test Session");
        mockSessionSummary.setStatus("draft");
        mockSessionSummary.setTotalAmount(new BigDecimal("5000"));
        mockSessionSummary.setTargetHeatValue(new BigDecimal("15000"));
        mockSessionSummary.setWasteCount(3);
        mockSessionSummary.setLastUpdateTime(LocalDateTime.now());

        // Create waste summary items
        List<SessionSummaryResponse.WasteSummaryItem> wastes = new ArrayList<>();
        
        // Waste 1
        SessionSummaryResponse.WasteSummaryItem waste1 = new SessionSummaryResponse.WasteSummaryItem();
        waste1.setWasteId(1L);
        waste1.setWasteCode("HW06-001");
        waste1.setWasteName("废有机溶剂");
        waste1.setSourceUnit("化工厂A");
        waste1.setPlannedAmount(new BigDecimal("2000"));
        waste1.setRemainingStorage(new BigDecimal("5000"));
        waste1.setStockSufficient(true);
        waste1.setHeatValueKjPerKg(new BigDecimal("16000"));
        waste1.setWaterContentPercent(new BigDecimal("10"));
        waste1.setCompatibilityCategoryCode(13);
        wastes.add(waste1);

        // Waste 2
        SessionSummaryResponse.WasteSummaryItem waste2 = new SessionSummaryResponse.WasteSummaryItem();
        waste2.setWasteId(2L);
        waste2.setWasteCode("HW08-002");
        waste2.setWasteName("废油泥");
        waste2.setSourceUnit("石化厂B");
        waste2.setPlannedAmount(new BigDecimal("2000"));
        waste2.setRemainingStorage(new BigDecimal("3000"));
        waste2.setStockSufficient(true);
        waste2.setHeatValueKjPerKg(new BigDecimal("14000"));
        waste2.setWaterContentPercent(new BigDecimal("20"));
        waste2.setCompatibilityCategoryCode(20);
        wastes.add(waste2);

        // Waste 3
        SessionSummaryResponse.WasteSummaryItem waste3 = new SessionSummaryResponse.WasteSummaryItem();
        waste3.setWasteId(3L);
        waste3.setWasteCode("HW49-003");
        waste3.setWasteName("废活性炭");
        waste3.setSourceUnit("制药厂C");
        waste3.setPlannedAmount(new BigDecimal("1000"));
        waste3.setRemainingStorage(new BigDecimal("800"));
        waste3.setStockSufficient(false); // Stock insufficient
        waste3.setHeatValueKjPerKg(new BigDecimal("15000"));
        waste3.setWaterContentPercent(new BigDecimal("5"));
        waste3.setCompatibilityCategoryCode(39);
        wastes.add(waste3);

        mockSessionSummary.setWastes(wastes);

        // Create compatibility summary
        SessionSummaryResponse.CompatibilitySummary compatibility = new SessionSummaryResponse.CompatibilitySummary();
        compatibility.setChecked(true);
        compatibility.setCompatible(true);
        compatibility.setIncompatiblePairs(0);
        compatibility.setRiskFactors(new ArrayList<>());
        compatibility.setIncompatiblePairDetails(new ArrayList<>());
        mockSessionSummary.setCompatibility(compatibility);

        // Create calculation readiness
        SessionSummaryResponse.CalculationReadiness readiness = new SessionSummaryResponse.CalculationReadiness();
        readiness.setReady(false);
        readiness.setStockSufficient(false);
        readiness.setCompatibilityPassed(true);
        readiness.setStatusReady(false);
        readiness.setTotalAmountSufficient(true);
        readiness.setEstimatedHeatValue(new BigDecimal("15000"));
        readiness.setBlockers(Arrays.asList("Insufficient stock for some wastes", "Session status does not allow calculation: draft"));
        mockSessionSummary.setReadiness(readiness);
    }

    @Test
    public void testGetSessionSummary_Success() throws Exception {
        // Given
        when(matchingService.getSessionSummary(1L)).thenReturn(mockSessionSummary);

        // When & Then
        mockMvc.perform(get("/api/matching/sessions/1/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(1))
                .andExpect(jsonPath("$.data.sessionName").value("Test Session"))
                .andExpect(jsonPath("$.data.status").value("draft"))
                .andExpect(jsonPath("$.data.totalAmount").value(5000))
                .andExpect(jsonPath("$.data.targetHeatValue").value(15000))
                .andExpect(jsonPath("$.data.wasteCount").value(3))
                .andExpect(jsonPath("$.data.wastes").isArray())
                .andExpect(jsonPath("$.data.wastes.length()").value(3))
                .andExpect(jsonPath("$.data.wastes[0].wasteCode").value("HW06-001"))
                .andExpect(jsonPath("$.data.wastes[0].stockSufficient").value(true))
                .andExpect(jsonPath("$.data.wastes[2].stockSufficient").value(false))
                .andExpect(jsonPath("$.data.compatibility.checked").value(true))
                .andExpect(jsonPath("$.data.compatibility.compatible").value(true))
                .andExpect(jsonPath("$.data.compatibility.incompatiblePairs").value(0))
                .andExpect(jsonPath("$.data.readiness.ready").value(false))
                .andExpect(jsonPath("$.data.readiness.stockSufficient").value(false))
                .andExpect(jsonPath("$.data.readiness.compatibilityPassed").value(true))
                .andExpect(jsonPath("$.data.readiness.blockers").isArray())
                .andExpect(jsonPath("$.data.readiness.blockers.length()").value(2));
    }

    @Test
    public void testGetSessionSummary_WithIncompatibleWastes() throws Exception {
        // Given - create session with incompatible wastes
        SessionSummaryResponse incompatibleSession = new SessionSummaryResponse();
        incompatibleSession.setSessionId(2L);
        incompatibleSession.setSessionName("Incompatible Session");
        incompatibleSession.setStatus("incompatible");
        incompatibleSession.setWasteCount(2);

        // Create compatibility summary with incompatible pairs
        SessionSummaryResponse.CompatibilitySummary compatibility = new SessionSummaryResponse.CompatibilitySummary();
        compatibility.setChecked(true);
        compatibility.setCompatible(false);
        compatibility.setIncompatiblePairs(1);
        compatibility.setRiskFactors(Arrays.asList("H", "F"));

        // Create incompatible pair detail
        SessionSummaryResponse.IncompatiblePairDetail pairDetail = new SessionSummaryResponse.IncompatiblePairDetail();
        pairDetail.setWasteId1(1L);
        pairDetail.setWasteCode1("HW01-001");
        pairDetail.setWasteId2(2L);
        pairDetail.setWasteCode2("HW22-001");
        pairDetail.setRiskCodes(Arrays.asList("H", "F"));
        pairDetail.setRiskDescription("Heat generation and fire hazard");
        
        compatibility.setIncompatiblePairDetails(Arrays.asList(pairDetail));
        incompatibleSession.setCompatibility(compatibility);

        // Create calculation readiness
        SessionSummaryResponse.CalculationReadiness readiness = new SessionSummaryResponse.CalculationReadiness();
        readiness.setReady(false);
        readiness.setCompatibilityPassed(false);
        readiness.setBlockers(Arrays.asList("Compatibility check failed"));
        incompatibleSession.setReadiness(readiness);

        when(matchingService.getSessionSummary(2L)).thenReturn(incompatibleSession);

        // When & Then
        mockMvc.perform(get("/api/matching/sessions/2/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(2))
                .andExpect(jsonPath("$.data.status").value("incompatible"))
                .andExpect(jsonPath("$.data.compatibility.checked").value(true))
                .andExpect(jsonPath("$.data.compatibility.compatible").value(false))
                .andExpect(jsonPath("$.data.compatibility.incompatiblePairs").value(1))
                .andExpect(jsonPath("$.data.compatibility.riskFactors").isArray())
                .andExpect(jsonPath("$.data.compatibility.riskFactors[0]").value("H"))
                .andExpect(jsonPath("$.data.compatibility.riskFactors[1]").value("F"))
                .andExpect(jsonPath("$.data.compatibility.incompatiblePairDetails").isArray())
                .andExpect(jsonPath("$.data.compatibility.incompatiblePairDetails[0].wasteCode1").value("HW01-001"))
                .andExpect(jsonPath("$.data.compatibility.incompatiblePairDetails[0].wasteCode2").value("HW22-001"))
                .andExpect(jsonPath("$.data.compatibility.incompatiblePairDetails[0].riskDescription").value("Heat generation and fire hazard"))
                .andExpect(jsonPath("$.data.readiness.ready").value(false))
                .andExpect(jsonPath("$.data.readiness.compatibilityPassed").value(false));
    }

    @Test
    public void testGetSessionSummary_SessionNotFound() throws Exception {
        // Given
        when(matchingService.getSessionSummary(999L)).thenThrow(new RuntimeException("Session not found: 999"));

        // When & Then
        mockMvc.perform(get("/api/matching/sessions/999/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    public void testGetSessionSummary_InvalidSessionId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/matching/sessions/0/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetSessionSummary_ReadyForCalculation() throws Exception {
        // Given - create session ready for calculation
        SessionSummaryResponse readySession = new SessionSummaryResponse();
        readySession.setSessionId(3L);
        readySession.setSessionName("Ready Session");
        readySession.setStatus("compatible");
        readySession.setTotalAmount(new BigDecimal("50000")); // Large enough amount
        readySession.setWasteCount(2);

        // Create wastes with sufficient stock
        List<SessionSummaryResponse.WasteSummaryItem> wastes = new ArrayList<>();
        SessionSummaryResponse.WasteSummaryItem waste1 = new SessionSummaryResponse.WasteSummaryItem();
        waste1.setWasteId(1L);
        waste1.setWasteCode("HW06-001");
        waste1.setPlannedAmount(new BigDecimal("25000"));
        waste1.setRemainingStorage(new BigDecimal("30000"));
        waste1.setStockSufficient(true);
        waste1.setHeatValueKjPerKg(new BigDecimal("14000"));
        wastes.add(waste1);

        SessionSummaryResponse.WasteSummaryItem waste2 = new SessionSummaryResponse.WasteSummaryItem();
        waste2.setWasteId(2L);
        waste2.setWasteCode("HW08-002");
        waste2.setPlannedAmount(new BigDecimal("25000"));
        waste2.setRemainingStorage(new BigDecimal("30000"));
        waste2.setStockSufficient(true);
        waste2.setHeatValueKjPerKg(new BigDecimal("16000"));
        wastes.add(waste2);

        readySession.setWastes(wastes);

        // Compatible
        SessionSummaryResponse.CompatibilitySummary compatibility = new SessionSummaryResponse.CompatibilitySummary();
        compatibility.setChecked(true);
        compatibility.setCompatible(true);
        compatibility.setIncompatiblePairs(0);
        compatibility.setRiskFactors(new ArrayList<>());
        readySession.setCompatibility(compatibility);

        // Ready for calculation
        SessionSummaryResponse.CalculationReadiness readiness = new SessionSummaryResponse.CalculationReadiness();
        readiness.setReady(true);
        readiness.setStockSufficient(true);
        readiness.setCompatibilityPassed(true);
        readiness.setStatusReady(true);
        readiness.setTotalAmountSufficient(true);
        readiness.setEstimatedHeatValue(new BigDecimal("15000"));
        readiness.setBlockers(new ArrayList<>());
        readySession.setReadiness(readiness);

        when(matchingService.getSessionSummary(3L)).thenReturn(readySession);

        // When & Then
        mockMvc.perform(get("/api/matching/sessions/3/summary")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sessionId").value(3))
                .andExpect(jsonPath("$.data.status").value("compatible"))
                .andExpect(jsonPath("$.data.totalAmount").value(50000))
                .andExpect(jsonPath("$.data.readiness.ready").value(true))
                .andExpect(jsonPath("$.data.readiness.stockSufficient").value(true))
                .andExpect(jsonPath("$.data.readiness.compatibilityPassed").value(true))
                .andExpect(jsonPath("$.data.readiness.statusReady").value(true))
                .andExpect(jsonPath("$.data.readiness.totalAmountSufficient").value(true))
                .andExpect(jsonPath("$.data.readiness.blockers").isEmpty());
    }
} 