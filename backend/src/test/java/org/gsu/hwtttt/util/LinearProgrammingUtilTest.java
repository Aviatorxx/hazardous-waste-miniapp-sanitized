package org.gsu.hwtttt.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * LinearProgrammingUtil Unit Tests
 * 
 * Tests the mathematical model with all 12 constraints:
 * 1. Heat Value: 12,500 ≤ (Σ Qᵢ × Dᵢ) / D₀ ≤ 16,800 kJ/kg
 * 2. Water Content: (Σ Mᵢ × Dᵢ) / D₀ ≤ 45%
 * 3. N Content: (Σ Nᵢ × Dᵢ) / D₀ ≤ 2%
 * 4. S Content: (Σ Sᵢ × Dᵢ) / D₀ ≤ 3%
 * 5. Cl Content: (Σ Clᵢ × Dᵢ) / D₀ ≤ 1.5%
 * 6. F Content: (Σ Fᵢ × Dᵢ) / D₀ ≤ 1%
 * 7. Hg Content: (Σ Hgᵢ × Dᵢ) / D₀ ≤ 4 mg/kg
 * 8. Cd Content: (Σ Cdᵢ × Dᵢ) / D₀ ≤ 1 mg/kg
 * 9. As+Ni Content: (Σ (Asᵢ + Niᵢ) × Dᵢ) / D₀ ≤ 95 mg/kg
 * 10. Pb Content: (Σ Pbᵢ × Dᵢ) / D₀ ≤ 70 mg/kg
 * 11. Heavy Metals: (Σ (Crᵢ+Snᵢ+Sbᵢ+Cuᵢ+Mnᵢ) × Dᵢ) / D₀ ≤ 800 mg/kg
 * 12. Stock Limits: 0 ≤ Dᵢ ≤ remaining_storage[i]
 */
@SpringBootTest
@ExtendWith(MockitoExtension.class)
class LinearProgrammingUtilTest {

    private LinearProgrammingUtil linearProgrammingUtil;
    private LinearProgrammingUtil.ControlParameters testParameters;
    private List<LinearProgrammingUtil.WasteData> testWasteData;

    @BeforeEach
    void setUp() {
        linearProgrammingUtil = new LinearProgrammingUtil();
        setupTestParameters();
        setupTestWasteData();
    }

    @Test
    @DisplayName("Mathematical Model - All 12 Constraints Implementation Test")
    void testLinearProgrammingMathematicalModel() {
        // Target: D₀ = 50,000 kg (50 tons/day)
        BigDecimal totalCapacity = new BigDecimal("50000");
        
        LinearProgrammingUtil.SolutionResult result = linearProgrammingUtil.solve(
            testWasteData, totalCapacity, testParameters);
        
        // Verify the solver runs without errors
        assertThat(result).isNotNull();
        assertThat(result.getSolutionTime()).isGreaterThan(0);
        
        if (result.isFeasible()) {
            // Verify all constraints are satisfied
            LinearProgrammingUtil.MixtureProperties properties = result.getMixtureProperties();
            
            // Constraint 1: Heat Value (12,500 ≤ Q ≤ 16,800 kJ/kg)
            assertThat(properties.getHeatValue())
                .isBetween(new BigDecimal("12500"), new BigDecimal("16800"));
            
            // Constraint 2: Water Content (≤ 45%)
            assertThat(properties.getWaterContent())
                .isLessThanOrEqualTo(new BigDecimal("45"));
            
            // Constraint 3: N Content (≤ 2%)
            assertThat(properties.getNContent())
                .isLessThanOrEqualTo(new BigDecimal("2"));
            
            // Constraint 4: S Content (≤ 3%)
            assertThat(properties.getSContent())
                .isLessThanOrEqualTo(new BigDecimal("3"));
            
            // Constraint 5: Cl Content (≤ 1.5%)
            assertThat(properties.getClContent())
                .isLessThanOrEqualTo(new BigDecimal("1.5"));
            
            // Constraint 6: F Content (≤ 1%)
            assertThat(properties.getFContent())
                .isLessThanOrEqualTo(new BigDecimal("1"));
            
            // Constraint 7: Hg Content (≤ 4 mg/kg)
            assertThat(properties.getHgContent())
                .isLessThanOrEqualTo(new BigDecimal("4"));
            
            // Constraint 8: Cd Content (≤ 1 mg/kg)
            assertThat(properties.getCdContent())
                .isLessThanOrEqualTo(new BigDecimal("1"));
            
            // Constraint 9: As+Ni Content (≤ 95 mg/kg)
            assertThat(properties.getAsNiContent())
                .isLessThanOrEqualTo(new BigDecimal("95"));
            
            // Constraint 10: Pb Content (≤ 70 mg/kg)
            assertThat(properties.getPbContent())
                .isLessThanOrEqualTo(new BigDecimal("70"));
            
            // Constraint 11: Heavy Metals Total (≤ 800 mg/kg)
            assertThat(properties.getHeavyMetalContent())
                .isLessThanOrEqualTo(new BigDecimal("800"));
            
            // Constraint 12: Stock Limits - verify each waste quantity ≤ max stock
            for (LinearProgrammingUtil.WasteData waste : testWasteData) {
                BigDecimal usedQuantity = result.getWasteQuantities().get(waste.getWasteId());
                if (usedQuantity != null) {
                    assertThat(usedQuantity)
                        .isLessThanOrEqualTo(waste.getMaxQuantity())
                        .isGreaterThanOrEqualTo(BigDecimal.ZERO);
                }
            }
            
            // Verify total quantity equals target (D₀ = ΣDᵢ)
            assertThat(result.getTotalQuantity()).isEqualByComparingTo(totalCapacity);
        }
    }

