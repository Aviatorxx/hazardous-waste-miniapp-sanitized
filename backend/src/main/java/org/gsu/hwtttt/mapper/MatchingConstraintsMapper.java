package org.gsu.hwtttt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.gsu.hwtttt.entity.MatchingConstraints;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 配伍算法约束配置表Mapper接口
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Mapper
public interface MatchingConstraintsMapper extends BaseMapper<MatchingConstraints> {

    /**
     * 根据参数代码查询约束
     *
     * @param parameterCode 参数代码
     * @return 约束配置
     */
    @Select("SELECT * FROM matching_constraints WHERE parameter_code = #{parameterCode}")
    MatchingConstraints selectByParameterCode(@Param("parameterCode") String parameterCode);

    /**
     * 查询所有启用的约束
     *
     * @return 启用的约束列表
     */
    @Select("SELECT * FROM matching_constraints WHERE is_active = true ORDER BY sort_order")
    List<MatchingConstraints> selectActiveConstraints();

    /**
     * 根据约束名称模糊查询
     *
     * @param constraintName 约束名称关键字
     * @return 约束列表
     */
    @Select("SELECT * FROM matching_constraints WHERE constraint_name LIKE CONCAT('%', #{constraintName}, '%') ORDER BY sort_order")
    List<MatchingConstraints> selectByConstraintNameLike(@Param("constraintName") String constraintName);

    /**
     * 根据参数代码列表查询约束
     *
     * @param parameterCodes 参数代码列表
     * @return 约束列表
     */
    @Select("<script>" +
            "SELECT * FROM matching_constraints WHERE parameter_code IN " +
            "<foreach collection='parameterCodes' item='code' open='(' close=')' separator=','>" +
            "#{code}" +
            "</foreach>" +
            "ORDER BY sort_order" +
            "</script>")
    List<MatchingConstraints> selectByParameterCodes(@Param("parameterCodes") List<String> parameterCodes);

    /**
     * 更新约束状态
     *
     * @param parameterCode 参数代码
     * @param isActive 是否启用
     * @return 更新条数
     */
    @Update("UPDATE matching_constraints SET is_active = #{isActive}, update_time = NOW() WHERE parameter_code = #{parameterCode}")
    int updateConstraintStatus(@Param("parameterCode") String parameterCode, @Param("isActive") Boolean isActive);

    /**
     * 更新约束值范围
     *
     * @param parameterCode 参数代码
     * @param minValue 最小值
     * @param maxValue 最大值
     * @return 更新条数
     */
    @Update("UPDATE matching_constraints SET min_value = #{minValue}, max_value = #{maxValue}, update_time = NOW() WHERE parameter_code = #{parameterCode}")
    int updateConstraintRange(@Param("parameterCode") String parameterCode, @Param("minValue") BigDecimal minValue, @Param("maxValue") BigDecimal maxValue);

    /**
     * 获取约束统计信息
     *
     * @return 约束统计
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN is_active = true THEN 1 ELSE 0 END) as active_count, " +
            "SUM(CASE WHEN min_value IS NOT NULL THEN 1 ELSE 0 END) as with_min_value_count, " +
            "SUM(CASE WHEN max_value IS NOT NULL THEN 1 ELSE 0 END) as with_max_value_count " +
            "FROM matching_constraints")
    ConstraintStatistics getConstraintStatistics();

    /**
     * 检查参数代码是否存在
     *
     * @param parameterCode 参数代码
     * @return 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(1) FROM matching_constraints WHERE parameter_code = #{parameterCode}")
    int existsByParameterCode(@Param("parameterCode") String parameterCode);

    /**
     * 获取最大排序号
     *
     * @return 最大排序号
     */
    @Select("SELECT MAX(sort_order) FROM matching_constraints")
    Integer getMaxSortOrder();

    /**
     * 约束统计信息内部类
     */
    @Data
    class ConstraintStatistics {
        private Integer totalCount;
        private Integer activeCount;
        private Integer withMinValueCount;
        private Integer withMaxValueCount;
    }
} 