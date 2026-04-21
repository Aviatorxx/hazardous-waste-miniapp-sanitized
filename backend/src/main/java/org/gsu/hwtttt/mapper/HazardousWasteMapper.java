package org.gsu.hwtttt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.*;
import org.gsu.hwtttt.dto.request.WasteSearchRequest;
import org.gsu.hwtttt.entity.HazardousWaste;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 危废主表Mapper接口
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Mapper
public interface HazardousWasteMapper extends BaseMapper<HazardousWaste> {

    /**
     * 分页搜索危废
     *
     * @param page 分页对象
     * @param request 搜索条件
     * @return 危废分页列表
     */
    @Select("<script>" +
            "SELECT * FROM hazardous_waste " +
            "<where> " +
            "deleted = 0 " +
            "<if test='req.keyword != null and req.keyword != \"\"'> " +
            "AND ( " +
            "waste_code LIKE CONCAT('%', #{req.keyword}, '%') " +
            "OR waste_name LIKE CONCAT('%', #{req.keyword}, '%') " +
            "OR source_unit LIKE CONCAT('%', #{req.keyword}, '%') " +
            "OR harmful_components LIKE CONCAT('%', #{req.keyword}, '%') " +
            ") " +
            "</if> " +
            "<if test='req.wasteCode != null and req.wasteCode != \"\"'> " +
            "AND waste_code = #{req.wasteCode} " +
            "</if> " +
            "<if test='req.wasteName != null and req.wasteName != \"\"'> " +
            "AND waste_name LIKE CONCAT('%', #{req.wasteName}, '%') " +
            "</if> " +
            "<if test='req.sourceUnit != null and req.sourceUnit != \"\"'> " +
            "AND source_unit LIKE CONCAT('%', #{req.sourceUnit}, '%') " +
            "</if> " +
            "<if test='req.auditStatus != null and req.auditStatus != \"\"'> " +
            "AND audit_status = #{req.auditStatus} " +
            "</if> " +
            "<if test='req.minStorage != null'> " +
            "AND remaining_storage &gt;= #{req.minStorage} " +
            "</if> " +
            "<if test='req.maxStorage != null'> " +
            "AND remaining_storage &lt;= #{req.maxStorage} " +
            "</if> " +
            "</where> " +
            "<choose> " +
            "<when test='req.sortField != null and req.sortField != \"\"'> " +
            "ORDER BY ${req.sortField} " +
            "<if test='req.sortOrder != null and req.sortOrder == \"asc\"'> " +
            "ASC " +
            "</if> " +
            "<if test='req.sortOrder != null and req.sortOrder == \"desc\"'> " +
            "DESC " +
            "</if> " +
            "</when> " +
            "<otherwise> " +
            "ORDER BY create_time DESC " +
            "</otherwise> " +
            "</choose> " +
            "</script>")
    Page<HazardousWaste> searchWaste(Page<HazardousWaste> page, @Param("req") WasteSearchRequest request);

    /**
     * 根据关键字搜索危废
     *
     * @param keyword 关键字
     * @return 危废列表
     */
    @Select("SELECT * FROM hazardous_waste " +
            "WHERE deleted = 0 " +
            "AND ( " +
            "waste_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR waste_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR source_unit LIKE CONCAT('%', #{keyword}, '%') " +
            "OR harmful_components LIKE CONCAT('%', #{keyword}, '%') " +
            ") " +
            "ORDER BY " +
            "CASE " +
            "WHEN waste_code = #{keyword} THEN 1 " +
            "WHEN waste_name = #{keyword} THEN 2 " +
            "WHEN waste_code LIKE CONCAT(#{keyword}, '%') THEN 3 " +
            "WHEN waste_name LIKE CONCAT(#{keyword}, '%') THEN 4 " +
            "ELSE 5 " +
            "END, " +
            "create_time DESC " +
            "LIMIT 50")
    List<HazardousWaste> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 根据危废代码列表查询
     *
     * @param wasteCodes 危废代码列表
     * @return 危废列表
     */
    @Select("<script>" +
            "SELECT * FROM hazardous_waste " +
            "WHERE deleted = 0 AND waste_code IN " +
            "<foreach collection='wasteCodes' item='code' open='(' close=')' separator=','> " +
            "#{code} " +
            "</foreach> " +
            "ORDER BY create_time DESC" +
            "</script>")
    List<HazardousWaste> selectByWasteCodes(@Param("wasteCodes") List<String> wasteCodes);

