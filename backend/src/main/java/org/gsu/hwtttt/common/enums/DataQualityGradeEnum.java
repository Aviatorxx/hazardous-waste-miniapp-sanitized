package org.gsu.hwtttt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据质量等级枚举
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Getter
@AllArgsConstructor
public enum DataQualityGradeEnum {

    A("A", "优秀"),
    B("B", "良好"),
    C("C", "一般"),
    D("D", "较差");

    private final String code;
    private final String desc;

    /**
     * 根据code获取枚举
     */
    public static DataQualityGradeEnum getByCode(String code) {
        for (DataQualityGradeEnum grade : values()) {
            if (grade.getCode().equals(code)) {
                return grade;
            }
        }
        return null;
    }
} 