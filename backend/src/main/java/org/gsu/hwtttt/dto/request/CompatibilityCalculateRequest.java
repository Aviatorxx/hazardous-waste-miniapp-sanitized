package org.gsu.hwtttt.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 配伍计算请求DTO
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@ApiModel(value = "配伍计算请求", description = "配伍计算请求参数")
public class CompatibilityCalculateRequest {

    /**
     * 危废项目列表
     */
    @ApiModelProperty(value = "危废项目列表", required = true)
    @NotEmpty(message = "危废列表不能为空")
    private List<WasteItem> wasteItems;

    /**
     * 目标热值(MJ/kg)
     */
    @ApiModelProperty(value = "目标热值", example = "15.0")
    private BigDecimal targetHeatValue;

    /**
     * 目标水分含量(%)
     */
    @ApiModelProperty(value = "目标水分含量", example = "10.0")
    private BigDecimal targetMoisture;

    /**
     * 最大酸性元素含量(%)
     */
    @ApiModelProperty(value = "最大酸性元素含量", example = "5.0")
    private BigDecimal maxAcidContent;

    /**
     * 最大重金属含量(mg/kg)
     */
    @ApiModelProperty(value = "最大重金属含量", example = "1000.0")
    private BigDecimal maxHeavyMetals;

    /**
     * 计算精度
     */
    @ApiModelProperty(value = "计算精度", example = "0.01")
    private BigDecimal precision = new BigDecimal("0.01");

    /**
     * 计算算法(WEIGHTED/LINEAR/OPTIMIZATION)
     */
    @ApiModelProperty(value = "计算算法", example = "WEIGHTED")
    private String algorithm = "WEIGHTED";

    /**
     * 危废项目
     */
    @Data
    @ApiModel(value = "危废项目", description = "参与配伍的危废项目")
    public static class WasteItem {

        /**
         * 危废ID
         */
        @ApiModelProperty(value = "危废ID", required = true)
        @NotNull(message = "危废ID不能为空")
        private Long wasteId;

        /**
         * 当前数量(吨)
         */
        @ApiModelProperty(value = "当前数量", required = true)
        @NotNull(message = "数量不能为空")
        private BigDecimal quantity;

        /**
         * 最小比例(%)
         */
        @ApiModelProperty(value = "最小比例", example = "0.0")
        private BigDecimal minRatio = BigDecimal.ZERO;

        /**
         * 最大比例(%)
         */
        @ApiModelProperty(value = "最大比例", example = "100.0")
        private BigDecimal maxRatio = new BigDecimal("100");

        /**
         * 优先级(1-5)
         */
        @ApiModelProperty(value = "优先级", example = "3")
        private Integer priority = 3;
    }
} 