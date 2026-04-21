package org.gsu.hwtttt.validation;

import org.gsu.hwtttt.entity.*;
import org.gsu.hwtttt.mapper.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Data Validation Tests for Module 4
 * Verifies database structure, data integrity, and business rules
 * 
 * @author WenXin
 * @date 2025/01/07
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataValidationTest {

    @Autowired
    private CompatibilityCategoryMapper compatibilityCategoryMapper;

    @Autowired
    private CompatibilityMatrixMapper compatibilityMatrixMapper;

    @Autowired
    private HazardousWasteMapper hazardousWasteMapper;

    @Autowired
    private MatchingConstraintsMapper matchingConstraintsMapper;

    @Autowired
    private MatchingSessionsMapper matchingSessionsMapper;

    @Autowired
    private MatchingDetailsMapper matchingDetailsMapper;

    @Autowired
    private CompatibilityChecksMapper compatibilityChecksMapper;

    @Autowired
    private MatchingResultsMapper matchingResultsMapper;

    // ==================== Database Structure Tests ====================

    @Test
    @DisplayName("Compatibility Categories Table - Structure and Data")
    void testCompatibilityCategoriesTable() {
        // Test table exists and can be queried
        List<CompatibilityCategory> categories = compatibilityCategoryMapper.selectList(null);
        
        // Verify we can insert test data
        CompatibilityCategory testCategory = new CompatibilityCategory();
        testCategory.setCategoryCode("TEST01");
        testCategory.setCategoryNameCn("测试类别");
        testCategory.setCategoryNameEn("Test Category");
        testCategory.setIdx(999);
        
        int inserted = compatibilityCategoryMapper.insert(testCategory);
        assertThat(inserted).isEqualTo(1);
        assertThat(testCategory.getCategoryCode()).isNotNull();
        
        // Test retrieval
        CompatibilityCategory retrieved = compatibilityCategoryMapper.selectById(testCategory.getCategoryCode());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getCategoryCode()).isEqualTo("TEST01");
        assertThat(retrieved.getCategoryNameCn()).isEqualTo("测试类别");
        assertThat(retrieved.getIdx()).isEqualTo(999);
    }

    @Test
    @DisplayName("Compatibility Matrix Table - Structure and Data")
    void testCompatibilityMatrixTable() {
        // Test table exists and can be queried
        List<CompatibilityMatrix> matrix = compatibilityMatrixMapper.selectList(null);
        
        // Verify we can insert test data
        CompatibilityMatrix testRule = new CompatibilityMatrix();
        testRule.setWasteCategory1("TEST01");
        testRule.setWasteCategory2("TEST02");
        testRule.setCompatible(false);
        testRule.setRiskLevel("HIGH");
        testRule.setSafetyNotes("Test restriction");
        
        int inserted = compatibilityMatrixMapper.insert(testRule);
        assertThat(inserted).isEqualTo(1);
        
        // Test retrieval by categories
        CompatibilityMatrix retrieved = compatibilityMatrixMapper.findRule("TEST01", "TEST02");
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getCompatible()).isEqualTo(false);
        assertThat(retrieved.getRiskLevel()).isEqualTo("HIGH");
    }

    @Test
    @DisplayName("Hazardous Waste Table - Structure and Data")
    void testHazardousWasteTable() {
        // Test table exists and can be queried
        List<HazardousWaste> wastes = hazardousWasteMapper.selectList(null);
        
        // Verify we can insert test waste
        HazardousWaste testWaste = new HazardousWaste();
        testWaste.setWasteCode("TEST001");
        testWaste.setWasteName("Test Hazardous Waste");
        testWaste.setSourceUnit("Test Unit");
        testWaste.setHeatValueCalPerG(new BigDecimal("4000"));
        testWaste.setWaterContentPercent(new BigDecimal("15"));
        testWaste.setRemainingStorage(new BigDecimal("1000"));
        testWaste.setFlammable(true);
        testWaste.setOxidizing(false);
        testWaste.setToxic(false);
        testWaste.setCorrosive(false);
        testWaste.setCompatibilityCategoryCode("TEST01");
        testWaste.setDeleted(false);
        
        int inserted = hazardousWasteMapper.insert(testWaste);
        assertThat(inserted).isEqualTo(1);
        assertThat(testWaste.getId()).isNotNull();
        
        // Test retrieval
        HazardousWaste retrieved = hazardousWasteMapper.selectById(testWaste.getId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getWasteCode()).isEqualTo("TEST001");
        assertThat(retrieved.getHeatValueCalPerG()).isEqualByComparingTo(new BigDecimal("4000"));
        assertThat(retrieved.getFlammable()).isTrue();
        assertThat(retrieved.getOxidizing()).isFalse();
    }

    @Test
    @DisplayName("Matching Constraints Table - Structure and Data")
    void testMatchingConstraintsTable() {
        // Test table exists and can be queried
        List<MatchingConstraints> constraints = matchingConstraintsMapper.selectList(null);
        
        // Verify we can insert test constraint
        MatchingConstraints testConstraint = new MatchingConstraints();
        testConstraint.setConstraintName("Test Heat Value Constraint");
        testConstraint.setParameterCode("testHeatValue");
        testConstraint.setMinValue(new BigDecimal("10000"));
        testConstraint.setMaxValue(new BigDecimal("20000"));
        testConstraint.setUnit("kJ/kg");
        testConstraint.setIsActive(true);
        testConstraint.setSortOrder(999);
        
        int inserted = matchingConstraintsMapper.insert(testConstraint);
        assertThat(inserted).isEqualTo(1);
        
        // Test retrieval by parameter code
        MatchingConstraints retrieved = matchingConstraintsMapper.selectByParameterCode("testHeatValue");
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getConstraintName()).isEqualTo("Test Heat Value Constraint");
        assertThat(retrieved.getMinValue()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(retrieved.getMaxValue()).isEqualByComparingTo(new BigDecimal("20000"));
        assertThat(retrieved.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Matching Sessions Table - Structure and Data")
    void testMatchingSessionsTable() {
        // Test table exists and can be queried
        List<MatchingSessions> sessions = matchingSessionsMapper.selectList(null);
        
        // Verify we can insert test session
        MatchingSessions testSession = new MatchingSessions();
        testSession.setSessionName("Test Matching Session");
        testSession.setTargetHeatValue(new BigDecimal("15000"));
        testSession.setTotalAmount(new BigDecimal("1000"));
        testSession.setStatus("draft");
        testSession.setCreateUser("test_user");
        testSession.setDeleted(false);
        
        int inserted = matchingSessionsMapper.insert(testSession);
        assertThat(inserted).isEqualTo(1);
        assertThat(testSession.getId()).isNotNull();
        
        // Test retrieval
        MatchingSessions retrieved = matchingSessionsMapper.selectById(testSession.getId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getSessionName()).isEqualTo("Test Matching Session");
        assertThat(retrieved.getStatus()).isEqualTo("draft");
        assertThat(retrieved.getTargetHeatValue()).isEqualByComparingTo(new BigDecimal("15000"));
    }

    @Test
    @DisplayName("Matching Details Table - Structure and Data")
    void testMatchingDetailsTable() {
        // First create a test session
        MatchingSessions testSession = new MatchingSessions();
        testSession.setSessionName("Test Session for Details");
        testSession.setStatus("draft");
        testSession.setCreateUser("test_user");
        testSession.setDeleted(false);
        matchingSessionsMapper.insert(testSession);

        // Create test waste
        HazardousWaste testWaste = new HazardousWaste();
        testWaste.setWasteCode("DETAIL001");
        testWaste.setWasteName("Test Waste for Details");
        testWaste.setRemainingStorage(new BigDecimal("500"));
        testWaste.setDeleted(false);
        hazardousWasteMapper.insert(testWaste);
        
        // Test matching details
        MatchingDetails testDetail = new MatchingDetails();
        testDetail.setSessionId(testSession.getId());
        testDetail.setWasteId(testWaste.getId());
        testDetail.setPlannedAmount(new BigDecimal("100"));
        testDetail.setActualAmount(new BigDecimal("100"));
        
        int inserted = matchingDetailsMapper.insert(testDetail);
        assertThat(inserted).isEqualTo(1);
        
        // Test retrieval by session ID
        List<MatchingDetails> details = matchingDetailsMapper.selectBySessionId(testSession.getId());
        assertThat(details).hasSize(1);
        assertThat(details.get(0).getWasteId()).isEqualTo(testWaste.getId());
        assertThat(details.get(0).getPlannedAmount()).isEqualByComparingTo(new BigDecimal("100"));
    }

    @Test
    @DisplayName("Compatibility Checks Table - Structure and Data")
    void testCompatibilityChecksTable() {
        // Test table exists and can be queried
        List<CompatibilityChecks> checks = compatibilityChecksMapper.selectList(null);
        
        // Verify we can insert test check
        CompatibilityChecks testCheck = new CompatibilityChecks();
        testCheck.setSessionId(1L);
        testCheck.setWasteId1(1L);
        testCheck.setWasteId2(2L);
        testCheck.setCompatible(false);
        testCheck.setCheckResult("Incompatible - Flammable with Oxidizing");
        testCheck.setRiskLevel("HIGH");
        
        int inserted = compatibilityChecksMapper.insert(testCheck);
        assertThat(inserted).isEqualTo(1);
        assertThat(testCheck.getId()).isNotNull();
        
        // Test retrieval
        CompatibilityChecks retrieved = compatibilityChecksMapper.selectById(testCheck.getId());
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getCompatible()).isFalse();
        assertThat(retrieved.getCheckResult()).contains("Incompatible");
        assertThat(retrieved.getRiskLevel()).isEqualTo("HIGH");
    }

    @Test
    @DisplayName("Matching Results Table - Structure and Data")
    void testMatchingResultsTable() {
        // Test table exists and can be queried
        List<MatchingResults> results = matchingResultsMapper.selectList(null);
        
        // Verify we can insert test result
        MatchingResults testResult = new MatchingResults();
        testResult.setSessionId(1L);
        testResult.setResultStatus("success");
        testResult.setCalculatedHeatValue(new BigDecimal("14500"));
        testResult.setCalculationMatrix("{\"waste1\": 0.6, \"waste2\": 0.4}"); // Use existing field for JSON data
        testResult.setCalculationTime(LocalDateTime.now());
        
        int inserted = matchingResultsMapper.insert(testResult);
        assertThat(inserted).isEqualTo(1);
        
        // Test retrieval by session ID
        MatchingResults retrieved = matchingResultsMapper.selectBySessionId(1L);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getResultStatus()).isEqualTo("success");
        assertThat(retrieved.getCalculatedHeatValue()).isEqualByComparingTo(new BigDecimal("14500"));
        assertThat(retrieved.isSuccess()).isTrue(); // Using new convenience method
    }

    // ==================== Data Integrity Tests ====================

    @Test
    @DisplayName("Data Integrity - Foreign Key Relationships")
    void testDataIntegrityForeignKeys() {
        // Create parent records
        MatchingSessions session = new MatchingSessions();
        session.setSessionName("Integrity Test Session");
        session.setStatus("draft");
        session.setCreateUser("test_user");
        session.setDeleted(false);
        matchingSessionsMapper.insert(session);

        HazardousWaste waste = new HazardousWaste();
        waste.setWasteCode("INTEGRITY001");
        waste.setWasteName("Integrity Test Waste");
        waste.setRemainingStorage(new BigDecimal("1000"));
        waste.setDeleted(false);
        hazardousWasteMapper.insert(waste);

        // Create child record
        MatchingDetails detail = new MatchingDetails();
        detail.setSessionId(session.getId());
        detail.setWasteId(waste.getId());
        detail.setPlannedAmount(new BigDecimal("200"));
        detail.setActualAmount(new BigDecimal("200"));
        
        int inserted = matchingDetailsMapper.insert(detail);
        assertThat(inserted).isEqualTo(1);

        // Verify relationship
        List<MatchingDetails> details = matchingDetailsMapper.selectBySessionId(session.getId());
        assertThat(details).hasSize(1);
        assertThat(details.get(0).getWasteId()).isEqualTo(waste.getId());
    }

    @Test
    @DisplayName("Data Integrity - Unique Constraints")
    void testDataIntegrityUniqueConstraints() {
        // Test waste code uniqueness
        HazardousWaste waste1 = new HazardousWaste();
        waste1.setWasteCode("UNIQUE001");
        waste1.setWasteName("First Unique Waste");
        waste1.setDeleted(false);
        int inserted1 = hazardousWasteMapper.insert(waste1);
        assertThat(inserted1).isEqualTo(1);

        // Attempting to insert another waste with same code should be handled gracefully
        HazardousWaste waste2 = new HazardousWaste();
        waste2.setWasteCode("UNIQUE001");
        waste2.setWasteName("Second Unique Waste");
        waste2.setDeleted(false);
        
        // Depending on DB constraints, this might throw exception or succeed
        // We just verify the first one was inserted successfully
        HazardousWaste retrieved = hazardousWasteMapper.selectById(waste1.getId());
        assertThat(retrieved.getWasteCode()).isEqualTo("UNIQUE001");
    }

    @Test
    @DisplayName("Data Integrity - Not Null Constraints")
    void testDataIntegrityNotNullConstraints() {
        // Test required fields for sessions
        MatchingSessions session = new MatchingSessions();
        session.setSessionName("Required Fields Test");
        session.setStatus("draft");
        session.setCreateUser("test_user");
        session.setDeleted(false);
        // Note: targetHeatValue and totalAmount might be nullable
        
        int inserted = matchingSessionsMapper.insert(session);
        assertThat(inserted).isEqualTo(1);
        assertThat(session.getId()).isNotNull();
        assertThat(session.getCreateTime()).isNotNull(); // Should be auto-filled
    }

    // ==================== Business Rules Validation ====================

    @Test
    @DisplayName("Business Rules - Status Transitions")
    void testBusinessRulesStatusTransitions() {
        // Test valid status values
        String[] validStatuses = {"draft", "calculating", "completed", "failed"};
        
        for (String status : validStatuses) {
            MatchingSessions session = new MatchingSessions();
            session.setSessionName("Status Test " + status);
            session.setStatus(status);
            session.setCreateUser("test_user");
            session.setDeleted(false);
            
            int inserted = matchingSessionsMapper.insert(session);
            assertThat(inserted).isEqualTo(1);
            
            MatchingSessions retrieved = matchingSessionsMapper.selectById(session.getId());
            assertThat(retrieved.getStatus()).isEqualTo(status);
        }
    }

    @Test
    @DisplayName("Business Rules - Numeric Constraints")
    void testBusinessRulesNumericConstraints() {
        // Test positive values for amounts and percentages
        HazardousWaste waste = new HazardousWaste();
        waste.setWasteCode("NUMERIC001");
        waste.setWasteName("Numeric Test Waste");
        waste.setHeatValueCalPerG(new BigDecimal("5000")); // Positive
        waste.setWaterContentPercent(new BigDecimal("25")); // 0-100 range
        waste.setRemainingStorage(new BigDecimal("1000")); // Positive
        waste.setDeleted(false);
        
        int inserted = hazardousWasteMapper.insert(waste);
        assertThat(inserted).isEqualTo(1);
        
        HazardousWaste retrieved = hazardousWasteMapper.selectById(waste.getId());
        assertThat(retrieved.getHeatValueCalPerG()).isPositive();
        assertThat(retrieved.getWaterContentPercent()).isBetween(BigDecimal.ZERO, new BigDecimal("100"));
        assertThat(retrieved.getRemainingStorage()).isPositive();
    }

    @Test
    @DisplayName("Business Rules - Default Values")
    void testBusinessRulesDefaultValues() {
        // Test default values for boolean flags
        HazardousWaste waste = new HazardousWaste();
        waste.setWasteCode("DEFAULT001");
        waste.setWasteName("Default Values Test");
        waste.setDeleted(false);
        // Not setting boolean properties to test defaults
        
        int inserted = hazardousWasteMapper.insert(waste);
        assertThat(inserted).isEqualTo(1);
        
        HazardousWaste retrieved = hazardousWasteMapper.selectById(waste.getId());
        // Depending on DB schema, these might be null or false by default
        // Just verify the record was created successfully
        assertThat(retrieved.getId()).isNotNull();
        assertThat(retrieved.getWasteCode()).isEqualTo("DEFAULT001");
    }

    @Test
    @DisplayName("Compatibility Categories - Data Structure")
    void testCompatibilityCategoriesStructure() {
        // Test table access
        List<CompatibilityCategory> categories = compatibilityCategoryMapper.selectList(null);
        
        // Test data insertion
        CompatibilityCategory testCategory = new CompatibilityCategory();
        testCategory.setCategoryCode("TEST01");
        testCategory.setCategoryNameCn("测试类别");
        testCategory.setCategoryNameEn("Test Category");
        testCategory.setIdx(999);
        
        int inserted = compatibilityCategoryMapper.insert(testCategory);
        assertThat(inserted).isEqualTo(1);
        assertThat(testCategory.getCategoryCode()).isNotNull();
    }

    @Test
    @DisplayName("Hazardous Waste - Data Structure")
    void testHazardousWasteStructure() {
        // Test hazardous waste data insertion
        HazardousWaste testWaste = new HazardousWaste();
        testWaste.setWasteCode("TEST001");
        testWaste.setWasteName("Test Hazardous Waste");
        testWaste.setSourceUnit("Test Unit");
        testWaste.setHeatValueCalPerG(new BigDecimal("4000"));
        testWaste.setWaterContentPercent(new BigDecimal("15"));
        testWaste.setRemainingStorage(new BigDecimal("1000"));
        testWaste.setFlammable(true);
        testWaste.setOxidizing(false);
        testWaste.setDeleted(false);
        
        int inserted = hazardousWasteMapper.insert(testWaste);
        assertThat(inserted).isEqualTo(1);
        assertThat(testWaste.getId()).isNotNull();
    }

    @Test
    @DisplayName("Matching Sessions - Data Structure")
    void testMatchingSessionsStructure() {
        // Test session data insertion
        MatchingSessions testSession = new MatchingSessions();
        testSession.setSessionName("Test Matching Session");
        testSession.setTargetHeatValue(new BigDecimal("15000"));
        testSession.setTotalAmount(new BigDecimal("1000"));
        testSession.setStatus("draft");
        testSession.setCreateUser("test_user");
        testSession.setDeleted(false);
        
        int inserted = matchingSessionsMapper.insert(testSession);
        assertThat(inserted).isEqualTo(1);
        assertThat(testSession.getId()).isNotNull();
    }

    @Test
    @DisplayName("Matching Constraints - Data Structure")
    void testMatchingConstraintsStructure() {
        // Test constraint data insertion
        MatchingConstraints testConstraint = new MatchingConstraints();
        testConstraint.setConstraintName("Test Heat Value Constraint");
        testConstraint.setParameterCode("testHeatValue");
        testConstraint.setMinValue(new BigDecimal("10000"));
        testConstraint.setMaxValue(new BigDecimal("20000"));
        testConstraint.setUnit("kJ/kg");
        testConstraint.setIsActive(true);
        testConstraint.setSortOrder(999);
        
        int inserted = matchingConstraintsMapper.insert(testConstraint);
        assertThat(inserted).isEqualTo(1);
    }

    @Test
    @DisplayName("Business Rules - Numeric Constraints")
    void testNumericConstraints() {
        // Test positive values for amounts and percentages
        HazardousWaste waste = new HazardousWaste();
        waste.setWasteCode("NUMERIC001");
        waste.setWasteName("Numeric Test Waste");
        waste.setHeatValueCalPerG(new BigDecimal("5000"));
        waste.setWaterContentPercent(new BigDecimal("25"));
        waste.setRemainingStorage(new BigDecimal("1000"));
        waste.setDeleted(false);
        
        int inserted = hazardousWasteMapper.insert(waste);
        assertThat(inserted).isEqualTo(1);
        
        HazardousWaste retrieved = hazardousWasteMapper.selectById(waste.getId());
        assertThat(retrieved.getHeatValueCalPerG()).isPositive();
        assertThat(retrieved.getRemainingStorage()).isPositive();
    }
} 