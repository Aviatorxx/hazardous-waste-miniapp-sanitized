package org.gsu.hwtttt.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 危废热力学性质汇总响应DTO
 *
 * @author WenXin
 * @date 2025/06/13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "WasteThermalSummary", description = "危废热力学性质汇总响应")
public class WasteThermalSummary {

    @ApiModelProperty("危废ID")
    private Long wasteId;

    @ApiModelProperty("危废代码")
    private String wasteCode;

    @ApiModelProperty("危废名称")
    private String wasteName;

    @ApiModelProperty("来源单位")
    private String sourceUnit;

    @ApiModelProperty("热力学图像信息列表")
    private List<ThermalImageInfo> images;

    @ApiModelProperty("图像总数")
    private Integer totalImages;

    @ApiModelProperty("最新测试日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate latestTestDate;
} 