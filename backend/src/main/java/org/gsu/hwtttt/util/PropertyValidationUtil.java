package org.gsu.hwtttt.util;

import lombok.experimental.UtilityClass;
import org.gsu.hwtttt.entity.PhysicalProperty;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 理化特性数据校验工具类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@UtilityClass
public class PropertyValidationUtil {

    // 数值格式正则表达式
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^-?\\d+(\\.\\d+)?$");
    
    // pH值范围
    private static final BigDecimal PH_MIN = BigDecimal.ZERO;
    private static final BigDecimal PH_MAX = BigDecimal.valueOf(14);
    
    // 百分比范围
    private static final BigDecimal PERCENT_MIN = BigDecimal.ZERO;
    private static final BigDecimal PERCENT_MAX = BigDecimal.valueOf(100);

    /**
     * 校验理化特性数据
     *
     * @param property 理化特性数据
     * @return 校验错误信息列表
     */
    public static List<String> validateProperty(PhysicalProperty property) {
        List<String> errors = new ArrayList<>();
        
        if (property == null) {
            errors.add("理化特性数据不能为空");
            return errors;
        }
        
        // 基础字段校验
        validateBasicFields(property, errors);
        
        // 数值类型校验
        validateNumericValue(property, errors);
        
        // 特定属性校验
        validateSpecificProperty(property, errors);
        
        // 范围校验
        validateValueRange(property, errors);
        
        return errors;
    }

    /**
     * 校验基础字段
     */
    private static void validateBasicFields(PhysicalProperty property, List<String> errors) {
        if (!StringUtils.hasText(property.getPropertyName())) {
            errors.add("特性名称不能为空");
        }
        
        if (!StringUtils.hasText(property.getPropertyValue())) {
            errors.add("特性值不能为空");
        }
        
        if (!StringUtils.hasText(property.getCategoryCode())) {
            errors.add("分类代码不能为空");
        }
        
        if (property.getWasteId() == null) {
            errors.add("危废ID不能为空");
        }
    }

    /**
     * 校验数值类型
     */
    private static void validateNumericValue(PhysicalProperty property, List<String> errors) {
        if ("numeric".equals(property.getPropertyType())) {
            String value = property.getPropertyValue();
            if (StringUtils.hasText(value)) {
                // 提取数值部分
                String numericPart = extractNumericPart(value);
                if (!NUMBER_PATTERN.matcher(numericPart).matches()) {
                    errors.add("数值类型特性值格式错误: " + value);
                }
            }
        }
    }

    /**
     * 校验特定属性
     */
    private static void validateSpecificProperty(PhysicalProperty property, List<String> errors) {
        String propertyName = property.getPropertyName();
        String value = property.getPropertyValue();
        
        if (!StringUtils.hasText(propertyName) || !StringUtils.hasText(value)) {
            return;
        }
        
        try {
            BigDecimal numericValue = parseNumericValue(value);
            if (numericValue == null) {
                return; // 非数值类型，跳过数值校验
            }
            
            // pH值校验
            if ("pH值".equals(propertyName) || "ph".equalsIgnoreCase(propertyName)) {
                if (numericValue.compareTo(PH_MIN) < 0 || numericValue.compareTo(PH_MAX) > 0) {
                    errors.add("pH值应在0-14范围内，当前值: " + numericValue);
                }
            }
            
            // 含水率校验
            else if ("含水率".equals(propertyName) || propertyName.contains("含水")) {
                if (numericValue.compareTo(PERCENT_MIN) < 0 || numericValue.compareTo(PERCENT_MAX) > 0) {
                    errors.add("含水率应在0-100%范围内，当前值: " + numericValue);
                }
            }
            
            // 灰分校验
            else if ("灰分".equals(propertyName) || propertyName.contains("灰分")) {
                if (numericValue.compareTo(PERCENT_MIN) < 0 || numericValue.compareTo(PERCENT_MAX) > 0) {
                    errors.add("灰分应在0-100%范围内，当前值: " + numericValue);
                }
            }
            
            // 热值校验（单位：MJ/kg）
            else if ("热值".equals(propertyName) || propertyName.contains("热值")) {
                if (numericValue.compareTo(BigDecimal.ZERO) < 0 || numericValue.compareTo(BigDecimal.valueOf(50)) > 0) {
                    errors.add("热值应在0-50 MJ/kg范围内，当前值: " + numericValue);
                }
            }
            
            // 闪点校验（单位：℃）
            else if ("闪点".equals(propertyName) || propertyName.contains("闪点")) {
                if (numericValue.compareTo(BigDecimal.valueOf(-50)) < 0 || numericValue.compareTo(BigDecimal.valueOf(500)) > 0) {
                    errors.add("闪点应在-50-500℃范围内，当前值: " + numericValue);
                }
            }
            
        } catch (Exception e) {
            errors.add("特性值解析失败: " + value);
        }
    }

