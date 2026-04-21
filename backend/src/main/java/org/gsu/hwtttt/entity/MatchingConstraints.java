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
 * 配伍算法约束配置表实体类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@TableName("matching_constraints")
@ApiModel(value = "MatchingConstraints", description = "配伍算法约束配置表")
public class MatchingConstraints implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("约束名称")
    @TableField("constraint_name")
    private String constraintName;

    @ApiModelProperty("参数代码")
    @TableField("parameter_code")
    private String parameterCode;

    @ApiModelProperty("最小值")
    @TableField("min_value")
    private BigDecimal minValue;

    @ApiModelProperty("最大值")
    @TableField("max_value")
    private BigDecimal maxValue;

    @ApiModelProperty("单位")
    @TableField("unit")
    private String unit;

    @ApiModelProperty("约束描述")
    @TableField("constraint_desc")
    private String constraintDesc;

    @ApiModelProperty("是否启用")
    @TableField("is_active")
    private Boolean isActive;

    @ApiModelProperty("排序")
    @TableField("sort_order")
    private Integer sortOrder;

    @ApiModelProperty("创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
} 
