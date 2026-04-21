package org.gsu.hwtttt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.gsu.hwtttt.entity.MatchingSessions;
import org.gsu.hwtttt.mapper.MatchingSessionsMapper.SessionStatistics;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Module 4: 配伍会话Service接口
 * 支持会话管理和历史查询
 *
 * @author WenXin
 * @date 2025/06/10
 */
public interface MatchingSessionsService {

    /**
     * 创建配伍会话
     *
     * @param session 会话信息
     * @return 创建的会话
     */
    MatchingSessions createSession(MatchingSessions session);

    /**
     * 更新会话状态
     *
     * @param sessionId 会话ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateSessionStatus(Long sessionId, String status);

    /**
     * 更新会话状态（带历史记录）
     *
     * @param sessionId 会话ID
     * @param status 新状态
     * @param changeReason 变更原因
     * @param changeUser 操作用户
     * @return 是否成功
     */
    boolean updateSessionStatusWithHistory(Long sessionId, String status, String changeReason, String changeUser);

    /**
     * 完成配伍计算
     *
     * @param sessionId 会话ID
     * @param success 是否成功
     * @param resultDescription 结果描述
     * @return 是否成功
     */
    boolean completeMatching(Long sessionId, Boolean success, String resultDescription);

    /**
     * 根据ID获取会话
     *
     * @param sessionId 会话ID
     * @return 会话信息
     */
    MatchingSessions getSessionById(Long sessionId);

    /**
     * 根据用户ID分页查询会话
     *
     * @param userId 用户ID
     * @param current 当前页
     * @param size 每页大小
     * @return 会话分页列表
     */
    Page<MatchingSessions> getSessionsByUserId(String userId, long current, long size);

    /**
     * 根据状态查询会话
     *
     * @param status 会话状态
     * @return 会话列表
     */
    List<MatchingSessions> getSessionsByStatus(String status);

    /**
     * 删除会话
     *
     * @param sessionId 会话ID
     * @return 是否成功
     */
    boolean deleteSession(Long sessionId);

    /**
     * 获取用户会话统计
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    SessionStatistics getUserSessionStatistics(String userId);

    /**
     * 清理过期会话
     *
     * @param expireDays 过期天数
     * @return 清理数量
     */
    int cleanExpiredSessions(int expireDays);

    /**
     * 获取活跃会话数量
     *
     * @return 活跃会话数量
     */
    Long getActiveSessionCount();

    /**
     * 验证会话参数
     *
     * @param session 会话信息
     * @return 验证结果
     */
    boolean validateSessionParameters(MatchingSessions session);

    // ==================== Module 4 新增方法 ====================

    /**
     * 获取会话历史记录
     *
     * @param sessionId 会话ID
     * @return 会话历史详情和统计信息
     */
    Map<String, Object> getSessionHistory(Long sessionId);

    /**
     * 更新配伍会话
     *
     * @param session 会话信息
     * @return 是否成功
     */
    boolean updateSession(MatchingSessions session);

    /**
     * 获取会话历史列表
     *
     * @param username 用户名（可选）
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 会话列表
     */
    List<MatchingSessions> getSessionHistoryList(String username, Integer pageNum, Integer pageSize);

    /**
     * 获取会话统计信息
     *
     * @param days 统计天数
     * @return 统计信息
     */
    Map<String, Object> getSessionStatistics(Integer days);

    /**
     * 验证会话约束
     *
     * @param sessionId 会话ID
     * @return 是否通过验证
     */
    boolean validateSessionConstraints(Long sessionId);

    /**
     * 更新会话信息
     *
     * @param sessionId 会话ID
     * @param request 更新请求
     * @return 是否成功
     */
    boolean updateSession(Long sessionId, org.gsu.hwtttt.dto.request.MatchingRequest request);
} 