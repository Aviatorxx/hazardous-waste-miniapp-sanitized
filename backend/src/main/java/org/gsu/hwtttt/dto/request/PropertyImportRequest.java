package org.gsu.hwtttt.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.gsu.hwtttt.entity.PhysicalProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 理化特性导入请求DTO
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@ApiModel(value = "PropertyImportRequest", description = "理化特性导入请求")
public class PropertyImportRequest {

    @ApiModelProperty(value = "危废ID", required = true, example = "1")
    @NotNull(message = "危废ID不能为空")
    private Long wasteId;

    @ApiModelProperty(value = "是否覆盖已有数据", example = "false")
    private Boolean overwrite = false;

    @ApiModelProperty(value = "数据质量检查", example = "true")
    private Boolean qualityCheck = true;

    @ApiModelProperty(value = "理化特性数据列表", required = true)
    @NotNull(message = "理化特性数据不能为空")
    @NotEmpty(message = "理化特性数据不能为空")
    @Valid
    private List<PhysicalProperty> properties;

    @ApiModelProperty(value = "导入备注", example = "批量导入2024年检测数据")
    private String importNote;

    @ApiModelProperty(value = "数据来源", example = "实验室检测报告")
    private String dataSource;
} 