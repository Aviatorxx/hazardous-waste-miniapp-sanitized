package org.gsu.hwtttt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gsu.hwtttt.common.result.Result;
import org.gsu.hwtttt.dto.request.WasteSearchRequest;
import org.gsu.hwtttt.dto.request.BatchStorageUpdateRequest;
import org.gsu.hwtttt.dto.response.WasteDetailResponse;
import org.gsu.hwtttt.entity.HazardousWaste;
import org.gsu.hwtttt.service.HazardousWasteService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Module 1: 危废目录模块
 * 提供危废搜索、详情查看、库存实时更新等功能
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/waste-directory")
@Api(tags = "Module 1: 危废目录模块")
@RequiredArgsConstructor
@Validated
public class HazardousWasteController {

    private final HazardousWasteService hazardousWasteService;

    // ==================== 核心搜索功能 ====================

    @GetMapping("/search")
    @ApiOperation("搜索危废 - 支持关键字或危废代码搜索")
    public Result<Page<HazardousWaste>> searchWaste(@Valid WasteSearchRequest request) {
        log.info("搜索危废，请求参数: {}", request);
        Page<HazardousWaste> result = hazardousWasteService.searchWaste(request);
        return Result.success(result);
    }

    @GetMapping("/search/quick")
    @ApiOperation("快速搜索 - 支持关键字或危废代码的快速查询")
    public Result<List<HazardousWaste>> quickSearch(
            @ApiParam("搜索关键字或危废代码") @RequestParam @NotBlank String keyword) {
        log.info("快速搜索危废: {}", keyword);
        List<HazardousWaste> result = hazardousWasteService.searchByKeyword(keyword);
        return Result.success(result);
    }

    @GetMapping("/search/by-code")
    @ApiOperation("根据危废代码精确搜索")
    public Result<HazardousWaste> searchByWasteCode(
            @ApiParam("危废代码") @RequestParam @NotBlank String wasteCode) {
        log.info("根据危废代码搜索: {}", wasteCode);
        return hazardousWasteService.searchByWasteCodeExact(wasteCode)
                .map(Result::success)
                .orElse(Result.fail("未找到危废代码为 " + wasteCode + " 的记录"));
    }

    // ==================== 详情查看功能 ====================

    @GetMapping("/detail/{id}")
    @ApiOperation("获取危废详情 - 查看单个危废的详细信息")
    public Result<WasteDetailResponse> getWasteDetail(
            @ApiParam("危废ID") @PathVariable @NotNull @Min(value = 1, message = "危废ID必须大于0") Long id) {
        log.info("获取危废详情: {}", id);
        WasteDetailResponse result = hazardousWasteService.getWasteDetail(id);
        return Result.success(result);
    }

    @GetMapping("/{id}/storage-status")
    @ApiOperation("获取危废实时库存状态")
    public Result<Map<String, Object>> getStorageStatus(
            @ApiParam("危废ID") @PathVariable @NotNull Long id) {
        log.info("获取危废库存状态，ID: {}", id);
        
        HazardousWaste waste = hazardousWasteService.getById(id);
        if (waste == null) {
            return Result.fail("危废记录不存在");
        }
        
        Map<String, Object> status = new HashMap<>();
        status.put("wasteId", id);
        status.put("wasteCode", waste.getWasteCode());
        status.put("wasteName", waste.getWasteName());
        status.put("remainingStorage", waste.getRemainingStorage());
        status.put("storageLocation", waste.getStorageLocation());
        status.put("lastUpdated", waste.getUpdateTime());
        
        return Result.success(status);
    }

    // ==================== 库存管理功能 ====================

    @PutMapping("/{id}/storage")
    @ApiOperation("更新危废库存 - 实时更新剩余贮存量")
    public Result<Boolean> updateStorage(
            @ApiParam("危废ID") @PathVariable @NotNull Long id,
            @ApiParam("新库存量(kg)") @RequestParam @NotNull BigDecimal storage) {
        log.info("更新危废库存，ID: {}, 新库存: {}", id, storage);
        
        if (storage.compareTo(BigDecimal.ZERO) < 0) {
            return Result.fail("库存量不能为负数");
        }
        
        boolean result = hazardousWasteService.updateStorage(id, storage);
        if (result) {
            return Result.success("库存更新成功", true);
        } else {
            return Result.fail("库存更新失败");
        }
    }

    @GetMapping("/storage/warning")
    @ApiOperation("库存预警查询 - 查找库存不足的危废")
    public Result<List<HazardousWaste>> getStorageWarning(
            @ApiParam("预警阈值(kg)") @RequestParam(defaultValue = "100") BigDecimal threshold) {
        log.info("查询库存预警，阈值: {}", threshold);
        List<HazardousWaste> result = hazardousWasteService.checkStorageWarning(threshold);
        return Result.success(result);
    }

    // ==================== 分类查询功能 ====================

