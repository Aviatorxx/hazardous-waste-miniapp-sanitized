package org.gsu.hwtttt.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 配伍计算响应DTO
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@ApiModel(value = "配伍计算响应", description = "配伍计算响应数据")
public class CompatibilityCalculateResponse {

    /**
     * 计算结果ID
     */
    @ApiModelProperty(value = "计算结果ID")
    private String resultId;

    /**
     * 计算状态(SUCCESS/FAILED/PARTIAL)
     */
    @ApiModelProperty(value = "计算状态")
    private String status;

    /**
     * 计算结果
     */
    @ApiModelProperty(value = "计算结果")
    private List<WasteRatio> wasteRatios;

    /**
     * 预测的混合物特性
     */
    @ApiModelProperty(value = "预测的混合物特性")
    private MixtureProperties mixtureProperties;

    /**
     * 风险评估
     */
    @ApiModelProperty(value = "风险评估")
    private RiskAssessment riskAssessment;

    /**
     * 计算过程
     */
    @ApiModelProperty(value = "计算过程")
    private CalculationProcess calculationProcess;

    /**
     * 建议和注意事项
     */
    @ApiModelProperty(value = "建议和注意事项")
    private List<String> recommendations;

    /**
     * 危废配比结果
     */
    @Data
    @ApiModel(value = "危废配比结果", description = "单个危废的配比结果")
    public static class WasteRatio {

        /**
         * 危废ID
         */
        @ApiModelProperty(value = "危废ID")
        private Long wasteId;

        /**
         * 危废名称
         */
        @ApiModelProperty(value = "危废名称")
        private String wasteName;

        /**
         * 危废代码
         */
        @ApiModelProperty(value = "危废代码")
        private String wasteCode;

        /**
         * 建议数量(吨)
         */
        @ApiModelProperty(value = "建议数量")
        private BigDecimal recommendedQuantity;

        /**
         * 配比百分比(%)
         */
        @ApiModelProperty(value = "配比百分比")
        private BigDecimal ratio;

        /**
         * 质量贡献度
         */
        @ApiModelProperty(value = "质量贡献度")
        private BigDecimal massContribution;
    }

    /**
     * 混合物特性
     */
    @Data
    @ApiModel(value = "混合物特性", description = "预测的混合物特性")
    public static class MixtureProperties {

        /**
         * 预测热值(MJ/kg)
         */
        @ApiModelProperty(value = "预测热值")
        private BigDecimal predictedHeatValue;

        /**
         * 预测水分含量(%)
         */
        @ApiModelProperty(value = "预测水分含量")
        private BigDecimal predictedMoisture;

        /**
         * 预测酸性元素含量(%)
         */
        @ApiModelProperty(value = "预测酸性元素含量")
        private BigDecimal predictedAcidContent;

        /**
         * 预测重金属含量(mg/kg)
         */
        @ApiModelProperty(value = "预测重金属含量")
        private BigDecimal predictedHeavyMetals;

        /**
         * 预测密度(kg/m³)
         */
        @ApiModelProperty(value = "预测密度")
        private BigDecimal predictedDensity;

        /**
         * 预测粘度(Pa·s)
         */
        @ApiModelProperty(value = "预测粘度")
        private BigDecimal predictedViscosity;
    }

    /**
     * 风险评估
     */
    @Data
    @ApiModel(value = "风险评估", description = "配伍风险评估结果")
    public static class RiskAssessment {

        /**
         * 整体风险等级
         */
        @ApiModelProperty(value = "整体风险等级")
        private Integer overallRiskLevel;

        /**
         * 风险描述
         */
        @ApiModelProperty(value = "风险描述")
        private String riskDescription;

        /**
         * 风险因子
         */
        @ApiModelProperty(value = "风险因子")
        private List<String> riskFactors;

        /**
         * 安全措施建议
         */
        @ApiModelProperty(value = "安全措施建议")
        private List<String> safetyMeasures;
    }

    /**
     * 计算过程
     */
    @Data
    @ApiModel(value = "计算过程", description = "详细的计算过程信息")
    public static class CalculationProcess {

        /**
         * 使用的算法
         */
        @ApiModelProperty(value = "使用的算法")
        private String algorithm;

        /**
         * 迭代次数
         */
        @ApiModelProperty(value = "迭代次数")
        private Integer iterations;

        /**
         * 计算耗时(毫秒)
         */
        @ApiModelProperty(value = "计算耗时")
        private Long calculationTime;

        /**
         * 收敛精度
         */
        @ApiModelProperty(value = "收敛精度")
        private BigDecimal convergencePrecision;

        /**
         * 约束条件满足情况
         */
        @ApiModelProperty(value = "约束条件满足情况")
        private List<String> constraintStatus;
    }
} 