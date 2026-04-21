package org.gsu.hwtttt.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统操作日志表实体类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@TableName("operation_logs")
@ApiModel(value = "OperationLog", description = "系统操作日志表")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("操作类型")
    @TableField("operation_type")
    private String operationType; // INSERT, UPDATE, DELETE, SELECT, IMPORT, EXPORT

    @ApiModelProperty("表名")
    @TableField("table_name")
    private String tableName;

    @ApiModelProperty("记录ID")
    @TableField("record_id")
    private Long recordId;

    @ApiModelProperty("操作描述")
    @TableField("operation_desc")
    private String operationDesc;

    @ApiModelProperty("旧数据(JSON)")
    @TableField("old_data")
    private String oldData;

    @ApiModelProperty("新数据(JSON)")
    @TableField("new_data")
    private String newData;

    @ApiModelProperty("操作人")
    @TableField("operator")
    private String operator;

    @ApiModelProperty("操作时间")
    @TableField(value = "operation_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;

    @ApiModelProperty("IP地址")
    @TableField("ip_address")
    private String ipAddress;

    @ApiModelProperty("用户代理")
    @TableField("user_agent")
    private String userAgent;

    @ApiModelProperty("执行时间(毫秒)")
    @TableField("execution_time")
    private Integer executionTime;
} 