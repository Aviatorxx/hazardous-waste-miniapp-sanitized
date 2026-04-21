package org.gsu.hwtttt.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.gsu.hwtttt.constant.SystemConstants;
import org.gsu.hwtttt.entity.MatchingSessions;
import org.gsu.hwtttt.entity.MatchingSessionHistory;
import org.gsu.hwtttt.mapper.MatchingSessionsMapper;
import org.gsu.hwtttt.mapper.MatchingSessionHistoryMapper;
import org.gsu.hwtttt.service.impl.MatchingSessionStateServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 配伍会话状态管理Service测试类
 *
 * @author WenXin
 * @date 2025/01/07
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Matching Session State Service Tests")
class MatchingSessionStateServiceTest {

    @Mock
    private MatchingSessionsMapper matchingSessionsMapper;

    @Mock
    private MatchingSessionHistoryMapper historyMapper;

    @InjectMocks
    private MatchingSessionStateServiceImpl stateService;

    private MatchingSessions testSession;

    @BeforeEach
    void setUp() {
        testSession = new MatchingSessions();
        testSession.setId(1L);
        testSession.setSessionName("Test Session");
        testSession.setStatus(SystemConstants.MatchingStatus.DRAFT);
        testSession.setCreateUser("test_user");
        testSession.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("Valid Status Transition - Draft to Waste Selected")
    void testValidStatusTransition() {
        // Given
        String newStatus = SystemConstants.MatchingStatus.WASTE_SELECTED;
        when(matchingSessionsMapper.selectById(1L)).thenReturn(testSession);
        when(matchingSessionsMapper.updateStatus(1L, newStatus)).thenReturn(1);
        when(historyMapper.insert(any(MatchingSessionHistory.class))).thenReturn(1);

        // When
        boolean result = stateService.changeSessionStatus(1L, newStatus, "Selected waste for matching", "test_user");

        // Then
        assertThat(result).isTrue();
        verify(matchingSessionsMapper).updateStatus(1L, newStatus);
        verify(historyMapper).insert(any(MatchingSessionHistory.class));
    }

    @Test
    @DisplayName("Invalid Status Transition - Draft to Calculating")
    void testInvalidStatusTransition() {
        // Given
        String newStatus = SystemConstants.MatchingStatus.CALCULATING;
        when(matchingSessionsMapper.selectById(1L)).thenReturn(testSession);

        // When & Then
        assertThatThrownBy(() -> stateService.changeSessionStatus(1L, newStatus, "Invalid transition", "test_user"))
            .hasMessageContaining("无效的状态转换");
    }

    @Test
    @DisplayName("Get Valid Transitions for Draft Status")
    void testGetValidTransitions() {
        // When
        Set<String> validTransitions = stateService.getValidTransitions(SystemConstants.MatchingStatus.DRAFT);

        // Then
        assertThat(validTransitions).contains(
            SystemConstants.MatchingStatus.WASTE_SELECTED,
            SystemConstants.MatchingStatus.ARCHIVED
        );
        assertThat(validTransitions).doesNotContain(
            SystemConstants.MatchingStatus.CALCULATING
        );
    }

    @Test
    @DisplayName("Rollback to Previous State")
    void testRollbackToPreviousState() {
        // Given
        MatchingSessionHistory history = new MatchingSessionHistory();
        history.setFromStatus(SystemConstants.MatchingStatus.DRAFT);
        history.setToStatus(SystemConstants.MatchingStatus.WASTE_SELECTED);
        
        when(historyMapper.selectBySessionId(1L)).thenReturn(List.of(history));
        when(matchingSessionsMapper.selectById(1L)).thenReturn(testSession);
        when(matchingSessionsMapper.updateStatus(eq(1L), eq(SystemConstants.MatchingStatus.DRAFT))).thenReturn(1);
        when(historyMapper.insert(any(MatchingSessionHistory.class))).thenReturn(1);

        // When
        boolean result = stateService.rollbackToPreviousState(1L, "Rollback test", "test_user");

        // Then
        assertThat(result).isTrue();
        verify(matchingSessionsMapper).updateStatus(1L, SystemConstants.MatchingStatus.DRAFT);
    }

    @Test
    @DisplayName("Force Set Status - Bypasses Validation")
    void testForceSetStatus() {
        // Given
        String targetStatus = SystemConstants.MatchingStatus.CALCULATING;
        when(matchingSessionsMapper.selectById(1L)).thenReturn(testSession);
        when(matchingSessionsMapper.updateStatus(1L, targetStatus)).thenReturn(1);
        when(historyMapper.insert(any(MatchingSessionHistory.class))).thenReturn(1);

        // When
        boolean result = stateService.forceSetStatus(1L, targetStatus, "Emergency fix", "admin_user");

        // Then
        assertThat(result).isTrue();
        verify(matchingSessionsMapper).updateStatus(1L, targetStatus);
        verify(historyMapper).insert(any(MatchingSessionHistory.class));
    }

    @Test
    @DisplayName("Get Session State History")
    void testGetSessionStateHistory() {
        // Given
        List<MatchingSessionHistory> expectedHistory = List.of(
            new MatchingSessionHistory(1L, SystemConstants.MatchingStatus.DRAFT, 
                SystemConstants.MatchingStatus.WASTE_SELECTED, "Test", "user1"),
            new MatchingSessionHistory(1L, SystemConstants.MatchingStatus.WASTE_SELECTED, 
                SystemConstants.MatchingStatus.COMPATIBILITY_CHECKING, "Test", "user2")
        );
        when(historyMapper.selectBySessionId(1L)).thenReturn(expectedHistory);

        // When
        List<MatchingSessionHistory> result = stateService.getSessionStateHistory(1L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getToStatus()).isEqualTo(SystemConstants.MatchingStatus.WASTE_SELECTED);
        assertThat(result.get(1).getToStatus()).isEqualTo(SystemConstants.MatchingStatus.COMPATIBILITY_CHECKING);
    }

    @Test
    @DisplayName("Validate Status Value")
    void testIsValidStatus() {
        // Valid statuses
        assertThat(stateService.isValidStatus(SystemConstants.MatchingStatus.DRAFT)).isTrue();
        assertThat(stateService.isValidStatus(SystemConstants.MatchingStatus.CALCULATING)).isTrue();
        assertThat(stateService.isValidStatus(SystemConstants.MatchingStatus.ARCHIVED)).isTrue();

        // Invalid statuses
        assertThat(stateService.isValidStatus("invalid_status")).isFalse();
        assertThat(stateService.isValidStatus(null)).isFalse();
        assertThat(stateService.isValidStatus("")).isFalse();
    }

    @Test
    @DisplayName("Get All Valid Statuses")
    void testGetAllValidStatuses() {
        // When
        Set<String> allStatuses = stateService.getAllValidStatuses();

        // Then
        assertThat(allStatuses).contains(
            SystemConstants.MatchingStatus.DRAFT,
            SystemConstants.MatchingStatus.WASTE_SELECTED,
            SystemConstants.MatchingStatus.COMPATIBILITY_CHECKING,
            SystemConstants.MatchingStatus.COMPATIBLE,
            SystemConstants.MatchingStatus.INCOMPATIBLE,
            SystemConstants.MatchingStatus.CALCULATING,
            SystemConstants.MatchingStatus.CALCULATION_SUCCESS,
            SystemConstants.MatchingStatus.CALCULATION_FAILED,
            SystemConstants.MatchingStatus.ARCHIVED
        );
    }
} 