package org.gsu.hwtttt.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.gsu.hwtttt.dto.request.WasteSearchRequest;
import org.gsu.hwtttt.dto.response.WasteDetailResponse;
import org.gsu.hwtttt.entity.HazardousWaste;
import org.gsu.hwtttt.mapper.HazardousWasteMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 危废主表业务服务接口
 *
 * @author WenXin
 * @date 2025/06/10
 */
public interface HazardousWasteService extends IService<HazardousWaste> {

    /**
     * 分页搜索危废
     *
     * @param request 搜索条件
     * @return 分页结果
     */
    Page<HazardousWaste> searchWaste(WasteSearchRequest request);

    /**
     * 根据关键字搜索危废
     *
     * @param keyword 关键字
     * @return 危废列表
     */
    List<HazardousWaste> searchByKeyword(String keyword);

    /**
     * 根据危废代码精确搜索
     *
     * @param wasteCode 危废代码
     * @return 危废实体Optional
     */
    Optional<HazardousWaste> searchByWasteCodeExact(String wasteCode);

    /**
     * 获取危废详情
     *
     * @param id 危废ID
     * @return 详情响应
     */
    WasteDetailResponse getWasteDetail(Long id);

    /**
     * 更新危废库存
     *
     * @param wasteId 危废ID
     * @param storage 新的库存量
     * @return 是否成功
     */
    boolean updateStorage(Long wasteId, BigDecimal storage);

    /**
     * 批量更新库存
     *
     * @param storageUpdates 库存更新数据 Map<危废ID, 新库存量>
     * @return 成功更新的数量
     */
    int batchUpdateStorage(Map<Long, BigDecimal> storageUpdates);

    /**
     * 获取所有危险特性选项
     *
     * @return 危险特性选项
     */
    Map<String, List<String>> getHazardProperties();

    /**
     * 获取危废统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getStatistics();

    /**
     * 检查库存预警
     *
     * @param threshold 预警阈值
     * @return 低库存危废列表
     */
    List<HazardousWaste> checkStorageWarning(BigDecimal threshold);

    /**
     * 根据危险特性筛选危废
     *
     * @param properties 危险特性条件
     * @return 符合条件的危废列表
     */
    List<HazardousWaste> filterByHazardProperties(Map<String, Boolean> properties);

    // ==================== 新增化学成分相关方法 ====================
    
    /**
     * 根据化学成分含量范围筛选危废
     *
     * @param componentName 成分名称（如 "cl_percent", "f_percent" 等）
     * @param minValue 最小值
     * @param maxValue 最大值
     * @return 符合条件的危废列表
     */
    List<HazardousWaste> filterByChemicalComponent(String componentName, BigDecimal minValue, BigDecimal maxValue);

    /**
     * 根据重金属含量范围筛选危废
     *
     * @param metalName 金属名称（如 "pb_mg_per_l", "cd_mg_per_l" 等）
     * @param minValue 最小值
     * @param maxValue 最大值
     * @return 符合条件的危废列表
     */
    List<HazardousWaste> filterByHeavyMetal(String metalName, BigDecimal minValue, BigDecimal maxValue);

    /**
     * 获取化学成分统计信息
     *
     * @param componentName 成分名称
     * @return 统计信息（平均值、最大值、最小值等）
     */
    Map<String, BigDecimal> getChemicalComponentStatistics(String componentName);

    /**
     * 获取重金属含量分布统计
     *
     * @return 重金属含量分布图数据
     */
    Map<String, Object> getHeavyMetalDistribution();

    // ==================== 数据质量管理方法 ====================
    
    /**
     * 根据数据质量评分筛选危废
     *
     * @param minScore 最小评分
     * @param maxScore 最大评分
     * @return 符合条件的危废列表
     */
    List<HazardousWaste> filterByDataQualityScore(BigDecimal minScore, BigDecimal maxScore);

    /**
     * 更新数据质量评分
     *
     * @param wasteId 危废ID
     * @param score 数据质量评分
     * @return 是否成功
     */
    boolean updateDataQualityScore(Long wasteId, BigDecimal score);

    /**
     * 批量计算数据质量评分
     *
     * @param wasteIds 危废ID列表，为空则计算所有
     * @return 计算成功的数量
     */
    int calculateDataQualityScores(List<Long> wasteIds);

    // ==================== 审核管理方法 ====================
    
    /**
     * 根据审核状态查询危废
     *
     * @param auditStatus 审核状态（pending, approved, rejected）
     * @return 危废列表
     */
    List<HazardousWaste> getByAuditStatus(String auditStatus);

    /**
     * 审核危废数据
     *
     * @param wasteId 危废ID
     * @param auditStatus 审核状态
     * @param auditUser 审核人
     * @param auditNotes 审核备注
     * @return 是否成功
     */
    boolean auditWaste(Long wasteId, String auditStatus, String auditUser, String auditNotes);

    /**
     * 批量审核危废数据
     *
     * @param wasteIds 危废ID列表
     * @param auditStatus 审核状态
     * @param auditUser 审核人
     * @param auditNotes 审核备注
     * @return 审核成功的数量
     */
    int batchAuditWaste(List<Long> wasteIds, String auditStatus, String auditUser, String auditNotes);

    /**
     * 获取审核统计信息
     *
     * @return 审核统计数据
     */
    Map<String, Object> getAuditStatistics();

    // ==================== 综合分析方法 ====================
    
    /**
     * 根据多条件组合筛选危废
     *
     * @param criteria 筛选条件Map
     * @return 符合条件的危废列表
     */
    List<HazardousWaste> filterByMultipleCriteria(Map<String, Object> criteria);

    /**
     * 获取危废相似度分析
     *
     * @param wasteId 基准危废ID
     * @param threshold 相似度阈值
     * @return 相似的危废列表及相似度评分
     */
    Map<Long, BigDecimal> findSimilarWastes(Long wasteId, BigDecimal threshold);

    /**
     * 获取危废风险评估
     *
     * @param wasteId 危废ID
     * @return 风险评估结果
     */
    Map<String, Object> getRiskAssessment(Long wasteId);
} 