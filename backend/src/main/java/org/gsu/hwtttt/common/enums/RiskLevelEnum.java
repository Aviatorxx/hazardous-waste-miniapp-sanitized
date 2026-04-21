package org.gsu.hwtttt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 风险等级枚举
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Getter
@AllArgsConstructor
public enum RiskLevelEnum {

    LOW("LOW", "低风险"),
    MEDIUM("MEDIUM", "中风险"),
    HIGH("HIGH", "高风险");

    private final String code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static RiskLevelEnum getByCode(String code) {
        for (RiskLevelEnum level : values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        return null;
    }
} 