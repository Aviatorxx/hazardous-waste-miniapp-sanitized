package org.gsu.hwtttt.common.enums;

import lombok.Getter;

/**
 * 配伍模拟状态枚举
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Getter
public enum BlendingStatusEnum {

    DRAFT("DRAFT", "草稿"),
    CHECKING("CHECKING", "检查中"),
    CALCULATING("CALCULATING", "计算中"),
    SUCCESS("SUCCESS", "成功"),
    FAILED("FAILED", "失败"),
    PARTIAL("PARTIAL", "部分成功");

    private final String code;
    private final String description;

    BlendingStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static BlendingStatusEnum getByCode(String code) {
        for (BlendingStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 验证状态代码是否有效
     */
    public static boolean isValidCode(String code) {
        return getByCode(code) != null;
    }
} 