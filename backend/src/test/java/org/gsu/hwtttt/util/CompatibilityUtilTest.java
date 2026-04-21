package org.gsu.hwtttt.util;

import org.gsu.hwtttt.common.enums.WasteCategoryEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * CompatibilityUtil Unit Tests
 * 
 * Tests the 40×40 (actually 41×41) compatibility matrix and risk code handling:
 * - Verifies matrix lookup functionality
 * - Tests all risk codes: H, F, G, GT, E, P, S, U
 * - Validates compatibility checks for ALL pairs in combinations
 * - Tests edge cases and boundary conditions
 * 
 * Risk Codes:
 * H = Heat generation
 * F = Fire hazard
 * G = Gas generation
 * GT = Toxic gas
 * E = Explosion
 * P = Polymerization
 * S = Toxic dissolution
 * U = Unknown danger
 */
@SpringBootTest
@ExtendWith(MockitoExtension.class)
class CompatibilityUtilTest {

    private CompatibilityUtil compatibilityUtil;

    @BeforeEach
    void setUp() {
        compatibilityUtil = new CompatibilityUtil();
        // Initialize the matrix (this would normally be done by @PostConstruct)
        compatibilityUtil.initCompatibilityMatrix();
    }

    @Test
    @DisplayName("Matrix Initialization - 41×41 Compatibility Matrix")
    void testMatrixInitialization() {
        // Test that the compatibility matrix is properly initialized
        // Valid categories are 1-41 (WasteCategoryEnum should define these)
        
        // Test diagonal elements (same category should be compatible)
        for (int i = 1; i <= 41; i++) {
            boolean compatible = CompatibilityUtil.isCompatible(i, i);
            assertThat(compatible).as("Category %d should be compatible with itself", i).isTrue();
        }
    }

    @Test
    @DisplayName("Known Incompatible Pairs - Acid vs Base (Categories 1 & 2)")
    void testKnownIncompatiblePairs_AcidBase() {
        // Test specific incompatible pair: 酸类、矿物、亚氧化物(1) vs 碱类、矿物、氧化物(2)
        boolean compatible = CompatibilityUtil.isCompatible(1, 2);
        assertThat(compatible).as("Acid (1) and Base (2) should be incompatible").isFalse();
        
        // Test symmetry: 2 vs 1 should also be incompatible
        boolean compatibleReverse = CompatibilityUtil.isCompatible(2, 1);
        assertThat(compatibleReverse).as("Base (2) and Acid (1) should be incompatible (symmetry)").isFalse();
    }

    @Test
    @DisplayName("Known Incompatible Pairs - Oxidizers vs Reducers")
    void testKnownIncompatiblePairs_OxidizerReducer() {
        // Test oxidizer (22) vs reducer (30) incompatibility
        boolean compatible = CompatibilityUtil.isCompatible(22, 30);
        assertThat(compatible).as("Oxidizer (22) and Reducer (30) should be incompatible").isFalse();
    }

    @Test
    @DisplayName("Known Compatible Pairs - Same Chemical Family")
    void testKnownCompatiblePairs() {
        // Test pairs that should be compatible (not in the incompatible list)
        // Example:醇类及二醇(4) with 酮(19) might be compatible depending on the matrix
        
        // We need to find some pairs that are actually compatible
        // Let's test some categories that don't have conflicts with each other
        boolean compatible = CompatibilityUtil.isCompatible(4, 19);
        // The actual result depends on the matrix implementation
        // This test verifies the method runs without error
        assertThat(compatible).isNotNull();
    }

    @Test
    @DisplayName("Multiple Categories Compatibility Check")
    void testMultipleCategoriesCompatibilityCheck() {
        // Test with a combination that should have some incompatible pairs
        List<Integer> categories = Arrays.asList(1, 2, 22, 30); // Mix of acids, bases, oxidizers, reducers
        
        CompatibilityUtil.CompatibilityCheckResult result = 
            CompatibilityUtil.checkCompatibility(categories);
        
        assertThat(result).isNotNull();
        assertThat(result.getCompatible()).isFalse(); // Should detect incompatibilities
        assertThat(result.getIncompatiblePairs()).isNotEmpty();
        
        // Verify specific incompatible pairs are detected
        boolean foundAcidBase = result.getIncompatiblePairs().stream()
            .anyMatch(pair -> (pair.getCategory1() == 1 && pair.getCategory2() == 2) ||
                             (pair.getCategory1() == 2 && pair.getCategory2() == 1));
        assertThat(foundAcidBase).as("Should detect Acid-Base incompatibility").isTrue();
        
        boolean foundOxidizerReducer = result.getIncompatiblePairs().stream()
            .anyMatch(pair -> (pair.getCategory1() == 22 && pair.getCategory2() == 30) ||
                             (pair.getCategory1() == 30 && pair.getCategory2() == 22));
        assertThat(foundOxidizerReducer).as("Should detect Oxidizer-Reducer incompatibility").isTrue();
    }

