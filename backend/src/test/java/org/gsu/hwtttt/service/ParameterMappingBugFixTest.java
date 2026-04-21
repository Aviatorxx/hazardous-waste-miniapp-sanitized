package org.gsu.hwtttt.service;

import org.gsu.hwtttt.entity.MatchingConstraints;
import org.gsu.hwtttt.service.impl.MatchingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Test class to verify parameter mapping bug fix
 * 
 * Verifies that constraint parameter codes (HEAT_VALUE, WATER_CONTENT, etc.)
 * are properly mapped to weighted averages keys (heatValue, waterContent, etc.)
 *
 * @author System
 * @date 2025/01/05
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class ParameterMappingBugFixTest {

    @InjectMocks
    private MatchingServiceImpl matchingService;

    private MatchingConstraints heatConstraint;
    private MatchingConstraints waterConstraint;
    private MatchingConstraints chlorineConstraint;

    @BeforeEach
    void setUp() {
        // Set up heat value constraint (stored in kJ/kg)
        heatConstraint = new MatchingConstraints();
        heatConstraint.setConstraintName("热值控制");
        heatConstraint.setParameterCode("HEAT_VALUE");
        heatConstraint.setMinValue(new BigDecimal("12500")); // kJ/kg
        heatConstraint.setMaxValue(new BigDecimal("16800")); // kJ/kg
        heatConstraint.setUnit("kJ/kg");
        heatConstraint.setIsActive(true);
        
        // Set up water content constraint
        waterConstraint = new MatchingConstraints();
        waterConstraint.setConstraintName("含水率控制");
        waterConstraint.setParameterCode("WATER_CONTENT");
        waterConstraint.setMaxValue(new BigDecimal("45")); // %
        waterConstraint.setUnit("%");
        waterConstraint.setIsActive(true);
        
        // Set up chlorine content constraint
        chlorineConstraint = new MatchingConstraints();
        chlorineConstraint.setConstraintName("氯含量控制");
        chlorineConstraint.setParameterCode("CL_CONTENT");
        chlorineConstraint.setMaxValue(new BigDecimal("1.5")); // %
        chlorineConstraint.setUnit("%");
        chlorineConstraint.setIsActive(true);
    }

    @Test
    @DisplayName("Parameter Mapping - Heat Value Constraint Should Now Work")
    void testHeatValueParameterMapping() throws Exception {
        // Create weighted averages with camelCase keys (as calculated by system)
        Map<String, Object> averages = new HashMap<>();
        averages.put("heatValue", new BigDecimal("4529.80")); // This should now be found!
        
        List<MatchingConstraints> constraints = List.of(heatConstraint);
        
        List<String> violations = callCheckViolations(averages, constraints);
        
        // Should detect violation: 4529.80 cal/g = 18,952.68 kJ/kg > 16800 kJ/kg
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0)).contains("热值控制");
        assertThat(violations.get(0)).contains("高于最大允许值");
        assertThat(violations.get(0)).contains("18952.68");
    }
    
    @Test
    @DisplayName("Parameter Mapping - Water Content Constraint Should Now Work")
    void testWaterContentParameterMapping() throws Exception {
        // Create weighted averages with camelCase keys
        Map<String, Object> averages = new HashMap<>();
        averages.put("waterContent", new BigDecimal("50.0")); // Exceeds 45% limit
        
        List<MatchingConstraints> constraints = List.of(waterConstraint);
        
        List<String> violations = callCheckViolations(averages, constraints);
        
        // Should detect violation: 50% > 45% (max)
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0)).contains("含水率控制");
        assertThat(violations.get(0)).contains("高于最大允许值");
        assertThat(violations.get(0)).contains("50.00");
        assertThat(violations.get(0)).contains("45.00");
    }
    
    @Test
    @DisplayName("Parameter Mapping - Chlorine Content Constraint Should Now Work")
    void testChlorineContentParameterMapping() throws Exception {
        // Create weighted averages with camelCase keys
        Map<String, Object> averages = new HashMap<>();
        averages.put("clContent", new BigDecimal("12.6582")); // Exceeds 1.5% limit
        
        List<MatchingConstraints> constraints = List.of(chlorineConstraint);
        
        List<String> violations = callCheckViolations(averages, constraints);
        
        // Should detect violation: 12.6582% > 1.5% (max)
        assertThat(violations).hasSize(1);
        assertThat(violations.get(0)).contains("氯含量控制");
        assertThat(violations.get(0)).contains("高于最大允许值");
        assertThat(violations.get(0)).contains("12.66");
        assertThat(violations.get(0)).contains("1.50");
    }
    
    @Test
    @DisplayName("Multiple Parameter Mapping - All Constraints Should Now Work")
    void testMultipleParameterMapping() throws Exception {
        // Create weighted averages with problematic values
        Map<String, Object> averages = new HashMap<>();
        averages.put("heatValue", new BigDecimal("4529.80"));     // Should fail heat constraint
        averages.put("waterContent", new BigDecimal("50.0"));     // Should fail water constraint
        averages.put("clContent", new BigDecimal("12.6582"));     // Should fail chlorine constraint
        
        List<MatchingConstraints> constraints = List.of(heatConstraint, waterConstraint, chlorineConstraint);
        
        List<String> violations = callCheckViolations(averages, constraints);
        
        // Should detect all 3 violations
        assertThat(violations).hasSize(3);
        assertThat(violations).anyMatch(v -> v.contains("热值控制") && v.contains("高于最大允许值"));
        assertThat(violations).anyMatch(v -> v.contains("含水率控制") && v.contains("高于最大允许值"));
        assertThat(violations).anyMatch(v -> v.contains("氯含量控制") && v.contains("高于最大允许值"));
    }
    
    @Test
    @DisplayName("No Parameter Mapping Issues - Valid Values Should Pass")
    void testValidParameterMapping() throws Exception {
        // Create weighted averages with valid values
        Map<String, Object> averages = new HashMap<>();
        averages.put("heatValue", new BigDecimal("3000.0"));      // Valid: 12,552 kJ/kg (within range)
        averages.put("waterContent", new BigDecimal("30.0"));     // Valid: 30% < 45%
        averages.put("clContent", new BigDecimal("1.0"));         // Valid: 1.0% < 1.5%
        
        List<MatchingConstraints> constraints = List.of(heatConstraint, waterConstraint, chlorineConstraint);
        
        List<String> violations = callCheckViolations(averages, constraints);
        
        // Should have no violations
        assertThat(violations).isEmpty();
    }
    
    @Test
    @DisplayName("Missing Parameter - Should Log Warning But Not Crash")
    void testMissingParameter() throws Exception {
        // Create weighted averages missing some parameters
        Map<String, Object> averages = new HashMap<>();
        averages.put("heatValue", new BigDecimal("3000.0"));      // Only provide heat value
        // Missing waterContent and clContent
        
        List<MatchingConstraints> constraints = List.of(heatConstraint, waterConstraint, chlorineConstraint);
        
        List<String> violations = callCheckViolations(averages, constraints);
        
        // Should only validate heat value (no violations), ignore missing parameters
        assertThat(violations).isEmpty();
    }
    
    /**
     * Helper method to call the private checkViolations method via reflection
     */
    private List<String> callCheckViolations(Map<String, Object> weightedAverages, 
                                           List<MatchingConstraints> constraints) throws Exception {
        Method checkViolationsMethod = MatchingServiceImpl.class.getDeclaredMethod(
            "checkViolations", Map.class, List.class);
        checkViolationsMethod.setAccessible(true);
        
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) checkViolationsMethod.invoke(matchingService, weightedAverages, constraints);
        
        return result;
    }
} 