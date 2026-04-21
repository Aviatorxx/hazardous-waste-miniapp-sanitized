package org.gsu.hwtttt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核状态枚举
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Getter
@AllArgsConstructor
public enum AuditStatus {

    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝");

    private final Integer code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static AuditStatus getByCode(Integer code) {
        for (AuditStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
} 