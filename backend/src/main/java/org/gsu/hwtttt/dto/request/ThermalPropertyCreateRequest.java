package org.gsu.hwtttt.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.gsu.hwtttt.constant.QualityGrade;
import org.gsu.hwtttt.constant.SpectrumType;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 热力学特性数据创建请求DTO
 *
 * @author WenXin
 * @date 2025/06/13
 */
@Data
@ApiModel(value = "ThermalPropertyCreateRequest", description = "热力学特性数据创建请求")
public class ThermalPropertyCreateRequest {

    @ApiModelProperty(value = "危废ID", required = true)
    @NotNull(message = "危废ID不能为空")
    private Long wasteId;

    @ApiModelProperty(value = "分析类型", required = true)
    @NotNull(message = "分析类型不能为空")
    private SpectrumType spectrumType;

    @ApiModelProperty(value = "测试名称", required = true)
    @NotBlank(message = "测试名称不能为空")
    @Size(max = 200, message = "测试名称长度不能超过200字符")
    private String testName;

    @ApiModelProperty(value = "图片文件名", required = true)
    @NotBlank(message = "图片文件名不能为空")
    @Size(max = 500, message = "图片文件名长度不能超过500字符")
    private String imageFileName;

    @ApiModelProperty(value = "图片文件路径", required = true)
    @NotBlank(message = "图片文件路径不能为空")
    @Size(max = 1000, message = "图片文件路径长度不能超过1000字符")
    private String imageFilePath;

    @ApiModelProperty(value = "文件大小(字节)")
    @Min(value = 0, message = "文件大小不能为负数")
    private Long imageFileSize;

    @ApiModelProperty(value = "文件MIME类型")
    @Size(max = 100, message = "文件MIME类型长度不能超过100字符")
    private String imageMimeType;

    @ApiModelProperty(value = "图片宽度")
    @Min(value = 0, message = "图片宽度不能为负数")
    private Integer imageWidth;

    @ApiModelProperty(value = "图片高度")
    @Min(value = 0, message = "图片高度不能为负数")
    private Integer imageHeight;

    @ApiModelProperty(value = "热行为描述")
    private String thermalBehavior;

    @ApiModelProperty(value = "测试条件")
    private String testConditions;

    @ApiModelProperty(value = "温度范围")
    @Size(max = 100, message = "温度范围长度不能超过100字符")
    private String temperatureRange;

    @ApiModelProperty(value = "升温速率(℃/min)")
    @DecimalMin(value = "0.00", message = "升温速率不能为负数")
    @Digits(integer = 4, fraction = 2, message = "升温速率格式不正确")
    private BigDecimal heatingRate;

    @ApiModelProperty(value = "测试气氛")
    @Size(max = 100, message = "测试气氛长度不能超过100字符")
    private String atmosphere;

    @ApiModelProperty(value = "样品质量(mg)")
    @DecimalMin(value = "0.0000", message = "样品质量不能为负数")
    @Digits(integer = 4, fraction = 4, message = "样品质量格式不正确")
    private BigDecimal sampleMass;

    @ApiModelProperty(value = "测试日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate testDate;

    @ApiModelProperty(value = "测试实验室")
    @Size(max = 200, message = "测试实验室长度不能超过200字符")
    private String testLab;

    @ApiModelProperty(value = "设备型号")
    @Size(max = 200, message = "设备型号长度不能超过200字符")
    private String equipmentModel;

    @ApiModelProperty(value = "操作员")
    @Size(max = 100, message = "操作员长度不能超过100字符")
    private String operator;

    @ApiModelProperty(value = "数据质量等级")
    private QualityGrade qualityGrade = QualityGrade.B;

    @ApiModelProperty(value = "同类型测试序号")
    @Min(value = 1, message = "序号必须大于0")
    private Integer sequenceNo = 1;

    @ApiModelProperty(value = "备注")
    private String remark;
} 