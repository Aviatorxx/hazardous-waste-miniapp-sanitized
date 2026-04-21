package org.gsu.hwtttt.util;

import org.gsu.hwtttt.constant.SystemConstants;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

/**
 * 状态验证工具类
 * 提供状态验证、转换检查和状态描述功能
 *
 * @author WenXin
 * @date 2025/01/07
 */
public class StatusValidationUtil {

    /**
     * 状态中文描述映射
     */
    private static final Map<String, String> STATUS_DESCRIPTIONS = new HashMap<>();

    /**
     * 状态分类映射
     */
    private static final Map<String, String> STATUS_CATEGORIES = new HashMap<>();

    static {
        // 初始化状态描述
        STATUS_DESCRIPTIONS.put(SystemConstants.MatchingStatus.DRAFT, "草稿状态 - 会话已创建，等待配置");
        STATUS_DESCRIPTIONS.put(SystemConstants.MatchingStatus.WASTE_SELECTED, "废物已选择 - 已选择危废进行配伍");
        STATUS_DESCRIPTIONS.put(SystemConstants.MatchingStatus.COMPATIBILITY_CHECKING, "相容性检查中 - 正在检查废物相容性");
        STATUS_DESCRIPTIONS.put(SystemConstants.MatchingStatus.COMPATIBLE, "相容 - 废物相容性检查通过");
        STATUS_DESCRIPTIONS.put(SystemConstants.MatchingStatus.INCOMPATIBLE, "不相容 - 废物相容性检查失败");
        STATUS_DESCRIPTIONS.put(SystemConstants.MatchingStatus.CALCULATING, "计算中 - 正在执行配伍计算");
        STATUS_DESCRIPTIONS.put(SystemConstants.MatchingStatus.CALCULATION_SUCCESS, "计算成功 - 配伍计算完成");
        STATUS_DESCRIPTIONS.put(SystemConstants.MatchingStatus.CALCULATION_FAILED, "计算失败 - 配伍计算出现错误");
        STATUS_DESCRIPTIONS.put(SystemConstants.MatchingStatus.ARCHIVED, "已归档 - 会话已归档，不可再修改");
        STATUS_DESCRIPTIONS.put(SystemConstants.MatchingStatus.COMPLETED, "已完成 - 兼容旧版本状态");
        STATUS_DESCRIPTIONS.put(SystemConstants.MatchingStatus.FAILED, "失败 - 兼容旧版本状态");

        // 初始化状态分类
        STATUS_CATEGORIES.put(SystemConstants.MatchingStatus.DRAFT, "preparation");
        STATUS_CATEGORIES.put(SystemConstants.MatchingStatus.WASTE_SELECTED, "preparation");
        STATUS_CATEGORIES.put(SystemConstants.MatchingStatus.COMPATIBILITY_CHECKING, "validation");
        STATUS_CATEGORIES.put(SystemConstants.MatchingStatus.COMPATIBLE, "validation");
        STATUS_CATEGORIES.put(SystemConstants.MatchingStatus.INCOMPATIBLE, "validation");
        STATUS_CATEGORIES.put(SystemConstants.MatchingStatus.CALCULATING, "processing");
        STATUS_CATEGORIES.put(SystemConstants.MatchingStatus.CALCULATION_SUCCESS, "completed");
        STATUS_CATEGORIES.put(SystemConstants.MatchingStatus.CALCULATION_FAILED, "failed");
        STATUS_CATEGORIES.put(SystemConstants.MatchingStatus.ARCHIVED, "archived");
        STATUS_CATEGORIES.put(SystemConstants.MatchingStatus.COMPLETED, "completed");
        STATUS_CATEGORIES.put(SystemConstants.MatchingStatus.FAILED, "failed");
    }

