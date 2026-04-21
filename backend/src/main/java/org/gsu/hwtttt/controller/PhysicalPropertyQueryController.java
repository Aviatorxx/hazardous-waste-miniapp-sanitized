package org.gsu.hwtttt.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gsu.hwtttt.common.result.Result;
import org.gsu.hwtttt.constant.SystemConstants;
import org.gsu.hwtttt.dto.request.PhysicalPropertySearchRequest;
import org.gsu.hwtttt.dto.response.PhysicalPropertyCategoryResponse;
import org.gsu.hwtttt.dto.response.PhysicalPropertySearchResponse;
import org.gsu.hwtttt.service.PhysicalPropertyQueryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 物理特性查询控制器
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Slf4j
@RestController
@RequestMapping("/api/physical-properties")
@Api(tags = "物理特性查询模块")
@RequiredArgsConstructor
@Validated
public class PhysicalPropertyQueryController {

    private final PhysicalPropertyQueryService physicalPropertyQueryService;

    @GetMapping("/categories")
    @ApiOperation("获取所有物理特性分类信息 - 返回7个分类的元数据")
    public Result<List<PhysicalPropertyCategoryResponse>> getPropertyCategories() {
        log.info("获取物理特性分类信息");
        List<PhysicalPropertyCategoryResponse> categories = physicalPropertyQueryService.getPropertyCategories();
        return Result.success(categories);
    }

    @GetMapping("/{categoryCode}")
    @ApiOperation("根据分类查询危废物理特性数据 - 支持搜索和分页")
    public Result<PhysicalPropertySearchResponse> searchByCategory(
            @ApiParam(value = "分类代码", required = true, 
                     allowableValues = "ELEMENT_COMPOSITION,HEAT_VALUE,PH,WATER_CONTENT,FLASH_POINT,HEAVY_METALS,ALKALI_METALS") 
            @PathVariable @NotBlank String categoryCode,
            
            @ApiParam("搜索关键字（危废代码或名称）") 
            @RequestParam(required = false) String search,
            
            @ApiParam("页码") 
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Long page,
            
            @ApiParam("每页大小") 
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "页大小必须大于0") Long size,

            // Hazard Properties filters
            @ApiParam("氧化性过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean oxidizing,
            
            @ApiParam("还原性过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean reducing,
            
            @ApiParam("挥发性过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean volatileProperty,
            
            @ApiParam("易燃性过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean flammable,
            
            @ApiParam("毒性过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean toxic,
            
            @ApiParam("反应性过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean reactive,
            
            @ApiParam("感染性过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean infectious,
            
            @ApiParam("腐蚀性过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean corrosive,
            
            @ApiParam("卤化烃类过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean halogenatedHydrocarbon,
            
            @ApiParam("含氰化物废物过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean cyanideContaining) {
        
        log.info("Controller received request: categoryCode={}, search={}, page={}, size={}", 
                categoryCode, search, page, size);
        
        // 验证分类代码
        if (!isValidCategoryCode(categoryCode)) {
            log.error("Invalid category code: {}", categoryCode);
            return Result.fail("无效的分类代码: " + categoryCode);
        }
        
        PhysicalPropertySearchRequest request = new PhysicalPropertySearchRequest();
        request.setCategoryCode(categoryCode);
        request.setSearch(search);
        request.setPage(page);
        request.setSize(size);
        
        // Set hazard properties filters
        request.setOxidizing(oxidizing);
        request.setReducing(reducing);
        request.setVolatileProperty(volatileProperty);
        request.setFlammable(flammable);
        request.setToxic(toxic);
        request.setReactive(reactive);
        request.setInfectious(infectious);
        request.setCorrosive(corrosive);
        request.setHalogenatedHydrocarbon(halogenatedHydrocarbon);
        request.setCyanideContaining(cyanideContaining);
        
        PhysicalPropertySearchResponse response = physicalPropertyQueryService.searchByCategory(request);
        
        log.info("Controller returning response: total={}, records={}", 
                response.getTotal(), response.getRecords().size());
        
        return Result.success(response);
    }

    @GetMapping("/{categoryCode}/count")
    @ApiOperation("获取指定分类的有效记录总数")
    public Result<Long> getCountByCategory(
            @ApiParam(value = "分类代码", required = true) 
            @PathVariable @NotBlank String categoryCode,
            
            @ApiParam("搜索关键字（危废代码或名称）") 
            @RequestParam(required = false) String search,

            // Hazard Properties filters
            @ApiParam("氧化性过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean oxidizing,
            
            @ApiParam("还原性过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean reducing,
            
            @ApiParam("挥发性过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean volatileProperty,
            
            @ApiParam("易燃性过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean flammable,
            
            @ApiParam("毒性过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean toxic,
            
            @ApiParam("反应性过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean reactive,
            
            @ApiParam("感染性过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean infectious,
            
            @ApiParam("腐蚀性过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean corrosive,
            
            @ApiParam("卤化烃类过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean halogenatedHydrocarbon,
            
            @ApiParam("含氰化物废物过滤条件（true=是，false=否，null=不过滤）") 
            @RequestParam(required = false) Boolean cyanideContaining) {
        
        log.info("获取分类记录总数: categoryCode={}, search={}", categoryCode, search);
        
        // 验证分类代码
        if (!isValidCategoryCode(categoryCode)) {
            return Result.fail("无效的分类代码: " + categoryCode);
        }
        
        // Create request object with hazard properties filters
        PhysicalPropertySearchRequest request = new PhysicalPropertySearchRequest();
        request.setCategoryCode(categoryCode);
        request.setSearch(search);
        request.setOxidizing(oxidizing);
        request.setReducing(reducing);
        request.setVolatileProperty(volatileProperty);
        request.setFlammable(flammable);
        request.setToxic(toxic);
        request.setReactive(reactive);
        request.setInfectious(infectious);
        request.setCorrosive(corrosive);
        request.setHalogenatedHydrocarbon(halogenatedHydrocarbon);
        request.setCyanideContaining(cyanideContaining);
        
        Long count = physicalPropertyQueryService.getCountByCategory(request);
        
        return Result.success(count);
    }

    /**
     * 验证分类代码是否有效
     */
    private boolean isValidCategoryCode(String categoryCode) {
        return categoryCode.equals(SystemConstants.PhysicalPropertyCategory.ELEMENT_COMPOSITION) ||
               categoryCode.equals(SystemConstants.PhysicalPropertyCategory.HEAT_VALUE) ||
               categoryCode.equals(SystemConstants.PhysicalPropertyCategory.PH) ||
               categoryCode.equals(SystemConstants.PhysicalPropertyCategory.WATER_CONTENT) ||
               categoryCode.equals(SystemConstants.PhysicalPropertyCategory.FLASH_POINT) ||
               categoryCode.equals(SystemConstants.PhysicalPropertyCategory.HEAVY_METALS) ||
               categoryCode.equals(SystemConstants.PhysicalPropertyCategory.ALKALI_METALS);
    }
} 