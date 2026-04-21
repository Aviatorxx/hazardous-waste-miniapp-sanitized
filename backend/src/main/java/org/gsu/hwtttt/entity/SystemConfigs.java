package org.gsu.hwtttt.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统参数配置表实体类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@TableName("system_configs")
@ApiModel(value = "SystemConfigs", description = "系统参数配置表")
public class SystemConfigs implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("配置组")
    @TableField("config_group")
    private String configGroup;

    @ApiModelProperty("配置键")
    @TableField("config_key")
    private String configKey;

    @ApiModelProperty("配置值")
    @TableField("config_value")
    private String configValue;

    @ApiModelProperty("值类型")
    @TableField("value_type")
    private String valueType; // string, number, boolean, json

    @ApiModelProperty("配置描述")
    @TableField("config_desc")
    private String configDesc;

    @ApiModelProperty("是否只读")
    @TableField("is_readonly")
    private Boolean isReadonly;

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