    /**
     * 更新危废库存
     *
     * @param wasteId 危废ID
     * @param storage 新库存量
     * @return 更新条数
     */
    @Update("UPDATE hazardous_waste SET remaining_storage = #{storage}, update_time = NOW() WHERE id = #{wasteId}")
    int updateStorage(@Param("wasteId") Long wasteId, @Param("storage") BigDecimal storage);

    /**
     * 获取危废统计信息
     *
     * @return 统计信息
     */
    @Select("SELECT " +
            "'total' as wasteCategory, " +
            "COUNT(*) as count, " +
            "COALESCE(SUM(remaining_storage), 0) as totalStorage " +
            "FROM hazardous_waste " +
            "WHERE deleted = 0 " +
            "UNION ALL " +
            "SELECT " +
            "audit_status as wasteCategory, " +
            "COUNT(*) as count, " +
            "COALESCE(SUM(remaining_storage), 0) as totalStorage " +
            "FROM hazardous_waste " +
            "WHERE deleted = 0 AND audit_status IS NOT NULL " +
            "GROUP BY audit_status")
    List<WasteStatistics> getWasteStatistics();

    // ==================== 新增化学成分相关方法 ====================
    
    /**
     * 根据化学成分含量范围筛选危废
     *
     * @param componentColumn 成分字段名（如 "cl_percent", "f_percent" 等）
     * @param minValue 最小值
     * @param maxValue 最大值
     * @return 符合条件的危废列表
     */
    @Select("SELECT * FROM hazardous_waste " +
            "WHERE deleted = 0 " +
            "AND ${componentColumn} BETWEEN #{minValue} AND #{maxValue} " +
            "AND ${componentColumn} IS NOT NULL " +
            "ORDER BY ${componentColumn} ASC")
    List<HazardousWaste> selectByChemicalComponentRange(@Param("componentColumn") String componentColumn,
                                                        @Param("minValue") BigDecimal minValue,
                                                        @Param("maxValue") BigDecimal maxValue);

    /**
     * 根据重金属含量范围筛选危废
     *
     * @param metalColumn 金属字段名（如 "pb_mg_per_l", "cd_mg_per_l" 等）
     * @param minValue 最小值
     * @param maxValue 最大值
     * @return 符合条件的危废列表
     */
    @Select("SELECT * FROM hazardous_waste " +
            "WHERE deleted = 0 " +
            "AND ${metalColumn} BETWEEN #{minValue} AND #{maxValue} " +
            "AND ${metalColumn} IS NOT NULL " +
            "ORDER BY ${metalColumn} ASC")
    List<HazardousWaste> selectByHeavyMetalRange(@Param("metalColumn") String metalColumn,
                                                 @Param("minValue") BigDecimal minValue,
                                                 @Param("maxValue") BigDecimal maxValue);

    /**
     * 获取化学成分统计信息
     *
     * @param componentColumn 成分字段名
     * @return 统计信息
     */
    @Select("SELECT " +
            "'${componentColumn}' as componentName, " +
            "AVG(${componentColumn}) as avgValue, " +
            "MIN(${componentColumn}) as minValue, " +
            "MAX(${componentColumn}) as maxValue, " +
            "STDDEV(${componentColumn}) as stdDev, " +
            "COUNT(*) as sampleCount " +
            "FROM hazardous_waste " +
            "WHERE deleted = 0 AND ${componentColumn} IS NOT NULL")
    ComponentStatistics getChemicalComponentStatistics(@Param("componentColumn") String componentColumn);

