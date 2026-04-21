package org.gsu.hwtttt.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 配伍会话状态历史表实体类
 *
 * @author WenXin
 * @date 2025/01/07
 */
@Data
@TableName("matching_session_history")
@ApiModel(value = "MatchingSessionHistory", description = "配伍会话状态历史表")
public class MatchingSessionHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("会话ID")
    @TableField("session_id")
    private Long sessionId;

    @ApiModelProperty("原状态")
    @TableField("from_status")
    private String fromStatus;

    @ApiModelProperty("目标状态")
    @TableField("to_status")
    private String toStatus;

    @ApiModelProperty("变更原因")
    @TableField("change_reason")
    private String changeReason;

    @ApiModelProperty("变更时间")
    @TableField(value = "change_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime changeTime;

    @ApiModelProperty("操作用户")
    @TableField("change_user")
    private String changeUser;

    /**
     * 构造器 - 用于快速创建历史记录
     */
    public MatchingSessionHistory() {}

    /**
     * 构造器 - 用于快速创建历史记录
     */
    public MatchingSessionHistory(Long sessionId, String fromStatus, String toStatus, 
                                String changeReason, String changeUser) {
        this.sessionId = sessionId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changeReason = changeReason;
        this.changeUser = changeUser;
        this.changeTime = LocalDateTime.now();
    }
} 