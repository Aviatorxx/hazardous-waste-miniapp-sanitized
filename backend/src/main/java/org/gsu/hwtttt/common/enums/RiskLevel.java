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
public enum RiskLevel {

    LOW(1, "低风险"),
    MEDIUM(2, "中风险"),
    HIGH(3, "高风险"),
    CRITICAL(4, "极高风险");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static RiskLevel getByCode(Integer code) {
        for (RiskLevel level : values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        return null;
    }
} 