package org.gsu.hwtttt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作类型枚举
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Getter
@AllArgsConstructor
public enum OperationTypeEnum {

    INSERT("INSERT", "新增"),
    UPDATE("UPDATE", "更新"),
    DELETE("DELETE", "删除"),
    SELECT("SELECT", "查询"),
    IMPORT("IMPORT", "导入"),
    EXPORT("EXPORT", "导出");

    private final String code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static OperationTypeEnum getByCode(String code) {
        for (OperationTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
} 