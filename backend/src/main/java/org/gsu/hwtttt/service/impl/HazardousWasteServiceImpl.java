package org.gsu.hwtttt.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gsu.hwtttt.common.exception.BusinessException;
import org.gsu.hwtttt.common.result.ResultCode;
import org.gsu.hwtttt.dto.request.WasteSearchRequest;
import org.gsu.hwtttt.dto.response.WasteDetailResponse;
import org.gsu.hwtttt.entity.HazardousWaste;
import org.gsu.hwtttt.entity.PhysicalProperty;
import org.gsu.hwtttt.entity.ThermalProperty;
import org.gsu.hwtttt.mapper.HazardousWasteMapper;
import org.gsu.hwtttt.service.HazardousWasteService;
import org.gsu.hwtttt.service.PhysicalPropertyService;
import org.gsu.hwtttt.service.ThermalPropertyService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 危废主表业务服务实现类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HazardousWasteServiceImpl extends ServiceImpl<HazardousWasteMapper, HazardousWaste> 
        implements HazardousWasteService {

    private final PhysicalPropertyService physicalPropertyService;
    private final ThermalPropertyService thermalPropertyService;

    @Override
    @Cacheable(value = "wasteSearch", key = "#request.toString()", unless = "#result.records.size() == 0")
    public Page<HazardousWaste> searchWaste(WasteSearchRequest request) {
        log.info("分页搜索危废，请求参数: {}", request);
        
        Page<HazardousWaste> page = new Page<>(request.getCurrent(), request.getSize());
        Page<HazardousWaste> result = baseMapper.searchWaste(page, request);

        // Add storage priority calculation for import page (Module 4)
        // NOTE: We DO NOT persist these fields to DB; front-end only needs them for display.
        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            LocalDate today = LocalDate.now();
            for (HazardousWaste waste : result.getRecords()) {
                // inbound_time is stored as java.util.Date in entity
                if (waste != null && waste.getInboundTime() != null) {
                    try {
                        LocalDate inboundDate = waste.getInboundTime().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                        long days = ChronoUnit.DAYS.between(inboundDate, today);
                        int storageDays = (int) Math.max(days, 0);
                        // This method returns HazardousWaste entities directly.
                        // We intentionally do NOT mutate entity fields here to avoid unexpected side effects.
                    } catch (Exception ignored) {
                        // ignore conversion issues
                    }
                }
            }
        }

        return result;
    }

    @Override
    @Cacheable(value = "wasteKeyword", key = "#keyword", unless = "#result.size() == 0")
    public List<HazardousWaste> searchByKeyword(String keyword) {
        log.info("关键字搜索危废: {}", keyword);
        
        if (StrUtil.isBlank(keyword)) {
            return Collections.emptyList();
        }
        
        return baseMapper.searchByKeyword(keyword);
    }

    @Override
    @Cacheable(value = "wasteCodeExact", key = "#wasteCode")
    public Optional<HazardousWaste> searchByWasteCodeExact(String wasteCode) {
        log.info("精确搜索危废代码: {}", wasteCode);
        if (StrUtil.isBlank(wasteCode)) {
            return Optional.empty();
        }
        LambdaQueryWrapper<HazardousWaste> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HazardousWaste::getWasteCode, wasteCode);
        return Optional.ofNullable(getOne(wrapper));
    }

    @Override
    @Cacheable(value = "wasteDetail", key = "#id")
    public WasteDetailResponse getWasteDetail(Long id) {
        log.info("获取危废详情, ID: {}", id);
        
        // 获取危废基本信息
        HazardousWaste waste = getById(id);
        if (waste == null) {
            throw new BusinessException(ResultCode.WASTE_NOT_FOUND);
        }

        WasteDetailResponse response = new WasteDetailResponse();
        response.setWasteInfo(waste);

        // 获取理化特性
        List<PhysicalProperty> physicalProperties = physicalPropertyService.getByWasteId(id);
        response.setPhysicalProperties(physicalProperties);

        // 获取热力学特性
        List<ThermalProperty> thermalProperties = thermalPropertyService.getByWasteId(id);
        response.setThermalProperties(thermalProperties);

        // TODO: 获取相容性信息
        response.setCompatibilityInfos(new ArrayList<>());

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"wasteSearch", "wasteDetail", "wasteStatistics"}, allEntries = true)
    public boolean updateStorage(Long wasteId, BigDecimal storage) {
        log.info("更新危废库存, ID: {}, 新库存: {}", wasteId, storage);
        
        if (storage.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "库存量不能为负数");
        }

        HazardousWaste waste = getById(wasteId);
        if (waste == null) {
            throw new BusinessException(ResultCode.WASTE_NOT_FOUND);
        }

        int result = baseMapper.updateStorage(wasteId, storage);
        if (result > 0) {
            log.info("库存更新成功, 危废ID: {}, 原库存: {}, 新库存: {}", 
                    wasteId, waste.getRemainingStorage(), storage);
        }
        
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"wasteSearch", "wasteDetail", "wasteStatistics"}, allEntries = true)
    public int batchUpdateStorage(Map<Long, BigDecimal> storageUpdates) {
        log.info("批量更新库存, 更新数量: {}", storageUpdates.size());
        
        int successCount = 0;
        for (Map.Entry<Long, BigDecimal> entry : storageUpdates.entrySet()) {
            try {
                if (updateStorage(entry.getKey(), entry.getValue())) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("更新库存失败, 危废ID: {}, 错误: {}", entry.getKey(), e.getMessage());
            }
        }
        
        log.info("批量更新库存完成, 成功: {}, 总数: {}", successCount, storageUpdates.size());
        return successCount;
    }

    @Override
    @Cacheable(value = "hazardProperties")
    public Map<String, List<String>> getHazardProperties() {
        log.info("获取所有危险特性选项");
        
        Map<String, List<String>> properties = new HashMap<>();
        
        // 危险特性布尔选项
        List<String> booleanProperties = Arrays.asList(
            "氧化性", "还原性", "挥发性", "易燃性", "毒性", 
            "反应性", "感染性", "腐蚀性", "卤化烃类", "含氰化物废物"
        );
        properties.put("危险特性", booleanProperties);
        
        // 物理状态选项
        List<String> physicalStates = Arrays.asList("固体", "液体", "气体", "混合物");
        properties.put("物理状态", physicalStates);
        
        // 审核状态选项
        List<String> auditStatuses = Arrays.asList("pending", "approved", "rejected");
        properties.put("审核状态", auditStatuses);
        
        return properties;
    }

    @Override
    @Cacheable(value = "wasteStatistics")
    public Map<String, Object> getStatistics() {
        log.info("获取危废统计信息");
        
        Map<String, Object> statistics = new HashMap<>();
        
        // 总数统计
        long totalCount = count();
        statistics.put("totalCount", totalCount);
        
        List<HazardousWaste> allWastes = list();

        // 审核状态统计
        Map<String, Long> auditStatusCount = allWastes.stream()
                .collect(Collectors.groupingBy(
                        waste -> Optional.ofNullable(waste.getAuditStatus()).orElse("pending"),
                        Collectors.counting()
                ));
        statistics.put("auditStatusCount", auditStatusCount);

        // 危险特性统计
        Map<String, Long> hazardCount = new HashMap<>();
        allWastes.forEach(waste -> {
            if (Boolean.TRUE.equals(waste.getOxidizing())) hazardCount.merge("氧化性", 1L, Long::sum);
            if (Boolean.TRUE.equals(waste.getReducing())) hazardCount.merge("还原性", 1L, Long::sum);
            if (Boolean.TRUE.equals(waste.getVolatileProperty())) hazardCount.merge("挥发性", 1L, Long::sum);
            if (Boolean.TRUE.equals(waste.getFlammable())) hazardCount.merge("易燃性", 1L, Long::sum);
            if (Boolean.TRUE.equals(waste.getToxic())) hazardCount.merge("毒性", 1L, Long::sum);
            if (Boolean.TRUE.equals(waste.getReactive())) hazardCount.merge("反应性", 1L, Long::sum);
            if (Boolean.TRUE.equals(waste.getInfectious())) hazardCount.merge("感染性", 1L, Long::sum);
            if (Boolean.TRUE.equals(waste.getCorrosive())) hazardCount.merge("腐蚀性", 1L, Long::sum);
        });
        statistics.put("hazardCount", hazardCount);
        
        // 库存统计
        BigDecimal totalStorage = allWastes.stream()
                .map(HazardousWaste::getRemainingStorage)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statistics.put("totalStorage", totalStorage);
        
        // 低库存预警数量 (小于100kg)
        long lowStorageCount = allWastes.stream()
                .filter(w -> w.getRemainingStorage() != null &&
                           w.getRemainingStorage().compareTo(new BigDecimal("100")) < 0)
                .count();
        statistics.put("lowStorageCount", lowStorageCount);
        
        return statistics;
    }

    @Override
    public List<HazardousWaste> checkStorageWarning(BigDecimal threshold) {
        log.info("检查库存预警, 阈值: {}", threshold);
        
        LambdaQueryWrapper<HazardousWaste> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(HazardousWaste::getRemainingStorage, threshold)
               .orderByAsc(HazardousWaste::getRemainingStorage);
        
        return list(wrapper);
    }

    @Override
    public List<HazardousWaste> filterByHazardProperties(Map<String, Boolean> properties) {
        log.info("根据危险特性筛选危废: {}", properties);
        
        LambdaQueryWrapper<HazardousWaste> wrapper = new LambdaQueryWrapper<>();
        
        properties.forEach((property, value) -> {
            if (value != null) {
                switch (property) {
                    case "oxidizing":
                        wrapper.eq(HazardousWaste::getOxidizing, value);
                        break;
                    case "reducing":
                        wrapper.eq(HazardousWaste::getReducing, value);
                        break;
                    case "volatile":
                        wrapper.eq(HazardousWaste::getVolatileProperty, value);
                        break;
                    case "flammable":
                        wrapper.eq(HazardousWaste::getFlammable, value);
                        break;
                    case "toxic":
                        wrapper.eq(HazardousWaste::getToxic, value);
                        break;
                    case "reactive":
                        wrapper.eq(HazardousWaste::getReactive, value);
                        break;
                    case "infectious":
                        wrapper.eq(HazardousWaste::getInfectious, value);
                        break;
                    case "corrosive":
                        wrapper.eq(HazardousWaste::getCorrosive, value);
                        break;
                    case "halogenatedHydrocarbon":
                        wrapper.eq(HazardousWaste::getHalogenatedHydrocarbon, value);
                        break;
                    case "cyanideContaining":
                        wrapper.eq(HazardousWaste::getCyanideContaining, value);
                        break;
                    default:
                        log.warn("未知的危险特性: {}", property);
                }
            }
        });
        
        return list(wrapper);
    }

    // ==================== 化学成分相关方法实现 ====================
    
    @Override
    @Cacheable(value = "chemicalComponentFilter", key = "#componentName + '_' + #minValue + '_' + #maxValue")
    public List<HazardousWaste> filterByChemicalComponent(String componentName, BigDecimal minValue, BigDecimal maxValue) {
        log.info("根据化学成分筛选危废: 成分={}, 范围=[{}, {}]", componentName, minValue, maxValue);
        
        // 验证字段名是否为有效的化学成分字段
        if (!isValidChemicalComponentField(componentName)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无效的化学成分字段名: " + componentName);
        }
        
        return baseMapper.selectByChemicalComponentRange(componentName, minValue, maxValue);
    }

    @Override
    @Cacheable(value = "heavyMetalFilter", key = "#metalName + '_' + #minValue + '_' + #maxValue")
    public List<HazardousWaste> filterByHeavyMetal(String metalName, BigDecimal minValue, BigDecimal maxValue) {
        log.info("根据重金属含量筛选危废: 金属={}, 范围=[{}, {}]", metalName, minValue, maxValue);
        
        // 验证字段名是否为有效的重金属字段
        if (!isValidHeavyMetalField(metalName)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无效的重金属字段名: " + metalName);
        }
        
        return baseMapper.selectByHeavyMetalRange(metalName, minValue, maxValue);
    }

    @Override
    @Cacheable(value = "componentStatistics", key = "#componentName")
    public Map<String, BigDecimal> getChemicalComponentStatistics(String componentName) {
        log.info("获取化学成分统计信息: {}", componentName);
        
        if (!isValidChemicalComponentField(componentName)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无效的化学成分字段名: " + componentName);
        }
        
        HazardousWasteMapper.ComponentStatistics stats = baseMapper.getChemicalComponentStatistics(componentName);
        
        Map<String, BigDecimal> result = new HashMap<>();
        if (stats != null) {
            result.put("avgValue", stats.getAvgValue());
            result.put("minValue", stats.getMinValue());
            result.put("maxValue", stats.getMaxValue());
            result.put("stdDev", stats.getStdDev());
            result.put("sampleCount", new BigDecimal(stats.getSampleCount()));
        }
        
        return result;
    }

    @Override
    @Cacheable(value = "heavyMetalDistribution")
    public Map<String, Object> getHeavyMetalDistribution() {
        log.info("获取重金属含量分布统计");
        
        List<HazardousWasteMapper.MetalDistribution> distributions = baseMapper.getHeavyMetalDistribution();
        
        Map<String, Object> result = new HashMap<>();
        result.put("distributions", distributions);
        result.put("totalSamples", distributions.stream().mapToInt(d -> d.getWasteCount()).sum());
        
        return result;
    }

    // ==================== 数据质量管理方法实现 ====================
    
    @Override
    @Cacheable(value = "qualityScoreFilter", key = "#minScore + '_' + #maxScore")
    public List<HazardousWaste> filterByDataQualityScore(BigDecimal minScore, BigDecimal maxScore) {
        log.info("根据数据质量评分筛选危废: 范围=[{}, {}]", minScore, maxScore);
        
        if (minScore.compareTo(BigDecimal.ZERO) < 0 || maxScore.compareTo(BigDecimal.ONE) > 0 || 
            minScore.compareTo(maxScore) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "数据质量评分范围应在0-1之间，且最小值不能大于最大值");
        }
        
        return baseMapper.selectByDataQualityScore(minScore, maxScore);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"wasteDetail", "qualityScoreFilter", "wasteStatistics"}, allEntries = true)
    public boolean updateDataQualityScore(Long wasteId, BigDecimal score) {
        log.info("更新数据质量评分: 危废ID={}, 评分={}", wasteId, score);
        
        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(BigDecimal.ONE) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "数据质量评分应在0-1之间");
        }
        
        HazardousWaste waste = getById(wasteId);
        if (waste == null) {
            throw new BusinessException(ResultCode.WASTE_NOT_FOUND);
        }
        
        int result = baseMapper.updateDataQualityScore(wasteId, score);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"wasteDetail", "qualityScoreFilter", "wasteStatistics"}, allEntries = true)
    public int calculateDataQualityScores(List<Long> wasteIds) {
        log.info("批量计算数据质量评分: 数量={}", wasteIds != null ? wasteIds.size() : "全部");
        
        List<HazardousWaste> wastes = baseMapper.selectForQualityCalculation(wasteIds);
        int successCount = 0;
        
        for (HazardousWaste waste : wastes) {
            try {
                BigDecimal score = calculateQualityScore(waste);
                if (updateDataQualityScore(waste.getId(), score)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("计算数据质量评分失败, 危废ID: {}, 错误: {}", waste.getId(), e.getMessage());
            }
        }
        
        log.info("批量计算数据质量评分完成, 成功: {}, 总数: {}", successCount, wastes.size());
        return successCount;
    }

    // ==================== 审核管理方法实现 ====================
    
    @Override
    @Cacheable(value = "auditStatusWastes", key = "#auditStatus")
    public List<HazardousWaste> getByAuditStatus(String auditStatus) {
        log.info("根据审核状态查询危废: {}", auditStatus);
        
        if (!Arrays.asList("pending", "approved", "rejected").contains(auditStatus)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无效的审核状态: " + auditStatus);
        }
        
        return baseMapper.selectByAuditStatus(auditStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"wasteDetail", "auditStatusWastes", "auditStatistics"}, allEntries = true)
    public boolean auditWaste(Long wasteId, String auditStatus, String auditUser, String auditNotes) {
        log.info("审核危废数据: ID={}, 状态={}, 审核人={}", wasteId, auditStatus, auditUser);
        
        if (!Arrays.asList("pending", "approved", "rejected").contains(auditStatus)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无效的审核状态: " + auditStatus);
        }
        
        HazardousWaste waste = getById(wasteId);
        if (waste == null) {
            throw new BusinessException(ResultCode.WASTE_NOT_FOUND);
        }
        
        int result = baseMapper.updateAuditInfo(wasteId, auditStatus, auditUser, auditNotes, LocalDateTime.now());
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = {"wasteDetail", "auditStatusWastes", "auditStatistics"}, allEntries = true)
    public int batchAuditWaste(List<Long> wasteIds, String auditStatus, String auditUser, String auditNotes) {
        log.info("批量审核危废数据: 数量={}, 状态={}, 审核人={}", wasteIds.size(), auditStatus, auditUser);
        
        if (!Arrays.asList("pending", "approved", "rejected").contains(auditStatus)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "无效的审核状态: " + auditStatus);
        }
        
        int successCount = 0;
        for (Long wasteId : wasteIds) {
            try {
                if (auditWaste(wasteId, auditStatus, auditUser, auditNotes)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("审核失败, 危废ID: {}, 错误: {}", wasteId, e.getMessage());
            }
        }
        
        log.info("批量审核完成, 成功: {}, 总数: {}", successCount, wasteIds.size());
        return successCount;
    }

    @Override
    @Cacheable(value = "auditStatistics")
    public Map<String, Object> getAuditStatistics() {
        log.info("获取审核统计信息");
        
        List<HazardousWasteMapper.AuditStatistics> auditStats = baseMapper.getAuditStatistics();
        
        Map<String, Object> result = new HashMap<>();
        result.put("auditStats", auditStats);
        
        // 计算总数和百分比
        int totalCount = auditStats.stream().mapToInt(s -> s.getCount()).sum();
        result.put("totalCount", totalCount);
        
        return result;
    }

    // ==================== 综合分析方法实现 ====================
    
    @Override
    public List<HazardousWaste> filterByMultipleCriteria(Map<String, Object> criteria) {
        log.info("根据多条件筛选危废: {}", criteria);
        
        return baseMapper.selectByMultipleCriteria(criteria);
    }

    @Override
    @Cacheable(value = "similarWastes", key = "#wasteId + '_' + #threshold")
    public Map<Long, BigDecimal> findSimilarWastes(Long wasteId, BigDecimal threshold) {
        log.info("查找相似危废: 基准ID={}, 阈值={}", wasteId, threshold);
        
        HazardousWaste referenceWaste = getById(wasteId);
        if (referenceWaste == null) {
            throw new BusinessException(ResultCode.WASTE_NOT_FOUND);
        }
        
        List<HazardousWasteMapper.WasteSimilarity> similarities = 
            baseMapper.findSimilarWastes(referenceWaste, threshold);
        
        Map<Long, BigDecimal> result = new HashMap<>();
        similarities.forEach(sim -> result.put(sim.getWasteId(), sim.getSimilarity()));
        
        return result;
    }

    @Override
    @Cacheable(value = "riskAssessment", key = "#wasteId")
    public Map<String, Object> getRiskAssessment(Long wasteId) {
        log.info("获取危废风险评估: ID={}", wasteId);
        
        HazardousWasteMapper.RiskAssessmentData riskData = baseMapper.getRiskAssessmentData(wasteId);
        
        Map<String, Object> result = new HashMap<>();
        if (riskData != null) {
            result.put("wasteId", riskData.getWasteId());
            result.put("riskScore", riskData.getRiskScore());
            result.put("riskLevel", riskData.getRiskLevel());
            result.put("riskFactors", riskData.getRiskFactors());
        }
        
        return result;
    }

    // ==================== 私有辅助方法 ====================
    
    /**
     * 验证是否为有效的化学成分字段
     */
    private boolean isValidChemicalComponentField(String fieldName) {
        List<String> validFields = Arrays.asList(
            "cl_percent", "f_percent", "c_percent", "h_percent", 
            "o_percent", "s_percent", "n_percent", "p_percent"
        );
        return validFields.contains(fieldName);
    }

    /**
     * 验证是否为有效的重金属字段
     */
    private boolean isValidHeavyMetalField(String fieldName) {
        List<String> validFields = Arrays.asList(
            "k_mg_per_l", "na_mg_per_l", "mg_mg_per_l", "mn_mg_per_l",
            "cu_mg_per_l", "cr_mg_per_l", "ni_mg_per_l", "pb_mg_per_l",
            "cd_mg_per_l", "sn_mg_per_l", "tl_mg_per_l", "sb_mg_per_l",
            "co_mg_per_l", "as_mg_per_l", "fe_mg_per_l"
        );
        return validFields.contains(fieldName);
    }

    /**
     * 计算数据质量评分
     */
    private BigDecimal calculateQualityScore(HazardousWaste waste) {
        // 数据质量评分算法
        BigDecimal score = BigDecimal.ZERO;
        int totalFields = 0;
        int completedFields = 0;
        
        // 检查基础字段完整性
        if (waste.getWasteCode() != null && !waste.getWasteCode().trim().isEmpty()) completedFields++;
        totalFields++;
        
        if (waste.getWasteName() != null && !waste.getWasteName().trim().isEmpty()) completedFields++;
        totalFields++;
        
        if (waste.getSourceUnit() != null && !waste.getSourceUnit().trim().isEmpty()) completedFields++;
        totalFields++;
        
        // 检查化学成分完整性
        BigDecimal[] chemicalComponents = {
            waste.getClPercent(), waste.getFPercent(), waste.getCPercent(),
            waste.getHPercent(), waste.getOPercent(), waste.getSPercent()
        };
        
        for (BigDecimal component : chemicalComponents) {
            totalFields++;
            if (component != null) completedFields++;
        }
        
        // 检查重金属含量完整性
        BigDecimal[] heavyMetals = {
            waste.getPbMgPerL(), waste.getCdMgPerL(), waste.getCrMgPerL(),
            waste.getNiMgPerL(), waste.getCuMgPerL(), waste.getAsMgPerL()
        };
        
        for (BigDecimal metal : heavyMetals) {
            totalFields++;
            if (metal != null) completedFields++;
        }
        
        // 计算完整性得分
        if (totalFields > 0) {
            score = new BigDecimal(completedFields).divide(new BigDecimal(totalFields), 2, BigDecimal.ROUND_HALF_UP);
        }
        
        return score;
    }
} 