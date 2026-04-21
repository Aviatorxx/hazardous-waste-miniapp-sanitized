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
 * 危废相容性矩阵表实体类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("compatibility_matrix")
@ApiModel(value = "CompatibilityMatrix", description = "危废相容性矩阵表")
public class CompatibilityMatrix implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("危废类别1")
    @TableField("waste_category_1")
    private String wasteCategory1;

    @ApiModelProperty("类别1名称")
    @TableField("category_1_name")
    private String category1Name;

    @ApiModelProperty("危废类别2")
    @TableField("waste_category_2")
    private String wasteCategory2;

    @ApiModelProperty("类别2名称")
    @TableField("category_2_name")
    private String category2Name;

    @ApiModelProperty("是否相容(TRUE-相容,FALSE-不相容)")
    @TableField("compatible")
    private Boolean compatible;

    @ApiModelProperty("风险等级")
    @TableField("risk_level")
    private String riskLevel; // LOW, MEDIUM, HIGH

    @ApiModelProperty("不相容原因")
    @TableField("incompatible_reason")
    private String incompatibleReason;

    @ApiModelProperty("安全注意事项")
    @TableField("safety_notes")
    private String safetyNotes;

    @ApiModelProperty("参考标准")
    @TableField("reference_standard")
    private String referenceStandard;

    @ApiModelProperty("创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ApiModelProperty("规则来源")
    @TableField("rule_source")
    private String ruleSource;

    @ApiModelProperty("最后验证时间")
    @TableField("last_verified_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastVerifiedTime;

    @ApiModelProperty("验证人")
    @TableField("verified_by")
    private String verifiedBy;
} 
