package org.gsu.hwtttt.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 危废配伍计算数据
 *
 * @author WenXin
 * @date 2025年
 */
@Data
@ApiModel(value = "WasteBlendingData", description = "危废配伍计算数据")
public class WasteBlendingData {

    @ApiModelProperty("危废ID")
    private Long wasteId;

    @ApiModelProperty("危废名称")
    private String wasteName;

    @ApiModelProperty("用量(kg)")
    private Double quantity;

    @ApiModelProperty("热值(kJ/kg)")
    private Double heatValue;

    @ApiModelProperty("水分含量(%)")
    private Double moisture;

    @ApiModelProperty("N含量(%)")
    private Double nContent;

    @ApiModelProperty("S含量(%)")
    private Double sContent;

    @ApiModelProperty("Cl含量(%)")
    private Double clContent;

    @ApiModelProperty("F含量(%)")
    private Double fContent;

    @ApiModelProperty("Cd含量(mg/L)")
    private Double cdContent;

    @ApiModelProperty("As含量(mg/L)")
    private Double asContent;

    @ApiModelProperty("Ni含量(mg/L)")
    private Double niContent;

    @ApiModelProperty("Pb含量(mg/L)")
    private Double pbContent;

    @ApiModelProperty("Cr含量(mg/L)")
    private Double crContent;

    @ApiModelProperty("Sn含量(mg/L)")
    private Double snContent;

    @ApiModelProperty("Sb含量(mg/L)")
    private Double sbContent;

    @ApiModelProperty("Cu含量(mg/L)")
    private Double cuContent;

    @ApiModelProperty("Mn含量(mg/L)")
    private Double mnContent;
} 