    @GetMapping("/by-properties")
    @ApiOperation("根据危险特性筛选危废 - 支持多种布尔特性组合筛选")
    public Result<List<HazardousWaste>> filterByHazardProperties(
            @RequestParam(required = false) Boolean oxidizing,
            @RequestParam(required = false) Boolean reducing,
            @RequestParam(required = false) Boolean flammable,
            @RequestParam(required = false) Boolean toxic,
            @RequestParam(required = false) Boolean reactive,
            @RequestParam(required = false) Boolean infectious,
            @RequestParam(required = false) Boolean corrosive,
            @RequestParam(required = false) Boolean halogenatedHydrocarbon,
            @RequestParam(required = false) Boolean cyanideContaining) {

        Map<String, Boolean> properties = new HashMap<>();
        if (oxidizing != null) properties.put("oxidizing", oxidizing);
        if (reducing != null) properties.put("reducing", reducing);
        if (flammable != null) properties.put("flammable", flammable);
        if (toxic != null) properties.put("toxic", toxic);
        if (reactive != null) properties.put("reactive", reactive);
        if (infectious != null) properties.put("infectious", infectious);
        if (corrosive != null) properties.put("corrosive", corrosive);
        if (halogenatedHydrocarbon != null) properties.put("halogenatedHydrocarbon", halogenatedHydrocarbon);
        if (cyanideContaining != null) properties.put("cyanideContaining", cyanideContaining);
        
        log.info("根据危险特性筛选危废: {}", properties);
        List<HazardousWaste> result = hazardousWasteService.filterByHazardProperties(properties);
        return Result.success(result);
    }

    @GetMapping("/properties/options")
    @ApiOperation("获取所有危险特性选项 - 用于前端筛选组件")
    public Result<Map<String, List<String>>> getHazardProperties() {
        log.info("获取所有危险特性选项");
        Map<String, List<String>> result = hazardousWasteService.getHazardProperties();
        return Result.success(result);
    }

    // ==================== 统计功能 ====================

    @GetMapping("/statistics")
    @ApiOperation("获取危废统计信息 - 数量、类型、库存等统计")
    public Result<Map<String, Object>> getStatistics() {
        log.info("获取危废统计信息");
        Map<String, Object> result = hazardousWasteService.getStatistics();
        return Result.success(result);
    }

    @GetMapping("/list")
    @ApiOperation("分页列表查询 - 支持关键字筛选的分页查询")
    public Result<Map<String, Object>> getWasteList(
            @ApiParam("当前页码") @RequestParam(defaultValue = "1") Long current,
            @ApiParam("每页大小") @RequestParam(defaultValue = "20") Long size,
            @ApiParam("关键字") @RequestParam(required = false) String keyword) {
        
        WasteSearchRequest request = new WasteSearchRequest();
        request.setCurrent(current);
        request.setSize(size);
        request.setKeyword(keyword);
        
        Page<HazardousWaste> page = hazardousWasteService.searchWaste(request);

        // IMPORTANT: This endpoint is used by Module 4 import page (wasteDirectoryAPI.getWasteList).
        // Add storageDays / priorityRecommend to each record without modifying other existing fields.
        List<Map<String, Object>> mappedRecords = new ArrayList<>();
        if (page != null && page.getRecords() != null) {
            java.time.LocalDate today = java.time.LocalDate.now();
            for (HazardousWaste w : page.getRecords()) {
                Map<String, Object> m = new HashMap<>();

                // Copy only fields used by the mini program (keep compatibility with existing front-end)
                m.put("id", w.getId());
                m.put("wasteCode", w.getWasteCode());
                m.put("sourceUnit", w.getSourceUnit());
                m.put("wasteName", w.getWasteName());
                m.put("appearance", w.getAppearance());
                m.put("remainingStorage", w.getRemainingStorage());
                m.put("heatValueCalPerG", w.getHeatValueCalPerG());
                m.put("waterContentPercent", w.getWaterContentPercent());

                Integer storageDays = null;
                boolean priorityRecommend = false;
                if (w != null && w.getInboundTime() != null) {
                    try {
                        java.time.LocalDate inbound = w.getInboundTime().toInstant()
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate();
                        long days = java.time.temporal.ChronoUnit.DAYS.between(inbound, today);
                        storageDays = (int) Math.max(days, 0);
                        priorityRecommend = storageDays >= 365;
                    } catch (Exception ignored) {
                        // keep null
                    }
                }
                m.put("storageDays", storageDays);
                m.put("priorityRecommend", priorityRecommend);
                m.put("priorityThresholdDays", 365);

                mappedRecords.add(m);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("records", mappedRecords);
        response.put("total", page != null ? page.getTotal() : 0);
        response.put("current", page != null ? page.getCurrent() : current);
        response.put("size", page != null ? page.getSize() : size);
        response.put("pages", page != null ? page.getPages() : 0);

        return Result.success(response);
    }

    @PostMapping("/batch-storage")
    @ApiOperation("批量更新库存")
    public Result<Integer> batchUpdateStorage(@Valid @RequestBody BatchStorageUpdateRequest request) {
        log.info("批量更新库存，更新数量: {}", request.getStorageUpdates().size());
        int result = hazardousWasteService.batchUpdateStorage(request.getStorageUpdates());
        return Result.success("成功更新" + result + "条记录", result);
    }
} 