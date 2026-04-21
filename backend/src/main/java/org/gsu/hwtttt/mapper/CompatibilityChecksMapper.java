package org.gsu.hwtttt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.gsu.hwtttt.entity.CompatibilityChecks;
import lombok.Data;

import java.util.List;

/**
 * 相容性检查记录表Mapper接口
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Mapper
public interface CompatibilityChecksMapper extends BaseMapper<CompatibilityChecks> {

    /**
     * 根据检查结果查询记录
     *
     * @param checkResult 检查结果
     * @return 检查记录列表
     */
    @Select("SELECT * FROM compatibility_checks WHERE check_result = #{checkResult} ORDER BY check_time DESC")
    List<CompatibilityChecks> selectByCheckResult(@Param("checkResult") String checkResult);

    /**
     * 根据危废组合查询检查记录
     *
     * @param wasteIds 危废ID列表
     * @return 检查记录列表
     */
    @Select("<script>" +
            "SELECT * FROM compatibility_checks WHERE " +
            "<foreach collection='wasteIds' item='wasteId' separator=' OR '>" +
            "waste_combination LIKE CONCAT('%', #{wasteId}, '%')" +
            "</foreach>" +
            "ORDER BY check_time DESC" +
            "</script>")
    List<CompatibilityChecks> selectByWasteIds(@Param("wasteIds") List<Long> wasteIds);

    /**
     * 根据检查类型查询记录
     *
     * @param checkType 检查类型
     * @return 检查记录列表
     */
    @Select("SELECT * FROM compatibility_checks WHERE check_type = #{checkType} ORDER BY check_time DESC")
    List<CompatibilityChecks> selectByCheckType(@Param("checkType") String checkType);

    /**
     * 查询最近的检查记录
     *
     * @param limit 限制数量
     * @return 最近的检查记录列表
     */
    @Select("SELECT * FROM compatibility_checks ORDER BY check_time DESC LIMIT #{limit}")
    List<CompatibilityChecks> selectRecentChecks(@Param("limit") Integer limit);

    /**
     * 根据会话ID查询检查记录
     *
     * @param sessionId 会话ID
     * @return 检查记录列表
     */
    @Select("SELECT * FROM compatibility_checks WHERE session_id = #{sessionId} ORDER BY check_time DESC")
    List<CompatibilityChecks> selectBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 统计检查结果分布
     *
     * @return 检查结果统计
     */
    @Select("SELECT check_result, COUNT(*) as count, " +
            "ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM compatibility_checks), 2) as percentage " +
            "FROM compatibility_checks " +
            "GROUP BY check_result " +
            "ORDER BY count DESC")
    List<CheckResultStatistics> getCheckResultStatistics();

    /**
     * 统计检查类型分布
     *
     * @return 检查类型统计
     */
    @Select("SELECT check_type, COUNT(*) as count FROM compatibility_checks GROUP BY check_type ORDER BY count DESC")
    List<CheckTypeStatistics> getCheckTypeStatistics();

    /**
     * 更新检查备注
     *
     * @param id 记录ID
     * @param notes 备注
     * @return 更新条数
     */
    @Update("UPDATE compatibility_checks SET notes = #{notes} WHERE id = #{id}")
    int updateNotes(@Param("id") Long id, @Param("notes") String notes);

    /**
     * 检查是否已存在相同的检查记录
     *
     * @param wasteCombination 危废组合
     * @param checkType 检查类型
     * @return 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(1) FROM compatibility_checks WHERE waste_combination = #{wasteCombination} AND check_type = #{checkType}")
    int existsByWasteCombinationAndType(@Param("wasteCombination") String wasteCombination, @Param("checkType") String checkType);

    /**
     * 检查结果统计信息内部类
     */
    @Data
    class CheckResultStatistics {
        private String checkResult;
        private Integer count;
        private Double percentage;
    }

    /**
     * 检查类型统计信息内部类
     */
    @Data
    class CheckTypeStatistics {
        private String checkType;
        private Integer count;
    }
} 