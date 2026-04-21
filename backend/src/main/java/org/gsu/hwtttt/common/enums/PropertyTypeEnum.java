package org.gsu.hwtttt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 属性类型枚举
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Getter
@AllArgsConstructor
public enum PropertyTypeEnum {

    NUMERIC("numeric", "数值型"),
    TEXT("text", "文本型"),
    BOOLEAN("boolean", "布尔型");

    private final String code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static PropertyTypeEnum getByCode(String code) {
        for (PropertyTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
} 