    /**
     * 获取重金属含量分布统计
     *
     * @return 重金属含量分布数据
     */
    @Select("SELECT " +
            "'Pb' as metalName, " +
            "CASE " +
            "WHEN pb_mg_per_l < 10 THEN '<10' " +
            "WHEN pb_mg_per_l < 50 THEN '10-50' " +
            "WHEN pb_mg_per_l < 100 THEN '50-100' " +
            "ELSE '>=100' " +
            "END as concentrationRange, " +
            "COUNT(*) as wasteCount, " +
            "ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM hazardous_waste WHERE deleted = 0 AND pb_mg_per_l IS NOT NULL), 2) as percentage " +
            "FROM hazardous_waste " +
            "WHERE deleted = 0 AND pb_mg_per_l IS NOT NULL " +
            "GROUP BY concentrationRange " +
            "UNION ALL " +
            "SELECT " +
            "'Cd' as metalName, " +
            "CASE " +
            "WHEN cd_mg_per_l < 5 THEN '<5' " +
            "WHEN cd_mg_per_l < 25 THEN '5-25' " +
            "WHEN cd_mg_per_l < 50 THEN '25-50' " +
            "ELSE '>=50' " +
            "END as concentrationRange, " +
            "COUNT(*) as wasteCount, " +
            "ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM hazardous_waste WHERE deleted = 0 AND cd_mg_per_l IS NOT NULL), 2) as percentage " +
            "FROM hazardous_waste " +
            "WHERE deleted = 0 AND cd_mg_per_l IS NOT NULL " +
            "GROUP BY concentrationRange " +
            "ORDER BY metalName, wasteCount DESC")
    List<MetalDistribution> getHeavyMetalDistribution();

    // ==================== 数据质量管理方法 ====================
    
    /**
     * 根据数据质量评分筛选危废
     *
     * @param minScore 最小评分
     * @param maxScore 最大评分
     * @return 符合条件的危废列表
     */
    @Select("SELECT * FROM hazardous_waste WHERE data_quality_score BETWEEN #{minScore} AND #{maxScore} AND deleted = 0")
    List<HazardousWaste> selectByDataQualityScore(@Param("minScore") BigDecimal minScore,
                                                  @Param("maxScore") BigDecimal maxScore);

    /**
     * 更新数据质量评分
     *
     * @param wasteId 危废ID
     * @param score 数据质量评分
     * @return 更新条数
     */
    @Update("UPDATE hazardous_waste SET data_quality_score = #{score}, update_time = NOW() WHERE id = #{wasteId}")
    int updateDataQualityScore(@Param("wasteId") Long wasteId, @Param("score") BigDecimal score);

    /**
     * 获取需要计算数据质量评分的危废列表
     *
     * @param wasteIds 指定的危废ID列表，为空则查询所有
     * @return 危废列表
     */
    @Select("<script>" +
            "SELECT * FROM hazardous_waste " +
            "WHERE deleted = 0 " +
            "<if test='wasteIds != null and wasteIds.size() > 0'> " +
            "AND id IN " +
            "<foreach collection='wasteIds' item='id' open='(' close=')' separator=','> " +
            "#{id} " +
            "</foreach> " +
            "</if> " +
            "ORDER BY update_time DESC" +
            "</script>")
    List<HazardousWaste> selectForQualityCalculation(@Param("wasteIds") List<Long> wasteIds);

    // ==================== 审核管理方法 ====================
    
    /**
     * 根据审核状态查询危废
     *
     * @param auditStatus 审核状态
     * @return 危废列表
     */
    @Select("SELECT * FROM hazardous_waste WHERE audit_status = #{auditStatus} AND deleted = 0")
    List<HazardousWaste> selectByAuditStatus(@Param("auditStatus") String auditStatus);

    /**
     * 更新审核信息
     *
     * @param wasteId 危废ID
     * @param auditStatus 审核状态
     * @param auditUser 审核人
     * @param auditNotes 审核备注
     * @param auditTime 审核时间
     * @return 更新条数
     */
    @Update("UPDATE hazardous_waste SET audit_status = #{auditStatus}, audit_user = #{auditUser}, " +
            "audit_notes = #{auditNotes}, audit_time = #{auditTime}, update_time = NOW() WHERE id = #{wasteId}")
    int updateAuditInfo(@Param("wasteId") Long wasteId,
                        @Param("auditStatus") String auditStatus,
                        @Param("auditUser") String auditUser,
                        @Param("auditNotes") String auditNotes,
                        @Param("auditTime") LocalDateTime auditTime);

