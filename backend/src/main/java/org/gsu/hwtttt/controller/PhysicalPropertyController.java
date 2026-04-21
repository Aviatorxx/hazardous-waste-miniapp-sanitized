package org.gsu.hwtttt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gsu.hwtttt.common.result.Result;
import org.gsu.hwtttt.dto.request.PropertyFilterRequest;
import org.gsu.hwtttt.dto.request.PropertyImportRequest;
import org.gsu.hwtttt.entity.PhysicalProperty;
import org.gsu.hwtttt.service.PhysicalPropertyService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 理化性质模块控制器
 * 
 * @author WenXin
 * @date 2025/06/10
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/physical-properties")
@Api(tags = "理化性质模块")
@RequiredArgsConstructor
@Validated
public class PhysicalPropertyController {

    private final PhysicalPropertyService physicalPropertyService;

    // ==================== 通用分类查询功能 ====================

    @GetMapping("/category/{categoryName}/search")
    @ApiOperation("按分类搜索理化性质 - 支持关键字或危废代码搜索")
    public Result<List<PhysicalProperty>> searchByCategory(
            @ApiParam(value = "分类名称", required = true) @PathVariable @NotBlank(message = "分类名称不能为空") String categoryName,
            @ApiParam(value = "搜索关键字或危废代码", required = true) @RequestParam @NotBlank(message = "搜索关键字不能为空") String keyword) {
        log.info("按分类搜索理化性质，分类: {}, 关键字: {}", categoryName, keyword);
        List<PhysicalProperty> result = physicalPropertyService.searchByKeywordAndCategory(keyword, categoryName);
        return Result.success(result);
    }

    @GetMapping("/category/{categoryName}/by-waste/{wasteId}")
    @ApiOperation("根据危废ID和分类获取理化性质")
    public Result<List<PhysicalProperty>> getByCategoryAndWasteId(
            @ApiParam(value = "分类名称", required = true) @PathVariable @NotBlank(message = "分类名称不能为空") String categoryName,
            @ApiParam(value = "危废ID", required = true) @PathVariable @NotNull @Min(value = 1, message = "危废ID必须大于0") Long wasteId) {
        log.info("根据危废ID和分类获取理化性质，分类: {}, 危废ID: {}", categoryName, wasteId);
        List<PhysicalProperty> result = physicalPropertyService.getByWasteIdAndCategory(wasteId, categoryName);
        return Result.success(result);
    }

    @GetMapping("/search")
    @ApiOperation("通用搜索理化性质 - 支持多条件筛选")
    public Result<Page<PhysicalProperty>> searchProperties(@Valid PropertyFilterRequest request) {
        log.info("搜索理化性质，请求参数: {}", request);
        Page<PhysicalProperty> result = physicalPropertyService.searchProperties(request);
        return Result.success(result);
    }

    @GetMapping("/by-waste/{wasteId}")
    @ApiOperation("根据危废ID获取所有理化性质")
    public Result<List<PhysicalProperty>> getPropertiesByWaste(
            @ApiParam(value = "危废ID", required = true) @PathVariable @NotNull @Min(value = 1, message = "危废ID必须大于0") Long wasteId) {
        log.info("根据危废ID获取理化性质: {}", wasteId);
        List<PhysicalProperty> result = physicalPropertyService.getByWasteId(wasteId);
        return Result.success(result);
    }

    @GetMapping("/by-waste-code")
    @ApiOperation("根据危废代码获取理化性质")
    public Result<List<PhysicalProperty>> getPropertiesByWasteCode(
            @ApiParam("危废代码") @RequestParam @NotBlank(message = "危废代码不能为空") String wasteCode) {
        log.info("根据危废代码获取理化性质，危废代码: {}", wasteCode);
        List<PhysicalProperty> result = physicalPropertyService.getByWasteCode(wasteCode);
        return Result.success(result);
    }

    @GetMapping("/categories")
    @ApiOperation("获取所有理化性质分类")
    public Result<List<String>> getPropertyCategories() {
        log.info("获取所有理化性质分类");
        List<String> result = physicalPropertyService.getAllCategories();
        return Result.success(result);
    }

    // ==================== 统计功能 ====================

    @GetMapping("/statistics")
    @ApiOperation("获取理化性质统计信息")
    public Result<Map<String, Object>> getStatistics() {
        log.info("获取理化性质统计信息");
        Map<String, Object> result = physicalPropertyService.getStatistics();
        return Result.success(result);
    }