    /**
     * 校验值范围
     */
    private static void validateValueRange(PhysicalProperty property, List<String> errors) {
        if (property.getMinValue() != null && property.getMaxValue() != null) {
            if (property.getMinValue().compareTo(property.getMaxValue()) > 0) {
                errors.add("最小值不能大于最大值");
            }
            
            // 校验当前值是否在范围内
            BigDecimal currentValue = parseNumericValue(property.getPropertyValue());
            if (currentValue != null) {
                if (currentValue.compareTo(property.getMinValue()) < 0) {
                    errors.add("当前值小于最小值: " + currentValue + " < " + property.getMinValue());
                }
                if (currentValue.compareTo(property.getMaxValue()) > 0) {
                    errors.add("当前值大于最大值: " + currentValue + " > " + property.getMaxValue());
                }
            }
        }
        
        // 置信度校验
        if (property.getConfidenceLevel() != null) {
            if (property.getConfidenceLevel().compareTo(BigDecimal.ZERO) < 0 || 
                property.getConfidenceLevel().compareTo(BigDecimal.ONE) > 0) {
                errors.add("置信度应在0-1范围内");
            }
        }
    }

    /**
     * 提取数值部分
     */
    private static String extractNumericPart(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        
        // 去除单位和特殊字符，保留数字、小数点和负号
        return value.replaceAll("[^0-9.-]", "").trim();
    }

    /**
     * 解析数值
     */
    private static BigDecimal parseNumericValue(String value) {
        try {
            String numericPart = extractNumericPart(value);
            if (numericPart.isEmpty()) {
                return null;
            }
            return new BigDecimal(numericPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 校验单位
     */
    public static boolean isValidUnit(String propertyName, String unit) {
        if (!StringUtils.hasText(propertyName) || !StringUtils.hasText(unit)) {
            return true; // 允许空单位
        }
        
        String lowerPropertyName = propertyName.toLowerCase();
        String lowerUnit = unit.toLowerCase();
        
        // pH值应该无单位或者是 "-"
        if (lowerPropertyName.contains("ph")) {
            return "-".equals(unit) || "".equals(unit);
        }
        
        // 含水率应该是百分比
        if (lowerPropertyName.contains("含水")) {
            return lowerUnit.contains("%") || lowerUnit.contains("percent");
        }
        
        // 热值单位
        if (lowerPropertyName.contains("热值")) {
            return lowerUnit.contains("mj/kg") || lowerUnit.contains("kj/kg") || lowerUnit.contains("cal/g");
        }
        
        // 温度单位
        if (lowerPropertyName.contains("闪点") || lowerPropertyName.contains("温度")) {
            return lowerUnit.contains("℃") || lowerUnit.contains("°c") || lowerUnit.contains("celsius");
        }
        
        return true; // 其他情况允许任意单位
    }

    /**
     * 校验测试方法格式
     */
    public static boolean isValidTestMethod(String testMethod) {
        if (!StringUtils.hasText(testMethod)) {
            return true; // 允许空值
        }
        
        // 常见的测试方法格式：GB/T xxxx-xxxx, ISO xxxx, ASTM Dxxxx等
        return testMethod.matches("^(GB/T|GB|ISO|ASTM|EN|JIS|DIN)\\s*[A-Z]*\\d+(-\\d+)?(:\\d+)?$") ||
               testMethod.length() <= 50; // 或者长度限制
    }
} 