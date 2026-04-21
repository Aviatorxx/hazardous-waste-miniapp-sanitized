package org.gsu.hwtttt.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 配伍分析结果VO
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@ApiModel(value = "BlendingAnalysisVO", description = "配伍分析结果")
public class BlendingAnalysisVO {

    @ApiModelProperty("分析ID")
    private String analysisId;

    @ApiModelProperty("危废ID列表")
    private List<Long> wasteIds;

    @ApiModelProperty("配比列表")
    private List<Double> proportions;

    @ApiModelProperty("总体可行性")
    private Boolean feasible;

    @ApiModelProperty("可行性评分（0-100）")
    private Double feasibilityScore;

    @ApiModelProperty("风险等级：LOW-低，MEDIUM-中，HIGH-高")
    private String riskLevel;

    @ApiModelProperty("风险评分（0-100）")
    private Double riskScore;

    @ApiModelProperty("预测热值（kJ/kg）")
    private Double predictedHeatingValue;

    @ApiModelProperty("预测含水率（%）")
    private Double predictedMoistureContent;

    @ApiModelProperty("预测含氯量（%）")
    private Double predictedChlorineContent;

    @ApiModelProperty("预测含硫量（%）")
    private Double predictedSulfurContent;

    @ApiModelProperty("预测灰分含量（%）")
    private Double predictedAshContent;

    @ApiModelProperty("兼容性检查结果")
    private Map<String, Boolean> compatibilityCheck;

    @ApiModelProperty("约束违反列表")
    private List<String> constraintViolations;

    @ApiModelProperty("风险因子列表")
    private List<String> riskFactors;

    @ApiModelProperty("优化建议")
    private List<String> optimizationSuggestions;

    @ApiModelProperty("处理建议")
    private String treatmentAdvice;

    @ApiModelProperty("不兼容组合")
    private List<Map<String, Object>> incompatibleCombinations;

    @ApiModelProperty("关键指标")
    private Map<String, Object> keyIndicators;

    @ApiModelProperty("成本估算（元/吨）")
    private Double estimatedCost;

    @ApiModelProperty("处理难度等级（1-5）")
    private Integer difficultyLevel;

    @ApiModelProperty("推荐操作参数")
    private Map<String, Object> recommendedParameters;

    @ApiModelProperty("分析时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime analysisTime;

    @ApiModelProperty("分析耗时（毫秒）")
    private Long analysisTimeMs;
} 