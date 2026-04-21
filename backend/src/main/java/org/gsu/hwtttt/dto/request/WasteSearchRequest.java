package org.gsu.hwtttt.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 危废搜索请求DTO
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@ApiModel(value = "WasteSearchRequest", description = "危废搜索请求")
public class WasteSearchRequest {

    @ApiModelProperty(value = "当前页码", required = true, example = "1")
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    private Long current = 1L;

    @ApiModelProperty(value = "每页大小", required = true, example = "20")
    @NotNull(message = "页大小不能为空")
    @Min(value = 1, message = "页大小必须大于0")
    private Long size = 20L;

    @ApiModelProperty(value = "关键字", example = "医疗废物")
    private String keyword;

    @ApiModelProperty(value = "危废代码", example = "HW01")
    private String wasteCode;

    @ApiModelProperty(value = "危废名称", example = "感染性废物")
    private String wasteName;

    @ApiModelProperty(value = "来源单位", example = "医院")
    private String sourceUnit;

    @ApiModelProperty(value = "审核状态", example = "pending")
    private String auditStatus;

    @ApiModelProperty(value = "危险特性条件")
    private Map<String, Boolean> hazardProperties;

    @ApiModelProperty(value = "最小库存量", example = "0")
    private BigDecimal minStorage;

    @ApiModelProperty(value = "最大库存量", example = "1000")
    private BigDecimal maxStorage;

    @ApiModelProperty(value = "排序字段", example = "create_time")
    private String sortField = "create_time";

    @ApiModelProperty(value = "排序方向", example = "desc")
    private String sortOrder = "desc";

    @Override
    public String toString() {
        return "WasteSearchRequest{" +
                "current=" + current +
                ", size=" + size +
                ", keyword='" + keyword + '\'' +
                ", wasteCode='" + wasteCode + '\'' +
                ", wasteName='" + wasteName + '\'' +
                ", sourceUnit='" + sourceUnit + '\'' +
                ", auditStatus='" + auditStatus + '\'' +
                ", hazardProperties=" + hazardProperties +
                ", minStorage=" + minStorage +
                ", maxStorage=" + maxStorage +
                ", sortField='" + sortField + '\'' +
                ", sortOrder='" + sortOrder + '\'' +
                '}';
    }
} 