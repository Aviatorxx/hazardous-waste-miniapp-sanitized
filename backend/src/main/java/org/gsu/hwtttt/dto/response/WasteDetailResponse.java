package org.gsu.hwtttt.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.gsu.hwtttt.entity.HazardousWaste;
import org.gsu.hwtttt.entity.PhysicalProperty;
import org.gsu.hwtttt.entity.ThermalProperty;

import java.util.List;

/**
 * 危废详情响应DTO
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@ApiModel(value = "WasteDetailResponse", description = "危废详情响应")
public class WasteDetailResponse {

    @ApiModelProperty("危废基础信息")
    private HazardousWaste wasteInfo;

    @ApiModelProperty("理化特性列表")
    private List<PhysicalProperty> physicalProperties;

    @ApiModelProperty("热力学特性列表")
    private List<ThermalProperty> thermalProperties;

    @ApiModelProperty("相容性信息列表")
    private List<CompatibilityInfo> compatibilityInfos;

    /**
     * 相容性信息内部类
     */
    @Data
    @ApiModel(value = "CompatibilityInfo", description = "相容性信息")
    public static class CompatibilityInfo {
        
        @ApiModelProperty("相关危废类别")
        private String relatedCategory;
        
        @ApiModelProperty("是否相容")
        private Boolean compatible;
        
        @ApiModelProperty("风险等级")
        private String riskLevel;
        
        @ApiModelProperty("不相容原因")
        private String incompatibleReason;
        
        @ApiModelProperty("安全注意事项")
        private String safetyNotes;
    }
} 