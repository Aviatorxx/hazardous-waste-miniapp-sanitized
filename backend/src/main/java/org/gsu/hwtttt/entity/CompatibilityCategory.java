package org.gsu.hwtttt.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 危废相容性41类主数据字典实体类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@TableName("compatibility_categories")
@ApiModel(value = "CompatibilityCategory", description = "危废相容性41类主数据字典")
public class CompatibilityCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.INPUT)
    @ApiModelProperty("分类代码")
    private String categoryCode;

    @ApiModelProperty("分类名称(中文)")
    @TableField("category_name_cn")
    private String categoryNameCn;

    @ApiModelProperty("分类名称(英文)")
    @TableField("category_name_en")
    private String categoryNameEn;

    @ApiModelProperty("描述信息")
    @TableField("description")
    private String description;

    @ApiModelProperty("顺序编号1~41")
    @TableField("idx")
    private Integer idx;
} 