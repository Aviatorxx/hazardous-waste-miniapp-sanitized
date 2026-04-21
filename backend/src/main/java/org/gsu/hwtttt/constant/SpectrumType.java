package org.gsu.hwtttt.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 光谱分析类型枚举
 *
 * @author WenXin
 * @date 2025/06/13
 */
@Getter
@AllArgsConstructor
public enum SpectrumType {
    
    FTIR("FTIR", "FTIR红外光谱"),
    TG_DSC("TG-DSC", "TGA-DSC热分析"),
    XRF("XRF", "X射线荧光光谱"),
    GC_MS("GC-MS", "气相色谱-质谱联用");

    @EnumValue
    @JsonValue
    private final String code;
    
    private final String name;

    /**
     * 根据代码获取枚举值
     *
     * @param code 代码
     * @return 枚举值
     */
    public static SpectrumType fromCode(String code) {
        for (SpectrumType type : SpectrumType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant for code: " + code);
    }

    /**
     * 检查代码是否有效
     *
     * @param code 代码
     * @return 是否有效
     */
    public static boolean isValidCode(String code) {
        for (SpectrumType type : values()) {
            if (type.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    // Add method to safely convert string to enum
    public static SpectrumType fromString(String value) {
        if (value == null) {
            return null;
        }
        
        // Handle direct enum name match
        try {
            return SpectrumType.valueOf(value.replace("-", "_"));
        } catch (IllegalArgumentException e) {
            // Handle code match
            return fromCode(value);
        }
    }
} 