    /**
     * 获取审核统计信息
     *
     * @return 审核统计数据
     */
    @Select("SELECT " +
            "COALESCE(audit_status, 'pending') as auditStatus, " +
            "COUNT(*) as count, " +
            "audit_user as auditUser, " +
            "ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM hazardous_waste WHERE deleted = 0), 2) as percentage " +
            "FROM hazardous_waste " +
            "WHERE deleted = 0 " +
            "GROUP BY audit_status, audit_user " +
            "ORDER BY count DESC")
    List<AuditStatistics> getAuditStatistics();

    // ==================== 相容性分类相关方法 ====================
    
    /**
     * 根据相容性分类代码查询危废
     *
     * @param categoryCode 相容性分类代码
     * @return 危废列表
     */
    @Select("SELECT * FROM hazardous_waste WHERE compatibility_category_code = #{categoryCode} AND deleted = 0")
    List<HazardousWaste> selectByCompatibilityCategory(@Param("categoryCode") String categoryCode);

    /**
     * 更新危废的相容性分类代码
     *
     * @param wasteId 危废ID
     * @param categoryCode 相容性分类代码
     * @return 更新条数
     */
    @Update("UPDATE hazardous_waste SET compatibility_category_code = #{categoryCode}, update_time = NOW() WHERE id = #{wasteId}")
    int updateCompatibilityCategory(@Param("wasteId") Long wasteId, @Param("categoryCode") String categoryCode);

    /**
     * 获取相容性分类统计信息
     *
     * @return 相容性分类统计数据
     */
    @Select("SELECT " +
            "cc.category_code as categoryCode, " +
            "cc.category_name_cn as categoryNameCn, " +
            "cc.category_name_en as categoryNameEn, " +
            "COUNT(hw.id) as wasteCount, " +
            "COALESCE(SUM(hw.remaining_storage), 0) as totalStorage, " +
            "ROUND(COUNT(hw.id) * 100.0 / (SELECT COUNT(*) FROM hazardous_waste WHERE deleted = 0), 2) as percentage " +
            "FROM compatibility_categories cc " +
            "LEFT JOIN hazardous_waste hw ON cc.category_code = hw.compatibility_category_code AND hw.deleted = 0 " +
            "GROUP BY cc.category_code, cc.category_name_cn, cc.category_name_en " +
            "ORDER BY wasteCount DESC")
    List<CompatibilityStatistics> getCompatibilityStatistics();

    /**
     * 查询没有设置相容性分类代码的危废
     *
     * @return 未分类的危废列表
     */
    @Select("SELECT * FROM hazardous_waste WHERE compatibility_category_code IS NULL AND deleted = 0")
    List<HazardousWaste> selectUnclassifiedWastes();

    // ==================== 综合分析方法 ====================
    
    /**
     * 根据多条件组合筛选危废
     *
     * @param criteria 筛选条件Map
     * @return 符合条件的危废列表
     */
    @Select("<script>" +
            "SELECT * FROM hazardous_waste " +
            "<where> " +
            "deleted = 0 " +
            "<if test='criteria.auditStatus != null'> " +
            "AND audit_status = #{criteria.auditStatus} " +
            "</if> " +
            "<if test='criteria.minDataQuality != null'> " +
            "AND data_quality_score &gt;= #{criteria.minDataQuality} " +
            "</if> " +
            "<if test='criteria.maxDataQuality != null'> " +
            "AND data_quality_score &lt;= #{criteria.maxDataQuality} " +
            "</if> " +
            "<if test='criteria.minStorage != null'> " +
            "AND remaining_storage &gt;= #{criteria.minStorage} " +
            "</if> " +
            "<if test='criteria.maxStorage != null'> " +
            "AND remaining_storage &lt;= #{criteria.maxStorage} " +
            "</if> " +
            "<if test='criteria.sourceUnit != null'> " +
            "AND source_unit LIKE CONCAT('%', #{criteria.sourceUnit}, '%') " +
            "</if> " +
            "<if test='criteria.hasHeavyMetal != null and criteria.hasHeavyMetal == true'> " +
            "AND (pb_mg_per_l > 0 OR cd_mg_per_l > 0 OR cr_mg_per_l > 0 OR ni_mg_per_l > 0) " +
            "</if> " +
            "</where> " +
            "ORDER BY create_time DESC" +
            "</script>")
    List<HazardousWaste> selectByMultipleCriteria(@Param("criteria") Map<String, Object> criteria);

