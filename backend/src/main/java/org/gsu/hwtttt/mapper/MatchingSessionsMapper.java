package org.gsu.hwtttt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.gsu.hwtttt.entity.MatchingSessions;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 配伍会话表Mapper接口
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Mapper
public interface MatchingSessionsMapper extends BaseMapper<MatchingSessions> {

    /**
     * 根据创建人查询会话
     *
     * @param createUser 创建人
     * @return 会话列表
     */
    @Select("SELECT * FROM matching_sessions WHERE create_user = #{createUser} AND deleted = 0 ORDER BY create_time DESC")
    List<MatchingSessions> selectByCreateUser(@Param("createUser") String createUser);

    /**
     * 根据状态查询会话
     *
     * @param status 状态
     * @return 会话列表
     */
    @Select("SELECT * FROM matching_sessions WHERE status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<MatchingSessions> selectByStatus(@Param("status") String status);

    /**
     * 根据会话名称模糊查询
     *
     * @param sessionName 会话名称关键字
     * @return 会话列表
     */
    @Select("SELECT * FROM matching_sessions WHERE session_name LIKE CONCAT('%', #{sessionName}, '%') AND deleted = 0 ORDER BY create_time DESC")
    List<MatchingSessions> selectBySessionNameLike(@Param("sessionName") String sessionName);

    /**
     * 查询最近的会话
     *
     * @param limit 限制数量
     * @return 会话列表
     */
    @Select("SELECT * FROM matching_sessions WHERE deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<MatchingSessions> selectRecent(@Param("limit") Integer limit);

    /**
     * 查询用户的最近会话
     *
     * @param createUser 创建人
     * @param limit 限制数量
     * @return 会话列表
     */
    @Select("SELECT * FROM matching_sessions WHERE create_user = #{createUser} AND deleted = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<MatchingSessions> selectRecentByUser(@Param("createUser") String createUser, @Param("limit") Integer limit);

    /**
     * 统计各状态的会话数量
     *
     * @return 统计结果
     */
    @Select("SELECT status, COUNT(*) as count FROM matching_sessions WHERE deleted = 0 GROUP BY status")
    List<SessionStatistics> getSessionStatistics();

    /**
     * 统计用户的会话数量
     *
     * @param createUser 创建人
     * @return 会话数量
     */
    @Select("SELECT COUNT(*) FROM matching_sessions WHERE create_user = #{createUser} AND deleted = 0")
    int countByCreateUser(@Param("createUser") String createUser);

    /**
     * 检查会话名称是否存在
     *
     * @param sessionName 会话名称
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 存在返回1，不存在返回0
     */
    @Select("<script>" +
            "SELECT COUNT(1) FROM matching_sessions WHERE session_name = #{sessionName} AND deleted = 0 " +
            "<if test='excludeId != null'> AND id != #{excludeId} </if>" +
            "</script>")
    int existsBySessionName(@Param("sessionName") String sessionName, @Param("excludeId") Long excludeId);

    /**
     * 根据用户ID分页查询配伍会话
     *
     * @param page 分页对象
     * @param createUser 创建用户
     * @return 配伍会话分页列表
     */
    Page<MatchingSessions> selectByUserId(Page<MatchingSessions> page, @Param("createUser") String createUser);

    /**
     * 更新会话状态
     *
     * @param sessionId 会话ID
     * @param status 新状态
     * @return 更新条数
     */
    @Update("UPDATE matching_sessions SET status = #{status}, update_time = NOW() WHERE id = #{sessionId} AND deleted = 0")
    int updateStatus(@Param("sessionId") Long sessionId, @Param("status") String status);

    /**
     * 更新配伍结果
     *
     * @param sessionId 会话ID
     * @param success 是否成功
     * @param resultDescription 结果描述
     * @return 更新条数
     */
    @Update("UPDATE matching_sessions SET is_successful = #{success}, result_description = #{resultDescription}, " +
            "completed_time = NOW(), update_time = NOW() WHERE id = #{sessionId} AND deleted = 0")
    int updateMatchingResult(@Param("sessionId") Long sessionId, 
                           @Param("success") Boolean success, 
                           @Param("resultDescription") String resultDescription);

    /**
     * 根据目标热值范围查询会话
     *
     * @param minHeatValue 最小热值
     * @param maxHeatValue 最大热值
     * @return 配伍会话列表
     */
    List<MatchingSessions> selectByHeatValueRange(@Param("minHeatValue") BigDecimal minHeatValue,
                                                @Param("maxHeatValue") BigDecimal maxHeatValue);

    /**
     * 获取用户的会话统计信息
     *
     * @param createUser 创建用户
     * @return 统计信息
     */
    SessionStatistics getUserSessionStatistics(@Param("createUser") String createUser);

    /**
     * 删除过期的会话（软删除）
     *
     * @param expireTime 过期时间
     * @return 删除条数
     */
    @Update("UPDATE matching_sessions SET deleted = 1, update_time = NOW() WHERE create_time < #{expireTime} AND deleted = 0")
    int deleteExpiredSessions(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 获取活跃会话数量
     *
     * @return 活跃会话数量
     */
    @Select("SELECT COUNT(*) FROM matching_sessions WHERE status IN ('draft', 'calculating') AND deleted = 0")
    Long getActiveSessionCount();

    /**
     * 软删除会话
     *
     * @param sessionId 会话ID
     * @return 删除条数
     */
    @Update("UPDATE matching_sessions SET deleted = 1, update_time = NOW() WHERE id = #{sessionId} AND deleted = 0")
    int softDeleteById(@Param("sessionId") Long sessionId);

    /**
     * 会话统计信息内部类
     */
    @Data
    class SessionStatistics {
        private String status;
        private Integer count;
    }
} 