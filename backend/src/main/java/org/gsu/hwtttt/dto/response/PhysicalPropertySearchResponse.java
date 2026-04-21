package org.gsu.hwtttt.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 物理特性搜索响应DTO
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "PhysicalPropertySearchResponse", description = "物理特性搜索响应")
public class PhysicalPropertySearchResponse {

    @ApiModelProperty("总记录数")
    private Long total;

    @ApiModelProperty("当前页码")
    private Long page;

    @ApiModelProperty("每页大小")
    private Long size;

    @ApiModelProperty("总页数")
    private Long pages;

    @ApiModelProperty("危废记录列表")
    private List<WastePropertyRecord> records;

    /**
     * 危废特性记录内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @ApiModel(value = "WastePropertyRecord", description = "危废特性记录")
    public static class WastePropertyRecord {
        
        @ApiModelProperty("危废ID")
        private Long id;
        
        @ApiModelProperty("危废代码")
        private String wasteCode;
        
        @ApiModelProperty("来源单位")
        private String sourceUnit;
        
        @ApiModelProperty("危废名称")
        private String wasteName;
        
        @ApiModelProperty("特性数据")
        private Map<String, Object> properties;
    }
} 