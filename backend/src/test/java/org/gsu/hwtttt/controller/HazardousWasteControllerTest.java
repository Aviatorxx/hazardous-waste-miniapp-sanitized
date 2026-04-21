package org.gsu.hwtttt.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gsu.hwtttt.dto.request.BatchStorageUpdateRequest;
import org.gsu.hwtttt.dto.request.WasteSearchRequest;
import org.gsu.hwtttt.dto.response.WasteDetailResponse;
import org.gsu.hwtttt.entity.HazardousWaste;
import org.gsu.hwtttt.service.HazardousWasteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HazardousWasteController.class)
@DisplayName("危废目录模块控制器测试")
class HazardousWasteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HazardousWasteService hazardousWasteService;

    @Autowired
    private ObjectMapper objectMapper;

    private HazardousWaste sampleWaste;
    private WasteDetailResponse sampleWasteDetail;
    private Page<HazardousWaste> samplePage;

    @BeforeEach
    void setUp() {
        // 创建测试用的危废对象
        sampleWaste = HazardousWaste.builder()
                .id(1L)
                .sequenceNo(1)
                .wasteCode("HW01")
                .wasteName("感染性废物")
                .sourceUnit("测试医院")
                .compatibilityCategoryCode("CAT01")
                .appearance("白色固体")
                .harmfulComponents("病原微生物")
                .storageLocation("储存区A-01")
                .remainingStorage(new BigDecimal("500.00"))
                .ph(new BigDecimal("7.2"))
                .heatValueCalPerG(new BigDecimal("3500"))
                .waterContentPercent(new BigDecimal("15.5"))
                .oxidizing(false)
                .reducing(false)
                .flammable(true)
                .toxic(true)
                .reactive(false)
                .infectious(true)
                .corrosive(false)
                .auditStatus("approved")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .createUser("测试用户")
                .updateUser("测试用户")
                .build();

        // 创建测试用的详情响应
        sampleWasteDetail = new WasteDetailResponse();
        sampleWasteDetail.setWasteInfo(sampleWaste);
        sampleWasteDetail.setPhysicalProperties(new ArrayList<>());
        sampleWasteDetail.setThermalProperties(new ArrayList<>());
        sampleWasteDetail.setCompatibilityInfos(new ArrayList<>());

        // 创建测试用的分页对象
        samplePage = new Page<>(1, 20);
        samplePage.setRecords(Arrays.asList(sampleWaste));
        samplePage.setTotal(1);
    }

    @Test
    @DisplayName("搜索危废 - 成功场景")
    void searchWaste_Success() throws Exception {
        when(hazardousWasteService.searchWaste(any(WasteSearchRequest.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/v1/waste-directory/search")
                        .param("current", "1")
                        .param("size", "20")
                        .param("keyword", "感染性"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records[0].waste_code").value("HW01"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("搜索危废 - 参数验证失败")
    void searchWaste_ValidationError() throws Exception {
        mockMvc.perform(get("/api/v1/waste-directory/search")
                        .param("current", "0") // 无效页码
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("快速搜索 - 成功场景")
    void quickSearch_Success() throws Exception {
        when(hazardousWasteService.searchByKeyword(anyString()))
                .thenReturn(Arrays.asList(sampleWaste));

        mockMvc.perform(get("/api/v1/waste-directory/search/quick")
                        .param("keyword", "HW01"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].waste_code").value("HW01"));
    }

    @Test
    @DisplayName("快速搜索 - 关键字为空")
    void quickSearch_EmptyKeyword() throws Exception {
        mockMvc.perform(get("/api/v1/waste-directory/search/quick")
                        .param("keyword", ""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("根据危废代码精确搜索 - 成功场景")
    void searchByWasteCode_Success() throws Exception {
        when(hazardousWasteService.searchByWasteCodeExact(anyString()))
                .thenReturn(Optional.of(sampleWaste));

        mockMvc.perform(get("/api/v1/waste-directory/search/by-code")
                        .param("wasteCode", "HW01"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.waste_code").value("HW01"));
    }

    @Test
    @DisplayName("根据危废代码精确搜索 - 未找到")
    void searchByWasteCode_NotFound() throws Exception {
        when(hazardousWasteService.searchByWasteCodeExact(anyString()))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/waste-directory/search/by-code")
                        .param("wasteCode", "HW99"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("未找到")));
    }

    @Test
    @DisplayName("获取危废详情 - 成功场景")
    void getWasteDetail_Success() throws Exception {
        when(hazardousWasteService.getWasteDetail(anyLong()))
                .thenReturn(sampleWasteDetail);

        mockMvc.perform(get("/api/v1/waste-directory/detail/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.waste_info.id").value(1))
                .andExpect(jsonPath("$.data.waste_info.waste_code").value("HW01"));
    }

    @Test
    @DisplayName("获取危废详情 - ID无效")
    void getWasteDetail_InvalidId() throws Exception {
        mockMvc.perform(get("/api/v1/waste-directory/detail/0"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("获取库存状态 - 成功场景")
    void getStorageStatus_Success() throws Exception {
        when(hazardousWasteService.getById(anyLong()))
                .thenReturn(sampleWaste);

        mockMvc.perform(get("/api/v1/waste-directory/1/storage-status"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.wasteId").value(1))
                .andExpect(jsonPath("$.data.remainingStorage").exists());
    }

    @Test
    @DisplayName("获取库存状态 - 记录不存在")
    void getStorageStatus_NotFound() throws Exception {
        when(hazardousWasteService.getById(anyLong()))
                .thenReturn(null);

        mockMvc.perform(get("/api/v1/waste-directory/999/storage-status"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("危废记录不存在"));
    }

    @Test
    @DisplayName("更新库存 - 成功场景")
    void updateStorage_Success() throws Exception {
        when(hazardousWasteService.updateStorage(anyLong(), any(BigDecimal.class)))
                .thenReturn(true);

        mockMvc.perform(put("/api/v1/waste-directory/1/storage")
                        .param("storage", "300.5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("更新库存 - 负数库存")
    void updateStorage_NegativeValue() throws Exception {
        mockMvc.perform(put("/api/v1/waste-directory/1/storage")
                        .param("storage", "-10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("库存量不能为负数"));
    }

    @Test
    @DisplayName("库存预警查询 - 成功场景")
    void getStorageWarning_Success() throws Exception {
        when(hazardousWasteService.checkStorageWarning(any(BigDecimal.class)))
                .thenReturn(Arrays.asList(sampleWaste));

        mockMvc.perform(get("/api/v1/waste-directory/storage/warning")
                        .param("threshold", "100"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("根据危险特性筛选 - 成功场景")
    void filterByHazardProperties_Success() throws Exception {
        when(hazardousWasteService.filterByHazardProperties(any(Map.class)))
                .thenReturn(Arrays.asList(sampleWaste));

        mockMvc.perform(get("/api/v1/waste-directory/by-properties")
                        .param("flammable", "true")
                        .param("toxic", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("获取危险特性选项 - 成功场景")
    void getHazardProperties_Success() throws Exception {
        Map<String, List<String>> properties = new HashMap<>();
        properties.put("危险特性", Arrays.asList("易燃性", "毒性", "腐蚀性"));
        
        when(hazardousWasteService.getHazardProperties())
                .thenReturn(properties);

        mockMvc.perform(get("/api/v1/waste-directory/properties/options"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("获取统计信息 - 成功场景")
    void getStatistics_Success() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", 100);
        stats.put("typeCount", 50);
        stats.put("totalStorage", new BigDecimal("10000"));
        
        when(hazardousWasteService.getStatistics())
                .thenReturn(stats);

        mockMvc.perform(get("/api/v1/waste-directory/statistics"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalCount").value(100));
    }

    @Test
    @DisplayName("分页列表查询 - 成功场景")
    void getWasteList_Success() throws Exception {
        when(hazardousWasteService.searchWaste(any(WasteSearchRequest.class)))
                .thenReturn(samplePage);

        mockMvc.perform(get("/api/v1/waste-directory/list")
                        .param("current", "1")
                        .param("size", "20")
                        .param("keyword", "测试"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("批量更新库存 - 成功场景")
    void batchUpdateStorage_Success() throws Exception {
        BatchStorageUpdateRequest request = new BatchStorageUpdateRequest();
        Map<Long, BigDecimal> updates = new HashMap<>();
        updates.put(1L, new BigDecimal("200"));
        updates.put(2L, new BigDecimal("300"));
        request.setStorageUpdates(updates);

        when(hazardousWasteService.batchUpdateStorage(any(Map.class)))
                .thenReturn(2);

        mockMvc.perform(post("/api/v1/waste-directory/batch-storage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(2));
    }

    @Test
    @DisplayName("批量更新库存 - 空请求体")
    void batchUpdateStorage_EmptyRequest() throws Exception {
        mockMvc.perform(post("/api/v1/waste-directory/batch-storage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
} 