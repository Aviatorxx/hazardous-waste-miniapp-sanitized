package org.gsu.hwtttt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gsu.hwtttt.dto.response.PhysicalPropertyCategoryResponse;
import org.gsu.hwtttt.dto.response.PhysicalPropertySearchResponse;
import org.gsu.hwtttt.service.PhysicalPropertyQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PhysicalPropertyQueryController.class)
@DisplayName("物理特性查询控制器测试")
class PhysicalPropertyQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhysicalPropertyQueryService physicalPropertyQueryService;

    @Autowired
    private ObjectMapper objectMapper;

    private PhysicalPropertyCategoryResponse sampleCategory;
    private PhysicalPropertySearchResponse sampleSearchResponse;

    @BeforeEach
    void setUp() {
        // 创建测试用的分类响应
        sampleCategory = new PhysicalPropertyCategoryResponse();
        sampleCategory.setCategoryCode("ELEMENT_COMPOSITION");
        sampleCategory.setCategoryName("元素组成");
        sampleCategory.setDescription("碳、氢、氧、氮、硫、磷等元素含量");
        sampleCategory.setFields(Arrays.asList("cPercent", "hPercent", "oPercent", "nPercent", "sPercent", "pPercent"));
        
        PhysicalPropertyCategoryResponse.FieldInfo fieldInfo = new PhysicalPropertyCategoryResponse.FieldInfo();
        fieldInfo.setFieldName("cPercent");
        fieldInfo.setDisplayName("碳含量");
        fieldInfo.setUnit("%");
        fieldInfo.setDataType("BigDecimal");
        sampleCategory.setFieldInfos(Arrays.asList(fieldInfo));

        // 创建测试用的搜索响应
        PhysicalPropertySearchResponse.WastePropertyRecord record = new PhysicalPropertySearchResponse.WastePropertyRecord();
        record.setId(1L);
        record.setWasteCode("271-001-02");
        record.setSourceUnit("华海立诚");
        record.setWasteName("高沸物");
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("cPercent", 46.60);
        properties.put("hPercent", 3.30);
        record.setProperties(properties);

        sampleSearchResponse = new PhysicalPropertySearchResponse();
        sampleSearchResponse.setTotal(23L);
        sampleSearchResponse.setPage(1L);
        sampleSearchResponse.setSize(20L);
        sampleSearchResponse.setPages(2L);
        sampleSearchResponse.setRecords(Arrays.asList(record));
    }

    @Test
    @DisplayName("获取物理特性分类信息 - 成功场景")
    void getPropertyCategories_Success() throws Exception {
        when(physicalPropertyQueryService.getPropertyCategories())
                .thenReturn(Arrays.asList(sampleCategory));

        mockMvc.perform(get("/api/physical-properties/categories"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].categoryCode").value("ELEMENT_COMPOSITION"))
                .andExpect(jsonPath("$.data[0].categoryName").value("元素组成"));
    }

    @Test
    @DisplayName("根据分类查询物理特性 - 成功场景")
    void searchByCategory_Success() throws Exception {
        when(physicalPropertyQueryService.searchByCategory(any()))
                .thenReturn(sampleSearchResponse);

        mockMvc.perform(get("/api/physical-properties/ELEMENT_COMPOSITION")
                        .param("search", "271-001-02")
                        .param("page", "1")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(23))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].wasteCode").value("271-001-02"));
    }

    @Test
    @DisplayName("根据分类查询物理特性 - 无效分类代码")
    void searchByCategory_InvalidCategoryCode() throws Exception {
        mockMvc.perform(get("/api/physical-properties/INVALID_CATEGORY"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("无效的分类代码")));
    }

    @Test
    @DisplayName("获取分类记录总数 - 成功场景")
    void getCountByCategory_Success() throws Exception {
        when(physicalPropertyQueryService.getCountByCategory(anyString()))
                .thenReturn(23L);

        mockMvc.perform(get("/api/physical-properties/ELEMENT_COMPOSITION/count"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(23));
    }

    @Test
    @DisplayName("获取分类记录总数 - 带搜索关键字")
    void getCountByCategory_WithSearch() throws Exception {
        when(physicalPropertyQueryService.getCountByCategory(anyString(), anyString()))
                .thenReturn(5L);

        mockMvc.perform(get("/api/physical-properties/HEAT_VALUE/count")
                        .param("search", "高沸物"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5));
    }

    @Test
    @DisplayName("获取分类记录总数 - 无效分类代码")
    void getCountByCategory_InvalidCategoryCode() throws Exception {
        mockMvc.perform(get("/api/physical-properties/INVALID_CATEGORY/count"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("无效的分类代码")));
    }
} 