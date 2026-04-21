package org.gsu.hwtttt.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gsu.hwtttt.common.result.PageResult;
import org.gsu.hwtttt.common.result.Result;
import org.gsu.hwtttt.constant.SpectrumType;
import org.gsu.hwtttt.dto.request.ThermalPropertySearchRequest;
import org.gsu.hwtttt.dto.request.ThermalPropertyUpdateRequest;
import org.gsu.hwtttt.dto.response.SpectrumTypeStatistics;
import org.gsu.hwtttt.dto.response.ThermalPropertyDetail;
import org.gsu.hwtttt.dto.response.WasteThermalSummary;
import org.gsu.hwtttt.entity.ThermalProperty;
import org.gsu.hwtttt.service.ThermalPropertyService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Module 3: 热力学性质模块控制器
 * 提供热力学图像上传、显示、搜索等功能
 *
 * @author WenXin
 * @date 2025/06/13
 */
@Slf4j
@RestController
@RequestMapping("/api/thermal-properties")
@CrossOrigin(origins = "*")
@Api(tags = "Module 3: 热力学性质模块")
@RequiredArgsConstructor
@Validated
public class ThermalPropertyController {

    private final ThermalPropertyService thermalPropertyService;

    // ==================== 基础统计功能 ====================

    @GetMapping("/types")
    @ApiOperation("获取光谱类型统计信息")
    public Result<List<SpectrumTypeStatistics>> getSpectrumTypes() {
        log.info("获取光谱类型统计信息");
        try {
            List<SpectrumTypeStatistics> result = thermalPropertyService.getSpectrumTypeStatistics();
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取光谱类型统计失败", e);
            return Result.error("获取光谱类型统计失败: " + e.getMessage());
        }
    }

    // ==================== 搜索功能 ====================

