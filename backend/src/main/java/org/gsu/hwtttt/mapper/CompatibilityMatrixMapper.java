package org.gsu.hwtttt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.gsu.hwtttt.entity.CompatibilityMatrix;
import lombok.Data;

import java.util.List;

/**
 * 相容性矩阵表Mapper接口
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Mapper
public interface CompatibilityMatrixMapper extends BaseMapper<CompatibilityMatrix> {

    /**
     * 根据两种危废类别查询相容性规则（双向）
     *
     * @param category1 类别1代码
     * @param category2 类别2代码
     * @return 相容性规则
     */
    @Select("SELECT * FROM compatibility_matrix WHERE " +
            "(waste_category_1 = #{category1} AND waste_category_2 = #{category2}) OR " +
            "(waste_category_1 = #{category2} AND waste_category_2 = #{category1}) " +
            "LIMIT 1")
    CompatibilityMatrix findRule(@Param("category1") String category1, @Param("category2") String category2);

    /**
     * 根据两种危废类别查询所有相容性规则（双向，返回列表）
     *
     * @param category1 类别1代码
     * @param category2 类别2代码
     * @return 相容性规则列表
     */
    @Select("SELECT * FROM compatibility_matrix WHERE " +
            "(waste_category_1 = #{category1} AND waste_category_2 = #{category2}) OR " +
            "(waste_category_1 = #{category2} AND waste_category_2 = #{category1}) " +
            "ORDER BY create_time DESC")
    List<CompatibilityMatrix> findRuleList(@Param("category1") String category1, @Param("category2") String category2);

    /**
     * 安全查询相容性规则（处理重复记录）
     *
     * @param category1 类别1代码
     * @param category2 类别2代码
     * @return 相容性规则，如果有多条返回最新的一条
     */
    default CompatibilityMatrix findRuleSafely(@Param("category1") String category1, @Param("category2") String category2) {
        List<CompatibilityMatrix> rules = findRuleList(category1, category2);
        return rules.isEmpty() ? null : rules.get(0);
    }

    /**
     * 根据源分类查询所有相容性规则
     *
     * @param sourceCategory 源分类代码
     * @return 相容性规则列表
     */
    @Select("SELECT * FROM compatibility_matrix WHERE source_category = #{sourceCategory} ORDER BY target_category")
    List<CompatibilityMatrix> selectBySourceCategory(@Param("sourceCategory") String sourceCategory);

    /**
     * 根据目标分类查询所有相容性规则
     *
     * @param targetCategory 目标分类代码
     * @return 相容性规则列表
     */
    @Select("SELECT * FROM compatibility_matrix WHERE target_category = #{targetCategory} ORDER BY source_category")
    List<CompatibilityMatrix> selectByTargetCategory(@Param("targetCategory") String targetCategory);

    /**
     * 查询所有相容的组合
     *
     * @return 相容的组合列表
     */
    @Select("SELECT * FROM compatibility_matrix WHERE is_compatible = true ORDER BY source_category, target_category")
    List<CompatibilityMatrix> selectCompatiblePairs();

    /**
     * 查询所有不相容的组合
     *
     * @return 不相容的组合列表
     */
    @Select("SELECT * FROM compatibility_matrix WHERE is_compatible = false ORDER BY source_category, target_category")
    List<CompatibilityMatrix> selectIncompatiblePairs();

    /**
     * 根据风险等级查询相容性规则
     *
     * @param riskLevel 风险等级
     * @return 相容性规则列表
     */
    @Select("SELECT * FROM compatibility_matrix WHERE risk_level = #{riskLevel} ORDER BY source_category, target_category")
    List<CompatibilityMatrix> selectByRiskLevel(@Param("riskLevel") String riskLevel);

    /**
     * 更新相容性状态
     *
     * @param category1 类别1代码
     * @param category2 类别2代码
     * @param isCompatible 是否相容
     * @param riskLevel 风险等级
     * @return 更新条数
     */
    @Update("UPDATE compatibility_matrix SET is_compatible = #{isCompatible}, risk_level = #{riskLevel}, update_time = NOW() " +
            "WHERE (waste_category_1 = #{category1} AND waste_category_2 = #{category2}) OR " +
            "(waste_category_1 = #{category2} AND waste_category_2 = #{category1})")
    int updateCompatibility(@Param("category1") String category1,
                           @Param("category2") String category2,
                           @Param("isCompatible") Boolean isCompatible,
                           @Param("riskLevel") String riskLevel);

    /**
     * 更新限制条件
     *
     * @param category1 类别1代码
     * @param category2 类别2代码
     * @param restrictions 限制条件
     * @return 更新条数
     */
    @Update("UPDATE compatibility_matrix SET restrictions = #{restrictions}, update_time = NOW() " +
            "WHERE (waste_category_1 = #{category1} AND waste_category_2 = #{category2}) OR " +
            "(waste_category_1 = #{category2} AND waste_category_2 = #{category1})")
    int updateRestrictions(@Param("category1") String category1,
                          @Param("category2") String category2,
                          @Param("restrictions") String restrictions);

    /**
     * 获取相容性统计信息
     *
     * @return 统计信息
     */
    @Select("SELECT " +
            "SUM(CASE WHEN is_compatible = true THEN 1 ELSE 0 END) as compatible_count, " +
            "SUM(CASE WHEN is_compatible = false THEN 1 ELSE 0 END) as incompatible_count, " +
            "COUNT(*) as total_count " +
            "FROM compatibility_matrix")
    CompatibilityStatistics getCompatibilityStatistics();

    /**
     * 根据风险等级统计相容性分布
     *
     * @return 风险等级统计
     */
    @Select("SELECT risk_level, " +
            "SUM(CASE WHEN is_compatible = true THEN 1 ELSE 0 END) as compatible_count, " +
            "SUM(CASE WHEN is_compatible = false THEN 1 ELSE 0 END) as incompatible_count, " +
            "COUNT(*) as total_count " +
            "FROM compatibility_matrix " +
            "GROUP BY risk_level " +
            "ORDER BY risk_level")
    List<RiskLevelStatistics> getRiskLevelStatistics();

    /**
     * 检查相容性规则是否存在
     *
     * @param category1 类别1代码
     * @param category2 类别2代码
     * @return 存在返回1，不存在返回0
     */
    @Select("SELECT COUNT(1) FROM compatibility_matrix WHERE " +
            "(waste_category_1 = #{category1} AND waste_category_2 = #{category2}) OR " +
            "(waste_category_1 = #{category2} AND waste_category_2 = #{category1})")
    int existsByCategories(@Param("category1") String category1, @Param("category2") String category2);

    /**
     * 获取所有涉及的分类代码
     *
     * @return 分类代码列表
     */
    @Select("SELECT DISTINCT waste_category_1 as category FROM compatibility_matrix " +
            "UNION " +
            "SELECT DISTINCT waste_category_2 as category FROM compatibility_matrix " +
            "ORDER BY category")
    List<String> getAllInvolvedCategories();

    /**
     * 查找重复的双向规则
     *
     * @return 重复规则统计
     */
    @Select("SELECT " +
            "CASE WHEN waste_category_1 < waste_category_2 THEN CONCAT(waste_category_1, '-', waste_category_2) " +
            "     ELSE CONCAT(waste_category_2, '-', waste_category_1) END as category_pair, " +
            "COUNT(*) as rule_count, " +
            "MIN(id) as min_id, " +
            "MAX(id) as max_id " +
            "FROM compatibility_matrix " +
            "GROUP BY " +
            "CASE WHEN waste_category_1 < waste_category_2 THEN CONCAT(waste_category_1, '-', waste_category_2) " +
            "     ELSE CONCAT(waste_category_2, '-', waste_category_1) END " +
            "HAVING COUNT(*) > 1 " +
            "ORDER BY rule_count DESC")
    List<DuplicateRuleStatistics> findDuplicateRules();

    /**
     * 相容性统计信息内部类
     */
    @Data
    class CompatibilityStatistics {
        private Integer compatibleCount;
        private Integer incompatibleCount;
        private Integer totalCount;
    }

    /**
     * 风险等级统计信息内部类
     */
    @Data
    class RiskLevelStatistics {
        private String riskLevel;
        private Integer compatibleCount;
        private Integer incompatibleCount;
        private Integer totalCount;
    }

    /**
     * 重复规则统计信息内部类
     */
    @Data
    class DuplicateRuleStatistics {
        private String categoryPair;
        private Integer ruleCount;
        private Long minId;
        private Long maxId;
    }
} 