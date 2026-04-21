package org.gsu.hwtttt.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配伍会话表实体类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@TableName("matching_sessions")
@ApiModel(value = "MatchingSessions", description = "配伍会话表")
public class MatchingSessions implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("会话名称")
    @TableField("session_name")
    private String sessionName;

    @ApiModelProperty("总配伍量(kg)")
    @TableField("total_amount")
    private BigDecimal totalAmount;

    @ApiModelProperty("目标热值(cal/g)")
    @TableField("target_heat_value")
    private BigDecimal targetHeatValue;

    @ApiModelProperty("创建人")
    @TableField(value = "create_user", fill = FieldFill.INSERT)
    private String createUser;

    @ApiModelProperty("创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ApiModelProperty("状态")
    @TableField("status")
    private String status; // draft, calculating, completed, failed

    @TableLogic
    @ApiModelProperty("删除标志")
    @TableField("deleted")
    private Boolean deleted = false;
} 
