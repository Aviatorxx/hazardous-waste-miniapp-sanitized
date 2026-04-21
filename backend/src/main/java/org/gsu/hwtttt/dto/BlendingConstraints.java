package org.gsu.hwtttt.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 配伍约束参数
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@ApiModel(value = "BlendingConstraints", description = "配伍约束参数")
public class BlendingConstraints {

    @ApiModelProperty("最小热值(kJ/kg)")
    private Double minHeatValue;

    @ApiModelProperty("最大热值(kJ/kg)")
    private Double maxHeatValue;

    @ApiModelProperty("最大水分含量(%)")
    private Double maxMoisture;

    @ApiModelProperty("最大N含量(%)")
    private Double maxNContent;

    @ApiModelProperty("最大S含量(%)")
    private Double maxSContent;

    @ApiModelProperty("最大Cl含量(%)")
    private Double maxClContent;

    @ApiModelProperty("最大F含量(%)")
    private Double maxFContent;

    @ApiModelProperty("最大Cd含量(mg/kg)")
    private Double maxCdContent;

    @ApiModelProperty("最大As+Ni含量(mg/kg)")
    private Double maxAsNiTotal;

    @ApiModelProperty("最大Pb含量(mg/kg)")
    private Double maxPbContent;

    @ApiModelProperty("最大Cr+Sn+Sb+Cu+Mn含量(mg/kg)")
    private Double maxCrSnSbCuMnTotal;

    @ApiModelProperty("权重系数A1")
    private Double weightA1;

    @ApiModelProperty("权重系数A2")
    private Double weightA2;

    @ApiModelProperty("权重系数A3")
    private Double weightA3;

    @ApiModelProperty("权重系数A4")
    private Double weightA4;

    @ApiModelProperty("权重系数A5")
    private Double weightA5;

    @ApiModelProperty("权重系数A6")
    private Double weightA6;

    @ApiModelProperty("权重系数A7")
    private Double weightA7;
} 