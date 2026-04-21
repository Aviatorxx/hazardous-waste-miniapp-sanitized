package org.gsu.hwtttt.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 状态转换请求DTO
 *
 * @author WenXin
 * @date 2025/01/07
 */
@Data
@ApiModel(value = "StatusTransitionRequest", description = "状态转换请求")
public class StatusTransitionRequest {

    @ApiModelProperty(value = "会话ID", required = true)
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    @ApiModelProperty(value = "目标状态", required = true)
    @NotBlank(message = "目标状态不能为空")
    @Size(max = 50, message = "状态长度不能超过50个字符")
    private String toStatus;

    @ApiModelProperty(value = "变更原因")
    @Size(max = 500, message = "变更原因长度不能超过500个字符")
    private String changeReason;

    @ApiModelProperty(value = "操作用户")
    @Size(max = 100, message = "操作用户长度不能超过100个字符")
    private String changeUser;

    @ApiModelProperty(value = "是否强制变更（跳过状态验证）", notes = "谨慎使用，仅用于异常恢复")
    private Boolean forceChange = false;
} 