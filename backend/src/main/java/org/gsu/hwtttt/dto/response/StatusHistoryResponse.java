package org.gsu.hwtttt.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 状态历史响应DTO
 *
 * @author WenXin
 * @date 2025/01/07
 */
@Data
@ApiModel(value = "StatusHistoryResponse", description = "状态历史响应")
public class StatusHistoryResponse {

    @ApiModelProperty("会话ID")
    private Long sessionId;

    @ApiModelProperty("会话名称")
    private String sessionName;

    @ApiModelProperty("当前状态")
    private String currentStatus;

    @ApiModelProperty("状态变更历史列表")
    private List<StatusChangeDetail> statusHistory;

    @ApiModelProperty("总变更次数")
    private Integer totalChanges;

    @Data
    @ApiModel(value = "StatusChangeDetail", description = "状态变更详情")
    public static class StatusChangeDetail {

        @ApiModelProperty("历史记录ID")
        private Long id;

        @ApiModelProperty("原状态")
        private String fromStatus;

        @ApiModelProperty("目标状态")
        private String toStatus;

        @ApiModelProperty("变更原因")
        private String changeReason;

        @ApiModelProperty("变更时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime changeTime;

        @ApiModelProperty("操作用户")
        private String changeUser;

        @ApiModelProperty("状态持续时长（秒）")
        private Long durationSeconds;
    }

    @Data
    @ApiModel(value = "StatusTransitionInfo", description = "状态转换信息")
    public static class StatusTransitionInfo {

        @ApiModelProperty("当前状态")
        private String currentStatus;

        @ApiModelProperty("可转换的状态列表")
        private List<String> validTransitions;

        @ApiModelProperty("状态说明")
        private String statusDescription;

        @ApiModelProperty("是否为终态")
        private Boolean isFinalState;
    }
} 