    /**
     * 验证状态值是否有效
     *
     * @param status 状态值
     * @return 是否有效
     */
    public static boolean isValidStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return false;
        }

        return STATUS_DESCRIPTIONS.containsKey(status);
    }

    /**
     * 获取状态描述
     *
     * @param status 状态值
     * @return 状态描述
     */
    public static String getStatusDescription(String status) {
        return STATUS_DESCRIPTIONS.getOrDefault(status, "未知状态");
    }

    /**
     * 获取状态分类
     *
     * @param status 状态值
     * @return 状态分类
     */
    public static String getStatusCategory(String status) {
        return STATUS_CATEGORIES.getOrDefault(status, "unknown");
    }

    /**
     * 检查状态是否为终态
     *
     * @param status 状态值
     * @return 是否为终态
     */
    public static boolean isFinalStatus(String status) {
        return SystemConstants.MatchingStatus.ARCHIVED.equals(status);
    }

    /**
     * 检查状态是否为处理中状态
     *
     * @param status 状态值
     * @return 是否为处理中状态
     */
    public static boolean isProcessingStatus(String status) {
        return SystemConstants.MatchingStatus.COMPATIBILITY_CHECKING.equals(status) ||
               SystemConstants.MatchingStatus.CALCULATING.equals(status);
    }

    /**
     * 检查状态是否为成功状态
     *
     * @param status 状态值
     * @return 是否为成功状态
     */
    public static boolean isSuccessStatus(String status) {
        return SystemConstants.MatchingStatus.CALCULATION_SUCCESS.equals(status) ||
               SystemConstants.MatchingStatus.COMPLETED.equals(status);
    }

    /**
     * 检查状态是否为失败状态
     *
     * @param status 状态值
     * @return 是否为失败状态
     */
    public static boolean isFailureStatus(String status) {
        return SystemConstants.MatchingStatus.CALCULATION_FAILED.equals(status) ||
               SystemConstants.MatchingStatus.INCOMPATIBLE.equals(status) ||
               SystemConstants.MatchingStatus.FAILED.equals(status);
    }

    /**
     * 获取所有有效状态
     *
     * @return 有效状态集合
     */
    public static Set<String> getAllValidStatuses() {
        return STATUS_DESCRIPTIONS.keySet();
    }

    /**
     * 根据分类获取状态列表
     *
     * @param category 状态分类
     * @return 状态列表
     */
    public static List<String> getStatusesByCategory(String category) {
        return STATUS_CATEGORIES.entrySet().stream()
            .filter(entry -> category.equals(entry.getValue()))
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 验证状态转换的业务逻辑
     *
     * @param fromStatus 原状态
     * @param toStatus 目标状态
     * @return 验证结果和消息
     */
    public static ValidationResult validateStatusTransition(String fromStatus, String toStatus) {
        if (!isValidStatus(fromStatus)) {
            return ValidationResult.invalid("无效的原状态: " + fromStatus);
        }

        if (!isValidStatus(toStatus)) {
            return ValidationResult.invalid("无效的目标状态: " + toStatus);
        }

        // 检查是否为终态
        if (isFinalStatus(fromStatus)) {
            return ValidationResult.invalid("已归档状态不能转换到其他状态");
        }

        // 相同状态转换
        if (fromStatus.equals(toStatus)) {
            return ValidationResult.valid("状态无变化");
        }

        // 特殊业务逻辑检查
        if (SystemConstants.MatchingStatus.DRAFT.equals(fromStatus) && 
            SystemConstants.MatchingStatus.CALCULATING.equals(toStatus)) {
            return ValidationResult.invalid("草稿状态不能直接进入计算状态，需要先选择废物并检查相容性");
        }

        if (SystemConstants.MatchingStatus.INCOMPATIBLE.equals(fromStatus) && 
            SystemConstants.MatchingStatus.CALCULATING.equals(toStatus)) {
            return ValidationResult.invalid("不相容状态不能进入计算状态，需要重新配置废物");
        }

        return ValidationResult.valid("状态转换验证通过");
    }

    /**
     * 获取状态转换建议
     *
     * @param currentStatus 当前状态
     * @return 转换建议
     */
    public static String getTransitionSuggestion(String currentStatus) {
        switch (currentStatus) {
            case SystemConstants.MatchingStatus.DRAFT:
                return "建议先选择危废进行配伍";
            case SystemConstants.MatchingStatus.WASTE_SELECTED:
                return "建议进行相容性检查";
            case SystemConstants.MatchingStatus.COMPATIBILITY_CHECKING:
                return "等待相容性检查完成";
            case SystemConstants.MatchingStatus.COMPATIBLE:
                return "可以开始配伍计算";
            case SystemConstants.MatchingStatus.INCOMPATIBLE:
                return "需要重新选择废物或调整配伍方案";
            case SystemConstants.MatchingStatus.CALCULATING:
                return "等待计算完成";
            case SystemConstants.MatchingStatus.CALCULATION_SUCCESS:
                return "计算成功，可以查看结果或归档";
            case SystemConstants.MatchingStatus.CALCULATION_FAILED:
                return "计算失败，建议检查参数后重新计算";
            case SystemConstants.MatchingStatus.ARCHIVED:
                return "会话已归档，无法进行操作";
            default:
                return "请检查当前状态是否正确";
        }
    }

    /**
     * 验证结果内部类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult valid(String message) {
            return new ValidationResult(true, message);
        }

        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
} 