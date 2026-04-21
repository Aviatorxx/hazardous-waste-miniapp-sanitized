package org.gsu.hwtttt.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配伍详情表实体类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("matching_details")
@ApiModel(value = "MatchingDetails", description = "配伍详情表")
public class MatchingDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("会话ID")
    @TableField("session_id")
    private Long sessionId;

    @ApiModelProperty("危废ID")
    @TableField("waste_id")
    private Long wasteId;

    @ApiModelProperty("计划用量(kg)")
    @TableField("planned_amount")
    private BigDecimal plannedAmount;

    @ApiModelProperty("实际分配量(kg)")
    @TableField("actual_amount")
    private BigDecimal actualAmount;

    @ApiModelProperty("占比(%)")
    @TableField("percentage")
    private BigDecimal percentage;

    @ApiModelProperty("添加时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
} 