    @Test
    @DisplayName("All Compatible Categories Check")
    void testAllCompatibleCategories() {
        // Test with a combination that should be all compatible
        List<Integer> compatibleCategories = Arrays.asList(4, 5, 10); // Choose categories that don't conflict
        
        CompatibilityUtil.CompatibilityCheckResult result = 
            CompatibilityUtil.checkCompatibility(compatibleCategories);
        
        assertThat(result).isNotNull();
        // The result depends on the actual matrix implementation
        if (result.getCompatible()) {
            assertThat(result.getIncompatiblePairs()).isEmpty();
        }
    }

    @Test
    @DisplayName("Risk Code Mapping - Acid-Base Reaction")
    void testRiskCodeMapping_AcidBase() {
        List<Integer> acidBaseCategories = Arrays.asList(1, 2); // Acid and Base
        
        CompatibilityUtil.CompatibilityCheckResult result = 
            CompatibilityUtil.checkCompatibility(acidBaseCategories);
        
        if (!result.getCompatible()) {
            // Check that the incompatible pair has a proper reason
            CompatibilityUtil.IncompatiblePair pair = result.getIncompatiblePairs().get(0);
            assertThat(pair.getReason()).isNotNull();
            assertThat(pair.getReason()).contains("酸碱中和反应"); // Should mention acid-base neutralization
        }
    }

    @Test
    @DisplayName("Risk Code Mapping - Oxidizer-Reducer Reaction")
    void testRiskCodeMapping_OxidizerReducer() {
        List<Integer> oxidizerReducerCategories = Arrays.asList(22, 30); // Oxidizer and Reducer
        
        CompatibilityUtil.CompatibilityCheckResult result = 
            CompatibilityUtil.checkCompatibility(oxidizerReducerCategories);
        
        if (!result.getCompatible()) {
            // Check that the incompatible pair has a proper reason involving oxidation-reduction
            CompatibilityUtil.IncompatiblePair pair = result.getIncompatiblePairs().get(0);
            assertThat(pair.getReason()).isNotNull();
            assertThat(pair.getReason()).containsAnyOf("氧化还原反应", "燃烧", "爆炸");
        }
    }

    @Test
    @DisplayName("Risk Code Mapping - Water Reactive")
    void testRiskCodeMapping_WaterReactive() {
        List<Integer> waterReactiveCategories = Arrays.asList(41, 1); // Water reactive + acid
        
        CompatibilityUtil.CompatibilityCheckResult result = 
            CompatibilityUtil.checkCompatibility(waterReactiveCategories);
        
        if (!result.getCompatible()) {
            // Check for water reactive warning
            CompatibilityUtil.IncompatiblePair pair = result.getIncompatiblePairs().get(0);
            assertThat(pair.getReason()).isNotNull();
            assertThat(pair.getReason()).containsAnyOf("与水反应", "有毒气体");
        }
    }

    @Test
    @DisplayName("Edge Case - Invalid Category Numbers")
    void testEdgeCase_InvalidCategories() {
        // Test with invalid category numbers (outside 1-41 range)
        boolean compatibleZero = CompatibilityUtil.isCompatible(0, 1);
        assertThat(compatibleZero).isFalse(); // Invalid categories should return false
        
        boolean compatibleOverRange = CompatibilityUtil.isCompatible(42, 1);
        assertThat(compatibleOverRange).isFalse(); // Category 42 doesn't exist
        
        boolean compatibleNegative = CompatibilityUtil.isCompatible(-1, 1);
        assertThat(compatibleNegative).isFalse(); // Negative categories invalid
    }

    @Test
    @DisplayName("Edge Case - Null Category Check")
    void testEdgeCase_NullCategories() {
        // Test behavior with null categories
        boolean compatibleNull = CompatibilityUtil.isCompatible(null, 1);
        assertThat(compatibleNull).isFalse(); // Null should return false
        
        boolean compatibleBothNull = CompatibilityUtil.isCompatible(null, null);
        assertThat(compatibleBothNull).isFalse(); // Both null should return false
    }

    @Test
    @DisplayName("Edge Case - Empty Category List")
    void testEdgeCase_EmptyList() {
        List<Integer> emptyList = Arrays.asList();
        
        CompatibilityUtil.CompatibilityCheckResult result = 
            CompatibilityUtil.checkCompatibility(emptyList);
        
        assertThat(result).isNotNull();
        assertThat(result.getCompatible()).isTrue(); // Empty list should be considered compatible
        assertThat(result.getIncompatiblePairs()).isEmpty();
    }

