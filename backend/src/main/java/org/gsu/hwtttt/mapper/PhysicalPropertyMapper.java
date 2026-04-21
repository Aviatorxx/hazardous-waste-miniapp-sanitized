package org.gsu.hwtttt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.gsu.hwtttt.entity.PhysicalProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 理化特性详细数据表Mapper接口
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Mapper
public interface PhysicalPropertyMapper extends BaseMapper<PhysicalProperty> {

    /**
     * 根据危废ID和分类代码查询理化特性
     *
     * @param wasteId 危废ID
     * @param categoryCode 分类代码
     * @return 理化特性列表
     */
    @Select("<script>" +
            "SELECT * FROM physical_properties WHERE waste_id = #{wasteId} " +
            "<if test='categoryCode != null and categoryCode != \"\"'>" +
            "AND category_code = #{categoryCode} " +
            "</if>" +
            "ORDER BY sort_order, create_time" +
            "</script>")
    List<PhysicalProperty> selectByWasteIdAndCategory(@Param("wasteId") Long wasteId, 
                                                      @Param("categoryCode") String categoryCode);

    /**
     * 根据危废ID查询所有理化特性
     *
     * @param wasteId 危废ID
     * @return 理化特性列表
     */
    @Select("SELECT * FROM physical_properties WHERE waste_id = #{wasteId} ORDER BY category_code, sort_order, create_time")
    List<PhysicalProperty> selectByWasteId(@Param("wasteId") Long wasteId);

    /**
     * 根据分类代码查询理化特性
     *
     * @param categoryCode 分类代码
     * @return 理化特性列表
     */
    @Select("SELECT * FROM physical_properties WHERE category_code = #{categoryCode} ORDER BY waste_id, sort_order")
    List<PhysicalProperty> selectByCategoryCode(@Param("categoryCode") String categoryCode);

    /**
     * 根据属性名称搜索
     *
     * @param propertyName 属性名称
     * @return 理化特性列表
     */
    @Select("SELECT * FROM physical_properties WHERE property_name LIKE CONCAT('%', #{propertyName}, '%') ORDER BY property_name, create_time DESC")
    List<PhysicalProperty> searchByPropertyName(@Param("propertyName") String propertyName);

    /**
     * 获取关键属性列表
     *
     * @param wasteId 危废ID
     * @return 关键属性列表
     */
    @Select("SELECT * FROM physical_properties WHERE waste_id = #{wasteId} " +
            "AND property_name IN ('pH', 'heat_value', 'water_content', 'ash_content', 'flash_point', 'sulfur_content') " +
            "ORDER BY sort_order, create_time")
    List<PhysicalProperty> selectKeyPropertiesByWasteId(@Param("wasteId") Long wasteId);

    /**
     * 根据检测方法查询理化特性
     *
     * @param testMethod 检测方法
     * @return 理化特性列表
     */
    @Select("SELECT * FROM physical_properties WHERE test_method = #{testMethod} ORDER BY create_time DESC")
    List<PhysicalProperty> selectByTestMethod(@Param("testMethod") String testMethod);

    /**
     * 根据数据质量标识查询理化特性
     *
     * @param qualityFlag 数据质量标识
     * @return 理化特性列表
     */
    @Select("SELECT * FROM physical_properties WHERE quality_flag = #{qualityFlag}")
    List<PhysicalProperty> selectByQualityFlag(@Param("qualityFlag") String qualityFlag);

    /**
     * 根据置信度阈值查询理化特性
     *
     * @param minConfidence 最小置信度
     * @return 理化特性列表
     */
    @Select("SELECT * FROM physical_properties WHERE confidence_level >= #{minConfidence}")
    List<PhysicalProperty> selectByMinConfidence(@Param("minConfidence") BigDecimal minConfidence);

    /**
     * 批量更新质量标识
     *
     * @param propertyId 理化特性ID
     * @param qualityFlag 质量标识
     * @return 更新条数
     */
    @Update("UPDATE physical_properties SET quality_flag = #{qualityFlag}, update_time = NOW() WHERE id = #{propertyId}")
    int updateQualityFlag(@Param("propertyId") Long propertyId, @Param("qualityFlag") String qualityFlag);

    /**
     * 根据属性值范围查询理化特性
     *
     * @param propertyName 属性名称
     * @param minValue 最小值
     * @param maxValue 最大值
     * @return 理化特性列表
     */
    @Select("SELECT * FROM physical_properties WHERE property_name = #{propertyName} " +
            "AND CAST(property_value AS DECIMAL(10,2)) BETWEEN #{minValue} AND #{maxValue} " +
            "ORDER BY CAST(property_value AS DECIMAL(10,2))")
    List<PhysicalProperty> selectByPropertyValueRange(@Param("propertyName") String propertyName,
                                                      @Param("minValue") BigDecimal minValue,
                                                      @Param("maxValue") BigDecimal maxValue);

