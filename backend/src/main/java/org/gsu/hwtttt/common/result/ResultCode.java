package org.gsu.hwtttt.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    VALIDATION_ERROR(422, "参数校验失败"),
    PARAM_VALID_ERROR(422, "参数校验失败"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // 服务器错误 5xx
    SYSTEM_ERROR(500, "系统内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // 业务错误 1xxx
    WASTE_NOT_FOUND(1001, "危废信息不存在"),
    WASTE_CODE_EXISTS(1002, "危废代码已存在"),
    STORAGE_INSUFFICIENT(1003, "库存不足"),
    AUDIT_STATUS_ERROR(1004, "审核状态错误"),
    
    // 理化特性错误 2xxx
    PROPERTY_NOT_FOUND(2001, "理化特性不存在"),
    PROPERTY_CATEGORY_NOT_FOUND(2002, "理化特性分类不存在"),
    
    // 热力学特性错误 3xxx
    THERMAL_PROPERTY_NOT_FOUND(3001, "热力学特性不存在"),
    SPECTRUM_TYPE_NOT_SUPPORT(3002, "不支持的光谱类型"),
    
    // 相容性错误 4xxx
    COMPATIBILITY_NOT_FOUND(4001, "相容性信息不存在"),
    INCOMPATIBLE_WASTE(4002, "危废不相容"),
    COMPATIBILITY_CHECK_FAILED(4003, "相容性检查失败"),
    
    // 配伍模拟错误 5xxx
    SIMULATION_SESSION_NOT_FOUND(5001, "配伍会话不存在"),
    SIMULATION_FAILED(5002, "配伍模拟失败"),
    OPTIMIZATION_FAILED(5003, "优化计算失败"),
    CONSTRAINT_VIOLATION(5004, "约束条件违反"),
    
    // 文件错误 6xxx
    FILE_UPLOAD_ERROR(6001, "文件上传失败"),
    FILE_TYPE_NOT_SUPPORT(6002, "不支持的文件类型"),
    FILE_SIZE_ERROR(6003, "文件大小超限"),
    FILE_SIZE_EXCEED(6003, "文件大小超限"),
    FILE_NOT_FOUND(6004, "文件不存在"),
    
    // 数据错误 7xxx
    DATA_IMPORT_ERROR(7001, "数据导入失败"),
    DATA_EXPORT_ERROR(7002, "数据导出失败"),
    DATA_INTEGRITY_ERROR(7003, "数据完整性错误"),
    DATA_QUALITY_LOW(7004, "数据质量不满足要求");

    private final Integer code;
    private final String message;
} 