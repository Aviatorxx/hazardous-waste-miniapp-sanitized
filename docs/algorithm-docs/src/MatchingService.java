package org.gsu.hwtttt.service;

import org.gsu.hwtttt.common.result.Result;
import org.gsu.hwtttt.dto.request.MatchingRequest;
import org.gsu.hwtttt.dto.response.MatchingResponse;
import org.gsu.hwtttt.entity.MatchingResults;
import org.gsu.hwtttt.entity.MatchingSessions;
import org.gsu.hwtttt.entity.MatchingDetails;
import org.gsu.hwtttt.entity.CompatibilityChecks;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Module 4: 配伍算法核心Service接口
 * 支持兼容性模拟全流程
 *
 * @author WenXin
 * @date 2025年
 */
public interface MatchingService {

    /**
     * 执行配伍计算
     *
     * @param sessionId 会话ID
     * @return 配伍结果
     */
    MatchingResponse executeMatching(Long sessionId);

    /**
     * 相容性检查
     *
     * @param sessionId 会话ID
     * @return 检查结果
     */
    boolean checkCompatibility(Long sessionId);

    /**
     * 约束验证
     *
     * @param sessionId 会话ID
     * @return 验证结果
     */
    boolean validateConstraints(Long sessionId);

    /**
     * 添加危废到配伍会话
     *
     * @param sessionId 会话ID
     * @param wasteId 危废ID
     * @param quantity 用量
     * @return 添加结果详情
     */
    Result<Map<String, Object>> addWasteToSession(Long sessionId, Long wasteId, Double quantity);

    /**
     * 从配伍会话中移除危废
     *
     * @param sessionId 会话ID
     * @param wasteId 危废ID
     * @return 是否成功
     */
    boolean removeWasteFromSession(Long sessionId, Long wasteId);

    /**
     * 更新危废用量
     *
     * @param sessionId 会话ID
     * @param wasteId 危废ID
     * @param quantity 新用量
     * @return 是否成功
     */
    boolean updateWasteQuantity(Long sessionId, Long wasteId, Double quantity);

    /**
     * 获取配伍会话详情 (仅从数据库读取已存储的结果,不触发新计算)
     *
     * @param sessionId 会话ID
     * @return 会话详情
     */
    MatchingResponse getSessionDetails(Long sessionId);

    /**
     * 获取已存储的计算结果 (仅查询数据库,不执行任何计算)
     * 
     * @param sessionId 会话ID
     * @return 已存储的计算结果
     */
    MatchingResponse getStoredCalculationResults(Long sessionId);

    /**
     * 重新计算配伍
     *
     * @param sessionId 会话ID
     * @return 重新计算结果
     */
    MatchingResponse recalculateMatching(Long sessionId);

    /**
     * 1. Import Waste - 导入危废到配伍会话
     *
     * @param sessionId 会话ID
     * @param wasteId 危废ID
     * @param plannedAmount 计划用量
     * @return 导入结果
     */
    Map<String, Object> importWasteToSession(Long sessionId, Long wasteId, Double plannedAmount);

    /**
     * 2. Compatibility Check - 兼容性检查
     *
     * @param sessionId 会话ID
     * @return 兼容性检查结果
     */
    Map<String, Object> performCompatibilityCheck(Long sessionId);

    /**
     * 3. Matching - 如果兼容则执行配伍计算
     *
     * @param sessionId 会话ID
     * @return 配伍计算结果
     */
    Map<String, Object> performMatching(Long sessionId);

    /**
     * 4. View Result - 查看配伍结果历史
     *
     * @param sessionId 会话ID
     * @return 配伍结果历史及统计
     */
    Map<String, Object> getMatchingHistory(Long sessionId);

    /**
     * 获取配伍约束检查结果
     *
     * @param sessionId 会话ID
     * @return 约束检查详情
     */
    Map<String, Object> getConstraintCheckResults(Long sessionId);

    /**
     * 获取风险评估结果
     *
     * @param sessionId 会话ID
     * @return 风险评估详情
     */
    Map<String, Object> getRiskAssessment(Long sessionId);

    /**
     * 获取配伍失败原因分析
     *
     * @param sessionId 会话ID
     * @return 失败原因详情
     */
    Map<String, Object> getFailureAnalysis(Long sessionId);

    /**
     * 获取配伍建议和优化方案
     *
     * @param sessionId 会话ID
     * @return 建议和优化方案
     */
    Map<String, Object> getOptimizationSuggestions(Long sessionId);

