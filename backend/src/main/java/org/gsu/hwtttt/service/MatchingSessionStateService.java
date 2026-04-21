package org.gsu.hwtttt.service;

import org.gsu.hwtttt.entity.MatchingSessionHistory;

import java.util.List;
import java.util.Set;

/**
 * 配伍会话状态管理Service接口
 * 负责状态转换、历史记录和状态验证
 *
 * @author WenXin
 * @date 2025/01/07
 */
public interface MatchingSessionStateService {

    /**
     * 更改会话状态（带历史记录）
     *
     * @param sessionId 会话ID
     * @param newStatus 新状态
     * @param changeReason 变更原因
     * @param changeUser 操作用户
     * @return 是否成功
     */
    boolean changeSessionStatus(Long sessionId, String newStatus, String changeReason, String changeUser);

    /**
     * 批量更改会话状态
     *
     * @param sessionIds 会话ID列表
     * @param newStatus 新状态
     * @param changeReason 变更原因
     * @param changeUser 操作用户
     * @return 成功更改的数量
     */
    int batchChangeSessionStatus(List<Long> sessionIds, String newStatus, String changeReason, String changeUser);

    /**
     * 验证状态转换是否有效
     *
     * @param fromStatus 当前状态
     * @param toStatus 目标状态
     * @return 是否有效
     */
    boolean isValidStateTransition(String fromStatus, String toStatus);

    /**
     * 获取指定状态可以转换到的状态列表
     *
     * @param currentStatus 当前状态
     * @return 可转换状态列表
     */
    Set<String> getValidTransitions(String currentStatus);

    /**
     * 获取会话状态变更历史
     *
     * @param sessionId 会话ID
     * @return 状态变更历史列表
     */
    List<MatchingSessionHistory> getSessionStateHistory(Long sessionId);

    /**
     * 获取最新的状态变更记录
     *
     * @param sessionId 会话ID
     * @return 最新状态变更记录
     */
    MatchingSessionHistory getLatestStateChange(Long sessionId);

    /**
     * 回滚到上一个状态
     *
     * @param sessionId 会话ID
     * @param changeReason 回滚原因
     * @param changeUser 操作用户
     * @return 是否成功
     */
    boolean rollbackToPreviousState(Long sessionId, String changeReason, String changeUser);

    /**
     * 强制设置状态（跳过验证，用于异常恢复）
     *
     * @param sessionId 会话ID
     * @param status 状态
     * @param changeReason 变更原因
     * @param changeUser 操作用户
     * @return 是否成功
     */
    boolean forceSetStatus(Long sessionId, String status, String changeReason, String changeUser);

    /**
     * 获取状态转换统计
     *
     * @param fromStatus 起始状态
     * @param toStatus 目标状态
     * @return 转换次数
     */
    Integer getStateTransitionCount(String fromStatus, String toStatus);

    /**
     * 清理会话的状态历史记录
     *
     * @param sessionId 会话ID
     * @param keepCount 保留的记录数量
     * @return 删除的记录数
     */
    int cleanSessionStateHistory(Long sessionId, int keepCount);

    /**
     * 验证状态值是否有效
     *
     * @param status 状态值
     * @return 是否有效
     */
    boolean isValidStatus(String status);

    /**
     * 获取所有有效状态
     *
     * @return 有效状态集合
     */
    Set<String> getAllValidStatuses();
} 