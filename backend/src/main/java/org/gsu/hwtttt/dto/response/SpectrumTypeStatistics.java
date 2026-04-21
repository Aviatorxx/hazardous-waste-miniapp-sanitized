package org.gsu.hwtttt.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 光谱类型统计信息响应DTO
 *
 * @author WenXin
 * @date 2025/06/13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "SpectrumTypeStatistics", description = "光谱类型统计信息响应")
public class SpectrumTypeStatistics {

    @ApiModelProperty("光谱类型代码")
    private String spectrumType;

    @ApiModelProperty("类型中文名称")
    private String typeName;

    @ApiModelProperty("类型英文名称")
    private String typeNameEn;

    @ApiModelProperty("关联危废数量")
    private Integer wasteCount;

    @ApiModelProperty("测试次数")
    private Integer testCount;

    @ApiModelProperty("图标")
    private String icon;

    @ApiModelProperty("描述")
    private String description;
} 