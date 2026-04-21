package org.gsu.hwtttt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 热力学分析类型枚举
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Getter
@AllArgsConstructor
public enum ThermalAnalysisType {

    TGA("TGA", "热重分析"),
    DSC("DSC", "差示扫描量热法"),
    DTA("DTA", "差热分析"),
    TMA("TMA", "热机械分析"),
    DMA("DMA", "动态力学分析");

    private final String code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static ThermalAnalysisType getByCode(String code) {
        for (ThermalAnalysisType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
} 