package org.gsu.hwtttt.service;

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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Business Logic Tests for Module 4
 * Tests core algorithms: compatibility checking and constraint satisfaction
 * 
 * @author WenXin
 * @date 2025/01/07
 */
@ExtendWith(MockitoExtension.class)
class BusinessLogicTest {

    @InjectMocks
    private MatchingServiceImpl matchingService;

    @Mock
    private HazardousWasteMapper hazardousWasteMapper;
    
    @Mock
    private CompatibilityMatrixMapper compatibilityMatrixMapper;
    
    @Mock
    private MatchingConstraintsMapper matchingConstraintsMapper;
    
    @Mock
    private MatchingDetailsMapper matchingDetailsMapper;
    
    @Mock
    private CompatibilityChecksMapper compatibilityChecksMapper;

    private HazardousWaste flammableWaste;
    private HazardousWaste oxidizingWaste;
    private HazardousWaste compatibleWaste;
    private List<MatchingConstraints> testConstraints;

    @BeforeEach
    void setUp() {
        // Setup flammable waste
        flammableWaste = new HazardousWaste();
        flammableWaste.setId(1L);
        flammableWaste.setWasteCode("HW01");
        flammableWaste.setWasteName("Flammable Organic Solvent");
        flammableWaste.setHeatValueCalPerG(new BigDecimal("4500"));
        flammableWaste.setWaterContentPercent(new BigDecimal("5"));
        flammableWaste.setNPercent(new BigDecimal("1.0"));
        flammableWaste.setSulfurContentPercent(new BigDecimal("1.5"));
        flammableWaste.setClPercent(new BigDecimal("0.5"));
        flammableWaste.setFPercent(new BigDecimal("0.2"));
        flammableWaste.setCdMgPerL(new BigDecimal("1"));
        flammableWaste.setPbMgPerL(new BigDecimal("15"));
        flammableWaste.setAsMgPerL(new BigDecimal("30"));
        flammableWaste.setFlammable(true);
        flammableWaste.setOxidizing(false);
        flammableWaste.setToxic(false);
        flammableWaste.setCorrosive(false);
        flammableWaste.setCompatibilityCategoryCode("F01");

        // Setup oxidizing waste
        oxidizingWaste = new HazardousWaste();
        oxidizingWaste.setId(2L);
        oxidizingWaste.setWasteCode("HW02");
        oxidizingWaste.setWasteName("Oxidizing Agent");
        oxidizingWaste.setHeatValueCalPerG(new BigDecimal("2500"));
        oxidizingWaste.setWaterContentPercent(new BigDecimal("20"));
        oxidizingWaste.setNPercent(new BigDecimal("0.5"));
        oxidizingWaste.setSulfurContentPercent(new BigDecimal("0.8"));
        oxidizingWaste.setClPercent(new BigDecimal("0.3"));
        oxidizingWaste.setFPercent(new BigDecimal("0.1"));
        oxidizingWaste.setCdMgPerL(new BigDecimal("0.2"));
        oxidizingWaste.setPbMgPerL(new BigDecimal("10"));
        oxidizingWaste.setAsMgPerL(new BigDecimal("20"));
        oxidizingWaste.setFlammable(false);
        oxidizingWaste.setOxidizing(true);
        oxidizingWaste.setToxic(false);
        oxidizingWaste.setCorrosive(false);
        oxidizingWaste.setCompatibilityCategoryCode("O01");

        // Setup compatible waste
        compatibleWaste = new HazardousWaste();
        compatibleWaste.setId(3L);
        compatibleWaste.setWasteCode("HW03");
        compatibleWaste.setWasteName("Compatible Plastic Waste");
        compatibleWaste.setHeatValueCalPerG(new BigDecimal("3800"));
        compatibleWaste.setWaterContentPercent(new BigDecimal("8"));
        compatibleWaste.setNPercent(new BigDecimal("0.8"));
        compatibleWaste.setSulfurContentPercent(new BigDecimal("1.2"));
        compatibleWaste.setClPercent(new BigDecimal("0.4"));
        compatibleWaste.setFPercent(new BigDecimal("0.15"));
        compatibleWaste.setCdMgPerL(new BigDecimal("0.25"));
        compatibleWaste.setPbMgPerL(new BigDecimal("12"));
        compatibleWaste.setAsMgPerL(new BigDecimal("25"));
        compatibleWaste.setFlammable(true);
        compatibleWaste.setOxidizing(false);
        compatibleWaste.setToxic(false);
        compatibleWaste.setCorrosive(false);
        compatibleWaste.setCompatibilityCategoryCode("F01");

        // Setup test constraints
        setupTestConstraints();
    }

