package org.gsu.hwtttt.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gsu.hwtttt.constant.SystemConstants;
import org.gsu.hwtttt.dto.request.PhysicalPropertySearchRequest;
import org.gsu.hwtttt.dto.response.PhysicalPropertyCategoryResponse;
import org.gsu.hwtttt.dto.response.PhysicalPropertySearchResponse;
import org.gsu.hwtttt.entity.HazardousWaste;
import org.gsu.hwtttt.mapper.HazardousWasteMapper;
import org.gsu.hwtttt.service.PhysicalPropertyQueryService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 物理特性查询服务实现类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhysicalPropertyQueryServiceImpl implements PhysicalPropertyQueryService {

    private final HazardousWasteMapper hazardousWasteMapper;

    @Override
    public List<PhysicalPropertyCategoryResponse> getPropertyCategories() {
        log.info("获取物理特性分类信息");
        
        List<PhysicalPropertyCategoryResponse> categories = new ArrayList<>();
        
        // 1. 元素组成
        categories.add(createCategoryResponse(
            SystemConstants.PhysicalPropertyCategory.ELEMENT_COMPOSITION,
            "元素组成",
            "碳、氢、氧、氮、硫、磷等元素含量",
            Arrays.asList("cPercent", "hPercent", "oPercent", "nPercent", "sPercent", "pPercent"),
            Arrays.asList(
                new PhysicalPropertyCategoryResponse.FieldInfo("cPercent", "碳含量", "%", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("hPercent", "氢含量", "%", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("oPercent", "氧含量", "%", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("nPercent", "氮含量", "%", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("sPercent", "硫含量", "%", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("pPercent", "磷含量", "%", "BigDecimal")
            )
        ));
        
        // 2. 热值
        categories.add(createCategoryResponse(
            SystemConstants.PhysicalPropertyCategory.HEAT_VALUE,
            "热值",
            "危废的热值特性",
            Arrays.asList("heatValueCalPerG"),
            Arrays.asList(
                new PhysicalPropertyCategoryResponse.FieldInfo("heatValueCalPerG", "热值", "cal/g", "BigDecimal")
            )
        ));
        
        // 3. pH值
        categories.add(createCategoryResponse(
            SystemConstants.PhysicalPropertyCategory.PH,
            "pH值",
            "酸碱度特性",
            Arrays.asList("ph"),
            Arrays.asList(
                new PhysicalPropertyCategoryResponse.FieldInfo("ph", "pH值", "", "BigDecimal")
            )
        ));
        
        // 4. 含水率
        categories.add(createCategoryResponse(
            SystemConstants.PhysicalPropertyCategory.WATER_CONTENT,
            "含水率",
            "水分含量特性",
            Arrays.asList("waterContentPercent"),
            Arrays.asList(
                new PhysicalPropertyCategoryResponse.FieldInfo("waterContentPercent", "含水率", "%", "BigDecimal")
            )
        ));
        
        // 5. 闪点
        categories.add(createCategoryResponse(
            SystemConstants.PhysicalPropertyCategory.FLASH_POINT,
            "闪点",
            "易燃性特性指标",
            Arrays.asList("flashPointCelsius"),
            Arrays.asList(
                new PhysicalPropertyCategoryResponse.FieldInfo("flashPointCelsius", "闪点", "℃", "BigDecimal")
            )
        ));
        
        // 6. 重金属
        categories.add(createCategoryResponse(
            SystemConstants.PhysicalPropertyCategory.HEAVY_METALS,
            "重金属",
            "重金属元素含量",
            Arrays.asList("feMgPerL", "cuMgPerL", "pbMgPerL", "cdMgPerL", "crMgPerL", "niMgPerL", 
                         "mnMgPerL", "snMgPerL", "asMgPerL", "coMgPerL", "sbMgPerL", "tlMgPerL"),
            Arrays.asList(
                new PhysicalPropertyCategoryResponse.FieldInfo("feMgPerL", "铁含量", "mg/L", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("cuMgPerL", "铜含量", "mg/L", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("pbMgPerL", "铅含量", "mg/L", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("cdMgPerL", "镉含量", "mg/L", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("crMgPerL", "铬含量", "mg/L", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("niMgPerL", "镍含量", "mg/L", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("mnMgPerL", "锰含量", "mg/L", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("snMgPerL", "锡含量", "mg/L", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("asMgPerL", "砷含量", "mg/L", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("coMgPerL", "钴含量", "mg/L", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("sbMgPerL", "锑含量", "mg/L", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("tlMgPerL", "铊含量", "mg/L", "BigDecimal")
            )
        ));
        
        // 7. 碱金属
        categories.add(createCategoryResponse(
            SystemConstants.PhysicalPropertyCategory.ALKALI_METALS,
            "碱金属",
            "碱金属元素含量",
            Arrays.asList("kMgPerL", "naMgPerL", "mgMgPerL"),
            Arrays.asList(
                new PhysicalPropertyCategoryResponse.FieldInfo("kMgPerL", "钾含量", "mg/L", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("naMgPerL", "钠含量", "mg/L", "BigDecimal"),
                new PhysicalPropertyCategoryResponse.FieldInfo("mgMgPerL", "镁含量", "mg/L", "BigDecimal")
            )
        ));
        
        return categories;
    }

    @Override
    public PhysicalPropertySearchResponse searchByCategory(PhysicalPropertySearchRequest request) {
        String categoryCode = request.getCategoryCode();
        String search = request.getSearch();
        Long page = request.getPage();
        Long size = request.getSize();

        // Debug logging
        log.info("搜索分类: {}, 关键字: {}, 页码: {}, 大小: {}", categoryCode, search, page, size);

        // Build query conditions based on category
        QueryWrapper<HazardousWaste> queryWrapper = buildQueryWrapper(request);
        
        // Debug the query conditions
        log.info("Query wrapper conditions: {}", queryWrapper.getSqlSegment());

        // Get paginated data
        Page<HazardousWaste> wastePages = new Page<>(page, size);
        Page<HazardousWaste> resultPage = hazardousWasteMapper.selectPage(wastePages, queryWrapper);
        
        log.info("Page result: total={}, records={}, current={}, pages={}", 
                resultPage.getTotal(), resultPage.getRecords().size(), resultPage.getCurrent(), resultPage.getPages());

        // Convert to response
        List<PhysicalPropertySearchResponse.WastePropertyRecord> wasteList = resultPage.getRecords().stream()
            .map(waste -> convertToPropertyRecord(waste, categoryCode))
            .collect(Collectors.toList());

        PhysicalPropertySearchResponse response = new PhysicalPropertySearchResponse();
        response.setTotal(resultPage.getTotal());
        response.setPage(resultPage.getCurrent());
        response.setSize(resultPage.getSize());
        response.setPages(resultPage.getPages());
        response.setRecords(wasteList);

        log.info("Final response: total={}, page={}, size={}, pages={}, records={}", 
                response.getTotal(), response.getPage(), response.getSize(), 
                response.getPages(), response.getRecords().size());

        return response;
    }

    @Override
    public Long getCountByCategory(String categoryCode) {
        return getCountByCategory(categoryCode, null);
    }

    @Override
    public Long getCountByCategory(String categoryCode, String search) {
        log.info("获取分类记录总数: categoryCode={}, search={}", categoryCode, search);
        QueryWrapper<HazardousWaste> queryWrapper = buildQueryWrapper(categoryCode, search);
        Long count = hazardousWasteMapper.selectCount(queryWrapper);
        log.info("Count result: {}", count);
        return count;
    }

    @Override
    public Long getCountByCategory(PhysicalPropertySearchRequest request) {
        log.info("获取分类记录总数（带危害特性过滤）: {}", request);
        QueryWrapper<HazardousWaste> queryWrapper = buildQueryWrapper(request);
        Long count = hazardousWasteMapper.selectCount(queryWrapper);
        log.info("Count result with hazard filters: {}", count);
        return count;
    }

    /**
     * 创建分类响应对象
     */
    private PhysicalPropertyCategoryResponse createCategoryResponse(String categoryCode, String categoryName,
                                                                   String description, List<String> fields,
                                                                   List<PhysicalPropertyCategoryResponse.FieldInfo> fieldInfos) {
        return new PhysicalPropertyCategoryResponse(categoryCode, categoryName, description, fields, fieldInfos);
    }

    /**
     * 构建QueryWrapper查询条件（原有方法，保持向后兼容）
     */
    private QueryWrapper<HazardousWaste> buildQueryWrapper(String categoryCode, String search) {
        PhysicalPropertySearchRequest request = new PhysicalPropertySearchRequest();
        request.setCategoryCode(categoryCode);
        request.setSearch(search);
        return buildQueryWrapper(request);
    }

    /**
     * 构建QueryWrapper查询条件（支持危害特性过滤）
     */
    private QueryWrapper<HazardousWaste> buildQueryWrapper(PhysicalPropertySearchRequest request) {
        QueryWrapper<HazardousWaste> wrapper = new QueryWrapper<>();
        
        // Base conditions
        wrapper.eq("deleted", 0);

        // Category-specific conditions - at least one field must not be null
        String categoryCode = request.getCategoryCode();
        switch (categoryCode) {
            case SystemConstants.PhysicalPropertyCategory.ELEMENT_COMPOSITION:
                wrapper.and(w -> w.isNotNull("c_percent")
                    .or().isNotNull("h_percent")
                    .or().isNotNull("o_percent")
                    .or().isNotNull("n_percent")
                    .or().isNotNull("s_percent")
                    .or().isNotNull("p_percent"));
                break;
            case SystemConstants.PhysicalPropertyCategory.HEAT_VALUE:
                wrapper.isNotNull("heat_value_cal_per_g");
                break;
            case SystemConstants.PhysicalPropertyCategory.PH:
                wrapper.isNotNull("ph");
                break;
            case SystemConstants.PhysicalPropertyCategory.WATER_CONTENT:
                wrapper.isNotNull("water_content_percent");
                break;
            case SystemConstants.PhysicalPropertyCategory.FLASH_POINT:
                wrapper.isNotNull("flash_point_celsius");
                break;
            case SystemConstants.PhysicalPropertyCategory.HEAVY_METALS:
                wrapper.and(w -> w.isNotNull("fe_mg_per_l")
                    .or().isNotNull("cu_mg_per_l")
                    .or().isNotNull("pb_mg_per_l")
                    .or().isNotNull("cd_mg_per_l")
                    .or().isNotNull("cr_mg_per_l")
                    .or().isNotNull("ni_mg_per_l")
                    .or().isNotNull("mn_mg_per_l")
                    .or().isNotNull("sn_mg_per_l")
                    .or().isNotNull("as_mg_per_l")
                    .or().isNotNull("co_mg_per_l")
                    .or().isNotNull("sb_mg_per_l")
                    .or().isNotNull("tl_mg_per_l"));
                break;
            case SystemConstants.PhysicalPropertyCategory.ALKALI_METALS:
                wrapper.and(w -> w.isNotNull("k_mg_per_l")
                    .or().isNotNull("na_mg_per_l")
                    .or().isNotNull("mg_mg_per_l"));
                break;
            default:
                throw new IllegalArgumentException("Invalid category code: " + categoryCode);
        }

        // Search conditions
        String search = request.getSearch();
        if (StringUtils.hasText(search)) {
            wrapper.and(w -> w.like("waste_code", search)
                .or().like("waste_name", search)
                .or().like("source_unit", search));
        }

        // Hazard Properties filters
        if (request.getOxidizing() != null) {
            wrapper.eq("oxidizing", request.getOxidizing() ? 1 : 0);
        }
        if (request.getReducing() != null) {
            wrapper.eq("reducing", request.getReducing() ? 1 : 0);
        }
        if (request.getVolatileProperty() != null) {
            wrapper.eq("volatile", request.getVolatileProperty() ? 1 : 0);
        }
        if (request.getFlammable() != null) {
            wrapper.eq("flammable", request.getFlammable() ? 1 : 0);
        }
        if (request.getToxic() != null) {
            wrapper.eq("toxic", request.getToxic() ? 1 : 0);
        }
        if (request.getReactive() != null) {
            wrapper.eq("reactive", request.getReactive() ? 1 : 0);
        }
        if (request.getInfectious() != null) {
            wrapper.eq("infectious", request.getInfectious() ? 1 : 0);
        }
        if (request.getCorrosive() != null) {
            wrapper.eq("corrosive", request.getCorrosive() ? 1 : 0);
        }
        if (request.getHalogenatedHydrocarbon() != null) {
            wrapper.eq("halogenated_hydrocarbon", request.getHalogenatedHydrocarbon() ? 1 : 0);
        }
        if (request.getCyanideContaining() != null) {
            wrapper.eq("cyanide_containing", request.getCyanideContaining() ? 1 : 0);
        }

        return wrapper;
    }

    /**
     * 转换为属性记录对象
     */
    private PhysicalPropertySearchResponse.WastePropertyRecord convertToPropertyRecord(HazardousWaste waste, String categoryCode) {
        Map<String, Object> properties = new HashMap<>();
        
        // 根据分类提取相应的属性数据
        switch (categoryCode) {
            case SystemConstants.PhysicalPropertyCategory.ELEMENT_COMPOSITION:
                addIfNotNull(properties, "cPercent", waste.getCPercent());
                addIfNotNull(properties, "hPercent", waste.getHPercent());
                addIfNotNull(properties, "oPercent", waste.getOPercent());
                addIfNotNull(properties, "nPercent", waste.getNPercent());
                addIfNotNull(properties, "sPercent", waste.getSPercent());
                addIfNotNull(properties, "pPercent", waste.getPPercent());
                break;
            
            case SystemConstants.PhysicalPropertyCategory.HEAT_VALUE:
                addIfNotNull(properties, "heatValueCalPerG", waste.getHeatValueCalPerG());
                break;
            
            case SystemConstants.PhysicalPropertyCategory.PH:
                addIfNotNull(properties, "ph", waste.getPh());
                break;
            
            case SystemConstants.PhysicalPropertyCategory.WATER_CONTENT:
                addIfNotNull(properties, "waterContentPercent", waste.getWaterContentPercent());
                break;
            
            case SystemConstants.PhysicalPropertyCategory.FLASH_POINT:
                addIfNotNull(properties, "flashPointCelsius", waste.getFlashPointCelsius());
                break;
            
            case SystemConstants.PhysicalPropertyCategory.HEAVY_METALS:
                addIfNotNull(properties, "feMgPerL", waste.getFeMgPerL());
                addIfNotNull(properties, "cuMgPerL", waste.getCuMgPerL());
                addIfNotNull(properties, "pbMgPerL", waste.getPbMgPerL());
                addIfNotNull(properties, "cdMgPerL", waste.getCdMgPerL());
                addIfNotNull(properties, "crMgPerL", waste.getCrMgPerL());
                addIfNotNull(properties, "niMgPerL", waste.getNiMgPerL());
                addIfNotNull(properties, "mnMgPerL", waste.getMnMgPerL());
                addIfNotNull(properties, "snMgPerL", waste.getSnMgPerL());
                addIfNotNull(properties, "asMgPerL", waste.getAsMgPerL());
                addIfNotNull(properties, "coMgPerL", waste.getCoMgPerL());
                addIfNotNull(properties, "sbMgPerL", waste.getSbMgPerL());
                addIfNotNull(properties, "tlMgPerL", waste.getTlMgPerL());
                break;
            
            case SystemConstants.PhysicalPropertyCategory.ALKALI_METALS:
                addIfNotNull(properties, "kMgPerL", waste.getKMgPerL());
                addIfNotNull(properties, "naMgPerL", waste.getNaMgPerL());
                addIfNotNull(properties, "mgMgPerL", waste.getMgMgPerL());
                break;
        }
        
        return new PhysicalPropertySearchResponse.WastePropertyRecord(
            waste.getId(),
            waste.getWasteCode(),
            waste.getSourceUnit(),
            waste.getWasteName(),
            properties
        );
    }

    /**
     * 添加非空属性
     */
    private void addIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }
} 