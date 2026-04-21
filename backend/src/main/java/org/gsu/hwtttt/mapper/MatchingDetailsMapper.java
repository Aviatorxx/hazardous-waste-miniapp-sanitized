package org.gsu.hwtttt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.gsu.hwtttt.entity.MatchingDetails;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 配伍详情表Mapper接口
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Mapper
public interface MatchingDetailsMapper extends BaseMapper<MatchingDetails> {

    /**
     * 根据会话ID查询配伍详情
     *
     * @param sessionId 会话ID
     * @return 配伍详情列表
     */
    @Select("SELECT * FROM matching_details WHERE session_id = #{sessionId} ORDER BY create_time DESC")
    List<MatchingDetails> selectBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 根据危废ID查询配伍详情
     *
     * @param wasteId 危废ID
     * @return 配伍详情列表
     */
    @Select("SELECT * FROM matching_details WHERE waste_id = #{wasteId} ORDER BY create_time DESC")
    List<MatchingDetails> selectByWasteId(@Param("wasteId") Long wasteId);

    /**
     * 根据会话ID和危废ID查询配伍详情
     *
     * @param sessionId 会话ID
     * @param wasteId 危废ID
     * @return 配伍详情
     */
    @Select("SELECT * FROM matching_details WHERE session_id = #{sessionId} AND waste_id = #{wasteId}")
    MatchingDetails selectBySessionIdAndWasteId(@Param("sessionId") Long sessionId, @Param("wasteId") Long wasteId);

    /**
     * 根据会话ID和危废ID删除配伍详情
     *
     * @param sessionId 会话ID
     * @param wasteId 危废ID
     * @return 删除条数
     */
    @Delete("DELETE FROM matching_details WHERE session_id = #{sessionId} AND waste_id = #{wasteId}")
    int deleteBySessionIdAndWasteId(@Param("sessionId") Long sessionId, @Param("wasteId") Long wasteId);

    /**
     * 根据会话ID和危废ID更新实际用量
     *
     * @param sessionId 会话ID
     * @param wasteId 危废ID
     * @param actualAmount 实际用量
     * @return 更新条数
     */
    @Update("UPDATE matching_details SET actual_amount = #{actualAmount} WHERE session_id = #{sessionId} AND waste_id = #{wasteId}")
    int updateActualAmount(@Param("sessionId") Long sessionId, @Param("wasteId") Long wasteId, @Param("actualAmount") BigDecimal actualAmount);

    /**
     * 统计会话的配伍详情
     *
     * @param sessionId 会话ID
     * @return 统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "SUM(planned_amount) as total_planned_amount, " +
            "SUM(actual_amount) as total_actual_amount, " +
            "AVG(percentage) as avg_percentage " +
            "FROM matching_details WHERE session_id = #{sessionId}")
    DetailStatistics getDetailStatistics(@Param("sessionId") Long sessionId);

    /**
     * 更新实际分配量 (by ID)
     *
     * @param id 详情ID
     * @param actualAmount 实际分配量
     * @param percentage 占比
     * @return 更新条数
     */
    @Update("UPDATE matching_details SET actual_amount = #{actualAmount}, percentage = #{percentage} WHERE id = #{id}")
    int updateActualAmountById(@Param("id") Long id, @Param("actualAmount") BigDecimal actualAmount, @Param("percentage") BigDecimal percentage);

    /**
     * 批量更新实际分配量
     *
     * @param details 配伍详情列表
     * @return 更新条数
     */
    @Update("<script>" +
            "<foreach collection='details' item='detail' separator=';'>" +
            "UPDATE matching_details SET actual_amount = #{detail.actualAmount}, percentage = #{detail.percentage} WHERE id = #{detail.id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateActualAmount(@Param("details") List<MatchingDetails> details);

    /**
     * 根据会话ID删除配伍详情
     *
     * @param sessionId 会话ID
     * @return 删除条数
     */
    @Delete("DELETE FROM matching_details WHERE session_id = #{sessionId}")
    int deleteBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 检查危废是否在会话中
     *
     * @param wasteId 危废ID
     * @param sessionId 会话ID
     * @return 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(1) FROM matching_details WHERE waste_id = #{wasteId} AND session_id = #{sessionId}")
    int existsByWasteIdAndSessionId(@Param("wasteId") Long wasteId, @Param("sessionId") Long sessionId);

    /**
     * 获取会话中的危废ID列表
     *
     * @param sessionId 会话ID
     * @return 危废ID列表
     */
    @Select("SELECT DISTINCT waste_id FROM matching_details WHERE session_id = #{sessionId}")
    List<Long> getWasteIdsBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 配伍详情统计信息内部类
     */
    @Data
    class DetailStatistics {
        private Integer totalCount;
        private BigDecimal totalPlannedAmount;
        private BigDecimal totalActualAmount;
        private BigDecimal avgPercentage;
    }
} 