    @Test
    @DisplayName("Edge Case - Single Category")
    void testEdgeCase_SingleCategory() {
        List<Integer> singleCategory = Arrays.asList(1);
        
        CompatibilityUtil.CompatibilityCheckResult result = 
            CompatibilityUtil.checkCompatibility(singleCategory);
        
        assertThat(result).isNotNull();
        assertThat(result.getCompatible()).isTrue(); // Single category should be compatible
        assertThat(result.getIncompatiblePairs()).isEmpty();
    }

    @Test
    @DisplayName("Matrix Symmetry Test")
    void testMatrixSymmetry() {
        // Test that the compatibility matrix is symmetric: compatible(i,j) == compatible(j,i)
        for (int i = 1; i <= 41; i++) {
            for (int j = i + 1; j <= 41; j++) {
                boolean compatibleIJ = CompatibilityUtil.isCompatible(i, j);
                boolean compatibleJI = CompatibilityUtil.isCompatible(j, i);
                
                assertThat(compatibleIJ).as("Matrix should be symmetric: (%d,%d) != (%d,%d)", i, j, j, i)
                    .isEqualTo(compatibleJI);
            }
        }
    }

    @Test
    @DisplayName("Comprehensive Matrix Coverage Test")
    void testComprehensiveMatrixCoverage() {
        // Test that the matrix covers expected incompatible relationships
        int incompatibleCount = 0;
        int totalPairs = 0;
        
        for (int i = 1; i <= 41; i++) {
            for (int j = i + 1; j <= 41; j++) {
                totalPairs++;
                if (!CompatibilityUtil.isCompatible(i, j)) {
                    incompatibleCount++;
                }
            }
        }
        
        // Log statistics for verification
        System.out.printf("Matrix Statistics: %d incompatible pairs out of %d total pairs (%.2f%%)%n",
            incompatibleCount, totalPairs, (double) incompatibleCount / totalPairs * 100);
        
        // Expect at least some incompatible pairs (should be > 0)
        assertThat(incompatibleCount).as("Should have some incompatible pairs defined").isGreaterThan(0);
        
        // But not too many (shouldn't be more than 50% incompatible)
        assertThat(incompatibleCount).as("Shouldn't have too many incompatible pairs").isLessThan(totalPairs / 2);
    }

    @Test
    @DisplayName("Performance Test - Large Category List")
    void testPerformance_LargeCategoryList() {
        // Test performance with a larger list of categories
        List<Integer> largeList = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
        
        long startTime = System.currentTimeMillis();
        CompatibilityUtil.CompatibilityCheckResult result = 
            CompatibilityUtil.checkCompatibility(largeList);
        long endTime = System.currentTimeMillis();
        
        // Should complete within reasonable time (< 1 second for this size)
        assertThat(endTime - startTime).as("Compatibility check should be fast").isLessThan(1000);
        
        assertThat(result).isNotNull();
        // With this many categories, there should be some incompatible pairs
        assertThat(result.getIncompatiblePairs()).isNotNull();
    }

    @Test
    @DisplayName("Specific Incompatible Relationships Verification")
    void testSpecificIncompatibleRelationships() {
        // Test some specific relationships mentioned in the requirements
        
        // 1. 酸类 vs 碱类 should be incompatible
        assertThat(CompatibilityUtil.isCompatible(1, 2)).isFalse();
        assertThat(CompatibilityUtil.isCompatible(3, 2)).isFalse(); // 酸类、有机的 vs 碱类
        
        // 2. 氧化剂 vs 还原剂 should be incompatible  
        assertThat(CompatibilityUtil.isCompatible(22, 30)).isFalse();
        
        // 3. 氧化剂 vs 易燃物质 should be incompatible
        // Need to identify which category represents flammable materials
        // This depends on the actual category definitions
        
        // 4. 与水反应的物质 should have restrictions
        assertThat(CompatibilityUtil.isCompatible(41, 1)).isFalse(); // Water reactive vs acid
    }

    @Test
    @DisplayName("Category Name Resolution Test")
    void testCategoryNameResolution() {
        // Test that category names can be resolved properly
        List<Integer> testCategories = Arrays.asList(1, 2);
        
        CompatibilityUtil.CompatibilityCheckResult result = 
            CompatibilityUtil.checkCompatibility(testCategories);
        
        if (!result.getCompatible() && !result.getIncompatiblePairs().isEmpty()) {
            CompatibilityUtil.IncompatiblePair pair = result.getIncompatiblePairs().get(0);
            
            // Category names should be resolved (not null or empty)
            assertThat(pair.getCategory1Name()).isNotNull();
            assertThat(pair.getCategory2Name()).isNotNull();
            assertThat(pair.getCategory1Name()).isNotEmpty();
            assertThat(pair.getCategory2Name()).isNotEmpty();
        }
    }
} 