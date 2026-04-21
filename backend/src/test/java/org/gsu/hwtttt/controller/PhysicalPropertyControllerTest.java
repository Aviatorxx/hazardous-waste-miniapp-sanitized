package org.gsu.hwtttt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gsu.hwtttt.dto.request.PropertyFilterRequest;
import org.gsu.hwtttt.dto.request.PropertyImportRequest;
import org.gsu.hwtttt.entity.PhysicalProperty;
import org.gsu.hwtttt.service.PhysicalPropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PhysicalPropertyController.class)
@DisplayName("理化性质模块控制器测试")
class PhysicalPropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhysicalPropertyService physicalPropertyService;

    @Autowired
    private ObjectMapper objectMapper;

    private PhysicalProperty sampleProperty;
    private Page<PhysicalProperty> samplePage;

    @BeforeEach
    void setUp() {
        // 创建测试用的理化性质对象
        sampleProperty = new PhysicalProperty();
        sampleProperty.setId(1L);
        sampleProperty.setWasteId(1L);
        sampleProperty.setCategoryCode("HEAT_VALUE");
        sampleProperty.setPropertyName("热值");
        sampleProperty.setPropertyValue("3500");
        sampleProperty.setPropertyUnit("cal/g");
        sampleProperty.setPropertyType("numeric");  // 添加必需的数据类型字段
        sampleProperty.setTestMethod("量热法");

        // 创建测试用的分页对象
        samplePage = new Page<>(1, 20);
        samplePage.setRecords(Arrays.asList(sampleProperty));
        samplePage.setTotal(1);
    }

    @Test
    @DisplayName("按分类搜索理化性质 - 成功场景")
    void searchByCategory_Success() throws Exception {
        when(physicalPropertyService.searchByKeywordAndCategory(anyString(), anyString()))
                .thenReturn(Arrays.asList(sampleProperty));

        mockMvc.perform(get("/api/v2/physical-properties/category/热值/search")
                        .param("keyword", "HW01"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].propertyName").value("热值"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("按分类搜索理化性质 - 关键字为空")
    void searchByCategory_EmptyKeyword() throws Exception {
        mockMvc.perform(get("/api/v2/physical-properties/category/热值/search")
                        .param("keyword", ""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("根据危废ID和分类获取理化性质 - 成功场景")
    void getByCategoryAndWasteId_Success() throws Exception {
        when(physicalPropertyService.getByWasteIdAndCategory(anyLong(), anyString()))
                .thenReturn(Arrays.asList(sampleProperty));

        mockMvc.perform(get("/api/v2/physical-properties/category/热值/by-waste/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].wasteId").value(1));
    }

    @Test
    @DisplayName("根据危废ID和分类获取理化性质 - 无效危废ID")
    void getByCategoryAndWasteId_InvalidWasteId() throws Exception {
        mockMvc.perform(get("/api/v2/physical-properties/category/热值/by-waste/0"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("通用搜索 - 成功场景")
    void searchProperties_Success() throws Exception {
        when(physicalPropertyService.searchProperties(any(PropertyFilterRequest.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/v2/physical-properties/search")
                        .param("current", "1")
                        .param("size", "20")
                        .param("keyword", "热值"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("根据危废ID获取所有理化性质 - 成功场景")
    void getPropertiesByWaste_Success() throws Exception {
        when(physicalPropertyService.getByWasteId(anyLong()))
                .thenReturn(Arrays.asList(sampleProperty));

        mockMvc.perform(get("/api/v2/physical-properties/by-waste/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].wasteId").value(1));
    }

    @Test
    @DisplayName("根据危废代码获取理化性质 - 成功场景")
    void getPropertiesByWasteCode_Success() throws Exception {
        when(physicalPropertyService.getByWasteCode(anyString()))
                .thenReturn(Arrays.asList(sampleProperty));

        mockMvc.perform(get("/api/v2/physical-properties/by-waste-code")
                        .param("wasteCode", "HW01"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].wasteId").value(1));
    }

    @Test
    @DisplayName("根据危废代码获取理化性质 - 代码为空")
    void getPropertiesByWasteCode_EmptyCode() throws Exception {
        mockMvc.perform(get("/api/v2/physical-properties/by-waste-code")
                        .param("wasteCode", ""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("获取所有理化性质分类 - 成功场景")
    void getPropertyCategories_Success() throws Exception {
        when(physicalPropertyService.getAllCategories())
                .thenReturn(Arrays.asList("热值", "pH值", "水分"));

        mockMvc.perform(get("/api/v2/physical-properties/categories"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(3)));
    }

    @Test
    @DisplayName("获取统计信息 - 成功场景")
    void getStatistics_Success() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", 500);
        stats.put("categoryCount", 10);
        
        when(physicalPropertyService.getStatistics())
                .thenReturn(stats);

        mockMvc.perform(get("/api/v2/physical-properties/statistics"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(500));
    }

    @Test
    @DisplayName("按分类获取统计信息 - 成功场景")
    void getStatisticsByCategory_Success() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        Map<String, Integer> categoryStats = new HashMap<>();
        categoryStats.put("热值", 100);
        categoryStats.put("pH值", 80);
        stats.put("categoryDistribution", categoryStats);
        
        when(physicalPropertyService.getStatisticsByCategory())
                .thenReturn(stats);

        mockMvc.perform(get("/api/v2/physical-properties/statistics/by-category"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categoryDistribution").exists());
    }

    @Test
    @DisplayName("添加特性数据 - 成功场景")
    void addProperty_Success() throws Exception {
        when(physicalPropertyService.save(any(PhysicalProperty.class)))
                .thenReturn(true);

        mockMvc.perform(post("/api/v2/physical-properties/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleProperty)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("添加特性数据 - 验证失败")
    void addProperty_ValidationError() throws Exception {
        PhysicalProperty invalidProperty = new PhysicalProperty();
        // 不设置必要字段，导致验证失败

        mockMvc.perform(post("/api/v2/physical-properties/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProperty)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("更新特性数据 - 成功场景")
    void updateProperty_Success() throws Exception {
        when(physicalPropertyService.updateById(any(PhysicalProperty.class)))
                .thenReturn(true);

        mockMvc.perform(put("/api/v2/physical-properties/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleProperty)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("删除特性数据 - 成功场景")
    void deleteProperty_Success() throws Exception {
        when(physicalPropertyService.removeById(anyLong()))
                .thenReturn(true);

        mockMvc.perform(delete("/api/v2/physical-properties/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("删除特性数据 - ID无效")
    void deleteProperty_InvalidId() throws Exception {
        mockMvc.perform(delete("/api/v2/physical-properties/0"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("批量导入数据 - 成功场景")
    void importProperties_Success() throws Exception {
        PropertyImportRequest request = new PropertyImportRequest();
        request.setWasteId(1L);
        request.setProperties(Arrays.asList(sampleProperty));

        Map<String, Object> importResult = new HashMap<>();
        importResult.put("successCount", 1);
        importResult.put("failCount", 0);
        
        when(physicalPropertyService.importProperties(any(PropertyImportRequest.class)))
                .thenReturn(importResult);

        mockMvc.perform(post("/api/v2/physical-properties/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.successCount").value(1));
    }

    @Test
    @DisplayName("导出数据 - 成功场景")
    void exportProperties_Success() throws Exception {
        when(physicalPropertyService.exportProperties(anyString(), any(List.class)))
                .thenReturn("export_file_path.xlsx");

        mockMvc.perform(get("/api/v2/physical-properties/export")
                        .param("categoryCode", "HEAT_VALUE")
                        .param("wasteIds", "1,2,3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("export_file_path.xlsx"));
    }

    @Test
    @DisplayName("高级筛选 - 成功场景")
    void filterProperties_Success() throws Exception {
        PropertyFilterRequest request = new PropertyFilterRequest();
        request.setCurrent(1L);
        request.setSize(20L);
        request.setCategoryCode("HEAT_VALUE");

        when(physicalPropertyService.filterProperties(any(PropertyFilterRequest.class)))
                .thenReturn(samplePage);

        mockMvc.perform(post("/api/v2/physical-properties/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }
} 