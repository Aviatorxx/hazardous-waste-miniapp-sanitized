package org.gsu.hwtttt.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

/**
 * 配伍请求DTO
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@ApiModel(value = "MatchingRequest", description = "配伍请求")
public class MatchingRequest {

    @ApiModelProperty(value = "会话名称", required = true)
    @NotBlank(message = "会话名称不能为空")
    private String sessionName;

    @ApiModelProperty(value = "目标热值(cal/g)", required = false)
    @Positive(message = "目标热值必须大于0")
    private BigDecimal targetHeatValue;

    @ApiModelProperty(value = "总配伍量(kg)", required = false)
    @Positive(message = "总配伍量必须大于0")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "创建用户", required = true)
    @NotBlank(message = "创建用户不能为空")
    private String createUser;

    @ApiModelProperty(value = "危废列表")
    private List<WasteItem> wasteItems;

    /**
     * 危废项目内部类
     */
    @Data
    @ApiModel(value = "WasteItem", description = "危废项目")
    public static class WasteItem {
        
        @ApiModelProperty(value = "危废ID", required = true)
        @NotNull(message = "危废ID不能为空")
        private Long wasteId;

        @ApiModelProperty(value = "用量(kg)", required = true)
        @NotNull(message = "用量不能为空")
        @Positive(message = "用量必须大于0")
        private BigDecimal quantity;

        @ApiModelProperty(value = "备注")
        private String remarks;
    }
} 