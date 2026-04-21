package org.gsu.hwtttt.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.gsu.hwtttt.constant.SystemConstants;
import org.gsu.hwtttt.entity.MatchingSessions;
import org.gsu.hwtttt.entity.MatchingSessionHistory;
import org.gsu.hwtttt.mapper.MatchingSessionsMapper;
import org.gsu.hwtttt.mapper.MatchingSessionHistoryMapper;
import org.gsu.hwtttt.service.MatchingSessionStateService;
import org.gsu.hwtttt.common.exception.BlendingException;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 配伍会话状态管理Service实现类
 * 实现状态机逻辑和历史记录管理
 *
 * @author WenXin
 * @date 2025/01/07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingSessionStateServiceImpl implements MatchingSessionStateService {

    private final MatchingSessionsMapper matchingSessionsMapper;
    private final MatchingSessionHistoryMapper historyMapper;

    /**
     * 状态转换规则定义
     * 定义了每个状态可以转换到哪些状态
     */
    private static final Map<String, Set<String>> STATE_TRANSITION_RULES = new HashMap<>();

    static {
        // 草稿状态可以转换到：废物已选择、已归档
        STATE_TRANSITION_RULES.put(SystemConstants.MatchingStatus.DRAFT,
            Set.of(SystemConstants.MatchingStatus.WASTE_SELECTED, SystemConstants.MatchingStatus.ARCHIVED));

        // 废物已选择可以转换到：相容性检查中、草稿、已归档
        STATE_TRANSITION_RULES.put(SystemConstants.MatchingStatus.WASTE_SELECTED,
            Set.of(SystemConstants.MatchingStatus.COMPATIBILITY_CHECKING, 
                   SystemConstants.MatchingStatus.DRAFT, 
                   SystemConstants.MatchingStatus.ARCHIVED));

        // 相容性检查中可以转换到：相容、不相容、废物已选择
        STATE_TRANSITION_RULES.put(SystemConstants.MatchingStatus.COMPATIBILITY_CHECKING,
            Set.of(SystemConstants.MatchingStatus.COMPATIBLE, 
                   SystemConstants.MatchingStatus.INCOMPATIBLE,
                   SystemConstants.MatchingStatus.WASTE_SELECTED));

        // 相容状态可以转换到：计算中、不相容、废物已选择、已归档、相容性检查中
        STATE_TRANSITION_RULES.put(SystemConstants.MatchingStatus.COMPATIBLE,
            Set.of(SystemConstants.MatchingStatus.CALCULATING, 
                   SystemConstants.MatchingStatus.INCOMPATIBLE,
                   SystemConstants.MatchingStatus.WASTE_SELECTED,
                   SystemConstants.MatchingStatus.ARCHIVED,
                   SystemConstants.MatchingStatus.COMPATIBILITY_CHECKING));

        // 不相容状态可以转换到：废物已选择、相容性检查中、已归档
        STATE_TRANSITION_RULES.put(SystemConstants.MatchingStatus.INCOMPATIBLE,
            Set.of(SystemConstants.MatchingStatus.WASTE_SELECTED, 
                   SystemConstants.MatchingStatus.COMPATIBILITY_CHECKING,
                   SystemConstants.MatchingStatus.ARCHIVED));

        // 计算中可以转换到：计算成功、计算失败、相容
        STATE_TRANSITION_RULES.put(SystemConstants.MatchingStatus.CALCULATING,
            Set.of(SystemConstants.MatchingStatus.CALCULATION_SUCCESS, 
                   SystemConstants.MatchingStatus.CALCULATION_FAILED,
                   SystemConstants.MatchingStatus.COMPATIBLE));

        // 计算成功可以转换到：已归档、计算中
        STATE_TRANSITION_RULES.put(SystemConstants.MatchingStatus.CALCULATION_SUCCESS,
            Set.of(SystemConstants.MatchingStatus.ARCHIVED, 
                   SystemConstants.MatchingStatus.CALCULATING));

        // 计算失败可以转换到：计算中、相容、废物已选择、已归档
        STATE_TRANSITION_RULES.put(SystemConstants.MatchingStatus.CALCULATION_FAILED,
            Set.of(SystemConstants.MatchingStatus.CALCULATING, 
                   SystemConstants.MatchingStatus.COMPATIBLE,
                   SystemConstants.MatchingStatus.WASTE_SELECTED,
                   SystemConstants.MatchingStatus.ARCHIVED));

        // 已归档状态不能转换到其他状态（终态）
        STATE_TRANSITION_RULES.put(SystemConstants.MatchingStatus.ARCHIVED, Set.of());

        // 兼容旧状态的转换规则
        STATE_TRANSITION_RULES.put(SystemConstants.MatchingStatus.COMPLETED,
            Set.of(SystemConstants.MatchingStatus.ARCHIVED));

        STATE_TRANSITION_RULES.put(SystemConstants.MatchingStatus.FAILED,
            Set.of(SystemConstants.MatchingStatus.WASTE_SELECTED, 
                   SystemConstants.MatchingStatus.ARCHIVED));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeSessionStatus(Long sessionId, String newStatus, String changeReason, String changeUser) {
        if (sessionId == null || !StringUtils.hasText(newStatus)) {
            throw BlendingException.invalidInput("会话ID和状态不能为空");
        }

        // 验证新状态是否有效
        if (!isValidStatus(newStatus)) {
            throw BlendingException.invalidInput("无效的状态值: " + newStatus);
        }

        // 获取当前会话信息
        MatchingSessions session = matchingSessionsMapper.selectById(sessionId);
        if (session == null) {
            throw BlendingException.invalidInput("会话不存在: " + sessionId);
        }

        String currentStatus = session.getStatus();

        // 如果状态相同，直接返回成功
        if (newStatus.equals(currentStatus)) {
            log.info("会话状态无变化: sessionId={}, status={}", sessionId, newStatus);
            return true;
        }

        // 验证状态转换是否有效
        if (!isValidStateTransition(currentStatus, newStatus)) {
            throw BlendingException.invalidInput(
                String.format("无效的状态转换: %s -> %s", currentStatus, newStatus));
        }

        try {
            // 更新会话状态
            int updateResult = matchingSessionsMapper.updateStatus(sessionId, newStatus);
            if (updateResult <= 0) {
                throw BlendingException.algorithmError("更新会话状态失败", null);
            }

            // 记录状态变更历史
            MatchingSessionHistory history = new MatchingSessionHistory(
                sessionId, currentStatus, newStatus, changeReason, changeUser);
            
            int historyResult = historyMapper.insert(history);
            if (historyResult <= 0) {
                throw BlendingException.algorithmError("记录状态变更历史失败", null);
            }

            log.info("会话状态变更成功: sessionId={}, {} -> {}, reason={}, user={}", 
                sessionId, currentStatus, newStatus, changeReason, changeUser);
            
            return true;

        } catch (Exception e) {
            log.error("会话状态变更失败: sessionId={}, {} -> {}", sessionId, currentStatus, newStatus, e);
            throw BlendingException.algorithmError("状态变更失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchChangeSessionStatus(List<Long> sessionIds, String newStatus, String changeReason, String changeUser) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Long sessionId : sessionIds) {
            try {
                if (changeSessionStatus(sessionId, newStatus, changeReason, changeUser)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量状态变更失败: sessionId={}", sessionId, e);
            }
        }

        return successCount;
    }

    @Override
    public boolean isValidStateTransition(String fromStatus, String toStatus) {
        if (!StringUtils.hasText(fromStatus) || !StringUtils.hasText(toStatus)) {
            return false;
        }

        Set<String> validTransitions = STATE_TRANSITION_RULES.get(fromStatus);
        return validTransitions != null && validTransitions.contains(toStatus);
    }

    @Override
    public Set<String> getValidTransitions(String currentStatus) {
        if (!StringUtils.hasText(currentStatus)) {
            return Set.of();
        }

        return STATE_TRANSITION_RULES.getOrDefault(currentStatus, Set.of());
    }

    @Override
    public List<MatchingSessionHistory> getSessionStateHistory(Long sessionId) {
        if (sessionId == null) {
            return List.of();
        }

        return historyMapper.selectBySessionId(sessionId);
    }

    @Override
    public MatchingSessionHistory getLatestStateChange(Long sessionId) {
        if (sessionId == null) {
            return null;
        }

        return historyMapper.selectLatestBySessionId(sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackToPreviousState(Long sessionId, String changeReason, String changeUser) {
        List<MatchingSessionHistory> history = getSessionStateHistory(sessionId);
        
        if (history.size() < 1) {
            log.warn("没有可回滚的历史状态: sessionId={}", sessionId);
            return false;
        }

        // 获取倒数第二个状态作为回滚目标
        String targetStatus;
        if (history.size() == 1) {
            // 只有一次变更，回滚到draft状态
            targetStatus = SystemConstants.MatchingStatus.DRAFT;
        } else {
            // 获取倒数第二个状态
            targetStatus = history.get(1).getFromStatus();
        }

        return changeSessionStatus(sessionId, targetStatus, 
            "状态回滚: " + changeReason, changeUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean forceSetStatus(Long sessionId, String status, String changeReason, String changeUser) {
        if (sessionId == null || !StringUtils.hasText(status)) {
            throw BlendingException.invalidInput("会话ID和状态不能为空");
        }

        if (!isValidStatus(status)) {
            throw BlendingException.invalidInput("无效的状态值: " + status);
        }

        // 获取当前状态
        MatchingSessions session = matchingSessionsMapper.selectById(sessionId);
        if (session == null) {
            throw BlendingException.invalidInput("会话不存在: " + sessionId);
        }

        String currentStatus = session.getStatus();

        try {
            // 强制更新状态（跳过验证）
            int updateResult = matchingSessionsMapper.updateStatus(sessionId, status);
            if (updateResult <= 0) {
                throw BlendingException.algorithmError("强制更新状态失败", null);
            }

            // 记录强制变更历史
            MatchingSessionHistory history = new MatchingSessionHistory(
                sessionId, currentStatus, status, "强制设置: " + changeReason, changeUser);
            
            historyMapper.insert(history);

            log.warn("强制设置会话状态: sessionId={}, {} -> {}, reason={}, user={}", 
                sessionId, currentStatus, status, changeReason, changeUser);
            
            return true;

        } catch (Exception e) {
            log.error("强制设置状态失败: sessionId={}", sessionId, e);
            throw BlendingException.algorithmError("强制设置状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Integer getStateTransitionCount(String fromStatus, String toStatus) {
        return historyMapper.countStatusTransition(fromStatus, toStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanSessionStateHistory(Long sessionId, int keepCount) {
        if (sessionId == null || keepCount < 0) {
            return 0;
        }

        List<MatchingSessionHistory> history = getSessionStateHistory(sessionId);
        if (history.size() <= keepCount) {
            return 0;
        }

        // 删除多余的历史记录
        List<Long> idsToDelete = history.stream()
            .skip(keepCount)
            .map(MatchingSessionHistory::getId)
            .collect(java.util.stream.Collectors.toList());

        int deletedCount = 0;
        for (Long id : idsToDelete) {
            deletedCount += historyMapper.deleteById(id);
        }

        log.info("清理会话状态历史: sessionId={}, 删除{}条记录", sessionId, deletedCount);
        return deletedCount;
    }

    @Override
    public boolean isValidStatus(String status) {
        return getAllValidStatuses().contains(status);
    }

    @Override
    public Set<String> getAllValidStatuses() {
        return Set.of(
            SystemConstants.MatchingStatus.DRAFT,
            SystemConstants.MatchingStatus.WASTE_SELECTED,
            SystemConstants.MatchingStatus.COMPATIBILITY_CHECKING,
            SystemConstants.MatchingStatus.COMPATIBLE,
            SystemConstants.MatchingStatus.INCOMPATIBLE,
            SystemConstants.MatchingStatus.CALCULATING,
            SystemConstants.MatchingStatus.CALCULATION_SUCCESS,
            SystemConstants.MatchingStatus.CALCULATION_FAILED,
            SystemConstants.MatchingStatus.ARCHIVED,
            // 兼容旧状态
            SystemConstants.MatchingStatus.COMPLETED,
            SystemConstants.MatchingStatus.FAILED
        );
    }
} 