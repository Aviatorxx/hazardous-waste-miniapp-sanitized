package org.gsu.hwtttt.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.gsu.hwtttt.constant.QualityGrade;
import org.gsu.hwtttt.constant.SpectrumType;

import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;

/**
 * 热力学特性数据搜索请求DTO
 *
 * @author WenXin
 * @date 2025/06/13
 */
@Data
@ApiModel(value = "ThermalPropertySearchRequest", description = "热力学特性数据搜索请求")
public class ThermalPropertySearchRequest {

    @ApiModelProperty(value = "危废ID")
    private Long wasteId;

    @ApiModelProperty(value = "危废ID列表")
    private List<Long> wasteIds;

    @ApiModelProperty(value = "分析类型")
    private SpectrumType spectrumType;

    @ApiModelProperty(value = "分析类型列表")
    private List<SpectrumType> spectrumTypes;

    @ApiModelProperty(value = "测试名称关键词")
    private String testNameKeyword;

    @ApiModelProperty(value = "数据质量等级")
    private QualityGrade qualityGrade;

    @ApiModelProperty(value = "数据质量等级列表")
    private List<QualityGrade> qualityGrades;

    @ApiModelProperty(value = "测试开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate testDateStart;

    @ApiModelProperty(value = "测试结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate testDateEnd;

    @ApiModelProperty(value = "测试实验室关键词")
    private String testLabKeyword;

    @ApiModelProperty(value = "设备型号关键词")
    private String equipmentModelKeyword;

    @ApiModelProperty(value = "操作员关键词")
    private String operatorKeyword;

    @ApiModelProperty(value = "热行为描述关键词")
    private String thermalBehaviorKeyword;

    @ApiModelProperty(value = "页码")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页大小")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 20;

    @ApiModelProperty(value = "排序字段")
    private String orderBy = "create_time";

    @ApiModelProperty(value = "排序方向(asc/desc)")
    private String orderDirection = "desc";
} 