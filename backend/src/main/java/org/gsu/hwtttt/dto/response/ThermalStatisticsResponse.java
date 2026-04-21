package org.gsu.hwtttt.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 热力学特性统计响应DTO
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@ApiModel(value = "ThermalStatisticsResponse", description = "热力学特性统计响应")
public class ThermalStatisticsResponse {

    @ApiModelProperty("总数据量")
    private Long totalCount;

    @ApiModelProperty("涉及危废数量")
    private Long wasteCount;

    @ApiModelProperty("光谱类型数量")
    private Integer spectrumTypeCount;

    @ApiModelProperty("测试实验室数量")
    private Integer labCount;

    @ApiModelProperty("按光谱类型统计")
    private List<SpectrumTypeStatistics> spectrumTypeStats;

    @ApiModelProperty("按实验室统计")
    private List<LabStatistics> labStats;

    @ApiModelProperty("按月份统计")
    private List<MonthlyStatistics> monthlyStats;

    @ApiModelProperty("数据质量分布")
    private Map<String, Integer> qualityDistribution;

    /**
     * 光谱类型统计
     */
    @Data
    @ApiModel(value = "SpectrumTypeStatistics", description = "光谱类型统计")
    public static class SpectrumTypeStatistics {
        @ApiModelProperty("光谱类型")
        private String spectrumType;

        @ApiModelProperty("数据量")
        private Integer count;

        @ApiModelProperty("危废数量")
        private Integer wasteCount;

        @ApiModelProperty("占比")
        private Double percentage;

        @ApiModelProperty("平均质量等级")
        private String avgQuality;
    }

    /**
     * 实验室统计
     */
    @Data
    @ApiModel(value = "LabStatistics", description = "实验室统计")
    public static class LabStatistics {
        @ApiModelProperty("实验室名称")
        private String labName;

        @ApiModelProperty("数据量")
        private Integer count;

        @ApiModelProperty("危废数量")
        private Integer wasteCount;

        @ApiModelProperty("光谱类型数量")
        private Integer spectrumTypeCount;

        @ApiModelProperty("占比")
        private Double percentage;
    }

    /**
     * 月度统计
     */
    @Data
    @ApiModel(value = "MonthlyStatistics", description = "月度统计")
    public static class MonthlyStatistics {
        @ApiModelProperty("年月")
        private String yearMonth;

        @ApiModelProperty("数据量")
        private Integer count;

        @ApiModelProperty("新增危废数量")
        private Integer newWasteCount;
    }
} 