    /**
     * 根据相似度阈值查找相似危废
     *
     * @param referenceWaste 参考危废
     * @param threshold 相似度阈值
     * @return 相似危废及相似度列表
     */
    @Select("SELECT " +
            "hw.id as wasteId, " +
            "hw.waste_code as wasteCode, " +
            "hw.waste_name as wasteName, " +
            "( " +
            "CASE WHEN #{referenceWaste.ph} IS NOT NULL AND hw.ph IS NOT NULL " +
            "THEN 1 - ABS(#{referenceWaste.ph} - hw.ph) / (SELECT MAX(ph) - MIN(ph) FROM hazardous_waste WHERE ph IS NOT NULL AND deleted = 0) " +
            "ELSE 0 END + " +
            "CASE WHEN #{referenceWaste.heatValueCalPerG} IS NOT NULL AND hw.heat_value_cal_per_g IS NOT NULL " +
            "THEN 1 - ABS(#{referenceWaste.heatValueCalPerG} - hw.heat_value_cal_per_g) / (SELECT MAX(heat_value_cal_per_g) - MIN(heat_value_cal_per_g) FROM hazardous_waste WHERE heat_value_cal_per_g IS NOT NULL AND deleted = 0) " +
            "ELSE 0 END + " +
            "CASE WHEN #{referenceWaste.waterContentPercent} IS NOT NULL AND hw.water_content_percent IS NOT NULL " +
            "THEN 1 - ABS(#{referenceWaste.waterContentPercent} - hw.water_content_percent) / 100 " +
            "ELSE 0 END " +
            ") / 3 as similarity " +
            "FROM hazardous_waste hw " +
            "WHERE hw.deleted = 0 " +
            "AND hw.id != #{referenceWaste.id} " +
            "HAVING similarity >= #{threshold} " +
            "ORDER BY similarity DESC " +
            "LIMIT 20")
    List<WasteSimilarity> findSimilarWastes(@Param("referenceWaste") HazardousWaste referenceWaste,
                                           @Param("threshold") BigDecimal threshold);

    /**
     * 获取危废风险评估数据
     *
     * @param wasteId 危废ID
     * @return 风险评估相关数据
     */
    @Select("SELECT " +
            "#{wasteId} as wasteId, " +
            "( " +
            "CASE WHEN oxidizing = 1 THEN 0.2 ELSE 0 END + " +
            "CASE WHEN flammable = 1 THEN 0.2 ELSE 0 END + " +
            "CASE WHEN toxic = 1 THEN 0.3 ELSE 0 END + " +
            "CASE WHEN corrosive = 1 THEN 0.15 ELSE 0 END + " +
            "CASE WHEN reactive = 1 THEN 0.15 ELSE 0 END + " +
            "CASE WHEN pb_mg_per_l > 100 THEN 0.1 ELSE 0 END + " +
            "CASE WHEN cd_mg_per_l > 50 THEN 0.1 ELSE 0 END " +
            ") as riskScore, " +
            "CASE " +
            "WHEN ( " +
            "CASE WHEN oxidizing = 1 THEN 0.2 ELSE 0 END + " +
            "CASE WHEN flammable = 1 THEN 0.2 ELSE 0 END + " +
            "CASE WHEN toxic = 1 THEN 0.3 ELSE 0 END + " +
            "CASE WHEN corrosive = 1 THEN 0.15 ELSE 0 END + " +
            "CASE WHEN reactive = 1 THEN 0.15 ELSE 0 END + " +
            "CASE WHEN pb_mg_per_l > 100 THEN 0.1 ELSE 0 END + " +
            "CASE WHEN cd_mg_per_l > 50 THEN 0.1 ELSE 0 END " +
            ") >= 0.7 THEN 'HIGH' " +
            "WHEN ( " +
            "CASE WHEN oxidizing = 1 THEN 0.2 ELSE 0 END + " +
            "CASE WHEN flammable = 1 THEN 0.2 ELSE 0 END + " +
            "CASE WHEN toxic = 1 THEN 0.3 ELSE 0 END + " +
            "CASE WHEN corrosive = 1 THEN 0.15 ELSE 0 END + " +
            "CASE WHEN reactive = 1 THEN 0.15 ELSE 0 END + " +
            "CASE WHEN pb_mg_per_l > 100 THEN 0.1 ELSE 0 END + " +
            "CASE WHEN cd_mg_per_l > 50 THEN 0.1 ELSE 0 END " +
            ") >= 0.3 THEN 'MEDIUM' " +
            "ELSE 'LOW' " +
            "END as riskLevel, " +
            "JSON_OBJECT( " +
            "'hasOxidizing', oxidizing, " +
            "'hasFlammable', flammable, " +
            "'hasToxic', toxic, " +
            "'hasCorrosive', corrosive, " +
            "'hasReactive', reactive, " +
            "'highPb', CASE WHEN pb_mg_per_l > 100 THEN 1 ELSE 0 END, " +
            "'highCd', CASE WHEN cd_mg_per_l > 50 THEN 1 ELSE 0 END " +
            ") as riskFactors " +
            "FROM hazardous_waste " +
            "WHERE id = #{wasteId} AND deleted = 0")
    RiskAssessmentData getRiskAssessmentData(@Param("wasteId") Long wasteId);

