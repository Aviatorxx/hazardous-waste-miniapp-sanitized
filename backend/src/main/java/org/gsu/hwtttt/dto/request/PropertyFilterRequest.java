package org.gsu.hwtttt.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 理化特性筛选请求DTO
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@ApiModel(value = "PropertyFilterRequest", description = "理化特性筛选请求")
public class PropertyFilterRequest {

    @ApiModelProperty(value = "当前页码", required = true, example = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    private Long current = 1L;

    @ApiModelProperty(value = "每页大小", required = true, example = "20")
    @NotNull(message = "页大小不能为空")
    @Min(value = 1, message = "页大小必须大于0")
    private Long size = 20L;

    @ApiModelProperty(value = "分类代码", example = "ELEMENT")
    private String categoryCode;

    @ApiModelProperty(value = "特性名称", example = "pH值")
    private String propertyName;

    @ApiModelProperty(value = "特性类型", example = "numeric")
    private String propertyType;

    @ApiModelProperty(value = "最小值", example = "0")
    private BigDecimal minValue;

    @ApiModelProperty(value = "最大值", example = "14")
    private BigDecimal maxValue;

    @ApiModelProperty(value = "检测方法", example = "GB/T")
    private String testMethod;

    @ApiModelProperty(value = "检测标准", example = "GB/T 5009.237-2016")
    private String testStandard;

    @ApiModelProperty(value = "检测实验室", example = "国家实验室")
    private String testLab;

    @ApiModelProperty(value = "检测开始日期", example = "2024-01-01")
    private LocalDate testDateStart;

    @ApiModelProperty(value = "检测结束日期", example = "2024-12-31")
    private LocalDate testDateEnd;

    @ApiModelProperty(value = "最小置信度", example = "0.8")
    private BigDecimal minConfidenceLevel;

    @ApiModelProperty(value = "排序字段", example = "testDate")
    private String sortField = "testDate";

    @ApiModelProperty(value = "排序方向", example = "desc")
    private String sortOrder = "desc";
} 