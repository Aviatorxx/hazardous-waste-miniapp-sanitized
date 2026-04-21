package org.gsu.hwtttt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.gsu.hwtttt.entity.MatchingResults;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 配伍结果表Mapper接口
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Mapper
public interface MatchingResultsMapper extends BaseMapper<MatchingResults> {

    /**
     * 根据会话ID查询配伍结果
     *
     * @param sessionId 会话ID
     * @return 配伍结果
     */
    @Select("SELECT * FROM matching_results WHERE session_id = #{sessionId}")
    MatchingResults selectBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 根据结果状态查询配伍结果
     *
     * @param resultStatus 结果状态
     * @return 配伍结果列表
     */
    @Select("SELECT * FROM matching_results WHERE result_status = #{resultStatus} ORDER BY calculation_time DESC")
    List<MatchingResults> selectByResultStatus(@Param("resultStatus") String resultStatus);

    /**
     * 查询成功的配伍结果
     *
     * @param limit 限制数量
     * @return 成功的配伍结果列表
     */
    @Select("SELECT * FROM matching_results WHERE result_status = 'success' ORDER BY calculation_time DESC LIMIT #{limit}")
    List<MatchingResults> selectSuccessfulResults(@Param("limit") Integer limit);

    /**
     * 查询失败的配伍结果
     *
     * @param limit 限制数量
     * @return 失败的配伍结果列表
     */
    @Select("SELECT * FROM matching_results WHERE result_status = 'failed' ORDER BY calculation_time DESC LIMIT #{limit}")
    List<MatchingResults> selectFailedResults(@Param("limit") Integer limit);

    /**
     * 根据热值范围查询配伍结果
     *
     * @param minHeatValue 最小热值
     * @param maxHeatValue 最大热值
     * @return 配伍结果列表
     */
    @Select("SELECT * FROM matching_results WHERE calculated_heat_value BETWEEN #{minHeatValue} AND #{maxHeatValue} ORDER BY calculated_heat_value DESC")
    List<MatchingResults> selectByHeatValueRange(@Param("minHeatValue") BigDecimal minHeatValue, @Param("maxHeatValue") BigDecimal maxHeatValue);

    /**
     * 根据含水率范围查询配伍结果
     *
     * @param minWaterContent 最小含水率
     * @param maxWaterContent 最大含水率
     * @return 配伍结果列表
     */
    @Select("SELECT * FROM matching_results WHERE calculated_water_content BETWEEN #{minWaterContent} AND #{maxWaterContent} ORDER BY calculated_water_content")
    List<MatchingResults> selectByWaterContentRange(@Param("minWaterContent") BigDecimal minWaterContent, @Param("maxWaterContent") BigDecimal maxWaterContent);

    /**
     * 更新计算结果
     *
     * @param sessionId 会话ID
     * @param resultStatus 结果状态
     * @param calculatedHeatValue 计算热值
     * @param calculatedWaterContent 计算含水率
     * @return 更新条数
     */
    @Update("UPDATE matching_results SET result_status = #{resultStatus}, " +
            "calculated_heat_value = #{calculatedHeatValue}, " +
            "calculated_water_content = #{calculatedWaterContent} " +
            "WHERE session_id = #{sessionId}")
    int updateCalculationResults(@Param("sessionId") Long sessionId, 
                                @Param("resultStatus") String resultStatus,
                                @Param("calculatedHeatValue") BigDecimal calculatedHeatValue,
                                @Param("calculatedWaterContent") BigDecimal calculatedWaterContent);

    /**
     * 更新失败原因
     *
     * @param sessionId 会话ID
     * @param failureReasons 失败原因
     * @return 更新条数
     */
    @Update("UPDATE matching_results SET result_status = 'failed', failure_reasons = #{failureReasons} WHERE session_id = #{sessionId}")
    int updateFailureReasons(@Param("sessionId") Long sessionId, @Param("failureReasons") String failureReasons);

    /**
     * 获取配伍结果统计信息
     *
     * @return 统计信息
     */
    @Select("SELECT " +
            "result_status, " +
            "COUNT(*) as count, " +
            "AVG(calculated_heat_value) as avg_heat_value, " +
            "AVG(calculated_water_content) as avg_water_content " +
            "FROM matching_results " +
            "GROUP BY result_status")
    List<ResultStatistics> getResultStatistics();

    /**
     * 获取最近的配伍结果
     *
     * @param limit 限制数量
     * @return 最近的配伍结果列表
     */
    @Select("SELECT * FROM matching_results ORDER BY calculation_time DESC LIMIT #{limit}")
    List<MatchingResults> selectRecentResults(@Param("limit") Integer limit);

    /**
     * 检查会话是否有结果
     *
     * @param sessionId 会话ID
     * @return 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(1) FROM matching_results WHERE session_id = #{sessionId}")
    int existsBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 删除会话的配伍结果
     *
     * @param sessionId 会话ID
     * @return 删除条数
     */
    @Update("DELETE FROM matching_results WHERE session_id = #{sessionId}")
    int deleteBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 配伍结果统计信息内部类
     */
    @Data
    class ResultStatistics {
        private String resultStatus;
        private Integer count;
        private BigDecimal avgHeatValue;
        private BigDecimal avgWaterContent;
    }
} 