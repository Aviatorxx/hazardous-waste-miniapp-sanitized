package org.gsu.hwtttt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 光谱类型枚举
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Getter
@AllArgsConstructor
public enum SpectrumTypeEnum {

    XRF("XRF", "X射线荧光光谱"),
    FTIR("FTIR", "傅里叶变换红外光谱"),
    GC_MS("GC-MS", "气相色谱-质谱联用"),
    TGA("TGA", "热重分析"),
    DSC("DSC", "差示扫描量热法"),
    DTA("DTA", "差热分析");

    private final String code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static SpectrumTypeEnum getByCode(String code) {
        for (SpectrumTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
} 