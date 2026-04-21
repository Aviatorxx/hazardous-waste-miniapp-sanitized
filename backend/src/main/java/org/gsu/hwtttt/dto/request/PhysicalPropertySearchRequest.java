package org.gsu.hwtttt.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 物理特性搜索请求DTO
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@ApiModel(value = "PhysicalPropertySearchRequest", description = "物理特性搜索请求")
public class PhysicalPropertySearchRequest {

    @ApiModelProperty(value = "特性分类代码", required = true, example = "ELEMENT_COMPOSITION")
    @NotBlank(message = "特性分类代码不能为空")
    private String categoryCode;

    @ApiModelProperty(value = "当前页码", example = "1")
    @Min(value = 1, message = "页码必须大于0")
    private Long page = 1L;

    @ApiModelProperty(value = "每页大小", example = "20")
    @Min(value = 1, message = "页大小必须大于0")
    private Long size = 20L;

    @ApiModelProperty(value = "搜索关键字（危废代码或危废名称）", example = "271-001-02")
    private String search;

    // Hazard Properties (all Boolean with null = no filter)
    @ApiModelProperty(value = "氧化性过滤条件（true=是，false=否，null=不过滤）", example = "true")
    private Boolean oxidizing;

    @ApiModelProperty(value = "还原性过滤条件（true=是，false=否，null=不过滤）", example = "false")
    private Boolean reducing;

    @ApiModelProperty(value = "挥发性过滤条件（true=是，false=否，null=不过滤）", example = "true")
    private Boolean volatileProperty; // avoid "volatile" keyword

    @ApiModelProperty(value = "易燃性过滤条件（true=是，false=否，null=不过滤）", example = "true")
    private Boolean flammable;

    @ApiModelProperty(value = "毒性过滤条件（true=是，false=否，null=不过滤）", example = "false")
    private Boolean toxic;

    @ApiModelProperty(value = "反应性过滤条件（true=是，false=否，null=不过滤）", example = "true")
    private Boolean reactive;

    @ApiModelProperty(value = "感染性过滤条件（true=是，false=否，null=不过滤）", example = "false")
    private Boolean infectious;

    @ApiModelProperty(value = "腐蚀性过滤条件（true=是，false=否，null=不过滤）", example = "true")
    private Boolean corrosive;

    @ApiModelProperty(value = "卤化烃类过滤条件（true=是，false=否，null=不过滤）", example = "false")
    private Boolean halogenatedHydrocarbon;

    @ApiModelProperty(value = "含氰化物废物过滤条件（true=是，false=否，null=不过滤）", example = "false")
    private Boolean cyanideContaining;

    @Override
    public String toString() {
        return "PhysicalPropertySearchRequest{" +
                "categoryCode='" + categoryCode + '\'' +
                ", page=" + page +
                ", size=" + size +
                ", search='" + search + '\'' +
                ", oxidizing=" + oxidizing +
                ", reducing=" + reducing +
                ", volatileProperty=" + volatileProperty +
                ", flammable=" + flammable +
                ", toxic=" + toxic +
                ", reactive=" + reactive +
                ", infectious=" + infectious +
                ", corrosive=" + corrosive +
                ", halogenatedHydrocarbon=" + halogenatedHydrocarbon +
                ", cyanideContaining=" + cyanideContaining +
                '}';
    }
} 