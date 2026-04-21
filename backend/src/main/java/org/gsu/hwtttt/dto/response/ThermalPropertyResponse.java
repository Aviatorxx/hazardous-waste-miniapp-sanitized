package org.gsu.hwtttt.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.gsu.hwtttt.constant.QualityGrade;
import org.gsu.hwtttt.constant.SpectrumType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 热力学特性数据响应DTO
 *
 * @author WenXin
 * @date 2025/06/13
 */
@Data
@ApiModel(value = "ThermalPropertyResponse", description = "热力学特性数据响应")
public class ThermalPropertyResponse {

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("危废ID")
    private Long wasteId;

    @ApiModelProperty("危废名称")
    private String wasteName;

    @ApiModelProperty("分析类型")
    private SpectrumType spectrumType;

    @ApiModelProperty("分析类型描述")
    private String spectrumTypeDesc;

    @ApiModelProperty("测试名称")
    private String testName;

    @ApiModelProperty("图片文件名")
    private String imageFileName;

    @ApiModelProperty("图片文件路径")
    private String imageFilePath;

    @ApiModelProperty("文件大小(字节)")
    private Long imageFileSize;

    @ApiModelProperty("文件MIME类型")
    private String imageMimeType;

    @ApiModelProperty("图片宽度")
    private Integer imageWidth;

    @ApiModelProperty("图片高度")
    private Integer imageHeight;

    @ApiModelProperty("热行为描述")
    private String thermalBehavior;

    @ApiModelProperty("测试条件")
    private String testConditions;

    @ApiModelProperty("温度范围")
    private String temperatureRange;

    @ApiModelProperty("升温速率(℃/min)")
    private BigDecimal heatingRate;

    @ApiModelProperty("测试气氛")
    private String atmosphere;

    @ApiModelProperty("样品质量(mg)")
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
    private QualityGrade qualityGrade;

    @ApiModelProperty("数据质量等级描述")
    private String qualityGradeDesc;

    @ApiModelProperty("同类型测试序号")
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