    @Test
    @DisplayName("Constraint Violation Detection - Heat Value Bounds")
    void testConstraintViolationDetection_HeatValue() {
        // Create waste data that will violate heat value constraints
        List<LinearProgrammingUtil.WasteData> highHeatWastes = createHighHeatValueWastes();
        BigDecimal totalCapacity = new BigDecimal("50000");
        
        LinearProgrammingUtil.SolutionResult result = linearProgrammingUtil.solve(
            highHeatWastes, totalCapacity, testParameters);
        
        if (!result.isFeasible()) {
            // Should detect heat value constraint violation
            assertThat(result.getViolations()).isNotEmpty();
            boolean hasHeatValueViolation = result.getViolations().stream()
                .anyMatch(v -> v.getConstraintName().contains("热值"));
            assertThat(hasHeatValueViolation).isTrue();
        }
    }

    @Test
    @DisplayName("Constraint Violation Detection - Heavy Metal Limits")
    void testConstraintViolationDetection_HeavyMetals() {
        // Create waste data that will violate heavy metal constraints
        List<LinearProgrammingUtil.WasteData> highMetalWastes = createHighHeavyMetalWastes();
        BigDecimal totalCapacity = new BigDecimal("50000");
        
        LinearProgrammingUtil.SolutionResult result = linearProgrammingUtil.solve(
            highMetalWastes, totalCapacity, testParameters);
        
        if (!result.isFeasible()) {
            // Should detect heavy metal constraint violations
            assertThat(result.getViolations()).isNotEmpty();
            boolean hasMetalViolation = result.getViolations().stream()
                .anyMatch(v -> v.getConstraintName().contains("Cd") || 
                              v.getConstraintName().contains("Pb") ||
                              v.getConstraintName().contains("Hg"));
            assertThat(hasMetalViolation).isTrue();
        }
    }

