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
import java.time.LocalDateTime;

/**
 * 相容性检查结果表实体类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("compatibility_checks")
@ApiModel(value = "CompatibilityChecks", description = "相容性检查结果表")
public class CompatibilityChecks implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("会话ID")
    @TableField("session_id")
    private Long sessionId;

    @ApiModelProperty("危废组合")
    @TableField("waste_combination")
    private String wastesCombination;

    @ApiModelProperty("检查类型")
    @TableField("check_type")
    private String checkType;

    @ApiModelProperty("检查结果")
    @TableField("check_result")
    private String checkResult;

    @ApiModelProperty("危废ID1")
    @TableField("waste_id_1")
    private Long wasteId1;

    @ApiModelProperty("危废ID2")
    @TableField("waste_id_2")
    private Long wasteId2;

    @ApiModelProperty("是否相容")
    @TableField("compatible")
    private Boolean compatible;

    @ApiModelProperty("风险等级")
    @TableField("risk_level")
    private String riskLevel; // LOW, MEDIUM, HIGH

    @ApiModelProperty("冲突原因")
    @TableField("conflict_reason")
    private String conflictReason;

    @ApiModelProperty("安全注意事项")
    @TableField("safety_notes")
    private String safetyNotes;

    @ApiModelProperty("检查时间")
    @TableField(value = "check_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkTime;

    @ApiModelProperty("创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
} 