    @GetMapping("/statistics/by-category")
    @ApiOperation("按分类获取理化性质统计信息")
    public Result<Map<String, Object>> getStatisticsByCategory() {
        log.info("按分类获取理化性质统计信息");
        Map<String, Object> result = physicalPropertyService.getStatisticsByCategory();
        return Result.success(result);
    }

    // ==================== 数据管理功能 ====================

    @PostMapping("/add")
    @ApiOperation("添加理化特性数据")
    public Result<Boolean> addProperty(@Valid @RequestBody PhysicalProperty property) {
        log.info("添加理化特性数据: {}", property.getPropertyName());
        boolean result = physicalPropertyService.save(property);
        return Result.success(result);
    }

    @PutMapping("/update")
    @ApiOperation("更新理化特性数据")
    public Result<Boolean> updateProperty(@Valid @RequestBody PhysicalProperty property) {
        log.info("更新理化特性数据，ID: {}", property.getId());
        boolean result = physicalPropertyService.updateById(property);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除理化特性数据")
    public Result<Boolean> deleteProperty(
            @ApiParam("特性数据ID") @PathVariable @NotNull @Min(value = 1, message = "特性数据ID必须大于0") Long id) {
        log.info("删除理化特性数据，ID: {}", id);
        boolean result = physicalPropertyService.removeById(id);
        return Result.success(result);
    }

    // ==================== 批量操作功能 ====================

    @PostMapping("/import")
    @ApiOperation("批量导入理化特性数据")
    public Result<Map<String, Object>> importProperties(@Valid @RequestBody PropertyImportRequest request) {
        log.info("批量导入理化特性数据，危废ID: {}, 数据量: {}", request.getWasteId(), request.getProperties().size());
        Map<String, Object> result = physicalPropertyService.importProperties(request);
        return Result.success(result);
    }

    @GetMapping("/export")
    @ApiOperation("导出理化特性数据")
    public Result<String> exportProperties(
            @ApiParam("分类代码") @RequestParam(required = false) String categoryCode,
            @ApiParam("危废ID列表") @RequestParam(required = false) List<Long> wasteIds) {
        log.info("导出理化特性数据，分类代码: {}, 危废ID列表: {}", categoryCode, wasteIds);
        String result = physicalPropertyService.exportProperties(categoryCode, wasteIds);
        return Result.success("导出成功", result);
    }

    // ==================== 高级筛选功能 ====================

    @PostMapping("/filter")
    @ApiOperation("高级筛选理化特性")
    public Result<Page<PhysicalProperty>> filterProperties(@Valid @RequestBody PropertyFilterRequest request) {
        log.info("高级筛选理化特性，请求参数: {}", request);
        Page<PhysicalProperty> result = physicalPropertyService.filterProperties(request);
        return Result.success(result);
    }

    @GetMapping("/filter/by-value-range")
    @ApiOperation("按数值范围筛选理化特性")
    public Result<List<PhysicalProperty>> filterByValueRange(
            @ApiParam("特性名称") @RequestParam @NotBlank String propertyName,
            @ApiParam("最小值") @RequestParam BigDecimal minValue,
            @ApiParam("最大值") @RequestParam BigDecimal maxValue) {
        log.info("按数值范围筛选理化特性，特性名称: {}, 范围: {} - {}", propertyName, minValue, maxValue);
        List<PhysicalProperty> result = physicalPropertyService.filterByValueRange(propertyName, minValue, maxValue);
        return Result.success(result);
    }

    @GetMapping("/statistics/property/{propertyName}")
    @ApiOperation("获取指定特性的统计信息")
    public Result<Map<String, Object>> getPropertyStatistics(
            @ApiParam("特性名称") @PathVariable String propertyName) {
        log.info("获取特性统计信息，特性名称: {}", propertyName);
        Map<String, Object> result = physicalPropertyService.getPropertyStatistics(propertyName);
        return Result.success(result);
    }

    @GetMapping("/search/similar")
    @ApiOperation("搜索相似特性值")
    public Result<List<PhysicalProperty>> searchSimilarProperties(
            @ApiParam("特性名称") @RequestParam @NotBlank String propertyName,
            @ApiParam("目标值") @RequestParam BigDecimal targetValue,
            @ApiParam("容差百分比") @RequestParam(defaultValue = "10") BigDecimal tolerance) {
        log.info("搜索相似特性值，特性名称: {}, 目标值: {}, 容差: {}%", propertyName, targetValue, tolerance);
        List<PhysicalProperty> result = physicalPropertyService.searchSimilarProperties(propertyName, targetValue, tolerance);
        return Result.success(result);
    }
} 