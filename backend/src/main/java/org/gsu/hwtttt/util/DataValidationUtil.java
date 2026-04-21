package org.gsu.hwtttt.util;

import org.gsu.hwtttt.constant.SystemConstants;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 数据校验工具类
 *
 * @author WenXin
 * @date 2025/06/10
 */
public class DataValidationUtil {

    // 常用正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^1[3-9]\\d{9}$");
    
    private static final Pattern WASTE_CODE_PATTERN = Pattern.compile(
            "^(HW\\d{2}|SW\\d{2})$");
    
    private static final Pattern NUMERIC_PATTERN = Pattern.compile(
            "^-?\\d+(\\.\\d+)?$");

    /**
     * 校验字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return !StringUtils.hasText(str);
    }

    /**
     * 校验字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return StringUtils.hasText(str);
    }

    /**
     * 校验字符串长度
     */
    public static boolean isValidLength(String str, int maxLength) {
        return str != null && str.length() <= maxLength;
    }

    /**
     * 校验字符串长度范围
     */
    public static boolean isValidLength(String str, int minLength, int maxLength) {
        return str != null && str.length() >= minLength && str.length() <= maxLength;
    }

    /**
     * 校验邮箱格式
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 校验手机号格式
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 校验危废代码格式
     */
    public static boolean isValidWasteCode(String wasteCode) {
        return wasteCode != null && WASTE_CODE_PATTERN.matcher(wasteCode).matches();
    }

    /**
     * 校验数值格式
     */
    public static boolean isNumeric(String str) {
        return str != null && NUMERIC_PATTERN.matcher(str).matches();
    }

    /**
     * 校验BigDecimal范围
     */
    public static boolean isInRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null) return false;
        return (min == null || value.compareTo(min) >= 0) && 
               (max == null || value.compareTo(max) <= 0);
    }

    /**
     * 校验Double范围
     */
    public static boolean isInRange(Double value, Double min, Double max) {
        if (value == null) return false;
        return (min == null || value >= min) && (max == null || value <= max);
    }

    /**
     * 校验Integer范围
     */
    public static boolean isInRange(Integer value, Integer min, Integer max) {
        if (value == null) return false;
        return (min == null || value >= min) && (max == null || value <= max);
    }

    /**
     * 校验正数
     */
    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 校验非负数
     */
    public static boolean isNonNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * 校验百分比（0-100）
     */
    public static boolean isValidPercentage(Double value) {
        return value != null && value >= 0.0 && value <= 100.0;
    }

    /**
     * 校验pH值（0-14）
     */
    public static boolean isValidPH(Double value) {
        return value != null && value >= 0.0 && value <= 14.0;
    }

    /**
     * 校验日期范围
     */
    public static boolean isDateInRange(LocalDate date, LocalDate minDate, LocalDate maxDate) {
        if (date == null) return false;
        return (minDate == null || !date.isBefore(minDate)) && 
               (maxDate == null || !date.isAfter(maxDate));
    }

    /**
     * 校验日期时间范围
     */
    public static boolean isDateTimeInRange(LocalDateTime dateTime, LocalDateTime minDateTime, LocalDateTime maxDateTime) {
        if (dateTime == null) return false;
        return (minDateTime == null || !dateTime.isBefore(minDateTime)) && 
               (maxDateTime == null || !dateTime.isAfter(maxDateTime));
    }

    /**
     * 校验日期不是未来日期
     */
    public static boolean isNotFutureDate(LocalDate date) {
        return date == null || !date.isAfter(LocalDate.now());
    }

    /**
     * 校验列表不为空
     */
    public static boolean isNotEmptyList(List<?> list) {
        return list != null && !list.isEmpty();
    }

    /**
     * 校验列表大小
     */
    public static boolean isValidListSize(List<?> list, int maxSize) {
        return list != null && list.size() <= maxSize;
    }

    /**
     * 校验列表大小范围
     */
    public static boolean isValidListSize(List<?> list, int minSize, int maxSize) {
        return list != null && list.size() >= minSize && list.size() <= maxSize;
    }

    /**
     * 校验热值范围（kJ/kg）
     */
    public static boolean isValidHeatingValue(Double value) {
        return value != null && value >= 0.0 && value <= 50000.0; // 一般危废热值不超过50MJ/kg
    }

    /**
     * 校验含水率（%）
     */
    public static boolean isValidMoistureContent(Double value) {
        return isValidPercentage(value);
    }

    /**
     * 校验含氯量（%）
     */
    public static boolean isValidChlorineContent(Double value) {
        return isValidPercentage(value);
    }

    /**
     * 校验含硫量（%）
     */
    public static boolean isValidSulfurContent(Double value) {
        return isValidPercentage(value);
    }

    /**
     * 校验灰分含量（%）
     */
    public static boolean isValidAshContent(Double value) {
        return isValidPercentage(value);
    }

    /**
     * 校验密度（g/cm³）
     */
    public static boolean isValidDensity(Double value) {
        return value != null && value > 0.0 && value <= 30.0; // 一般物质密度不超过30g/cm³
    }

    /**
     * 校验温度（°C）
     */
    public static boolean isValidTemperature(Double value) {
        return value != null && value >= -273.15 && value <= 3000.0; // 绝对零度到高温范围
    }

    /**
     * 校验文件扩展名
     */
    public static boolean isValidFileExtension(String fileName, String... allowedExtensions) {
        if (isEmpty(fileName)) return false;
        
        String extension = getFileExtension(fileName);
        if (isEmpty(extension)) return false;
        
        for (String allowed : allowedExtensions) {
            if (extension.equalsIgnoreCase(allowed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (isEmpty(fileName)) return "";
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * 校验状态值是否有效
     */
    public static boolean isValidStatus(String status, String... validStatuses) {
        if (isEmpty(status)) return false;
        
        for (String validStatus : validStatuses) {
            if (status.equals(validStatus)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验危废状态
     */
    public static boolean isValidWasteStatus(String status) {
        return isValidStatus(status, 
                "ACTIVE", "INACTIVE", "PENDING", "DISPOSED");
    }

    /**
     * 校验审核状态
     */
    public static boolean isValidAuditStatus(String status) {
        return isValidStatus(status,
                SystemConstants.AuditStatus.PENDING,
                SystemConstants.AuditStatus.APPROVED,
                SystemConstants.AuditStatus.REJECTED);
    }

    /**
     * 校验质量等级
     */
    public static boolean isValidQualityGrade(String grade) {
        return isValidStatus(grade,
                SystemConstants.QualityGrade.A,
                SystemConstants.QualityGrade.B,
                SystemConstants.QualityGrade.C,
                SystemConstants.QualityGrade.D);
    }

    /**
     * 校验质量标识
     */
    public static boolean isValidQualityFlag(String flag) {
        return isValidStatus(flag,
                SystemConstants.QualityFlag.EXCELLENT,
                SystemConstants.QualityFlag.GOOD,
                SystemConstants.QualityFlag.FAIR,
                SystemConstants.QualityFlag.POOR);
    }

    /**
     * 校验风险等级
     */
    public static boolean isValidRiskLevel(String level) {
        return isValidStatus(level,
                SystemConstants.RiskLevel.LOW,
                SystemConstants.RiskLevel.MEDIUM,
                SystemConstants.RiskLevel.HIGH);
    }
} 