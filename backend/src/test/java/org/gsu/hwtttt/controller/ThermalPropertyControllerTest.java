package org.gsu.hwtttt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.gsu.hwtttt.dto.response.ThermalStatisticsResponse;
import org.gsu.hwtttt.entity.ThermalProperty;
import org.gsu.hwtttt.service.ThermalPropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ThermalPropertyController.class)
@DisplayName("热力学性质模块控制器测试")
class ThermalPropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ThermalPropertyService thermalPropertyService;

    @Autowired
    private ObjectMapper objectMapper;

    private ThermalProperty sampleProperty;
    private ThermalStatisticsResponse sampleStats;

   

    @Test
    @DisplayName("按光谱类型搜索热力学性质 - 成功场景")
    void searchBySpectrumType_Success() throws Exception {
        when(thermalPropertyService.searchByKeywordAndCategory(anyString(), anyString()))
                .thenReturn(Arrays.asList(sampleProperty));

        mockMvc.perform(get("/api/v3/thermal-properties/spectrum/XRF/search")
                        .param("keyword", "测试关键字"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].categoryCode").value("XRF"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("按光谱类型搜索热力学性质 - 关键字为空")
    void searchBySpectrumType_EmptyKeyword() throws Exception {
        mockMvc.perform(get("/api/v3/thermal-properties/spectrum/XRF/search")
                        .param("keyword", ""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("根据危废ID和光谱类型获取热力学性质 - 成功场景")
    void getBySpectrumTypeAndWasteId_Success() throws Exception {
        when(thermalPropertyService.getByWasteIdAndCategory(anyLong(), anyString()))
                .thenReturn(Arrays.asList(sampleProperty));

        mockMvc.perform(get("/api/v3/thermal-properties/spectrum/TGA/by-waste/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].wasteId").value(1));
    }

    @Test
    @DisplayName("根据危废ID和光谱类型获取热力学性质 - 无效危废ID")
    void getBySpectrumTypeAndWasteId_InvalidWasteId() throws Exception {
        mockMvc.perform(get("/api/v3/thermal-properties/spectrum/TGA/by-waste/0"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("根据危废ID获取所有热力学性质 - 成功场景")
    void getThermalPropertiesByWaste_Success() throws Exception {
        when(thermalPropertyService.getByWasteId(anyLong()))
                .thenReturn(Arrays.asList(sampleProperty));

        mockMvc.perform(get("/api/v3/thermal-properties/by-waste/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].wasteId").value(1));
    }

    @Test
    @DisplayName("获取所有支持的光谱类型 - 成功场景")
    void getSupportedSpectrumTypes_Success() throws Exception {
        when(thermalPropertyService.getSupportedSpectrumTypes())
                .thenReturn(Arrays.asList("XRF", "TGA", "DSC", "FTIR"));

        mockMvc.perform(get("/api/v3/thermal-properties/spectrum-types"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(4)));
    }

    @Test
    @DisplayName("获取热力学数据文件路径 - 成功场景")
    void getDataFilePath_Success() throws Exception {
        when(thermalPropertyService.getById(anyLong()))
                .thenReturn(sampleProperty);

        mockMvc.perform(get("/api/v3/thermal-properties/file-path/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.category").value("XRF"))
                .andExpect(jsonPath("$.data.dataFile").exists())
                .andExpect(jsonPath("$.data.testMethod").exists());
    }

    @Test
    @DisplayName("获取热力学数据文件路径 - 记录不存在")
    void getDataFilePath_NotFound() throws Exception {
        when(thermalPropertyService.getById(anyLong()))
                .thenReturn(null);

        mockMvc.perform(get("/api/v3/thermal-properties/file-path/999"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("记录不存在"));
    }

    @Test
    @DisplayName("根据分类获取所有数据文件信息 - 成功场景")
    void getFilesByCategory_Success() throws Exception {
        List<Map<String, Object>> fileInfos = new ArrayList<>();
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("id", 1L);
        fileInfo.put("dataFile", "/path/to/data.txt");
        fileInfo.put("testMethod", "X射线荧光光谱法");
        fileInfos.add(fileInfo);

        when(thermalPropertyService.getFileInfoByCategory(anyString()))
                .thenReturn(fileInfos);

        mockMvc.perform(get("/api/v3/thermal-properties/files/by-category/XRF"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    @DisplayName("获取热力学性质统计信息 - 成功场景")
    void getStatistics_Success() throws Exception {
        when(thermalPropertyService.getStatistics())
                .thenReturn(sampleStats);

        mockMvc.perform(get("/api/v3/thermal-properties/statistics"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(100))
                .andExpect(jsonPath("$.data.wasteCount").value(50))
                .andExpect(jsonPath("$.data.spectrumTypeCount").value(5));
    }

    @Test
    @DisplayName("按分类获取统计信息 - 成功场景")
    void getStatisticsByCategory_Success() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        Map<String, Integer> categoryStats = new HashMap<>();
        categoryStats.put("XRF", 30);
        categoryStats.put("TGA", 25);
        categoryStats.put("DSC", 20);
        stats.put("categoryDistribution", categoryStats);

        when(thermalPropertyService.getStatisticsByCategory())
                .thenReturn(stats);

        mockMvc.perform(get("/api/v3/thermal-properties/statistics/by-category"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categoryDistribution").exists())
                .andExpect(jsonPath("$.data.categoryDistribution.XRF").value(30));
    }

    @Test
    @DisplayName("获取数据完整性统计 - 成功场景")
    void getDataCompletenessStats_Success() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDataPoints", 1000);
        stats.put("completeDataPoints", 850);
        stats.put("completenessPercentage", 85.0);
        stats.put("missingDataByCategory", new HashMap<>());

        when(thermalPropertyService.getDataCompletenessStats())
                .thenReturn(stats);

        mockMvc.perform(get("/api/v3/thermal-properties/statistics/data-completeness"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalDataPoints").value(1000))
                .andExpect(jsonPath("$.data.completenessPercentage").value(85.0));
    }
} 