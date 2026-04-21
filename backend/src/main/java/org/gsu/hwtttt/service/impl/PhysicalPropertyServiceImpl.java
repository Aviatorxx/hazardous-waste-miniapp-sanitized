package org.gsu.hwtttt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gsu.hwtttt.common.exception.BusinessException;
import org.gsu.hwtttt.common.result.ResultCode;
import org.gsu.hwtttt.dto.request.PropertyFilterRequest;
import org.gsu.hwtttt.dto.request.PropertyImportRequest;
import org.gsu.hwtttt.entity.PhysicalProperty;
import org.gsu.hwtttt.mapper.PhysicalPropertyMapper;
import org.gsu.hwtttt.service.PhysicalPropertyService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Module 2: 理化性质服务实现类
 * 支持各种理化性质子模块的查询功能
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhysicalPropertyServiceImpl extends ServiceImpl<PhysicalPropertyMapper, PhysicalProperty> 
        implements PhysicalPropertyService {

    private final PhysicalPropertyMapper physicalPropertyMapper;

    // ==================== Module 2 新增方法实现 ====================

    @Override
    public List<PhysicalProperty> searchByKeywordAndCategory(String keyword, String category) {
        log.info("根据关键字和分类搜索理化性质，关键字: {}, 分类: {}", keyword, category);
        
        if (!StringUtils.hasText(keyword)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "搜索关键字不能为空");
        }
        
        // 使用基本查询构建复杂搜索条件
        LambdaQueryWrapper<PhysicalProperty> wrapper = new LambdaQueryWrapper<>();
        
        // 如果指定了分类，添加分类过滤
        if (StringUtils.hasText(category)) {
            wrapper.eq(PhysicalProperty::getCategoryCode, category);
        }
        
        // 添加关键字搜索条件 - 搜索多个字段
        wrapper.and(w -> w.like(PhysicalProperty::getPropertyName, keyword)
                         .or().like(PhysicalProperty::getPropertyValue, keyword)
                         .or().like(PhysicalProperty::getTestMethod, keyword));
        
        wrapper.orderByDesc(PhysicalProperty::getTestDate);
        
        return list(wrapper);
    }

    @Override
    public List<PhysicalProperty> getByWasteIdAndCategory(Long wasteId, String category) {
        log.info("根据危废ID和分类获取理化性质，危废ID: {}, 分类: {}", wasteId, category);
        
        if (wasteId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "危废ID不能为空");
        }
        
        LambdaQueryWrapper<PhysicalProperty> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhysicalProperty::getWasteId, wasteId);
        
        if (StringUtils.hasText(category)) {
            wrapper.eq(PhysicalProperty::getCategoryCode, category);
        }
        
        wrapper.orderByAsc(PhysicalProperty::getSortOrder)
               .orderByDesc(PhysicalProperty::getTestDate);
        
        return list(wrapper);
    }

    @Override
    public Page<PhysicalProperty> searchProperties(PropertyFilterRequest request) {
        log.info("通用搜索理化性质，请求参数: {}", request);
        
        Page<PhysicalProperty> page = new Page<>(request.getCurrent(), request.getSize());
        QueryWrapper<PhysicalProperty> wrapper = buildFilterWrapper(request);
        
        return page(page, wrapper);
    }

    @Override
    public List<PhysicalProperty> getByWasteCode(String wasteCode) {
        log.info("根据危废代码获取理化性质，危废代码: {}", wasteCode);
        
        if (!StringUtils.hasText(wasteCode)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "危废代码不能为空");
        }
        
        // 通过关联查询获取危废ID，然后查询理化性质
        // 这里使用简化方式，直接通过危废代码搜索相关的理化性质
        LambdaQueryWrapper<PhysicalProperty> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(PhysicalProperty::getPropertyValue, wasteCode)
               .or().like(PhysicalProperty::getPropertyName, wasteCode)
               .or().like(PhysicalProperty::getRemark, wasteCode);
        
        wrapper.orderByDesc(PhysicalProperty::getTestDate);
        
        return list(wrapper);
    }

    @Override
    @Cacheable(value = "propertyCategories")
    public List<String> getAllCategories() {
        log.info("获取所有理化性质分类");
        
        // 使用基础查询获取所有不同的分类代码
        LambdaQueryWrapper<PhysicalProperty> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(PhysicalProperty::getCategoryCode)
               .groupBy(PhysicalProperty::getCategoryCode)
               .orderBy(true, true, PhysicalProperty::getCategoryCode);
        
        List<PhysicalProperty> categories = list(wrapper);
        return categories.stream()
                .map(PhysicalProperty::getCategoryCode)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<PhysicalProperty> getByCategory(String category, Integer pageNum, Integer pageSize) {
        log.info("根据分类获取理化性质记录，分类: {}, 页码: {}, 页大小: {}", category, pageNum, pageSize);
        
        if (!StringUtils.hasText(category)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "性质分类不能为空");
        }
        
        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;
        
        LambdaQueryWrapper<PhysicalProperty> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhysicalProperty::getCategoryCode, category)
               .orderByDesc(PhysicalProperty::getTestDate)
               .last("LIMIT " + offset + ", " + pageSize);
               
        return list(wrapper);
    }

    @Override
    @Cacheable(value = "propertyStatistics")
    public Map<String, Object> getStatistics() {
        log.info("获取理化性质统计信息");
        
        Map<String, Object> stats = new HashMap<>();
        
        // 总记录数
        long totalCount = count();
        stats.put("totalCount", totalCount);
        
        // 按分类统计
        Map<String, Long> categoryStats = getAllCategories().stream()
                .collect(Collectors.toMap(
                    category -> category,
                    category -> count(new LambdaQueryWrapper<PhysicalProperty>()
                            .eq(PhysicalProperty::getCategoryCode, category))
                ));
        stats.put("categoryStats", categoryStats);
        
        // 按质量标识统计
        Map<String, Long> qualityStats = Arrays.asList("excellent", "good", "fair", "poor").stream()
                .collect(Collectors.toMap(
                    quality -> quality,
                    quality -> count(new LambdaQueryWrapper<PhysicalProperty>()
                            .eq(PhysicalProperty::getQualityFlag, quality))
                ));
        stats.put("qualityStats", qualityStats);
        
        // 按实验室统计前5名
        LambdaQueryWrapper<PhysicalProperty> labWrapper = new LambdaQueryWrapper<>();
        labWrapper.select(PhysicalProperty::getTestLab)
                 .groupBy(PhysicalProperty::getTestLab)
                 .orderByDesc(PhysicalProperty::getTestLab)
                 .last("LIMIT 5");
        
        List<PhysicalProperty> labs = list(labWrapper);
        Map<String, Long> labStats = labs.stream()
                .filter(p -> StringUtils.hasText(p.getTestLab()))
                .collect(Collectors.toMap(
                    PhysicalProperty::getTestLab,
                    lab -> count(new LambdaQueryWrapper<PhysicalProperty>()
                            .eq(PhysicalProperty::getTestLab, lab.getTestLab()))
                ));
        stats.put("labStats", labStats);
        
        // 最近更新时间
        PhysicalProperty latest = getOne(new LambdaQueryWrapper<PhysicalProperty>()
                .orderByDesc(PhysicalProperty::getUpdateTime)
                .last("LIMIT 1"));
        stats.put("lastUpdated", latest != null ? latest.getUpdateTime() : null);
        
        return stats;
    }

    @Override
    @Cacheable(value = "propertyStatsByCategory")
    public Map<String, Object> getStatisticsByCategory() {
        log.info("按分类获取理化性质统计信息");
        
        Map<String, Object> result = new HashMap<>();
        
        // 获取所有分类
        List<String> categories = getAllCategories();
        
        for (String category : categories) {
            Map<String, Object> categoryData = new HashMap<>();
            
            // 该分类下的记录数
            long count = count(new LambdaQueryWrapper<PhysicalProperty>()
                    .eq(PhysicalProperty::getCategoryCode, category));
            categoryData.put("count", count);
            
            // 该分类下的质量分布
            Map<String, Long> qualityDist = Arrays.asList("excellent", "good", "fair", "poor").stream()
                    .collect(Collectors.toMap(
                        quality -> quality,
                        quality -> count(new LambdaQueryWrapper<PhysicalProperty>()
                                .eq(PhysicalProperty::getCategoryCode, category)
                                .eq(PhysicalProperty::getQualityFlag, quality))
                    ));
            categoryData.put("qualityDistribution", qualityDist);
            
            // 该分类下的完整性统计
            long totalWithValue = count(new LambdaQueryWrapper<PhysicalProperty>()
                    .eq(PhysicalProperty::getCategoryCode, category)
                    .isNotNull(PhysicalProperty::getPropertyValue));
            long totalWithMethod = count(new LambdaQueryWrapper<PhysicalProperty>()
                    .eq(PhysicalProperty::getCategoryCode, category)
                    .isNotNull(PhysicalProperty::getTestMethod));
            
            Map<String, Object> completeness = new HashMap<>();
            completeness.put("withValue", totalWithValue);
            completeness.put("withMethod", totalWithMethod);
            completeness.put("completenessRate", count > 0 ? (double) totalWithValue / count : 0.0);
            categoryData.put("completeness", completeness);
            
            result.put(category, categoryData);
        }
        
        return result;
    }

    // ==================== 原有方法保持不变 ====================

    @Override
    @Cacheable(value = "propertyByWaste", key = "#wasteId")
    public List<PhysicalProperty> getByWasteId(Long wasteId) {
        log.info("获取危废理化特性，危废ID: {}", wasteId);
        
        if (wasteId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "危废ID不能为空");
        }
        
        return physicalPropertyMapper.selectByWasteId(wasteId);
    }

    @Override
    @Cacheable(value = "propertyByCategory", key = "#categoryCode")
    public List<PhysicalProperty> getByCategoryCode(String categoryCode) {
        log.info("根据分类代码获取理化特性，分类代码: {}", categoryCode);
        
        if (!StringUtils.hasText(categoryCode)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "分类代码不能为空");
        }

        LambdaQueryWrapper<PhysicalProperty> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhysicalProperty::getCategoryCode, categoryCode)
               .orderByAsc(PhysicalProperty::getSortOrder)
               .orderByDesc(PhysicalProperty::getCreateTime);
        
        return list(wrapper);
    }

    @Override
    public Page<PhysicalProperty> getByCategoryCodePage(String categoryCode, Long current, Long size) {
        log.info("根据分类代码分页获取理化特性，分类代码: {}, 页码: {}, 页大小: {}", categoryCode, current, size);
        
        if (!StringUtils.hasText(categoryCode)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "分类代码不能为空");
        }

        Page<PhysicalProperty> page = new Page<>(current, size);
        LambdaQueryWrapper<PhysicalProperty> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhysicalProperty::getCategoryCode, categoryCode)
               .orderByAsc(PhysicalProperty::getSortOrder)
               .orderByDesc(PhysicalProperty::getCreateTime);
        
        return page(page, wrapper);
    }

    @Override
    public Page<PhysicalProperty> filterProperties(PropertyFilterRequest request) {
        log.info("高级筛选理化特性，请求参数: {}", request);
        
        Page<PhysicalProperty> page = new Page<>(request.getCurrent(), request.getSize());
        QueryWrapper<PhysicalProperty> wrapper = buildFilterWrapper(request);
        
        return page(page, wrapper);
    }

    @Override
    public List<PhysicalProperty> filterByValueRange(String propertyName, BigDecimal minValue, BigDecimal maxValue) {
        log.info("按数值范围筛选理化特性，特性名称: {}, 范围: {} - {}", propertyName, minValue, maxValue);
        
        if (!StringUtils.hasText(propertyName)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "特性名称不能为空");
        }

        LambdaQueryWrapper<PhysicalProperty> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhysicalProperty::getPropertyName, propertyName);
        
        if (minValue != null) {
            wrapper.ge(PhysicalProperty::getPropertyValue, minValue.toString());
        }
        if (maxValue != null) {
            wrapper.le(PhysicalProperty::getPropertyValue, maxValue.toString());
        }
        
        wrapper.orderByDesc(PhysicalProperty::getTestDate);
        
        return list(wrapper);
    }

    @Override
    @Cacheable(value = "propertyByWaste", key = "'key_' + #wasteId")
    public List<PhysicalProperty> getKeyProperties(Long wasteId) {
        log.info("获取关键理化特性，危废ID: {}", wasteId);
        
        if (wasteId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "危废ID不能为空");
        }
        
        return physicalPropertyMapper.selectKeyPropertiesByWasteId(wasteId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importProperties(PropertyImportRequest request) {
        log.info("批量导入理化特性数据，危废ID: {}, 数据量: {}", request.getWasteId(), request.getProperties().size());
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 数据质量检查
            if (request.getQualityCheck()) {
                List<String> errors = validateProperties(request.getProperties());
                if (!errors.isEmpty()) {
                    result.put("success", false);
                    result.put("errors", errors);
                    return result;
                }
            }
            
            // 是否覆盖已有数据
            if (request.getOverwrite()) {
                LambdaQueryWrapper<PhysicalProperty> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(PhysicalProperty::getWasteId, request.getWasteId());
                remove(wrapper);
            }
            
            // 设置统一的危废ID和创建时间
            LocalDateTime now = LocalDateTime.now();
            request.getProperties().forEach(property -> {
                property.setWasteId(request.getWasteId());
                property.setCreateTime(now);
                property.setUpdateTime(now);
            });
            
            // 批量插入
            boolean success = physicalPropertyMapper.batchInsert(request.getProperties()) > 0;
            
            result.put("success", success);
            result.put("importCount", request.getProperties().size());
            result.put("dataSource", request.getDataSource());
            result.put("importNote", request.getImportNote());
            
            return result;
            
        } catch (Exception e) {
            log.error("批量导入理化特性数据失败", e);
            throw new BusinessException(ResultCode.DATA_IMPORT_ERROR, "批量导入失败: " + e.getMessage());
        }
    }

    @Override
    public String exportProperties(String categoryCode, List<Long> wasteIds) {
        log.info("导出理化特性数据，分类代码: {}, 危废ID列表: {}", categoryCode, wasteIds);
        
        try {
            LambdaQueryWrapper<PhysicalProperty> wrapper = new LambdaQueryWrapper<>();
            
            if (StringUtils.hasText(categoryCode)) {
                wrapper.eq(PhysicalProperty::getCategoryCode, categoryCode);
            }
            
            if (wasteIds != null && !wasteIds.isEmpty()) {
                wrapper.in(PhysicalProperty::getWasteId, wasteIds);
            }
            
            wrapper.orderByAsc(PhysicalProperty::getWasteId)
                   .orderByAsc(PhysicalProperty::getCategoryCode)
                   .orderByAsc(PhysicalProperty::getSortOrder);
            
            List<PhysicalProperty> properties = list(wrapper);
            
            // 生成导出文件路径（这里简化处理，实际应该调用文件服务）
            String fileName = "physical_properties_" + System.currentTimeMillis() + ".xlsx";
            String filePath = "/exports/" + fileName;
            
            // TODO: 实际的Excel导出逻辑
            log.info("导出理化特性数据完成，文件路径: {}, 数据量: {}", filePath, properties.size());
            
            return filePath;
            
        } catch (Exception e) {
            log.error("导出理化特性数据失败", e);
            throw new BusinessException(ResultCode.DATA_EXPORT_ERROR, "导出失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getPropertyStatistics(String propertyName) {
        log.info("获取特性统计信息，特性名称: {}", propertyName);
        
        if (!StringUtils.hasText(propertyName)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "特性名称不能为空");
        }
        
        LambdaQueryWrapper<PhysicalProperty> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhysicalProperty::getPropertyName, propertyName)
               .isNotNull(PhysicalProperty::getPropertyValue);
        
        List<PhysicalProperty> properties = list(wrapper);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", properties.size());
        statistics.put("propertyName", propertyName);
        
        if (properties.isEmpty()) {
            return statistics;
        }
        
        // 尝试进行数值统计
        List<BigDecimal> numericValues = properties.stream()
                .map(this::parseNumericValue)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (!numericValues.isEmpty()) {
            BigDecimal min = numericValues.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal max = numericValues.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal avg = numericValues.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(numericValues.size()), 2, RoundingMode.HALF_UP);
            
            statistics.put("numericCount", numericValues.size());
            statistics.put("minValue", min);
            statistics.put("maxValue", max);
            statistics.put("avgValue", avg);
        }
        
        // 按检测实验室统计
        Map<String, Long> labStats = properties.stream()
                .filter(p -> StringUtils.hasText(p.getTestLab()))
                .collect(Collectors.groupingBy(PhysicalProperty::getTestLab, Collectors.counting()));
        statistics.put("labStatistics", labStats);
        
        return statistics;
    }

    @Override
    public List<PhysicalProperty> searchSimilarProperties(String propertyName, BigDecimal targetValue, BigDecimal tolerance) {
        log.info("搜索相似特性值，特性名称: {}, 目标值: {}, 容差: {}%", propertyName, targetValue, tolerance);
        
        if (!StringUtils.hasText(propertyName) || targetValue == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "特性名称和目标值不能为空");
        }
        
        BigDecimal toleranceValue = tolerance != null ? tolerance : BigDecimal.valueOf(10);
        BigDecimal toleranceRatio = toleranceValue.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        
        BigDecimal minValue = targetValue.multiply(BigDecimal.ONE.subtract(toleranceRatio));
        BigDecimal maxValue = targetValue.multiply(BigDecimal.ONE.add(toleranceRatio));
        
        return filterByValueRange(propertyName, minValue, maxValue);
    }

    /**
     * 构建筛选条件
     */
    private QueryWrapper<PhysicalProperty> buildFilterWrapper(PropertyFilterRequest request) {
        QueryWrapper<PhysicalProperty> wrapper = new QueryWrapper<>();
        
        if (StringUtils.hasText(request.getCategoryCode())) {
            wrapper.eq("category_code", request.getCategoryCode());
        }
        
        if (StringUtils.hasText(request.getPropertyName())) {
            wrapper.like("property_name", request.getPropertyName());
        }
        
        if (StringUtils.hasText(request.getPropertyType())) {
            wrapper.eq("property_type", request.getPropertyType());
        }
        
        if (request.getMinValue() != null) {
            wrapper.ge("CAST(property_value AS DECIMAL(20,6))", request.getMinValue());
        }
        
        if (request.getMaxValue() != null) {
            wrapper.le("CAST(property_value AS DECIMAL(20,6))", request.getMaxValue());
        }
        
        if (StringUtils.hasText(request.getTestMethod())) {
            wrapper.like("test_method", request.getTestMethod());
        }
        
        if (StringUtils.hasText(request.getTestStandard())) {
            wrapper.like("test_standard", request.getTestStandard());
        }
        
        if (StringUtils.hasText(request.getTestLab())) {
            wrapper.like("test_lab", request.getTestLab());
        }
        
        if (request.getTestDateStart() != null) {
            wrapper.ge("test_date", request.getTestDateStart());
        }
        
        if (request.getTestDateEnd() != null) {
            wrapper.le("test_date", request.getTestDateEnd());
        }
        
        if (request.getMinConfidenceLevel() != null) {
            wrapper.ge("confidence_level", request.getMinConfidenceLevel());
        }
        
        // 排序
        if (StringUtils.hasText(request.getSortField())) {
            if ("desc".equalsIgnoreCase(request.getSortOrder())) {
                wrapper.orderByDesc(request.getSortField());
            } else {
                wrapper.orderByAsc(request.getSortField());
            }
        } else {
            wrapper.orderByDesc("test_date");
        }
        
        return wrapper;
    }

    /**
     * 数据质量检查
     */
    private List<String> validateProperties(List<PhysicalProperty> properties) {
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < properties.size(); i++) {
            PhysicalProperty property = properties.get(i);
            String prefix = "第" + (i + 1) + "条数据: ";
            
            if (!StringUtils.hasText(property.getPropertyName())) {
                errors.add(prefix + "特性名称不能为空");
            }
            
            if (!StringUtils.hasText(property.getPropertyValue())) {
                errors.add(prefix + "特性值不能为空");
            }
            
            if (!StringUtils.hasText(property.getCategoryCode())) {
                errors.add(prefix + "分类代码不能为空");
            }
            
            // 数值类型检查
            if ("numeric".equals(property.getPropertyType()) && parseNumericValue(property) == null) {
                errors.add(prefix + "数值类型特性值格式错误");
            }
            
            // 范围检查
            if (property.getMinValue() != null && property.getMaxValue() != null) {
                if (property.getMinValue().compareTo(property.getMaxValue()) > 0) {
                    errors.add(prefix + "最小值不能大于最大值");
                }
            }
        }
        
        return errors;
    }

    /**
     * 解析数值
     */
    private BigDecimal parseNumericValue(PhysicalProperty property) {
        try {
            String value = property.getPropertyValue();
            if (!StringUtils.hasText(value)) {
                return null;
            }
            
            // 去除单位和特殊字符，只保留数字和小数点
            String numericPart = value.replaceAll("[^0-9.-]", "");
            
            if (numericPart.isEmpty()) {
                return null;
            }
            
            return new BigDecimal(numericPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateQualityFlag(Map<Long, String> updates) {
        log.info("批量更新质量标识，更新数量: {}", updates.size());
        
        if (updates == null || updates.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "更新数据不能为空");
        }
        
        // 验证质量标识值
        Set<String> validFlags = Set.of("EXCELLENT", "GOOD", "FAIR", "POOR");
        for (String flag : updates.values()) {
            if (!validFlags.contains(flag)) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "无效的质量标识: " + flag);
            }
        }
        
        int updateCount = 0;
        LocalDateTime now = LocalDateTime.now();
        
        for (Map.Entry<Long, String> entry : updates.entrySet()) {
            LambdaUpdateWrapper<PhysicalProperty> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(PhysicalProperty::getId, entry.getKey())
                   .set(PhysicalProperty::getQualityFlag, entry.getValue())
                   .set(PhysicalProperty::getUpdateTime, now);
                   
            if (update(wrapper)) {
                updateCount++;
            }
        }
        
        return updateCount;
    }

    @Override
    public List<PhysicalProperty> getQualityDistribution() {
        log.info("获取理化特性质量分布");
        // TODO: 修复类型转换问题，暂时返回按质量标识分组的数据
        LambdaQueryWrapper<PhysicalProperty> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(PhysicalProperty::getQualityFlag)
               .orderByDesc(PhysicalProperty::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<PhysicalProperty> getPropertiesByTestMethod(String testMethod) {
        log.info("根据检测方法查询理化特性，检测方法: {}", testMethod);
        
        if (!StringUtils.hasText(testMethod)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "检测方法不能为空");
        }
        
        return physicalPropertyMapper.selectByTestMethod(testMethod);
    }

    @Override
    public List<Map<String, Object>> getCountByTestLab() {
        log.info("根据检测实验室统计理化特性数量");
        // TODO: 修复类型转换问题，暂时返回空列表
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> validateDataConsistency(Long wasteId) {
        log.info("验证理化特性数据一致性，危废ID: {}", wasteId);
        
        if (wasteId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "危废ID不能为空");
        }
        
        // TODO: 修复类型转换问题，暂时返回空列表
        return new ArrayList<>();
    }

    @Override
    public List<PhysicalProperty> getByMinConfidence(BigDecimal minConfidence) {
        log.info("根据最小置信度查询理化特性，最小置信度: {}", minConfidence);
        
        if (minConfidence == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "最小置信度不能为空");
        }
        
        LambdaQueryWrapper<PhysicalProperty> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(PhysicalProperty::getConfidenceLevel, minConfidence)
               .orderByDesc(PhysicalProperty::getConfidenceLevel);
        
        return list(wrapper);
    }

    @Override
    public Map<String, Object> getCompletenessReport(Long wasteId) {
        log.info("获取理化特性完整性报告，危废ID: {}", wasteId);
        
        if (wasteId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "危废ID不能为空");
        }
        
        // TODO: 修复类型转换问题，暂时返回空Map
        return new HashMap<>();
    }

    @Override
    public List<PhysicalProperty> getByQualityFlag(String qualityFlag) {
        log.info("根据质量标识查询理化特性，质量标识: {}", qualityFlag);
        
        if (!StringUtils.hasText(qualityFlag)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "质量标识不能为空");
        }
        
        LambdaQueryWrapper<PhysicalProperty> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PhysicalProperty::getQualityFlag, qualityFlag)
               .orderByDesc(PhysicalProperty::getCreateTime);
        
        return list(wrapper);
    }

    @Override
    public List<Map<String, Object>> getTrendAnalysis(String propertyName, List<Long> wasteIds) {
        log.info("获取理化特性趋势分析，特性名称: {}, 危废ID列表: {}", propertyName, wasteIds);
        
        if (!StringUtils.hasText(propertyName)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "特性名称不能为空");
        }
        
        // TODO: 修复类型转换问题，暂时返回空列表
        return new ArrayList<>();
    }
} 