package org.gsu.hwtttt.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 理化特性详细数据表实体类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("physical_properties")
@ApiModel(value = "PhysicalProperty", description = "理化特性详细数据表")
public class PhysicalProperty implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("危废ID")
    @TableField("waste_id")
    @NotNull(message = "危废ID不能为空")
    private Long wasteId;

    @ApiModelProperty("分类代码")
    @TableField("category_code")
    @NotBlank(message = "分类代码不能为空")
    private String categoryCode;

    @ApiModelProperty("特性名称")
    @TableField("property_name")
    @NotBlank(message = "特性名称不能为空")
    private String propertyName;

    @ApiModelProperty("特性值")
    @TableField("property_value")
    @NotBlank(message = "特性值不能为空")
    private String propertyValue;

    @ApiModelProperty("单位")
    @TableField("property_unit")
    private String propertyUnit;

    @ApiModelProperty("数据类型")
    @TableField("property_type")
    @NotBlank(message = "数据类型不能为空")
    private String propertyType; // numeric, text, boolean

    @ApiModelProperty("最小值")
    @TableField("min_value")
    @DecimalMin(value = "0.0", message = "最小值不能为负数")
    private BigDecimal minValue;

    @ApiModelProperty("最大值")
    @TableField("max_value")
    @DecimalMin(value = "0.0", message = "最大值不能为负数")
    private BigDecimal maxValue;

    @ApiModelProperty("检测方法")
    @TableField("test_method")
    private String testMethod;

    @ApiModelProperty("检测标准")
    @TableField("test_standard")
    private String testStandard;

    @ApiModelProperty("检测日期")
    @TableField("test_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate testDate;

    @ApiModelProperty("检测实验室")
    @TableField("test_lab")
    private String testLab;

    @ApiModelProperty("置信度")
    @TableField("confidence_level")
    @DecimalMin(value = "0.0", message = "置信度不能为负数")
    private BigDecimal confidenceLevel;

    @ApiModelProperty("排序")
    @TableField("sort_order")
    @Positive(message = "排序必须为正数")
    private Integer sortOrder;

    @ApiModelProperty("备注")
    @TableField("remark")
    private String remark;

    @ApiModelProperty("创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ApiModelProperty("数据来源")
    @TableField("data_source")
    private String dataSource;

    @ApiModelProperty("数据质量标识")
    @TableField("quality_flag")
    private String qualityFlag; // excellent, good, fair, poor

    @ApiModelProperty("测量不确定度")
    @TableField("measurement_uncertainty")
    @DecimalMin(value = "0.0", message = "测量不确定度不能为负数")
    private BigDecimal measurementUncertainty;

    @ApiModelProperty("参考文档")
    @TableField("reference_doc")
    private String referenceDoc;
} 
