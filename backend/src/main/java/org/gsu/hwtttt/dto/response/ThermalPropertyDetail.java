package org.gsu.hwtttt.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 热力学特性详细信息响应DTO
 *
 * @author WenXin
 * @date 2025/06/13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "ThermalPropertyDetail", description = "热力学特性详细信息响应")
public class ThermalPropertyDetail {

    @ApiModelProperty("记录ID")
    private Long id;

    @ApiModelProperty("危废ID")
    private Long wasteId;

    @ApiModelProperty("危废代码")
    private String wasteCode;

    @ApiModelProperty("危废名称")
    private String wasteName;

    @ApiModelProperty("来源单位")
    private String sourceUnit;

    @ApiModelProperty("光谱类型")
    private String spectrumType;

    @ApiModelProperty("测试名称")
    private String testName;

    @ApiModelProperty("图像访问URL")
    private String imageUrl;

    @ApiModelProperty("文件名")
    private String fileName;

    @ApiModelProperty("文件大小")
    private Long fileSize;

    @ApiModelProperty("MIME类型")
    private String mimeType;

    @ApiModelProperty("图片宽度")
    private Integer width;

    @ApiModelProperty("图片高度")
    private Integer height;

    @ApiModelProperty("热行为描述")
    private String thermalBehavior;

    @ApiModelProperty("测试条件")
    private String testConditions;

    @ApiModelProperty("温度范围")
    private String temperatureRange;

    @ApiModelProperty("升温速率")
    private BigDecimal heatingRate;

    @ApiModelProperty("测试气氛")
    private String atmosphere;

    @ApiModelProperty("样品质量")
    private BigDecimal sampleMass;

    @ApiModelProperty("测试日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate testDate;

    @ApiModelProperty("测试实验室")
    private String testLab;

    @ApiModelProperty("设备型号")
    private String equipmentModel;

    @ApiModelProperty("操作员")
    private String operator;

    @ApiModelProperty("数据质量等级")
    private String qualityGrade;

    @ApiModelProperty("序号")
    private Integer sequenceNo;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
} 