    // ==================== 内部类定义 ====================
    
    /**
     * 危废统计信息
     */
    @Data
    class WasteStatistics {
        private String wasteCategory;
        private Integer count;
        private BigDecimal totalStorage;
    }

    /**
     * 化学成分统计信息
     */
    @Data
    class ComponentStatistics {
        private String componentName;
        private BigDecimal avgValue;
        private BigDecimal minValue;
        private BigDecimal maxValue;
        private BigDecimal stdDev;
        private Integer sampleCount;
    }

    /**
     * 重金属分布统计
     */
    @Data
    class MetalDistribution {
        private String metalName;
        private String concentrationRange;
        private Integer wasteCount;
        private BigDecimal percentage;
    }

    /**
     * 审核统计信息
     */
    @Data
    class AuditStatistics {
        private String auditStatus;
        private Integer count;
        private String auditUser;
        private BigDecimal percentage;
    }

    /**
     * 危废相似度
     */
    @Data
    class WasteSimilarity {
        private Long wasteId;
        private String wasteCode;
        private String wasteName;
        private BigDecimal similarity;
    }

    /**
     * 风险评估数据
     */
    @Data
    class RiskAssessmentData {
        private Long wasteId;
        private BigDecimal riskScore;
        private String riskLevel;
        private Map<String, Object> riskFactors;
    }

    /**
     * 相容性分类统计信息
     */
    @Data
    class CompatibilityStatistics {
        private String categoryCode;
        private String categoryNameCn;
        private String categoryNameEn;
        private Integer wasteCount;
        private BigDecimal totalStorage;
        private BigDecimal percentage;
    }

    // ==================== 物理特性查询方法 ====================
    
