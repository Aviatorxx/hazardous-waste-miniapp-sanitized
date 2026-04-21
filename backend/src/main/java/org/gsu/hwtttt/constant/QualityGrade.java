package org.gsu.hwtttt.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据质量等级枚举
 *
 * @author WenXin
 * @date 2025/06/13
 */
@Getter
@AllArgsConstructor
public enum QualityGrade {
    
    A("A", "优秀", "数据完整、准确性高、标准化程度高"),
    B("B", "良好", "数据较完整、准确性较高、部分标准化"),
    C("C", "一般", "数据基本完整、准确性一般、标准化程度低"),
    D("D", "较差", "数据不完整、准确性较低、需要进一步验证");

    @EnumValue
    @JsonValue
    private final String code;
    
    private final String name;
    
    private final String description;

    /**
     * 根据代码获取枚举值
     *
     * @param code 代码
     * @return 枚举值
     */
    public static QualityGrade fromCode(String code) {
        for (QualityGrade grade : values()) {
            if (grade.getCode().equals(code)) {
                return grade;
            }
        }
        throw new IllegalArgumentException("Unknown quality grade code: " + code);
    }

    /**
     * 检查代码是否有效
     *
     * @param code 代码
     * @return 是否有效
     */
    public static boolean isValidCode(String code) {
        for (QualityGrade grade : values()) {
            if (grade.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
} 