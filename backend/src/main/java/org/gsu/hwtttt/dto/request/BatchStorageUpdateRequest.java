package org.gsu.hwtttt.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 批量更新库存请求DTO
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@ApiModel(value = "BatchStorageUpdateRequest", description = "批量更新库存请求")
public class BatchStorageUpdateRequest {

    @ApiModelProperty(value = "库存更新数据", required = true, notes = "Map<危废ID, 新库存量>")
    @NotNull(message = "库存更新数据不能为空")
    @NotEmpty(message = "库存更新数据不能为空")
    private Map<Long, BigDecimal> storageUpdates;
} 