package org.gsu.hwtttt.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 热力学特性更新请求DTO
 *
 * @author WenXin
 * @date 2025/06/13
 */
@Data
@ApiModel(value = "ThermalPropertyUpdateRequest", description = "热力学特性更新请求")
public class ThermalPropertyUpdateRequest {

    @ApiModelProperty(value = "测试名称")
    @Size(max = 200, message = "测试名称长度不能超过200字符")
    private String testName;

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
    private String qualityGrade;

    @ApiModelProperty(value = "备注")
    private String remark;
} 