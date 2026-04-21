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
import java.util.Date;

/**
 * 危险废物信息主表实体类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("hazardous_waste")
@ApiModel(value = "HazardousWaste", description = "危险废物信息主表")
public class HazardousWaste implements Serializable {

    private static final long serialVersionUID = 1L;

    // 主键和基础信息
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("序号")
    @TableField("sequence_no")
    private Integer sequenceNo;

    @ApiModelProperty("危废代码")
    @TableField("waste_code")
    private String wasteCode;

    @ApiModelProperty("来源单位")
    @TableField("source_unit")
    private String sourceUnit;

    @ApiModelProperty("危废名称")
    @TableField("waste_name")
    private String wasteName;

    @ApiModelProperty("相容性分类代码")
    @TableField("compatibility_category_code")
    private String compatibilityCategoryCode;

    @ApiModelProperty("外观说明")
    @TableField("appearance")
    private String appearance;

    @ApiModelProperty("有害成分")
    @TableField("harmful_components")
    private String harmfulComponents;

    // 存储管理信息
    @ApiModelProperty("贮存位置")
    @TableField("storage_location")
    private String storageLocation;

    @ApiModelProperty("投加方式")
    @TableField("feeding_method")
    private String feedingMethod;

    @ApiModelProperty("入库时间")
    @TableField("inbound_time")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date inboundTime;

    @ApiModelProperty("剩余贮存量(kg)")
    @TableField("remaining_storage")
    private BigDecimal remainingStorage;

    // 常规指标 - 修正字段映射
    @ApiModelProperty("pH值")
    @TableField("ph")
    private BigDecimal ph;

    @ApiModelProperty("烘前热值(cal/g)")
    @TableField("heat_value_cal_per_g")
    private BigDecimal heatValueCalPerG;

    @ApiModelProperty("含水率(%)")
    @TableField("water_content_percent")
    private BigDecimal waterContentPercent;

    @ApiModelProperty("灰分(%)")
    @TableField("ash_content_percent")
    private BigDecimal ashContentPercent;

    @ApiModelProperty("灰熔点(℃)")
    @TableField("ash_melting_point_celsius")
    private BigDecimal ashMeltingPointCelsius;

    @ApiModelProperty("闪点(℃)")
    @TableField("flash_point_celsius")
    private BigDecimal flashPointCelsius;

    @ApiModelProperty("含硫量(%)")
    @TableField("sulfur_content_percent")
    private BigDecimal sulfurContentPercent;

    // 化学成分含量(%) - 修正字段映射
    @ApiModelProperty("Cl含量(%)")
    @TableField("cl_percent")
    private BigDecimal clPercent;

    @ApiModelProperty("F含量(%)")
    @TableField("f_percent")
    private BigDecimal fPercent;

    @ApiModelProperty("C含量(%)")
    @TableField("c_percent")
    private BigDecimal cPercent;

    @ApiModelProperty("H含量(%)")
    @TableField("h_percent")
    private BigDecimal hPercent;

    @ApiModelProperty("O含量(%)")
    @TableField("o_percent")
    private BigDecimal oPercent;

    @ApiModelProperty("S含量(%)")
    @TableField("s_percent")
    private BigDecimal sPercent;

    @ApiModelProperty("N含量(%)")
    @TableField("n_percent")
    private BigDecimal nPercent;

    @ApiModelProperty("P含量(%)")
    @TableField("p_percent")
    private BigDecimal pPercent;

    // 碱性金属含量(mg/L) - 修正字段映射
    @ApiModelProperty("K含量(mg/L)")
    @TableField("k_mg_per_l")
    private BigDecimal kMgPerL;

    @ApiModelProperty("Na含量(mg/L)")
    @TableField("na_mg_per_l")
    private BigDecimal naMgPerL;

    // 重金属含量(mg/L) - 修正字段映射
    @ApiModelProperty("Mg含量(mg/L)")
    @TableField("mg_mg_per_l")
    private BigDecimal mgMgPerL;

    @ApiModelProperty("Mn含量(mg/L)")
    @TableField("mn_mg_per_l")
    private BigDecimal mnMgPerL;

    @ApiModelProperty("Cu含量(mg/L)")
    @TableField("cu_mg_per_l")
    private BigDecimal cuMgPerL;

    @ApiModelProperty("Cr含量(mg/L)")
    @TableField("cr_mg_per_l")
    private BigDecimal crMgPerL;

    @ApiModelProperty("Ni含量(mg/L)")
    @TableField("ni_mg_per_l")
    private BigDecimal niMgPerL;

    @ApiModelProperty("Pb含量(mg/L)")
    @TableField("pb_mg_per_l")
    private BigDecimal pbMgPerL;

    @ApiModelProperty("Cd含量(mg/L)")
    @TableField("cd_mg_per_l")
    private BigDecimal cdMgPerL;

    @ApiModelProperty("Sn含量(mg/L)")
    @TableField("sn_mg_per_l")
    private BigDecimal snMgPerL;

    @ApiModelProperty("Tl含量(mg/L)")
    @TableField("tl_mg_per_l")
    private BigDecimal tlMgPerL;

    @ApiModelProperty("Sb含量(mg/L)")
    @TableField("sb_mg_per_l")
    private BigDecimal sbMgPerL;

    @ApiModelProperty("Co含量(mg/L)")
    @TableField("co_mg_per_l")
    private BigDecimal coMgPerL;

    @ApiModelProperty("As含量(mg/L)")
    @TableField("as_mg_per_l")
    private BigDecimal asMgPerL;

    @ApiModelProperty("Fe含量(mg/L)")
    @TableField("fe_mg_per_l")
    private BigDecimal feMgPerL;

    // 危险特性（布尔类型）
    @ApiModelProperty("氧化性")
    @TableField("oxidizing")
    private Boolean oxidizing;

    @ApiModelProperty("还原性")
    @TableField("reducing")
    private Boolean reducing;

    @ApiModelProperty("挥发性")
    @TableField("volatile")
    private Boolean volatileProperty;

    @ApiModelProperty("易燃性")
    @TableField("flammable")
    private Boolean flammable;

    @ApiModelProperty("毒性")
    @TableField("toxic")
    private Boolean toxic;

    @ApiModelProperty("反应性")
    @TableField("reactive")
    private Boolean reactive;

    @ApiModelProperty("感染性")
    @TableField("infectious")
    private Boolean infectious;

    @ApiModelProperty("腐蚀性")
    @TableField("corrosive")
    private Boolean corrosive;

    @ApiModelProperty("卤化烃类")
    @TableField("halogenated_hydrocarbon")
    private Boolean halogenatedHydrocarbon;

    @ApiModelProperty("含氰化物废物")
    @TableField("cyanide_containing")
    private Boolean cyanideContaining;

    // 数据质量和审核
    @ApiModelProperty("数据质量评分(0-1)")
    @TableField("data_quality_score")
    private BigDecimal dataQualityScore;

    @ApiModelProperty("审核状态")
    @TableField("audit_status")
    private String auditStatus; // 使用枚举：pending, approved, rejected

    @ApiModelProperty("审核时间")
    @TableField("audit_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    @ApiModelProperty("审核人")
    @TableField("audit_user")
    private String auditUser;

    @ApiModelProperty("审核备注")
    @TableField("audit_notes")
    private String auditNotes;

    // 系统字段
    @ApiModelProperty("创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ApiModelProperty("创建人")
    @TableField(value = "create_user", fill = FieldFill.INSERT)
    private String createUser;

    @ApiModelProperty("更新人")
    @TableField(value = "update_user", fill = FieldFill.INSERT_UPDATE)
    private String updateUser;

    @Version
    @ApiModelProperty("版本号")
    @TableField("version")
    private Integer version;

    @TableLogic
    @ApiModelProperty("删除标志")
    @TableField("deleted")
    private Boolean deleted = false;

    @ApiModelProperty("备注信息")
    @TableField("remark")
    private String remark;
} 
