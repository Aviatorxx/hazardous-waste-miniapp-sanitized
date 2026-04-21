package org.gsu.hwtttt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.gsu.hwtttt.entity.MatchingSessions;
import org.gsu.hwtttt.mapper.MatchingSessionsMapper;
import org.gsu.hwtttt.mapper.MatchingSessionsMapper.SessionStatistics;
import org.gsu.hwtttt.service.MatchingSessionsService;
import org.gsu.hwtttt.service.MatchingSessionStateService;
import org.gsu.hwtttt.constant.SystemConstants;
import org.gsu.hwtttt.common.exception.BlendingException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 配伍会话Service实现类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingSessionsServiceImpl implements MatchingSessionsService {

    private final MatchingSessionsMapper matchingSessionsMapper;
    private final MatchingSessionStateService stateService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MatchingSessions createSession(MatchingSessions session) {
        log.info("创建配伍会话: {}", session);
        
        // 参数验证
        if (!validateSessionParameters(session)) {
            throw BlendingException.invalidInput("会话参数验证失败");
        }
        
        // 设置默认值
        if (session.getStatus() == null) {
            session.setStatus(SystemConstants.MatchingStatus.DRAFT);
        }
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        
        int result = matchingSessionsMapper.insert(session);
        if (result <= 0) {
            throw BlendingException.algorithmError("创建配伍会话失败", null);
        }
        
        log.info("配伍会话创建成功, ID: {}", session.getId());
        return session;
    }

    @Override
    public boolean updateSessionStatus(Long sessionId, String status) {
        if (sessionId == null || !StringUtils.hasText(status)) {
            throw BlendingException.invalidInput("会话ID和状态不能为空");
        }
        
        int result = matchingSessionsMapper.updateStatus(sessionId, status);
        return result > 0;
    }

    @Override
    public boolean updateSessionStatusWithHistory(Long sessionId, String status, String changeReason, String changeUser) {
        return stateService.changeSessionStatus(sessionId, status, changeReason, changeUser);
    }

    @Override
    public boolean completeMatching(Long sessionId, Boolean success, String resultDescription) {
        if (sessionId == null || success == null) {
            throw BlendingException.invalidInput("参数不能为空");
        }
        
        int result = matchingSessionsMapper.updateMatchingResult(sessionId, success, resultDescription);
        return result > 0;
    }

    @Override
    public MatchingSessions getSessionById(Long sessionId) {
        if (sessionId == null) {
            throw BlendingException.invalidInput("会话ID不能为空");
        }
        
        return matchingSessionsMapper.selectById(sessionId);
    }

    @Override
    public Page<MatchingSessions> getSessionsByUserId(String userId, long current, long size) {
        Page<MatchingSessions> page = new Page<>(current, size);
        return matchingSessionsMapper.selectByUserId(page, userId);
    }

    @Override
    public List<MatchingSessions> getSessionsByStatus(String status) {
        return matchingSessionsMapper.selectByStatus(status);
    }

    @Override
    @Transactional
    public boolean deleteSession(Long sessionId) {
        try {
            // 使用软删除而不是物理删除
            return matchingSessionsMapper.softDeleteById(sessionId) > 0;
        } catch (Exception e) {
            log.error("删除配伍会话失败: {}", e.getMessage(), e);
            return false;
        }
    }



    @Override
    public SessionStatistics getUserSessionStatistics(String userId) {
        return matchingSessionsMapper.getUserSessionStatistics(userId);
    }

    @Override
    public int cleanExpiredSessions(int expireDays) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(expireDays);
        return matchingSessionsMapper.deleteExpiredSessions(expireTime);
    }

    @Override
    public Long getActiveSessionCount() {
        return matchingSessionsMapper.getActiveSessionCount();
    }

    @Override
    public boolean validateSessionParameters(MatchingSessions session) {
        if (session == null || !StringUtils.hasText(session.getCreateUser()) || 
            !StringUtils.hasText(session.getSessionName())) {
            return false;
        }
        return true;
    }

    @Override
    public Map<String, Object> getSessionHistory(Long sessionId) {
        log.info("获取会话历史记录，会话ID: {}", sessionId);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 获取会话基本信息
            MatchingSessions session = getSessionById(sessionId);
            if (session == null) {
                result.put("success", false);
                result.put("message", "会话不存在");
                return result;
            }
            
            // 2. 获取会话统计信息 (简化处理，直接用会话信息)
            SessionStatistics statistics = null; // 暂时设为null，需要时可以实现具体统计逻辑
            
            // 3. 获取会话状态变更历史（简化处理）
            List<Map<String, Object>> statusHistory = new ArrayList<>();
            Map<String, Object> statusChange = new HashMap<>();
            statusChange.put("status", session.getStatus());
            statusChange.put("changeTime", session.getUpdateTime());
            statusChange.put("description", "会话状态: " + session.getStatus());
            statusHistory.add(statusChange);
            
            // 4. 构建响应
            result.put("success", true);
            result.put("sessionInfo", session);
            result.put("statistics", statistics);
            result.put("statusHistory", statusHistory);
            result.put("queryTime", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("获取会话历史记录失败", e);
            result.put("success", false);
            result.put("message", "获取会话历史失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean updateSession(MatchingSessions session) {
        try {
            session.setUpdateTime(LocalDateTime.now());
            return matchingSessionsMapper.updateById(session) > 0;
        } catch (Exception e) {
            log.error("更新配伍会话失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<MatchingSessions> getSessionHistoryList(String username, Integer pageNum, Integer pageSize) {
        try {
            QueryWrapper<MatchingSessions> wrapper = new QueryWrapper<>();
            
            if (username != null && !username.trim().isEmpty()) {
                wrapper.eq("create_user", username);
            }
            
            wrapper.orderByDesc("create_time");
            
            // 使用分页
            Page<MatchingSessions> page = new Page<>(pageNum, pageSize);
            Page<MatchingSessions> resultPage = matchingSessionsMapper.selectPage(page, wrapper);
            
            return resultPage.getRecords();
        } catch (Exception e) {
            log.error("获取会话历史失败: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public Map<String, Object> getSessionStatistics(Integer days) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);
            
            // 查询时间范围内的会话总数
            QueryWrapper<MatchingSessions> wrapper = new QueryWrapper<>();
            wrapper.ge("create_time", startTime);
            Long totalSessionsCount = matchingSessionsMapper.selectCount(wrapper);
            int totalSessions = totalSessionsCount.intValue();
            
            // 查询活跃会话数（最近有更新的）
            QueryWrapper<MatchingSessions> activeWrapper = new QueryWrapper<>();
            activeWrapper.ge("update_time", startTime);
            Long activeSessionsCount = matchingSessionsMapper.selectCount(activeWrapper);
            int activeSessions = activeSessionsCount.intValue();
            
            // 按用户统计
            List<MatchingSessions> sessions = matchingSessionsMapper.selectList(wrapper);
            Map<String, Integer> userStats = new HashMap<>();
            for (MatchingSessions session : sessions) {
                String user = session.getCreateUser() != null ? session.getCreateUser() : "未知用户";
                userStats.put(user, userStats.getOrDefault(user, 0) + 1);
            }
            
            result.put("period", days + "天");
            result.put("totalSessions", totalSessions);
            result.put("activeSessions", activeSessions);
            result.put("userStatistics", userStats);
            result.put("avgSessionsPerDay", days > 0 ? (double) totalSessions / days : 0);
            
        } catch (Exception e) {
            log.error("获取会话统计失败: {}", e.getMessage(), e);
            result.put("error", "统计失败");
        }
        
        return result;
    }

    @Override
    public boolean validateSessionConstraints(Long sessionId) {
        try {
            MatchingSessions session = matchingSessionsMapper.selectById(sessionId);
            if (session == null) {
                return false;
            }
            
            // 基本验证：会话名称不能为空
            if (session.getSessionName() == null || session.getSessionName().trim().isEmpty()) {
                return false;
            }
            
            // 目标热值验证：如果为空则设置默认值，如果不为空则必须大于0
            if (session.getTargetHeatValue() == null) {
                session.setTargetHeatValue(new BigDecimal("15000")); // 默认目标热值 15000 cal/g
                matchingSessionsMapper.updateById(session);
            } else if (session.getTargetHeatValue().doubleValue() <= 0) {
                return false;
            }
            
            // 总配伍量验证：如果为空则设置默认值，如果不为空则必须大于0
            if (session.getTotalAmount() == null) {
                session.setTotalAmount(new BigDecimal("1000")); // 默认总配伍量 1000 kg
                matchingSessionsMapper.updateById(session);
            } else if (session.getTotalAmount().doubleValue() <= 0) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("验证会话约束失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean updateSession(Long sessionId, org.gsu.hwtttt.dto.request.MatchingRequest request) {
        try {
            MatchingSessions session = matchingSessionsMapper.selectById(sessionId);
            if (session == null) {
                log.warn("Session not found for update: sessionId={}", sessionId);
                return false;
            }
            
            // Update session fields from request
            if (request.getSessionName() != null) {
                session.setSessionName(request.getSessionName());
            }
            if (request.getTargetHeatValue() != null) {
                session.setTargetHeatValue(request.getTargetHeatValue());
            }
            if (request.getTotalAmount() != null) {
                session.setTotalAmount(request.getTotalAmount());
            }
            
            // Update the session
            int updated = matchingSessionsMapper.updateById(session);
            
            log.info("Updated session: sessionId={}, success={}", sessionId, updated > 0);
            return updated > 0;
            
        } catch (Exception e) {
            log.error("Failed to update session: sessionId={}", sessionId, e);
            return false;
        }
    }
} 