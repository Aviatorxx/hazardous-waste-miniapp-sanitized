package org.gsu.hwtttt.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 物理特性分类响应DTO
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "PhysicalPropertyCategoryResponse", description = "物理特性分类响应")
public class PhysicalPropertyCategoryResponse {

    @ApiModelProperty("分类代码")
    private String categoryCode;

    @ApiModelProperty("分类名称")
    private String categoryName;

    @ApiModelProperty("分类描述")
    private String description;

    @ApiModelProperty("包含的字段列表")
    private List<String> fields;

    @ApiModelProperty("字段显示名称映射")
    private List<FieldInfo> fieldInfos;

    /**
     * 字段信息内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(value = "FieldInfo", description = "字段信息")
    public static class FieldInfo {
        
        @ApiModelProperty("字段名")
        private String fieldName;
        
        @ApiModelProperty("显示名称")
        private String displayName;
        
        @ApiModelProperty("单位")
        private String unit;
        
        @ApiModelProperty("数据类型")
        private String dataType;
    }
} 