    @GetMapping("/search")
    @ApiOperation("搜索热力学特性 - 支持分页和多条件筛选")
    public Result<PageResult<WasteThermalSummary>> searchBySpectrumType(
            @ApiParam("光谱类型") @RequestParam(required = false) String spectrumType,
            @ApiParam("搜索关键字（危废代码、名称或来源单位）") @RequestParam(required = false) String keyword,
            @ApiParam("页码") @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer pageNum,
            @ApiParam("每页大小") @RequestParam(defaultValue = "12") @Min(value = 1, message = "每页大小必须大于0") Integer pageSize) {
        log.info("搜索热力学特性，光谱类型: {}, 关键字: {}, 页码: {}, 页大小: {}", spectrumType, keyword, pageNum, pageSize);
        try {
            PageResult<WasteThermalSummary> result = thermalPropertyService.searchThermalProperties(spectrumType, keyword, pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索失败", e);
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    @PostMapping("/search/advanced")
    @ApiOperation("高级搜索热力学特性")
    public Result<PageResult<WasteThermalSummary>> advancedSearch(@Valid @RequestBody ThermalPropertySearchRequest request) {
        log.info("高级搜索热力学特性，请求参数: {}", request);
        PageResult<WasteThermalSummary> result = thermalPropertyService.advancedSearchThermalProperties(request);
        return Result.success(result);
    }

    // ==================== 详情查询功能 ====================

    @GetMapping("/{id}")
    @ApiOperation("根据ID获取热力学特性详情")
    public Result<ThermalPropertyDetail> getThermalPropertyById(
            @ApiParam("热力学特性ID") @PathVariable @NotNull @Min(value = 1, message = "ID必须大于0") Long id) {
        log.info("获取热力学特性详情，ID: {}", id);
        try {
            ThermalPropertyDetail result = thermalPropertyService.getThermalPropertyById(id);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取详情失败", e);
            return Result.error("获取详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/waste/{wasteId}")
    @ApiOperation("根据危废ID获取热力学特性列表")
    public Result<List<ThermalPropertyDetail>> getThermalPropertiesByWasteId(
            @ApiParam("危废ID") @PathVariable @NotNull @Min(value = 1, message = "危废ID必须大于0") Long wasteId,
            @ApiParam("光谱类型（可选）") @RequestParam(required = false) String spectrumType) {
        log.info("根据危废ID获取热力学特性，危废ID: {}, 光谱类型: {}", wasteId, spectrumType);
        try {
            List<ThermalPropertyDetail> result = thermalPropertyService.getThermalPropertiesByWasteId(wasteId, spectrumType);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取危废热力学特性失败", e);
            return Result.error("获取危废热力学特性失败: " + e.getMessage());
        }
    }

    // ==================== 文件上传功能 ====================

    @PostMapping("/upload")
    @ApiOperation("上传热力学分析图像")
    public Result<ThermalProperty> uploadThermalImage(
            @ApiParam("图像文件") @RequestParam("file") MultipartFile file,
            @ApiParam("危废ID") @RequestParam("wasteId") @NotNull Long wasteId,
            @ApiParam("光谱类型") @RequestParam("spectrumType") @NotBlank String spectrumType,
            @ApiParam("测试名称") @RequestParam(required = false) String testName,
            @ApiParam("测试实验室") @RequestParam(required = false) String testLab,
            @ApiParam("备注") @RequestParam(required = false) String remark) {
        log.info("上传热力学图像，危废ID: {}, 光谱类型: {}, 文件名: {}", wasteId, spectrumType, file.getOriginalFilename());
        ThermalProperty result = thermalPropertyService.uploadThermalImage(file, wasteId, spectrumType, testName, testLab, remark);
        return Result.success(result);
    }

    // ==================== 图像访问功能 ====================

    @GetMapping("/image/{spectrumType}/{fileName}")
    @ApiOperation("获取热力学图像文件")
    public ResponseEntity<Resource> getImage(
            @ApiParam("光谱类型") @PathVariable @NotBlank String spectrumType,
            @ApiParam("文件名") @PathVariable @NotBlank String fileName) {
        log.info("获取热力学图像，光谱类型: {}, 文件名: {}", spectrumType, fileName);
        return thermalPropertyService.getImageResource(spectrumType, fileName);
    }

    // ==================== 数据管理功能 ====================

    @PutMapping("/{id}")
    @ApiOperation("更新热力学特性信息（不包含图像）")
    public Result<ThermalProperty> updateThermalProperty(
            @ApiParam("热力学特性ID") @PathVariable @NotNull @Min(value = 1, message = "ID必须大于0") Long id,
            @Valid @RequestBody ThermalPropertyUpdateRequest request) {
        log.info("更新热力学特性，ID: {}, 请求参数: {}", id, request);
        ThermalProperty result = thermalPropertyService.updateThermalProperty(id, request);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除热力学特性")
    public Result<Void> deleteThermalProperty(
            @ApiParam("热力学特性ID") @PathVariable @NotNull @Min(value = 1, message = "ID必须大于0") Long id) {
        log.info("删除热力学特性，ID: {}", id);
        thermalPropertyService.deleteThermalProperty(id);
        return Result.success();
    }

    // ==================== 兼容性支持 ====================

    @GetMapping("/spectrum-types")
    @ApiOperation("获取所有支持的光谱类型")
    public Result<List<String>> getSupportedSpectrumTypes() {
        log.info("获取所有支持的光谱类型");
        List<String> result = thermalPropertyService.getSupportedSpectrumTypes();
        return Result.success(result);
    }

    @GetMapping("/by-waste/{wasteId}")
    @ApiOperation("根据危废ID获取所有热力学特性记录（兼容性接口）")
    public Result<List<ThermalProperty>> getThermalPropertiesByWaste(
            @ApiParam("危废ID") @PathVariable @NotNull @Min(value = 1, message = "危废ID必须大于0") Long wasteId) {
        log.info("获取危废所有热力学特性，危废ID: {}", wasteId);
        List<ThermalProperty> result = thermalPropertyService.getByWasteId(wasteId);
        return Result.success(result);
    }
} 