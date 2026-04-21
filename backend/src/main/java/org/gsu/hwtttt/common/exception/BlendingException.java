package org.gsu.hwtttt.common.exception;

/**
 * 配伍模拟异常类
 *
 * @author WenXin
 * @date 2025/06/10
 */
public class BlendingException extends RuntimeException {

    private String errorCode;
    private String errorMessage;

    public BlendingException(String message) {
        super(message);
        this.errorMessage = message;
    }

    public BlendingException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public BlendingException(String message, Throwable cause) {
        super(message, cause);
        this.errorMessage = message;
    }

    public BlendingException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * 配伍异常类型枚举
     */
    public enum BlendingErrorType {
        INCOMPATIBLE_WASTES("INCOMPATIBLE_WASTES", "危废不相容"),
        INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", "库存不足"),
        CONSTRAINT_VIOLATION("CONSTRAINT_VIOLATION", "约束条件不满足"),
        NO_FEASIBLE_SOLUTION("NO_FEASIBLE_SOLUTION", "无可行解"),
        CALCULATION_TIMEOUT("CALCULATION_TIMEOUT", "计算超时"),
        INVALID_INPUT("INVALID_INPUT", "输入参数无效"),
        DATA_NOT_FOUND("DATA_NOT_FOUND", "数据不存在"),
        ALGORITHM_ERROR("ALGORITHM_ERROR", "算法计算错误"),
        SYSTEM_ERROR("SYSTEM_ERROR", "系统错误");

        private final String code;
        private final String message;

        BlendingErrorType(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 创建不相容异常
     */
    public static BlendingException incompatibleWastes(String message) {
        return new BlendingException(BlendingErrorType.INCOMPATIBLE_WASTES.getCode(), message);
    }

    /**
     * 创建库存不足异常
     */
    public static BlendingException insufficientStock(String message) {
        return new BlendingException(BlendingErrorType.INSUFFICIENT_STOCK.getCode(), message);
    }

    /**
     * 创建约束违反异常
     */
    public static BlendingException constraintViolation(String message) {
        return new BlendingException(BlendingErrorType.CONSTRAINT_VIOLATION.getCode(), message);
    }

    /**
     * 创建无可行解异常
     */
    public static BlendingException noFeasibleSolution(String message) {
        return new BlendingException(BlendingErrorType.NO_FEASIBLE_SOLUTION.getCode(), message);
    }

    /**
     * 创建输入无效异常
     */
    public static BlendingException invalidInput(String message) {
        return new BlendingException(BlendingErrorType.INVALID_INPUT.getCode(), message);
    }

    /**
     * 创建数据不存在异常
     */
    public static BlendingException dataNotFound(String message) {
        return new BlendingException(BlendingErrorType.DATA_NOT_FOUND.getCode(), message);
    }

    /**
     * 创建算法错误异常
     */
    public static BlendingException algorithmError(String message, Throwable cause) {
        return new BlendingException(BlendingErrorType.ALGORITHM_ERROR.getCode(), message, cause);
    }
} 