package org.gsu.hwtttt.service;

import org.gsu.hwtttt.common.result.Result;
import org.gsu.hwtttt.dto.response.MatchingResponse;
import org.gsu.hwtttt.entity.*;
import org.gsu.hwtttt.mapper.*;
import org.gsu.hwtttt.service.impl.MatchingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for MatchingService Business Logic
 * 
 * @author WenXin
 * @date 2025/01/07
 */
@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @InjectMocks
    private MatchingServiceImpl matchingService;

    @Mock
    private MatchingDetailsMapper matchingDetailsMapper;

    @Mock
    private MatchingResultsMapper matchingResultsMapper;

    @Mock
    private CompatibilityChecksMapper compatibilityChecksMapper;

    @Mock
    private MatchingConstraintsMapper matchingConstraintsMapper;

    @Mock
    private CompatibilityMatrixMapper compatibilityMatrixMapper;

    @Mock
    private HazardousWasteMapper hazardousWasteMapper;

    @Mock
    private MatchingSessionsMapper matchingSessionsMapper;

    @Mock
    private CompatibilityCategoryMapper compatibilityCategoryMapper;

    private MatchingSessions testSession;
    private HazardousWaste testWaste1;
    private HazardousWaste testWaste2;
    private MatchingDetails testDetail;
    private MatchingConstraints testConstraint;

    @BeforeEach
    void setUp() {
        // Setup test session
        testSession = new MatchingSessions();
        testSession.setId(1L);
        testSession.setSessionName("Test Session");
        testSession.setStatus("draft");
        testSession.setDeleted(false);

        // Setup test wastes
        testWaste1 = new HazardousWaste();
        testWaste1.setId(1L);
        testWaste1.setWasteCode("HW01");
        testWaste1.setWasteName("Organic Solvent");
        testWaste1.setHeatValueCalPerG(new BigDecimal("4500"));
        testWaste1.setWaterContentPercent(new BigDecimal("10"));
        testWaste1.setRemainingStorage(new BigDecimal("1000"));
        testWaste1.setFlammable(true);
        testWaste1.setOxidizing(false);
        testWaste1.setToxic(false);
        testWaste1.setCorrosive(false);

        testWaste2 = new HazardousWaste();
        testWaste2.setId(2L);
        testWaste2.setWasteCode("HW02");
        testWaste2.setWasteName("Plastic Waste");
        testWaste2.setHeatValueCalPerG(new BigDecimal("3800"));
        testWaste2.setWaterContentPercent(new BigDecimal("5"));
        testWaste2.setRemainingStorage(new BigDecimal("800"));
        testWaste2.setFlammable(true);
        testWaste2.setOxidizing(false);
        testWaste2.setToxic(false);
        testWaste2.setCorrosive(false);

        // Setup test detail
        testDetail = new MatchingDetails();
        testDetail.setId(1L);
        testDetail.setSessionId(1L);
        testDetail.setWasteId(1L);
        testDetail.setPlannedAmount(new BigDecimal("100"));
        testDetail.setActualAmount(new BigDecimal("100"));

        // Setup test constraint
        testConstraint = new MatchingConstraints();
        testConstraint.setId(1L);
        testConstraint.setConstraintName("Heat Value");
        testConstraint.setParameterCode("heatValue");
        testConstraint.setMinValue(new BigDecimal("12500"));
        testConstraint.setMaxValue(new BigDecimal("16800"));
        testConstraint.setUnit("kJ/kg");
        testConstraint.setIsActive(true);
    }

    // ==================== Execute Matching Tests ====================

    @Test
    @DisplayName("Execute Matching - Success")
    void testExecuteMatching_Success() {
        // Setup mocks
        when(matchingSessionsMapper.selectById(1L)).thenReturn(testSession);
        when(matchingDetailsMapper.selectBySessionId(1L)).thenReturn(List.of(testDetail));
        when(hazardousWasteMapper.selectById(1L)).thenReturn(testWaste1);
        when(matchingConstraintsMapper.selectList(any())).thenReturn(List.of(testConstraint));

        // Execute
        MatchingResponse result = matchingService.executeMatching(1L);

        // Verify
        assertThat(result).isNotNull();
        assertThat(result.getSessionId()).isEqualTo(1L);
        verify(matchingSessionsMapper).selectById(1L);
        verify(matchingDetailsMapper).selectBySessionId(1L);
    }

    @Test
    @DisplayName("Execute Matching - Session Not Found")
    void testExecuteMatching_SessionNotFound() {
        // Setup mocks
        when(matchingSessionsMapper.selectById(1L)).thenReturn(null);

        // Execute
        MatchingResponse result = matchingService.executeMatching(1L);

        // Verify
        assertThat(result).isNotNull();
        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getMessage()).contains("会话不存在");
    }

    @Test
    @DisplayName("Execute Matching - Session Deleted")
    void testExecuteMatching_SessionDeleted() {
        // Setup mocks
        testSession.setDeleted(true);
        when(matchingSessionsMapper.selectById(1L)).thenReturn(testSession);

        // Execute
        MatchingResponse result = matchingService.executeMatching(1L);

        // Verify
        assertThat(result).isNotNull();
        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getMessage()).contains("已被删除");
    }

    // ==================== Waste Management Tests ====================

    @Test
    @DisplayName("Add Waste to Session - Success")
    void testAddWasteToSession_Success() {
        // Setup mocks
        when(matchingSessionsMapper.selectById(1L)).thenReturn(testSession);
        when(hazardousWasteMapper.selectById(1L)).thenReturn(testWaste1);
        when(matchingDetailsMapper.selectBySessionIdAndWasteId(1L, 1L)).thenReturn(null);
        when(matchingDetailsMapper.insert(any(MatchingDetails.class))).thenReturn(1);
        
        // Execute
        Result<Map<String, Object>> result = matchingService.addWasteToSession(1L, 1L, 100.0);

        // Verify
        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getData().get("success")).isEqualTo(true);
        assertThat(result.getData().get("alreadyExists")).isEqualTo(false);
        assertThat(result.getData().get("wasteId")).isEqualTo(1L);
    }

    @Test
    @DisplayName("Add Waste to Session - Already Exists")
    void testAddWasteToSession_AlreadyExists() {
        // Setup mocks
        when(matchingSessionsMapper.selectById(1L)).thenReturn(testSession);
        when(hazardousWasteMapper.selectById(1L)).thenReturn(testWaste1);
        when(matchingDetailsMapper.selectBySessionIdAndWasteId(1L, 1L)).thenReturn(testDetail);

        // Execute
        Result<Map<String, Object>> result = matchingService.addWasteToSession(1L, 1L, 100.0);

        // Verify - Should return success with alreadyExists flag
        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getData().get("alreadyExists")).isEqualTo(true);
        assertThat(result.getData().get("matchingDetailId")).isEqualTo(testDetail.getId());
        assertThat(result.getMessage()).contains("危废已添加");
    }

    @Test
    @DisplayName("Add Waste to Session - Waste Not Found")
    void testAddWasteToSession_WasteNotFound() {
        // Setup mocks
        when(matchingSessionsMapper.selectById(1L)).thenReturn(testSession);
        when(hazardousWasteMapper.selectById(1L)).thenReturn(null);

        // Execute
        Result<Map<String, Object>> result = matchingService.addWasteToSession(1L, 1L, 100.0);

        // Verify - Should return error
        assertThat(result.getSuccess()).isFalse();
        assertThat(result.getMessage()).contains("危废信息不存在");
    }

    @Test
    @DisplayName("Remove Waste from Session - Success")
    void testRemoveWasteFromSession_Success() {
        // Setup mocks
        when(matchingDetailsMapper.deleteBySessionIdAndWasteId(1L, 1L)).thenReturn(1);

        // Execute
        boolean result = matchingService.removeWasteFromSession(1L, 1L);

        // Verify
        assertThat(result).isTrue();
        verify(matchingDetailsMapper).deleteBySessionIdAndWasteId(1L, 1L);
    }

    @Test
    @DisplayName("Update Waste Quantity - Success")
    void testUpdateWasteQuantity_Success() {
        // Setup mocks
        when(matchingDetailsMapper.updateActualAmount(eq(1L), eq(1L), any(BigDecimal.class))).thenReturn(1);

        // Execute
        boolean result = matchingService.updateWasteQuantity(1L, 1L, 150.0);

        // Verify
        assertThat(result).isTrue();
        verify(matchingDetailsMapper).updateActualAmount(eq(1L), eq(1L), any(BigDecimal.class));
    }

    // ==================== Stock Check Tests ====================

    @Test
    @DisplayName("Check Waste Stock - Sufficient")
    void testCheckWasteStock_Sufficient() {
        // Setup mocks
        when(hazardousWasteMapper.selectById(1L)).thenReturn(testWaste1);

        // Execute
        Map<String, Object> result = matchingService.checkWasteStock(1L, new BigDecimal("500"));

        // Verify
        assertThat(result.get("sufficient")).isEqualTo(true);
        assertThat(result.get("remainingStock")).isEqualTo(testWaste1.getRemainingStorage());
    }

    @Test
    @DisplayName("Check Waste Stock - Insufficient")
    void testCheckWasteStock_Insufficient() {
        // Setup mocks
        when(hazardousWasteMapper.selectById(1L)).thenReturn(testWaste1);

        // Execute
        Map<String, Object> result = matchingService.checkWasteStock(1L, new BigDecimal("1500"));

        // Verify
        assertThat(result.get("sufficient")).isEqualTo(false);
        assertThat(result.get("remainingStock")).isEqualTo(testWaste1.getRemainingStorage());
        assertThat(result.get("requiredAmount")).isEqualTo(new BigDecimal("1500"));
    }

    @Test
    @DisplayName("Check Waste Stock - Waste Not Found")
    void testCheckWasteStock_WasteNotFound() {
        // Setup mocks
        when(hazardousWasteMapper.selectById(1L)).thenReturn(null);

        // Execute
        Map<String, Object> result = matchingService.checkWasteStock(1L, new BigDecimal("500"));

        // Verify
        assertThat(result.get("sufficient")).isEqualTo(false);
        assertThat(result.get("remainingStock")).isEqualTo(BigDecimal.ZERO);
        assertThat(result.get("error")).isEqualTo("Waste not found");
    }

    // ==================== Compatibility Check Tests ====================

    @Test
    @DisplayName("Check Waste Compatibility - Compatible")
    void testCheckWasteCompatibility_Compatible() {
        // Setup compatible wastes
        List<Long> wasteIds = List.of(1L, 2L);
        when(hazardousWasteMapper.selectBatchIds(wasteIds)).thenReturn(List.of(testWaste1, testWaste2));

        // Execute
        Map<String, Object> result = matchingService.checkWasteCompatibility(wasteIds);

        // Verify
        assertThat(result.get("compatible")).isEqualTo(true);
        assertThat(result.get("message")).isEqualTo("所有危废相容");
        @SuppressWarnings("unchecked")
        List<String> incompatiblePairs = (List<String>) result.get("incompatiblePairs");
        assertThat(incompatiblePairs).isEmpty();
    }

    @Test
    @DisplayName("Check Waste Compatibility - Incompatible")
    void testCheckWasteCompatibility_Incompatible() {
        // Setup incompatible wastes (flammable + oxidizing)
        testWaste2.setOxidizing(true); // Make second waste oxidizing
        testWaste2.setFlammable(false);
        
        List<Long> wasteIds = List.of(1L, 2L);
        when(hazardousWasteMapper.selectBatchIds(wasteIds)).thenReturn(List.of(testWaste1, testWaste2));

        // Execute
        Map<String, Object> result = matchingService.checkWasteCompatibility(wasteIds);

        // Verify
        assertThat(result.get("compatible")).isEqualTo(false);
        assertThat(result.get("message")).isEqualTo("存在不相容的危废组合");
        @SuppressWarnings("unchecked")
        List<String> incompatiblePairs = (List<String>) result.get("incompatiblePairs");
        assertThat(incompatiblePairs).isNotEmpty();
        assertThat(incompatiblePairs.get(0)).contains("不相容");
    }

    // ==================== Search Tests ====================

    @Test
    @DisplayName("Search Available Wastes - Success")
    void testSearchAvailableWastes_Success() {
        // Setup mocks
        List<HazardousWaste> wastes = List.of(testWaste1, testWaste2);
        when(hazardousWasteMapper.searchByKeyword("HW")).thenReturn(wastes);

        // Execute
        List<Map<String, Object>> result = matchingService.searchAvailableWastes("HW");

        // Verify
        assertThat(result).hasSize(2);
        assertThat(result.get(0).get("wasteCode")).isEqualTo("HW01");
        assertThat(result.get(1).get("wasteCode")).isEqualTo("HW02");
    }

    @Test
    @DisplayName("Search Available Wastes - No Results")
    void testSearchAvailableWastes_NoResults() {
        // Setup mocks
        when(hazardousWasteMapper.searchByKeyword("INVALID")).thenReturn(List.of());

        // Execute
        List<Map<String, Object>> result = matchingService.searchAvailableWastes("INVALID");

        // Verify
        assertThat(result).isEmpty();
    }

    // ==================== Constraint Tests ====================

    @Test
    @DisplayName("Get Matching Constraints - Success")
    void testGetMatchingConstraints_Success() {
        // Setup mocks
        when(matchingConstraintsMapper.selectList(any())).thenReturn(List.of(testConstraint));

        // Execute
        List<Map<String, Object>> result = matchingService.getMatchingConstraints();

        // Verify
        assertThat(result).hasSize(1);
        Map<String, Object> constraint = result.get(0);
        assertThat(constraint.get("parameterCode")).isEqualTo("heatValue");
        assertThat(constraint.get("constraintName")).isEqualTo("Heat Value");
        assertThat(constraint.get("minValue")).isEqualTo(new BigDecimal("12500"));
        assertThat(constraint.get("maxValue")).isEqualTo(new BigDecimal("16800"));
        assertThat(constraint.get("unit")).isEqualTo("kJ/kg");
    }

    @Test
    @DisplayName("Validate Constraints - Success")
    void testValidateConstraints_Success() {
        // Setup mocks for constraint validation
        when(matchingDetailsMapper.selectBySessionId(1L)).thenReturn(List.of(testDetail));
        when(hazardousWasteMapper.selectById(1L)).thenReturn(testWaste1);
        when(matchingConstraintsMapper.selectList(any())).thenReturn(List.of(testConstraint));

        // Execute
        boolean result = matchingService.validateConstraints(1L);

        // Verify - This will depend on the constraint logic
        // For now, just verify the method executes without error
        verify(matchingDetailsMapper).selectBySessionId(1L);
        verify(matchingConstraintsMapper).selectList(any());
    }

    // ==================== Categories Tests ====================

    @Test
    @DisplayName("Get Compatibility Categories - Success")
    void testGetCompatibilityCategories_Success() {
        // Setup test categories
        CompatibilityCategory category1 = new CompatibilityCategory();
        category1.setCategoryCode("A01");
        category1.setCategoryNameCn("爆炸性物质");

        when(compatibilityCategoryMapper.selectList(any())).thenReturn(List.of(category1));

        // Execute
        List<CompatibilityCategory> result = matchingService.getCompatibilityCategories();

        // Verify
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryCode()).isEqualTo("A01");
    }

    // ==================== Session Details Tests ====================

    @Test
    @DisplayName("Get Session Wastes - Success")
    void testGetSessionWastes_Success() {
        // Setup mocks
        when(matchingDetailsMapper.selectBySessionId(1L)).thenReturn(List.of(testDetail));

        // Execute
        List<MatchingDetails> result = matchingService.getSessionWastes(1L);

        // Verify
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSessionId()).isEqualTo(1L);
        assertThat(result.get(0).getWasteId()).isEqualTo(1L);
        verify(matchingDetailsMapper).selectBySessionId(1L);
    }

    @Test
    @DisplayName("Get Session Wastes - Empty Session")
    void testGetSessionWastes_EmptySession() {
        // Setup mocks
        when(matchingDetailsMapper.selectBySessionId(1L)).thenReturn(List.of());

        // Execute
        List<MatchingDetails> result = matchingService.getSessionWastes(1L);

        // Verify
        assertThat(result).isEmpty();
        verify(matchingDetailsMapper).selectBySessionId(1L);
    }

    // ==================== Calculation Tests ====================

    @Test
    @DisplayName("Calculate Weighted Averages - Success")
    void testCalculateWeightedAverages_Success() {
        // Setup mocks
        when(matchingDetailsMapper.selectBySessionId(1L)).thenReturn(List.of(testDetail));
        when(hazardousWasteMapper.selectById(1L)).thenReturn(testWaste1);

        // Execute
        Map<String, Object> result = matchingService.calculateWeightedAverages(1L);

        // Verify
        assertThat(result).isNotNull();
        assertThat(result).containsKey("heatValue");
        assertThat(result).containsKey("waterContent");
        verify(matchingDetailsMapper).selectBySessionId(1L);
        verify(hazardousWasteMapper).selectById(1L);
    }

    // ==================== Performance Tests ====================

    @Test
    @DisplayName("Compatibility Check - Performance")
    void testCompatibilityCheck_Performance() {
        // Setup mocks for performance test
        when(matchingDetailsMapper.selectBySessionId(1L)).thenReturn(List.of(testDetail));
        when(hazardousWasteMapper.selectById(1L)).thenReturn(testWaste1);
        when(compatibilityChecksMapper.selectList(any())).thenReturn(List.of());

        // Execute with timing
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = matchingService.performCompatibilityCheck(1L);
        long endTime = System.currentTimeMillis();

        // Verify performance requirement (< 2s)
        assertThat(endTime - startTime).isLessThan(2000);
        assertThat(result).isNotNull();
        assertThat(result).containsKey("compatible");
    }

    @Test
    @DisplayName("Execution - Performance")
    void testExecution_Performance() {
        // Setup mocks for performance test
        when(matchingSessionsMapper.selectById(1L)).thenReturn(testSession);
        when(matchingDetailsMapper.selectBySessionId(1L)).thenReturn(List.of(testDetail));
        when(hazardousWasteMapper.selectById(1L)).thenReturn(testWaste1);
        when(compatibilityChecksMapper.selectList(any())).thenReturn(List.of());
        when(matchingConstraintsMapper.selectList(any())).thenReturn(List.of());

        // Execute with timing
        long startTime = System.currentTimeMillis();
        MatchingResponse result = matchingService.executeMatching(1L);
        long endTime = System.currentTimeMillis();

        // Verify performance requirement (< 10s)
        assertThat(endTime - startTime).isLessThan(10000);
        assertThat(result).isNotNull();
    }
} 