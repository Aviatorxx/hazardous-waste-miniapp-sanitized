package org.gsu.hwtttt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.gsu.hwtttt.dto.request.PropertyFilterRequest;
import org.gsu.hwtttt.dto.request.PropertyImportRequest;
import org.gsu.hwtttt.entity.PhysicalProperty;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Module 2: 理化性质服务接口
 * 支持各种理化性质子模块的查询功能
 *
 * @author WenXin
 * @date 2025/06/10
 */
public interface PhysicalPropertyService extends IService<PhysicalProperty> {

    // ==================== Module 2 新增方法 ====================

    /**
     * 根据关键字和分类搜索理化性质
     *
     * @param keyword 搜索关键字或危废代码
     * @param category 性质分类
     * @return 理化性质列表
     */
    List<PhysicalProperty> searchByKeywordAndCategory(String keyword, String category);

    /**
     * 根据危废ID和分类获取理化性质
     *
     * @param wasteId 危废ID
     * @param category 性质分类
     * @return 理化性质列表
     */
    List<PhysicalProperty> getByWasteIdAndCategory(Long wasteId, String category);

    /**
     * 通用搜索理化性质
     *
     * @param request 搜索请求
     * @return 分页结果
     */
    Page<PhysicalProperty> searchProperties(PropertyFilterRequest request);

    /**
     * 根据危废代码获取理化性质
     *
     * @param wasteCode 危废代码
     * @return 理化性质列表
     */
    List<PhysicalProperty> getByWasteCode(String wasteCode);

    /**
     * 获取所有理化性质分类
     *
     * @return 分类列表
     */
    List<String> getAllCategories();

    /**
     * 根据分类获取理化性质记录
     *
     * @param category 性质分类
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 理化性质列表
     */
    List<PhysicalProperty> getByCategory(String category, Integer pageNum, Integer pageSize);

    /**
     * 获取理化性质统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getStatistics();

    /**
     * 按分类获取统计信息
     *
     * @return 分类统计信息
     */
    Map<String, Object> getStatisticsByCategory();

    // ==================== 原有方法 ====================

    /**
     * 根据危废ID获取理化特性
     *
     * @param wasteId 危废ID
     * @return 理化特性列表
     */
    List<PhysicalProperty> getByWasteId(Long wasteId);

    /**
     * 根据分类代码获取理化特性
     *
     * @param categoryCode 分类代码
     * @return 理化特性列表
     */
    List<PhysicalProperty> getByCategoryCode(String categoryCode);

    /**
     * 根据分类代码分页查询理化特性
     *
     * @param categoryCode 分类代码
     * @param current 当前页
     * @param size 页大小
     * @return 分页结果
     */
    Page<PhysicalProperty> getByCategoryCodePage(String categoryCode, Long current, Long size);

    /**
     * 高级筛选理化特性
     *
     * @param request 筛选条件
     * @return 筛选结果
     */
    Page<PhysicalProperty> filterProperties(PropertyFilterRequest request);

    /**
     * 根据特性值范围筛选危废
     *
     * @param propertyName 特性名称
     * @param minValue 最小值
     * @param maxValue 最大值
     * @return 符合条件的理化特性列表
     */
    List<PhysicalProperty> filterByValueRange(String propertyName, BigDecimal minValue, BigDecimal maxValue);

    /**
     * 获取关键特性数据
     *
     * @param wasteId 危废ID
     * @return 关键特性列表
     */
    List<PhysicalProperty> getKeyProperties(Long wasteId);

    /**
     * 批量导入理化特性数据
     *
     * @param request 导入请求
     * @return 导入结果
     */
    Map<String, Object> importProperties(PropertyImportRequest request);

    /**
     * 导出理化特性数据
     *
     * @param categoryCode 分类代码（可选）
     * @param wasteIds 危废ID列表（可选）
     * @return 导出文件路径
     */
    String exportProperties(String categoryCode, List<Long> wasteIds);

    /**
     * 获取特性值统计信息
     *
     * @param propertyName 特性名称
     * @return 统计信息
     */
    Map<String, Object> getPropertyStatistics(String propertyName);

    /**
     * 搜索相似特性值的危废
     *
     * @param propertyName 特性名称
     * @param targetValue 目标值
     * @param tolerance 容差百分比
     * @return 相似的理化特性列表
     */
    List<PhysicalProperty> searchSimilarProperties(String propertyName, BigDecimal targetValue, BigDecimal tolerance);

    /**
     * 根据检测方法分组查询理化特性
     *
     * @param testMethod 检测方法
     * @return 分组的理化特性数据
     */
    List<PhysicalProperty> getPropertiesByTestMethod(String testMethod);

    /**
     * 根据数据质量标识筛选理化特性
     *
     * @param qualityFlag 数据质量标识（excellent, good, fair, poor）
     * @return 理化特性列表
     */
    List<PhysicalProperty> getByQualityFlag(String qualityFlag);

    /**
     * 获取理化特性质量分布统计
     *
     * @return 质量分布统计数据
     */
    List<PhysicalProperty> getQualityDistribution();

    /**
     * 根据检测实验室统计理化特性数量
     *
     * @return 实验室统计数据
     */
    List<Map<String, Object>> getCountByTestLab();

    /**
     * 获取理化特性完整性报告
     *
     * @param wasteId 危废ID
     * @return 完整性报告
     */
    Map<String, Object> getCompletenessReport(Long wasteId);

    /**
     * 根据置信度阈值筛选理化特性
     *
     * @param minConfidence 最小置信度
     * @return 理化特性列表
     */
    List<PhysicalProperty> getByMinConfidence(BigDecimal minConfidence);

    /**
     * 获取理化特性数据趋势分析
     *
     * @param propertyName 特性名称
     * @param wasteIds 危废ID列表
     * @return 趋势分析数据
     */
    List<Map<String, Object>> getTrendAnalysis(String propertyName, List<Long> wasteIds);

    /**
     * 验证理化特性数据一致性
     *
     * @param wasteId 危废ID
     * @return 一致性检查结果
     */
    List<Map<String, Object>> validateDataConsistency(Long wasteId);

    /**
     * 批量更新理化特性质量标识
     *
     * @param updates 更新数据 Map<ID, 质量标识>
     * @return 更新成功的数量
     */
    int batchUpdateQualityFlag(Map<Long, String> updates);
} 