    @Test
    @DisplayName("Compatibility Check - Flammable + Oxidizing = Incompatible")
    void testCompatibilityCheck_FlammableOxidizingIncompatible() {
        // Setup
        List<Long> wasteIds = List.of(1L, 2L);
        when(hazardousWasteMapper.selectBatchIds(wasteIds))
            .thenReturn(List.of(flammableWaste, oxidizingWaste));

        // Execute
        Map<String, Object> result = matchingService.checkWasteCompatibility(wasteIds);

        // Verify incompatibility
        assertThat(result.get("compatible")).isEqualTo(false);
        assertThat(result.get("message")).isEqualTo("存在不相容的危废组合");
        
        @SuppressWarnings("unchecked")
        List<String> incompatiblePairs = (List<String>) result.get("incompatiblePairs");
        assertThat(incompatiblePairs).hasSize(1);
        assertThat(incompatiblePairs.get(0)).contains("易燃物质与氧化剂不相容");
    }

    @Test
    @DisplayName("Compatibility Check - Same Category = Compatible")
    void testCompatibilityCheck_SameCategoryCompatible() {
        // Setup
        List<Long> wasteIds = List.of(1L, 3L);
        when(hazardousWasteMapper.selectBatchIds(wasteIds))
            .thenReturn(List.of(flammableWaste, compatibleWaste));

        // Execute
        Map<String, Object> result = matchingService.checkWasteCompatibility(wasteIds);

        // Verify compatibility
        assertThat(result.get("compatible")).isEqualTo(true);
        assertThat(result.get("message")).isEqualTo("所有危废相容");
        
        @SuppressWarnings("unchecked")
        List<String> incompatiblePairs = (List<String>) result.get("incompatiblePairs");
        assertThat(incompatiblePairs).isEmpty();
    }

    @Test
    @DisplayName("Stock Validation - Available Quantity")
    void testStockValidation_AvailableQuantity() {
        // Setup sufficient stock
        flammableWaste.setRemainingStorage(new BigDecimal("1000"));
        when(hazardousWasteMapper.selectById(1L)).thenReturn(flammableWaste);

        // Execute
        Map<String, Object> result = matchingService.checkWasteStock(1L, new BigDecimal("500"));

        // Verify sufficient stock
        assertThat(result.get("sufficient")).isEqualTo(true);
        assertThat(result.get("remainingStock")).isEqualTo(new BigDecimal("1000"));
        assertThat(result.get("requiredAmount")).isEqualTo(new BigDecimal("500"));
    }

    @Test
    @DisplayName("Stock Validation - Insufficient Quantity")
    void testStockValidation_InsufficientQuantity() {
        // Setup insufficient stock
        flammableWaste.setRemainingStorage(new BigDecimal("200"));
        when(hazardousWasteMapper.selectById(1L)).thenReturn(flammableWaste);

        // Execute
        Map<String, Object> result = matchingService.checkWasteStock(1L, new BigDecimal("500"));

        // Verify insufficient stock
        assertThat(result.get("sufficient")).isEqualTo(false);
        assertThat(result.get("remainingStock")).isEqualTo(new BigDecimal("200"));
        assertThat(result.get("requiredAmount")).isEqualTo(new BigDecimal("500"));
    }

