package org.gsu.hwtttt.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import org.gsu.hwtttt.constant.QualityGrade;
import org.gsu.hwtttt.constant.SpectrumType;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 热力学特性数据表实体类
 *
 * @author WenXin
 * @date 2025/06/13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("thermal_properties")
@ApiModel(value = "ThermalProperty", description = "热力学特性数据表")
public class ThermalProperty implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("危废ID")
    @TableField("waste_id")
    @NotNull(message = "危废ID不能为空")
    private Long wasteId;

    @ApiModelProperty("分析类型")
    @TableField("spectrum_type")
    @NotNull(message = "分析类型不能为空")
    private SpectrumType spectrumType;

    @ApiModelProperty("测试名称")
    @TableField("test_name")
    @NotBlank(message = "测试名称不能为空")
    @Size(max = 200, message = "测试名称长度不能超过200字符")
    private String testName;

    @ApiModelProperty("图片文件名")
    @TableField("image_file_name")
    @NotBlank(message = "图片文件名不能为空")
    @Size(max = 500, message = "图片文件名长度不能超过500字符")
    private String imageFileName;

    @ApiModelProperty("图片文件路径")
    @TableField("image_file_path")
    @NotBlank(message = "图片文件路径不能为空")
    @Size(max = 1000, message = "图片文件路径长度不能超过1000字符")
    private String imageFilePath;

    @ApiModelProperty("文件大小(字节)")
    @TableField("image_file_size")
    @Min(value = 0, message = "文件大小不能为负数")
    private Long imageFileSize;

    @ApiModelProperty("文件MIME类型")
    @TableField("image_mime_type")
    @Size(max = 100, message = "文件MIME类型长度不能超过100字符")
    private String imageMimeType;

    @ApiModelProperty("图片宽度")
    @TableField("image_width")
    @Min(value = 0, message = "图片宽度不能为负数")
    private Integer imageWidth;

    @ApiModelProperty("图片高度")
    @TableField("image_height")
    @Min(value = 0, message = "图片高度不能为负数")
    private Integer imageHeight;

    @ApiModelProperty("热行为描述")
    @TableField("thermal_behavior")
    private String thermalBehavior;

    @ApiModelProperty("测试条件")
    @TableField("test_conditions")
    private String testConditions;

    @ApiModelProperty("温度范围")
    @TableField("temperature_range")
    @Size(max = 100, message = "温度范围长度不能超过100字符")
    private String temperatureRange;

    @ApiModelProperty("升温速率(℃/min)")
    @TableField("heating_rate")
    @DecimalMin(value = "0.00", message = "升温速率不能为负数")
    @Digits(integer = 4, fraction = 2, message = "升温速率格式不正确")
    private BigDecimal heatingRate;

    @ApiModelProperty("测试气氛")
    @TableField("atmosphere")
    @Size(max = 100, message = "测试气氛长度不能超过100字符")
    private String atmosphere;

    @ApiModelProperty("样品质量(mg)")
    @TableField("sample_mass")
    @DecimalMin(value = "0.0000", message = "样品质量不能为负数")
    @Digits(integer = 4, fraction = 4, message = "样品质量格式不正确")
    private BigDecimal sampleMass;

    @ApiModelProperty("测试日期")
    @TableField("test_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date testDate;

    @ApiModelProperty("测试实验室")
    @TableField("test_lab")
    @Size(max = 200, message = "测试实验室长度不能超过200字符")
    private String testLab;

    @ApiModelProperty("设备型号")
    @TableField("equipment_model")
    @Size(max = 200, message = "设备型号长度不能超过200字符")
    private String equipmentModel;

    @ApiModelProperty("操作员")
    @TableField("operator")
    @Size(max = 100, message = "操作员长度不能超过100字符")
    private String operator;

    @ApiModelProperty("数据质量等级")
    @TableField("quality_grade")
    @Builder.Default
    private QualityGrade qualityGrade = QualityGrade.B;

    @ApiModelProperty("同类型测试序号")
    @TableField("sequence_no")
    @Min(value = 1, message = "序号必须大于0")
    @Builder.Default
    private Integer sequenceNo = 1;

    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    @ApiModelProperty("创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty("更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    // Add custom setter/getter to handle database values
    public void setSpectrumTypeFromDb(String dbValue) {
        this.spectrumType = SpectrumType.fromString(dbValue);
    }
    
    public String getSpectrumTypeForDb() {
        return this.spectrumType != null ? this.spectrumType.getCode() : null;
    }
} 