    /**
     * 计算加权平均值
     *
     * @param sessionId 会话ID
     * @return 加权平均值结果
     */
    Map<String, Object> calculateWeightedAverages(Long sessionId);

    /**
     * 搜索可用危废
     *
     * @param keyword 搜索关键字
     * @return 可用危废列表
     */
    List<Map<String, Object>> searchAvailableWastes(String keyword);

    /**
     * 检查危废库存
     *
     * @param wasteId 危废ID
     * @param requiredAmount 需要用量
     * @return 库存检查结果
     */
    Map<String, Object> checkWasteStock(Long wasteId, BigDecimal requiredAmount);

    /**
     * 获取会话中的危废列表
     *
     * @param sessionId 会话ID
     * @return 危废详情列表
     */
    List<MatchingDetails> getSessionWastes(Long sessionId);

    /**
     * 获取相容性矩阵
     *
     * @param sessionId 会话ID
     * @return 相容性矩阵
     */
    List<List<Map<String, Object>>> getCompatibilityMatrix(Long sessionId);

    /**
     * 获取相容性检查结果列表
     *
     * @param sessionId 会话ID
     * @return 检查结果列表
     */
    List<CompatibilityChecks> getCompatibilityCheckResults(Long sessionId);

    /**
     * 获取计算指标
     *
     * @param sessionId 会话ID
     * @return 计算指标
     */
    Map<String, Object> getCalculatedProperties(Long sessionId);

    /**
     * 获取混配比例
     *
     * @param sessionId 会话ID
     * @return 混配比例数据
     */
    Map<String, Object> getMixingRatios(Long sessionId);

    /**
     * 获取相容性检查历史
     *
     * @param sessionId 会话ID
     * @return 检查历史列表
     */
    List<CompatibilityChecks> getCompatibilityHistory(Long sessionId);

    /**
     * 获取成功率统计
     *
     * @param days 统计天数
     * @return 成功率统计数据
     */
    Map<String, Object> getSuccessStatistics(Integer days);

    /**
     * 获取失败原因统计
     *
     * @param days 统计天数
     * @return 失败原因统计
     */
    Map<String, Object> getFailureReasons(Integer days);

    /**
     * 获取会话进度
     *
     * @param sessionId 会话ID
     * @return 会话进度信息
     */
    Map<String, Object> getSessionProgress(Long sessionId);

    /**
     * 获取所有相容性分类
     *
     * @return 分类列表
     */
    List<org.gsu.hwtttt.entity.CompatibilityCategory> getCompatibilityCategories();

    /**
     * 检查指定危废列表的相容性
     *
     * @param wasteIds 危废ID列表
     * @return 相容性检查结果
     */
    Map<String, Object> checkWasteCompatibility(List<Long> wasteIds);

    /**
     * 获取相容性分析结果
     *
     * @param sessionId 会话ID
     * @return 分析结果
     */
    Map<String, Object> getCompatibilityAnalysis(Long sessionId);

    /**
     * 获取配伍约束参数
     *
     * @return 约束参数列表
     */
    List<Map<String, Object>> getMatchingConstraints();

    /**
     * 验证会话中的危废数量和约束
     *
     * @param sessionId 会话ID
     * @return 验证结果
     */
    Map<String, Object> validateSessionWastes(Long sessionId);

    /**
     * 获取兼容性检查结果
     *
     * @param sessionId 会话ID
     * @return 检查结果详情
     */
    Map<String, Object> getCompatibilityCheckResult(Long sessionId);

    /**
     * 导出会话结果
     *
     * @param sessionId 会话ID
     * @param format 导出格式
     * @return 导出数据
     */
    Map<String, Object> exportSessionResults(Long sessionId, String format);

    /**
     * 更新配伍约束参数
     *
     * @param constraints 约束参数列表
     * @return 是否成功
     */
    boolean updateMatchingConstraints(List<Map<String, Object>> constraints);

    /**
     * 获取完整的40×40相容性矩阵
     *
     * @return 相容性矩阵
     */
    List<List<Map<String, Object>>> getFullCompatibilityMatrix();

    /**
     * 获取会话综合摘要
     * 提供包括危废详情、数量、相容性状态和计算准备情况的全面会话概览
     *
     * @param sessionId 会话ID
     * @return 会话摘要响应
     */
    org.gsu.hwtttt.dto.response.SessionSummaryResponse getSessionSummary(Long sessionId);

    /**
     * 诊断相容性矩阵问题
     * 用于识别和记录可能导致TooManyResultsException的重复规则
     *
     * @return 诊断结果
     */
    Map<String, Object> diagnoseCompatibilityMatrix();
} 