    /**
     * 根据物理特性分类查询危废记录（分页）
     *
     * @param categoryCode 分类代码
     * @param search 搜索关键字
     * @param offset 偏移量
     * @param size 页大小
     * @return 危废记录列表
     */
    @Select("<script>" +
            "SELECT * FROM hazardous_waste " +
            "WHERE deleted = 0 " +
            "<choose>" +
            "<when test='categoryCode == \"ELEMENT_COMPOSITION\"'>" +
            "AND (c_percent IS NOT NULL OR h_percent IS NOT NULL OR o_percent IS NOT NULL OR " +
            "n_percent IS NOT NULL OR s_percent IS NOT NULL OR p_percent IS NOT NULL) " +
            "</when>" +
            "<when test='categoryCode == \"HEAT_VALUE\"'>" +
            "AND heat_value_cal_per_g IS NOT NULL " +
            "</when>" +
            "<when test='categoryCode == \"PH\"'>" +
            "AND ph IS NOT NULL " +
            "</when>" +
            "<when test='categoryCode == \"WATER_CONTENT\"'>" +
            "AND water_content_percent IS NOT NULL " +
            "</when>" +
            "<when test='categoryCode == \"FLASH_POINT\"'>" +
            "AND flash_point_celsius IS NOT NULL " +
            "</when>" +
            "<when test='categoryCode == \"HEAVY_METALS\"'>" +
            "AND (fe_mg_per_l IS NOT NULL OR cu_mg_per_l IS NOT NULL OR pb_mg_per_l IS NOT NULL OR " +
            "cd_mg_per_l IS NOT NULL OR cr_mg_per_l IS NOT NULL OR ni_mg_per_l IS NOT NULL OR " +
            "mn_mg_per_l IS NOT NULL OR sn_mg_per_l IS NOT NULL OR as_mg_per_l IS NOT NULL OR " +
            "co_mg_per_l IS NOT NULL OR sb_mg_per_l IS NOT NULL OR tl_mg_per_l IS NOT NULL) " +
            "</when>" +
            "<when test='categoryCode == \"ALKALI_METALS\"'>" +
            "AND (k_mg_per_l IS NOT NULL OR na_mg_per_l IS NOT NULL OR mg_mg_per_l IS NOT NULL) " +
            "</when>" +
            "</choose>" +
            "<if test='search != null and search != \"\"'>" +
            "AND (waste_code LIKE CONCAT('%', #{search}, '%') OR waste_name LIKE CONCAT('%', #{search}, '%')) " +
            "</if>" +
            "ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{size}" +
            "</script>")
    List<HazardousWaste> selectByPhysicalPropertyCategory(@Param("categoryCode") String categoryCode, 
                                                         @Param("search") String search,
                                                         @Param("offset") long offset, 
                                                         @Param("size") Long size);
    
    /**
     * 根据物理特性分类统计记录数量
     *
     * @param categoryCode 分类代码
     * @param search 搜索关键字
     * @return 记录数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM hazardous_waste " +
            "WHERE deleted = 0 " +
            "<choose>" +
            "<when test='categoryCode == \"ELEMENT_COMPOSITION\"'>" +
            "AND (c_percent IS NOT NULL OR h_percent IS NOT NULL OR o_percent IS NOT NULL OR " +
            "n_percent IS NOT NULL OR s_percent IS NOT NULL OR p_percent IS NOT NULL) " +
            "</when>" +
            "<when test='categoryCode == \"HEAT_VALUE\"'>" +
            "AND heat_value_cal_per_g IS NOT NULL " +
            "</when>" +
            "<when test='categoryCode == \"PH\"'>" +
            "AND ph IS NOT NULL " +
            "</when>" +
            "<when test='categoryCode == \"WATER_CONTENT\"'>" +
            "AND water_content_percent IS NOT NULL " +
            "</when>" +
            "<when test='categoryCode == \"FLASH_POINT\"'>" +
            "AND flash_point_celsius IS NOT NULL " +
            "</when>" +
            "<when test='categoryCode == \"HEAVY_METALS\"'>" +
            "AND (fe_mg_per_l IS NOT NULL OR cu_mg_per_l IS NOT NULL OR pb_mg_per_l IS NOT NULL OR " +
            "cd_mg_per_l IS NOT NULL OR cr_mg_per_l IS NOT NULL OR ni_mg_per_l IS NOT NULL OR " +
            "mn_mg_per_l IS NOT NULL OR sn_mg_per_l IS NOT NULL OR as_mg_per_l IS NOT NULL OR " +
            "co_mg_per_l IS NOT NULL OR sb_mg_per_l IS NOT NULL OR tl_mg_per_l IS NOT NULL) " +
            "</when>" +
            "<when test='categoryCode == \"ALKALI_METALS\"'>" +
            "AND (k_mg_per_l IS NOT NULL OR na_mg_per_l IS NOT NULL OR mg_mg_per_l IS NOT NULL) " +
            "</when>" +
            "</choose>" +
            "<if test='search != null and search != \"\"'>" +
            "AND (waste_code LIKE CONCAT('%', #{search}, '%') OR waste_name LIKE CONCAT('%', #{search}, '%')) " +
            "</if>" +
            "</script>")
    Long countByPhysicalPropertyCategory(@Param("categoryCode") String categoryCode, @Param("search") String search);
} 