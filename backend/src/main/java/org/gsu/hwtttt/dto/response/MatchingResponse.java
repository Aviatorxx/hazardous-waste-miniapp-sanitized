package org.gsu.hwtttt.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 配伍响应DTO
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@ApiModel(value = "MatchingResponse", description = "配伍响应")
public class MatchingResponse {

    @ApiModelProperty("会话ID")
    private Long sessionId;

    @ApiModelProperty("会话名称")
    private String sessionName;

    @ApiModelProperty("配伍状态")
    private String status;

    @ApiModelProperty("是否成功")
    private Boolean success;

    @ApiModelProperty("响应消息")
    private String message;

    @ApiModelProperty("响应数据")
    private Object data;

    @ApiModelProperty("结果描述")
    private String resultDescription;

    @ApiModelProperty("目标热值(cal/g)")
    private BigDecimal targetHeatValue;

    @ApiModelProperty("实际热值(cal/g)")
    private BigDecimal actualHeatValue;

    @ApiModelProperty("总配伍量(kg)")
    private BigDecimal totalAmount;

    @ApiModelProperty("计算时间")
    private LocalDateTime calculationTime;

    @ApiModelProperty("危废详情列表")
    private List<WasteDetail> wasteDetails;

    @ApiModelProperty("违反的约束列表")
    private List<String> constraintViolations;

    @ApiModelProperty("约束检查结果")
    private List<ConstraintResult> constraintResults;

    @ApiModelProperty("相容性检查结果")
    private List<CompatibilityResult> compatibilityResults;

    @ApiModelProperty("配伍指标")
    private MatchingIndicators indicators;

    /**
     * 危废详情内部类
     */
    @Data
    @ApiModel(value = "WasteDetail", description = "危废详情")
    public static class WasteDetail {
        
        @ApiModelProperty("危废ID")
        private Long wasteId;

        @ApiModelProperty("危废代码")
        private String wasteCode;

        @ApiModelProperty("危废名称")
        private String wasteName;

        @ApiModelProperty("用量(kg)")
        private BigDecimal quantity;

        @ApiModelProperty("占比(%)")
        private BigDecimal percentage;

        @ApiModelProperty("热值(cal/g)")
        private BigDecimal heatValue;

        @ApiModelProperty("含水率(%)")
        private BigDecimal waterContent;
    }

    /**
     * 约束检查结果内部类
     */
    @Data
    @ApiModel(value = "ConstraintResult", description = "约束检查结果")
    public static class ConstraintResult {
        
        @ApiModelProperty("约束名称")
        private String constraintName;

        @ApiModelProperty("约束类型")
        private String constraintType;

        @ApiModelProperty("是否通过")
        private Boolean passed;

        @ApiModelProperty("期望值")
        private String expectedValue;

        @ApiModelProperty("实际值")
        private String actualValue;

        @ApiModelProperty("说明")
        private String description;
    }

    /**
     * 相容性检查结果内部类
     */
    @Data
    @ApiModel(value = "CompatibilityResult", description = "相容性检查结果")
    public static class CompatibilityResult {
        
        @ApiModelProperty("危废1ID")
        private Long wasteId1;

        @ApiModelProperty("危废1名称")
        private String wasteName1;

        @ApiModelProperty("危废2ID")
        private Long wasteId2;

        @ApiModelProperty("危废2名称")
        private String wasteName2;

        @ApiModelProperty("是否相容")
        private Boolean compatible;

        @ApiModelProperty("风险等级")
        private String riskLevel;

        @ApiModelProperty("不相容原因")
        private String incompatibleReason;
    }

    /**
     * 配伍指标内部类
     */
    @Data
    @ApiModel(value = "MatchingIndicators", description = "配伍指标")
    public static class MatchingIndicators {
        
        @ApiModelProperty("热值(cal/g)")
        private BigDecimal heatValue;

        @ApiModelProperty("含水率(%)")
        private BigDecimal waterContent;

        @ApiModelProperty("灰分(%)")
        private BigDecimal ashContent;

        @ApiModelProperty("氯含量(%)")
        private BigDecimal chlorineContent;

        @ApiModelProperty("氟含量(%)")
        private BigDecimal fluorineContent;

        @ApiModelProperty("硫含量(%)")
        private BigDecimal sulfurContent;

        @ApiModelProperty("氮含量(%)")
        private BigDecimal nitrogenContent;

        @ApiModelProperty("汞含量(mg/kg) - 固定为0")
        private BigDecimal mercuryContent;

        @ApiModelProperty("镉含量(mg/kg)")
        private BigDecimal cadmiumContent;

        @ApiModelProperty("砷+镍含量(mg/kg)")
        private BigDecimal arsenicNickelContent;

        @ApiModelProperty("铅含量(mg/kg)")
        private BigDecimal leadContent;

        @ApiModelProperty("Cr+Sn+Sb+Cu+Mn重金属总量(mg/kg)")
        private BigDecimal totalHeavyMetals;

        @ApiModelProperty("其他指标")
        private Map<String, BigDecimal> otherIndicators;
    }
} 