    @Test
    @DisplayName("Linear Programming - Two Variable Optimization")
    void testLinearProgramming_TwoVariableOptimization() {
        // Setup two wastes with different heat values
        MatchingDetails detail1 = createMatchingDetail(1L, 1L, new BigDecimal("400"));
        MatchingDetails detail2 = createMatchingDetail(1L, 3L, new BigDecimal("600"));
        
        when(matchingDetailsMapper.selectBySessionId(1L)).thenReturn(List.of(detail1, detail2));
        when(hazardousWasteMapper.selectById(1L)).thenReturn(flammableWaste);
        when(hazardousWasteMapper.selectById(3L)).thenReturn(compatibleWaste);

        // Execute weighted average calculation
        Map<String, Object> result = matchingService.calculateWeightedAverages(1L);

        // Verify optimization results
        assertThat(result).containsKey("heatValue");
        BigDecimal calculatedHeat = (BigDecimal) result.get("heatValue");
        
        // Expected: (4500*400 + 3800*600) / 1000 = 4.08 kJ/g
        BigDecimal expected = new BigDecimal("4080");
        assertThat(calculatedHeat).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Test Total Amount Constraint Validation")
    void testTotalAmountConstraintValidation() {
        // Setup test data using mocks
        Long sessionId = 1L;
        
        // Create test waste details that sum to different total amounts
        MatchingDetails detail1 = new MatchingDetails();
        detail1.setSessionId(sessionId);
        detail1.setWasteId(1L);
        detail1.setPlannedAmount(new BigDecimal("40000")); // 40 tonnes = 40000 kg
        detail1.setActualAmount(new BigDecimal("40000"));
        
        // Mock the mappers to return our test data
        when(matchingDetailsMapper.selectBySessionId(sessionId)).thenReturn(List.of(detail1));
        when(hazardousWasteMapper.selectById(1L)).thenReturn(flammableWaste);
        
        // Create total amount constraint (35-50 t/d range)
        MatchingConstraints totalAmountConstraint = new MatchingConstraints();
        totalAmountConstraint.setParameterCode("TOTAL_AMOUNT");
        totalAmountConstraint.setConstraintName("总量控制");
        totalAmountConstraint.setMinValue(new BigDecimal("35.000"));
        totalAmountConstraint.setMaxValue(new BigDecimal("50.000"));
        totalAmountConstraint.setUnit("t/d");
        totalAmountConstraint.setIsActive(true);
        
        // Test 1: Total amount within range (40 t/d) - should pass
        when(matchingConstraintsMapper.selectList(any())).thenReturn(List.of(totalAmountConstraint));
        boolean isValid = matchingService.validateConstraints(sessionId);
        assertThat(isValid).isTrue();
        
        // Test 2: Total amount below minimum (30 t/d) - should fail
        detail1.setPlannedAmount(new BigDecimal("30000")); // 30 tonnes in kg
        detail1.setActualAmount(new BigDecimal("30000"));
        when(matchingDetailsMapper.selectBySessionId(sessionId)).thenReturn(List.of(detail1));
        
        isValid = matchingService.validateConstraints(sessionId);
        assertThat(isValid).isFalse();
        
        // Test 3: Total amount above maximum (55 t/d) - should fail  
        detail1.setPlannedAmount(new BigDecimal("55000")); // 55 tonnes in kg
        detail1.setActualAmount(new BigDecimal("55000"));
        when(matchingDetailsMapper.selectBySessionId(sessionId)).thenReturn(List.of(detail1));
        
        isValid = matchingService.validateConstraints(sessionId);
        assertThat(isValid).isFalse();
    }

    private MatchingDetails createMatchingDetail(Long sessionId, Long wasteId, BigDecimal amount) {
        MatchingDetails detail = new MatchingDetails();
        detail.setSessionId(sessionId);
        detail.setWasteId(wasteId);
        detail.setPlannedAmount(amount);
        detail.setActualAmount(amount);
        return detail;
    }

    private void setupTestConstraints() {
        testConstraints = new ArrayList<>();

        // Heat value constraint: 12500-16800 kJ/kg
        MatchingConstraints heatConstraint = new MatchingConstraints();
        heatConstraint.setParameterCode("heatValue");
        heatConstraint.setConstraintName("Heat Value");
        heatConstraint.setMinValue(new BigDecimal("12500"));
        heatConstraint.setMaxValue(new BigDecimal("16800"));
        heatConstraint.setUnit("kJ/kg");
        heatConstraint.setIsActive(true);
        testConstraints.add(heatConstraint);

        // Water content constraint: ≤45%
        MatchingConstraints waterConstraint = new MatchingConstraints();
        waterConstraint.setParameterCode("waterContent");
        waterConstraint.setConstraintName("Water Content");
        waterConstraint.setMaxValue(new BigDecimal("45"));
        waterConstraint.setUnit("%");
        waterConstraint.setIsActive(true);
        testConstraints.add(waterConstraint);

        // Nitrogen content constraint: ≤3%
        MatchingConstraints nitrogenConstraint = new MatchingConstraints();
        nitrogenConstraint.setParameterCode("nitrogenContent");
        nitrogenConstraint.setConstraintName("Nitrogen Content");
        nitrogenConstraint.setMaxValue(new BigDecimal("3"));
        nitrogenConstraint.setUnit("%");
        nitrogenConstraint.setIsActive(true);
        testConstraints.add(nitrogenConstraint);

        // Sulfur content constraint: ≤4%
        MatchingConstraints sulfurConstraint = new MatchingConstraints();
        sulfurConstraint.setParameterCode("sulfurContent");
        sulfurConstraint.setConstraintName("Sulfur Content");
        sulfurConstraint.setMaxValue(new BigDecimal("4"));
        sulfurConstraint.setUnit("%");
        sulfurConstraint.setIsActive(true);
        testConstraints.add(sulfurConstraint);

        // Chlorine content constraint: ≤1%
        MatchingConstraints chlorineConstraint = new MatchingConstraints();
        chlorineConstraint.setParameterCode("chlorineContent");
        chlorineConstraint.setConstraintName("Chlorine Content");
        chlorineConstraint.setMaxValue(new BigDecimal("1"));
        chlorineConstraint.setUnit("%");
        chlorineConstraint.setIsActive(true);
        testConstraints.add(chlorineConstraint);

        // Mercury constraint: ≤5 mg/kg
        MatchingConstraints mercuryConstraint = new MatchingConstraints();
        mercuryConstraint.setParameterCode("mercuryContent");
        mercuryConstraint.setConstraintName("Mercury Content");
        mercuryConstraint.setMaxValue(new BigDecimal("5"));
        mercuryConstraint.setUnit("mg/kg");
        mercuryConstraint.setIsActive(true);
        testConstraints.add(mercuryConstraint);

        // Heavy metals constraint: Cr+Sn+Sb+Cu+Mn ≤800mg/kg
        MatchingConstraints heavyMetalsConstraint = new MatchingConstraints();
        heavyMetalsConstraint.setParameterCode("HEAVY_METALS_TOTAL");
        heavyMetalsConstraint.setConstraintName("Heavy Metals Total");
        heavyMetalsConstraint.setMaxValue(new BigDecimal("800"));
        heavyMetalsConstraint.setUnit("mg/kg");
        heavyMetalsConstraint.setIsActive(true);
        testConstraints.add(heavyMetalsConstraint);
        
        // Total amount constraint: 35-50 t/d
        MatchingConstraints totalAmountConstraint = new MatchingConstraints();
        totalAmountConstraint.setParameterCode("TOTAL_AMOUNT");
        totalAmountConstraint.setConstraintName("总量控制");
        totalAmountConstraint.setMinValue(new BigDecimal("35.000"));
        totalAmountConstraint.setMaxValue(new BigDecimal("50.000"));
        totalAmountConstraint.setUnit("t/d");
        totalAmountConstraint.setConstraintDesc("配伍危废总量控制在35-50吨/天范围内");
        totalAmountConstraint.setIsActive(true);
        testConstraints.add(totalAmountConstraint);
    }
} 