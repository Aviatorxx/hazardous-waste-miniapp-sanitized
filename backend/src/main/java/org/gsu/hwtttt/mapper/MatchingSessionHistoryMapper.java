package org.gsu.hwtttt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.gsu.hwtttt.entity.MatchingSessionHistory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 配伍会话状态历史表Mapper接口
 *
 * @author WenXin
 * @date 2025/01/07
 */
@Mapper
public interface MatchingSessionHistoryMapper extends BaseMapper<MatchingSessionHistory> {

    /**
     * 根据会话ID查询状态历史
     *
     * @param sessionId 会话ID
     * @return 状态历史列表
     */
    @Select("SELECT * FROM matching_session_history WHERE session_id = #{sessionId} ORDER BY change_time DESC")
    List<MatchingSessionHistory> selectBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 根据会话ID查询最新的状态变更记录
     *
     * @param sessionId 会话ID
     * @return 最新的状态变更记录
     */
    @Select("SELECT * FROM matching_session_history WHERE session_id = #{sessionId} ORDER BY change_time DESC LIMIT 1")
    MatchingSessionHistory selectLatestBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 根据状态查询历史记录
     *
     * @param status 状态
     * @return 历史记录列表
     */
    @Select("SELECT * FROM matching_session_history WHERE to_status = #{status} ORDER BY change_time DESC")
    List<MatchingSessionHistory> selectByToStatus(@Param("status") String status);

    /**
     * 根据用户查询状态变更历史
     *
     * @param changeUser 变更用户
     * @return 历史记录列表
     */
    @Select("SELECT * FROM matching_session_history WHERE change_user = #{changeUser} ORDER BY change_time DESC")
    List<MatchingSessionHistory> selectByChangeUser(@Param("changeUser") String changeUser);

    /**
     * 根据时间范围查询状态变更历史
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 历史记录列表
     */
    @Select("SELECT * FROM matching_session_history WHERE change_time BETWEEN #{startTime} AND #{endTime} ORDER BY change_time DESC")
    List<MatchingSessionHistory> selectByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定会话的状态变更次数
     *
     * @param sessionId 会话ID
     * @return 变更次数
     */
    @Select("SELECT COUNT(*) FROM matching_session_history WHERE session_id = #{sessionId}")
    Integer countBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 查询状态转换统计
     *
     * @param fromStatus 起始状态
     * @param toStatus 目标状态
     * @return 转换次数
     */
    @Select("SELECT COUNT(*) FROM matching_session_history WHERE from_status = #{fromStatus} AND to_status = #{toStatus}")
    Integer countStatusTransition(@Param("fromStatus") String fromStatus, @Param("toStatus") String toStatus);

    /**
     * 删除指定会话的所有历史记录
     *
     * @param sessionId 会话ID
     * @return 删除的记录数
     */
    @Select("DELETE FROM matching_session_history WHERE session_id = #{sessionId}")
    int deleteBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 删除过期的历史记录
     *
     * @param expireTime 过期时间
     * @return 删除的记录数
     */
    @Select("DELETE FROM matching_session_history WHERE change_time < #{expireTime}")
    int deleteExpiredHistory(@Param("expireTime") LocalDateTime expireTime);
} 