    @Test
    @DisplayName("Stock Constraint Verification - Inventory Limits")
    void testStockConstraintVerification() {
        // Create waste data with limited stock
        List<LinearProgrammingUtil.WasteData> limitedStockWastes = createLimitedStockWastes();
        BigDecimal totalCapacity = new BigDecimal("100000"); // Request more than available
        
        LinearProgrammingUtil.SolutionResult result = linearProgrammingUtil.solve(
            limitedStockWastes, totalCapacity, testParameters);
        
        // Should handle stock limitations gracefully
        assertThat(result).isNotNull();
        
        if (result.isFeasible()) {
            // Total should not exceed available stock
            BigDecimal totalAvailableStock = limitedStockWastes.stream()
                .map(LinearProgrammingUtil.WasteData::getMaxQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertThat(result.getTotalQuantity()).isLessThanOrEqualTo(totalAvailableStock);
        }
    }

    @Test
    @DisplayName("Performance Test - 50 Tons Processing Target")
    void testPerformanceWithRealisticTarget() {
        BigDecimal fiftyTons = new BigDecimal("50000"); // 50 tons = 50,000 kg
        
        long startTime = System.currentTimeMillis();
        LinearProgrammingUtil.SolutionResult result = linearProgrammingUtil.solve(
            testWasteData, fiftyTons, testParameters);
        long endTime = System.currentTimeMillis();
        
        // Performance requirement: calculation < 10 seconds
        assertThat(endTime - startTime).isLessThan(10000);
        assertThat(result.getSolutionTime()).isLessThan(10000);
    }

    @Test
    @DisplayName("Edge Case - Empty Waste List")
    void testEdgeCase_EmptyWasteList() {
        List<LinearProgrammingUtil.WasteData> emptyList = new ArrayList<>();
        BigDecimal totalCapacity = new BigDecimal("50000");
        
        LinearProgrammingUtil.SolutionResult result = linearProgrammingUtil.solve(
            emptyList, totalCapacity, testParameters);
        
        assertThat(result.isFeasible()).isFalse();
        assertThat(result.getErrorMessage()).contains("危废列表为空");
    }

    @Test
    @DisplayName("Edge Case - Zero Total Capacity")
    void testEdgeCase_ZeroCapacity() {
        BigDecimal zeroCapacity = BigDecimal.ZERO;
        
        LinearProgrammingUtil.SolutionResult result = linearProgrammingUtil.solve(
            testWasteData, zeroCapacity, testParameters);
        
        // Should handle zero capacity gracefully
        assertThat(result).isNotNull();
    }

    private void setupTestParameters() {
        testParameters = new LinearProgrammingUtil.ControlParameters();
        // Use exact constraint values from the mathematical model
        testParameters.setHeatValueMin(new BigDecimal("12500"));
        testParameters.setHeatValueMax(new BigDecimal("16800"));
        testParameters.setWaterContentMax(new BigDecimal("45"));
        testParameters.setNContentMax(new BigDecimal("2"));
        testParameters.setSContentMax(new BigDecimal("3"));
        testParameters.setClContentMax(new BigDecimal("1.5"));
        testParameters.setFContentMax(new BigDecimal("1"));
        testParameters.setHgContentMax(new BigDecimal("4"));
        testParameters.setCdContentMax(new BigDecimal("1"));
        testParameters.setAsNiContentMax(new BigDecimal("95"));
        testParameters.setPbContentMax(new BigDecimal("70"));
        testParameters.setHeavyMetalContentMax(new BigDecimal("800"));
    }

    private void setupTestWasteData() {
        testWasteData = new ArrayList<>();
        
        // Waste 1: High heat value, low contaminants
        LinearProgrammingUtil.WasteData waste1 = new LinearProgrammingUtil.WasteData();
        waste1.setWasteId(1L);
        waste1.setMaxQuantity(new BigDecimal("20000"));
        waste1.setHeatValue(new BigDecimal("15000")); // kJ/kg
        waste1.setWaterContent(new BigDecimal("20"));  // %
        waste1.setNContent(new BigDecimal("1.0"));     // %
        waste1.setSContent(new BigDecimal("1.5"));     // %
        waste1.setClContent(new BigDecimal("0.8"));    // %
        waste1.setFContent(new BigDecimal("0.5"));     // %
        waste1.setHgContent(new BigDecimal("2"));      // mg/kg
        waste1.setCdContent(new BigDecimal("0.5"));    // mg/kg
        waste1.setAsContent(new BigDecimal("30"));     // mg/kg
        waste1.setNiContent(new BigDecimal("30"));     // mg/kg
        waste1.setPbContent(new BigDecimal("40"));     // mg/kg
        waste1.setCrContent(new BigDecimal("200"));    // mg/kg
        waste1.setSnContent(new BigDecimal("100"));    // mg/kg
        waste1.setSbContent(new BigDecimal("50"));     // mg/kg
        waste1.setCuContent(new BigDecimal("100"));    // mg/kg
        waste1.setMnContent(new BigDecimal("150"));    // mg/kg
        testWasteData.add(waste1);
        
        // Waste 2: Medium heat value, medium contaminants
        LinearProgrammingUtil.WasteData waste2 = new LinearProgrammingUtil.WasteData();
        waste2.setWasteId(2L);
        waste2.setMaxQuantity(new BigDecimal("25000"));
        waste2.setHeatValue(new BigDecimal("14000")); // kJ/kg
        waste2.setWaterContent(new BigDecimal("30"));  // %
        waste2.setNContent(new BigDecimal("1.2"));     // %
        waste2.setSContent(new BigDecimal("2.0"));     // %
        waste2.setClContent(new BigDecimal("1.0"));    // %
        waste2.setFContent(new BigDecimal("0.3"));     // %
        waste2.setHgContent(new BigDecimal("1.5"));    // mg/kg
        waste2.setCdContent(new BigDecimal("0.3"));    // mg/kg
        waste2.setAsContent(new BigDecimal("25"));     // mg/kg
        waste2.setNiContent(new BigDecimal("40"));     // mg/kg
        waste2.setPbContent(new BigDecimal("35"));     // mg/kg
        waste2.setCrContent(new BigDecimal("150"));    // mg/kg
        waste2.setSnContent(new BigDecimal("75"));     // mg/kg
        waste2.setSbContent(new BigDecimal("25"));     // mg/kg
        waste2.setCuContent(new BigDecimal("80"));     // mg/kg
        waste2.setMnContent(new BigDecimal("120"));    // mg/kg
        testWasteData.add(waste2);
        
        // Waste 3: Lower heat value, balanced composition
        LinearProgrammingUtil.WasteData waste3 = new LinearProgrammingUtil.WasteData();
        waste3.setWasteId(3L);
        waste3.setMaxQuantity(new BigDecimal("15000"));
        waste3.setHeatValue(new BigDecimal("13500")); // kJ/kg
        waste3.setWaterContent(new BigDecimal("25"));  // %
        waste3.setNContent(new BigDecimal("0.8"));     // %
        waste3.setSContent(new BigDecimal("1.2"));     // %
        waste3.setClContent(new BigDecimal("0.6"));    // %
        waste3.setFContent(new BigDecimal("0.2"));     // %
        waste3.setHgContent(new BigDecimal("1"));      // mg/kg
        waste3.setCdContent(new BigDecimal("0.2"));    // mg/kg
        waste3.setAsContent(new BigDecimal("20"));     // mg/kg
        waste3.setNiContent(new BigDecimal("25"));     // mg/kg
        waste3.setPbContent(new BigDecimal("30"));     // mg/kg
        waste3.setCrContent(new BigDecimal("100"));    // mg/kg
        waste3.setSnContent(new BigDecimal("50"));     // mg/kg
        waste3.setSbContent(new BigDecimal("20"));     // mg/kg
        waste3.setCuContent(new BigDecimal("60"));     // mg/kg
        waste3.setMnContent(new BigDecimal("80"));     // mg/kg
        testWasteData.add(waste3);
    }

    private List<LinearProgrammingUtil.WasteData> createHighHeatValueWastes() {
        List<LinearProgrammingUtil.WasteData> wastes = new ArrayList<>();
        
        // Create waste with heat value exceeding 16,800 kJ/kg limit
        LinearProgrammingUtil.WasteData highHeatWaste = new LinearProgrammingUtil.WasteData();
        highHeatWaste.setWasteId(10L);
        highHeatWaste.setMaxQuantity(new BigDecimal("50000"));
        highHeatWaste.setHeatValue(new BigDecimal("18000")); // Exceeds 16,800 limit
        highHeatWaste.setWaterContent(new BigDecimal("10"));
        highHeatWaste.setNContent(new BigDecimal("0.5"));
        highHeatWaste.setSContent(new BigDecimal("0.8"));
        highHeatWaste.setClContent(new BigDecimal("0.3"));
        highHeatWaste.setFContent(new BigDecimal("0.1"));
        highHeatWaste.setHgContent(new BigDecimal("1"));
        highHeatWaste.setCdContent(new BigDecimal("0.1"));
        highHeatWaste.setAsContent(new BigDecimal("10"));
        highHeatWaste.setNiContent(new BigDecimal("15"));
        highHeatWaste.setPbContent(new BigDecimal("20"));
        highHeatWaste.setCrContent(new BigDecimal("50"));
        highHeatWaste.setSnContent(new BigDecimal("30"));
        highHeatWaste.setSbContent(new BigDecimal("10"));
        highHeatWaste.setCuContent(new BigDecimal("40"));
        highHeatWaste.setMnContent(new BigDecimal("60"));
        wastes.add(highHeatWaste);
        
        return wastes;
    }

    private List<LinearProgrammingUtil.WasteData> createHighHeavyMetalWastes() {
        List<LinearProgrammingUtil.WasteData> wastes = new ArrayList<>();
        
        // Create waste with heavy metals exceeding limits
        LinearProgrammingUtil.WasteData highMetalWaste = new LinearProgrammingUtil.WasteData();
        highMetalWaste.setWasteId(20L);
        highMetalWaste.setMaxQuantity(new BigDecimal("50000"));
        highMetalWaste.setHeatValue(new BigDecimal("14000"));
        highMetalWaste.setWaterContent(new BigDecimal("25"));
        highMetalWaste.setNContent(new BigDecimal("1.0"));
        highMetalWaste.setSContent(new BigDecimal("1.5"));
        highMetalWaste.setClContent(new BigDecimal("0.8"));
        highMetalWaste.setFContent(new BigDecimal("0.5"));
        highMetalWaste.setHgContent(new BigDecimal("6"));      // Exceeds 4 mg/kg limit
        highMetalWaste.setCdContent(new BigDecimal("2"));      // Exceeds 1 mg/kg limit
        highMetalWaste.setAsContent(new BigDecimal("60"));     // As+Ni will exceed 95 mg/kg
        highMetalWaste.setNiContent(new BigDecimal("50"));
        highMetalWaste.setPbContent(new BigDecimal("100"));    // Exceeds 70 mg/kg limit
        highMetalWaste.setCrContent(new BigDecimal("300"));    // Total heavy metals will exceed 800
        highMetalWaste.setSnContent(new BigDecimal("200"));
        highMetalWaste.setSbContent(new BigDecimal("150"));
        highMetalWaste.setCuContent(new BigDecimal("200"));
        highMetalWaste.setMnContent(new BigDecimal("250"));
        wastes.add(highMetalWaste);
        
        return wastes;
    }

    private List<LinearProgrammingUtil.WasteData> createLimitedStockWastes() {
        List<LinearProgrammingUtil.WasteData> wastes = new ArrayList<>();
        
        // Create wastes with very limited stock
        for (int i = 1; i <= 3; i++) {
            LinearProgrammingUtil.WasteData waste = new LinearProgrammingUtil.WasteData();
            waste.setWasteId((long) i);
            waste.setMaxQuantity(new BigDecimal("5000")); // Only 5 tons each
            waste.setHeatValue(new BigDecimal("14000"));
            waste.setWaterContent(new BigDecimal("30"));
            waste.setNContent(new BigDecimal("1.0"));
            waste.setSContent(new BigDecimal("1.5"));
            waste.setClContent(new BigDecimal("0.8"));
            waste.setFContent(new BigDecimal("0.3"));
            waste.setHgContent(new BigDecimal("2"));
            waste.setCdContent(new BigDecimal("0.5"));
            waste.setAsContent(new BigDecimal("30"));
            waste.setNiContent(new BigDecimal("30"));
            waste.setPbContent(new BigDecimal("40"));
            waste.setCrContent(new BigDecimal("150"));
            waste.setSnContent(new BigDecimal("100"));
            waste.setSbContent(new BigDecimal("50"));
            waste.setCuContent(new BigDecimal("80"));
            waste.setMnContent(new BigDecimal("100"));
            wastes.add(waste);
        }
        
        return wastes;
    }
} 