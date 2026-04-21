package org.gsu.hwtttt.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 配伍计算结果
 *
 * @author WenXin
 * @date 2025年
 */
@Data
@ApiModel(value = "BlendingResult", description = "配伍计算结果")
public class BlendingResult {

    @ApiModelProperty("是否可行")
    private boolean feasible;

    @ApiModelProperty("失败原因")
    private String failureReason;

    @ApiModelProperty("总用量(kg)")
    private Double totalQuantity;

    @ApiModelProperty("平均热值(kJ/kg)")
    private Double averageHeatValue;

    @ApiModelProperty("平均水分含量(%)")
    private Double averageMoisture;

    @ApiModelProperty("元素组成")
    private Map<String, Double> elementComposition;

    @ApiModelProperty("重金属含量")
    private Map<String, Double> heavyMetalContent;

    @ApiModelProperty("约束违反情况")
    private List<String> constraintViolations;

    @ApiModelProperty("优化目标值")
    private Double objectiveValue;

    @ApiModelProperty("单项危废用量分配")
    private Map<Long, Double> wasteAllocations;
} 