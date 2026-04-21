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
 * 配伍结果表实体类
 * 
 * Database Schema:
 * - id (primary key)
 * - session_id 
 * - result_status enum('success','failed','warning') NOT NULL
 * - calculated_heat_value decimal(10,2)
 * - calculated_water_content decimal(6,3)
 * - calculated_n_content decimal(6,3)
 * - calculated_s_content decimal(6,3)
 * - calculated_cl_content decimal(6,3)
 * - calculated_f_content decimal(6,3)
 * - calculated_hg_content decimal(12,3)
 * - calculated_cd_content decimal(12,3)
 * - calculated_pb_content decimal(12,3)
 * - calculated_heavy_metals_total decimal(12,3)
 * - constraint_violations text
 * - failure_reasons text
 * - warnings text
 * - calculation_matrix text
 * - optimization_log text
 * - calculation_time timestamp
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("matching_results")
@ApiModel(value = "MatchingResults", description = "配伍结果表")
public class MatchingResults implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("会话ID")
    @TableField("session_id")
    private Long sessionId;

    @ApiModelProperty("结果状态: success-成功, failed-失败, warning-警告")
    @TableField("result_status")
    private String resultStatus; // success, failed, warning

    @ApiModelProperty("计算热值(cal/g)")
    @TableField("calculated_heat_value")
    private BigDecimal calculatedHeatValue;

    @ApiModelProperty("计算含水率(%)")
    @TableField("calculated_water_content")
    private BigDecimal calculatedWaterContent;

    @ApiModelProperty("计算氮含量(%)")
    @TableField("calculated_n_content")
    private BigDecimal calculatedNContent;

    @ApiModelProperty("计算硫含量(%)")
    @TableField("calculated_s_content")
    private BigDecimal calculatedSContent;

    @ApiModelProperty("计算氯含量(%)")
    @TableField("calculated_cl_content")
    private BigDecimal calculatedClContent;

    @ApiModelProperty("计算氟含量(%)")
    @TableField("calculated_f_content")
    private BigDecimal calculatedFContent;

    @ApiModelProperty("计算汞含量(mg/kg)")
    @TableField("calculated_hg_content")
    private BigDecimal calculatedHgContent;

    @ApiModelProperty("计算镉含量(mg/kg)")
    @TableField("calculated_cd_content")
    private BigDecimal calculatedCdContent;

    @ApiModelProperty("计算铅含量(mg/kg)")
    @TableField("calculated_pb_content")
    private BigDecimal calculatedPbContent;

    @ApiModelProperty("计算重金属总量(mg/kg)")
    @TableField("calculated_heavy_metals_total")
    private BigDecimal calculatedHeavyMetalsTotal;

    @ApiModelProperty("约束违反情况(JSON)")
    @TableField("constraint_violations")
    private String constraintViolations;

    @ApiModelProperty("失败原因")
    @TableField("failure_reasons")
    private String failureReasons;

    @ApiModelProperty("警告信息")
    @TableField("warnings")
    private String warnings;

    @ApiModelProperty("计算矩阵(JSON)")
    @TableField("calculation_matrix")
    private String calculationMatrix;

    @ApiModelProperty("优化日志")
    @TableField("optimization_log")
    private String optimizationLog;

    @ApiModelProperty("计算时间")
    @TableField(value = "calculation_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime calculationTime;

    /**
     * 结果状态枚举常量
     */
    public static class ResultStatus {
        public static final String SUCCESS = "success";
        public static final String FAILED = "failed";
        public static final String WARNING = "warning";
    }

    /**
     * 便利方法：判断结果是否成功
     */
    public boolean isSuccess() {
        return ResultStatus.SUCCESS.equals(this.resultStatus);
    }

    /**
     * 便利方法：判断结果是否失败
     */
    public boolean isFailed() {
        return ResultStatus.FAILED.equals(this.resultStatus);
    }

    /**
     * 便利方法：判断结果是否为警告
     */
    public boolean isWarning() {
        return ResultStatus.WARNING.equals(this.resultStatus);
    }

    /**
     * 便利方法：设置结果为成功
     */
    public void setStatusSuccess() {
        this.resultStatus = ResultStatus.SUCCESS;
    }

    /**
     * 便利方法：设置结果为失败
     */
    public void setStatusFailed() {
        this.resultStatus = ResultStatus.FAILED;
    }

    /**
     * 便利方法：设置结果为警告
     */
    public void setStatusWarning() {
        this.resultStatus = ResultStatus.WARNING;
    }
} 