    /**
     * 统计检测实验室数据
     *
     * @return 实验室统计数据
     */
    @Select("SELECT test_lab, COUNT(*) as count FROM physical_properties WHERE test_lab IS NOT NULL GROUP BY test_lab ORDER BY count DESC")
    List<LabStatistics> getLabStatistics();

    /**
     * 获取理化特性质量分布统计
     *
     * @return 质量分布统计
     */
    @Select("SELECT quality_flag, COUNT(*) as count, " +
            "ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM physical_properties), 2) as percentage " +
            "FROM physical_properties " +
            "GROUP BY quality_flag " +
            "ORDER BY count DESC")
    List<QualityDistribution> getQualityDistribution();

    /**
     * 获取理化特性趋势数据
     *
     * @param propertyName 特性名称
     * @param wasteIds 危废ID列表
     * @return 趋势数据
     */
    @Select("<script>" +
            "SELECT waste_id, property_name, property_value, test_date, create_time " +
            "FROM physical_properties " +
            "WHERE property_name = #{propertyName} " +
            "<if test='wasteIds != null and wasteIds.size() > 0'>" +
            "AND waste_id IN " +
            "<foreach collection='wasteIds' item='wasteId' open='(' close=')' separator=','>" +
            "#{wasteId}" +
            "</foreach> " +
            "</if>" +
            "ORDER BY waste_id, test_date, create_time" +
            "</script>")
    List<TrendData> getTrendData(@Param("propertyName") String propertyName,
                                 @Param("wasteIds") List<Long> wasteIds);

    /**
     * 获取数据一致性检查结果
     *
     * @param wasteId 危废ID
     * @return 一致性检查数据
     */
    @Select("SELECT pp1.waste_id, pp1.property_name, " +
            "pp1.property_value as value1, pp2.property_value as value2, " +
            "pp1.test_lab as lab1, pp2.test_lab as lab2, " +
            "ABS(CAST(pp1.property_value AS DECIMAL(10,2)) - CAST(pp2.property_value AS DECIMAL(10,2))) as difference " +
            "FROM physical_properties pp1 " +
            "INNER JOIN physical_properties pp2 ON pp1.waste_id = pp2.waste_id " +
            "AND pp1.property_name = pp2.property_name AND pp1.id != pp2.id " +
            "WHERE pp1.waste_id = #{wasteId} " +
            "AND pp1.property_value REGEXP '^[0-9]+\\.?[0-9]*$' " +
            "AND pp2.property_value REGEXP '^[0-9]+\\.?[0-9]*$' " +
            "HAVING difference > 0 " +
            "ORDER BY difference DESC")
    List<ConsistencyCheck> getConsistencyCheckData(@Param("wasteId") Long wasteId);

    /**
     * 批量插入理化特性
     *
     * @param properties 理化特性列表
     * @return 插入条数
     */
    @Insert("<script>" +
            "INSERT INTO physical_properties (" +
            "waste_id, category_code, property_name, property_value, property_unit, " +
            "property_type, test_method, test_standard, test_date, test_lab, " +
            "confidence_level, data_source, quality_flag, sort_order, remark, " +
            "create_time, update_time" +
            ") VALUES " +
            "<foreach collection='properties' item='property' separator=','>" +
            "(#{property.wasteId}, #{property.categoryCode}, #{property.propertyName}, " +
            "#{property.propertyValue}, #{property.propertyUnit}, #{property.propertyType}, " +
            "#{property.testMethod}, #{property.testStandard}, #{property.testDate}, " +
            "#{property.testLab}, #{property.confidenceLevel}, #{property.dataSource}, " +
            "#{property.qualityFlag}, #{property.sortOrder}, #{property.remark}, " +
            "#{property.createTime}, #{property.updateTime})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("properties") List<PhysicalProperty> properties);

    /**
     * 质量分布统计
     */
    @Data
    class QualityDistribution {
        private String qualityFlag;
        private Integer count;
        private BigDecimal percentage;
    }

    /**
     * 实验室统计
     */
    @Data
    class LabStatistics {
        private String testLab;
        private Long count;
    }

    /**
     * 趋势数据
     */
    @Data
    class TrendData {
        private Long wasteId;
        private String propertyName;
        private String propertyValue;
        private LocalDate testDate;
        private LocalDateTime createTime;
    }

    /**
     * 一致性检查结果
     */
    @Data
    class ConsistencyCheck {
        private Long wasteId;
        private String propertyName;
        private String value1;
        private String value2;
        private String lab1;
        private String lab2;
        private BigDecimal difference;
    }
} 