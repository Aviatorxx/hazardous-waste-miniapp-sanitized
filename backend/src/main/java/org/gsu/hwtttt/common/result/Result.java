package org.gsu.hwtttt.common.result;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 统一响应结果类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Data
@ApiModel(value = "Result", description = "统一响应结果")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("响应码")
    private Integer code;

    @ApiModelProperty("是否成功")
    private Boolean success;

    @ApiModelProperty("响应消息")
    private String message;

    @ApiModelProperty("响应数据")
    private T data;

    @ApiModelProperty("时间戳")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @ApiModelProperty("追踪ID")
    private String traceId;

    public Result() {
        this.timestamp = LocalDateTime.now();
        this.traceId = UUID.randomUUID().toString().replace("-", "");
    }

    public Result(Integer code, Boolean success, String message, T data) {
        this();
        this.code = code;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), true, ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), true, ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（带消息和数据）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), true, message, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> fail() {
        return new Result<>(ResultCode.SYSTEM_ERROR.getCode(), false, ResultCode.SYSTEM_ERROR.getMessage(), null);
    }

    /**
     * 失败响应（带消息）
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(ResultCode.SYSTEM_ERROR.getCode(), false, message, null);
    }

    /**
     * 失败响应（带结果码）
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), false, resultCode.getMessage(), null);
    }

    /**
     * 失败响应（带结果码和数据）
     */
    public static <T> Result<T> fail(ResultCode resultCode, T data) {
        return new Result<>(resultCode.getCode(), false, resultCode.getMessage(), data);
    }

    /**
     * 错误响应（带结果码）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), false, resultCode.getMessage(), null);
    }

    /**
     * 错误响应（带错误码和消息）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, false, message, null);
    }

    /**
     * 错误响应（带消息）
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.SYSTEM_ERROR.getCode(), false, message, null);
    }

    /**
     * 错误响应（带结果码和数据）
     */
    public static <T> Result<T> error(ResultCode resultCode, T data) {
        return new Result<>(resultCode.getCode(), false, resultCode.getMessage(), data);
    }

    /**
     * 自定义响应
     */
    public static <T> Result<T> result(Integer code, Boolean success, String message, T data) {
        return new Result<>(code, success, message, data);
    }
} 