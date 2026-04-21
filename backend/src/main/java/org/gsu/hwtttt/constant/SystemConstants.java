package org.gsu.hwtttt.constant;

import java.math.BigDecimal;

/**
 * 系统常量类
 *
 * @author WenXin
 * @date 2025/06/10
 */
public class SystemConstants {

    /**
     * 默认页大小
     */
    public static final Long DEFAULT_PAGE_SIZE = 20L;

    /**
     * 最大页大小
     */
    public static final Long MAX_PAGE_SIZE = 1000L;

    /**
     * 默认缓存时间（秒）
     */
    public static final int DEFAULT_CACHE_TIME = 3600;

    /**
     * 文件上传路径
     */
    public static final String UPLOAD_PATH = "/uploads/";

    /**
     * 导出文件路径
     */
    public static final String EXPORT_PATH = "/exports/";

    /**
     * 热力学数据存储路径
     */
    public static final String THERMAL_DATA_PATH = "/data/thermal/";

    /**
     * 默认字符编码
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 日期时间格式
     */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 审核状态（与数据库枚举完全匹配）
     */
    public static class AuditStatus {
        public static final String PENDING = "pending";
        public static final String APPROVED = "approved";
        public static final String REJECTED = "rejected";
    }

    /**
     * 配伍会话状态（与数据库枚举完全匹配）
     */
    public static class MatchingStatus {
        public static final String DRAFT = "draft";
        public static final String WASTE_SELECTED = "waste_selected";
        public static final String COMPATIBILITY_CHECKING = "compatibility_checking";
        public static final String COMPATIBLE = "compatible";
        public static final String INCOMPATIBLE = "incompatible";
        public static final String CALCULATING = "calculating";
        public static final String CALCULATION_SUCCESS = "calculation_success";
        public static final String CALCULATION_FAILED = "calculation_failed";
        public static final String ARCHIVED = "archived";
        // Legacy status for backward compatibility
        public static final String COMPLETED = "completed";
        public static final String FAILED = "failed";
    }

    /**
     * 配伍结果状态（与数据库枚举完全匹配）
     */
    public static class ResultStatus {
        public static final String SUCCESS = "success";
        public static final String FAILED = "failed";
        public static final String WARNING = "warning";
    }

    /**
     * 风险等级（与数据库枚举完全匹配）
     */
    public static class RiskLevel {
        public static final String LOW = "LOW";
        public static final String MEDIUM = "MEDIUM";
        public static final String HIGH = "HIGH";
    }

    /**
     * 数据质量等级（与数据库枚举完全匹配）
     */
    public static class QualityGrade {
        public static final String A = "A";
        public static final String B = "B";
        public static final String C = "C";
        public static final String D = "D";
    }

    /**
     * 数据质量标识（与数据库枚举完全匹配）
     */
    public static class QualityFlag {
        public static final String EXCELLENT = "excellent";
        public static final String GOOD = "good";
        public static final String FAIR = "fair";
        public static final String POOR = "poor";
    }

    /**
     * 光谱分析类型（与数据库枚举完全匹配）
     */
    public static class SpectrumType {
        public static final String XRF = "XRF";           // X射线荧光光谱
        public static final String FTIR = "FTIR";         // 傅里叶变换红外光谱
        public static final String GC_MS = "GC-MS";       // 气相色谱-质谱联用
        public static final String TGA = "TGA";           // 热重分析
        public static final String DSC = "DSC";           // 差示扫描量热法
        public static final String DTA = "DTA";           // 差热分析
    }

    /**
     * 理化特性数据类型（与数据库枚举完全匹配）
     */
    public static class PropertyType {
        public static final String NUMERIC = "numeric";
        public static final String TEXT = "text";
        public static final String BOOLEAN = "boolean";
    }

    /**
     * 系统操作类型（与数据库枚举完全匹配）
     */
    public static class OperationType {
        public static final String INSERT = "INSERT";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String SELECT = "SELECT";
        public static final String IMPORT = "IMPORT";
        public static final String EXPORT = "EXPORT";
    }

    /**
     * 系统配置值类型（与数据库枚举完全匹配）
     */
    public static class ValueType {
        public static final String STRING = "string";
        public static final String NUMBER = "number";
        public static final String BOOLEAN = "boolean";
        public static final String JSON = "json";
    }

    /**
     * 配伍算法约束参数代码（与数据库数据匹配）
     */
    public static class ConstraintParams {
        public static final String HEAT_VALUE = "HEAT_VALUE";                    // 热值控制
        public static final String WATER_CONTENT = "WATER_CONTENT";              // 水分控制
        public static final String N_CONTENT = "N_CONTENT";                      // 氮含量控制
        public static final String S_CONTENT = "S_CONTENT";                      // 硫含量控制
        public static final String CL_CONTENT = "CL_CONTENT";                    // 氯含量控制
        public static final String F_CONTENT = "F_CONTENT";                      // 氟含量控制
        public static final String HG_CONTENT = "HG_CONTENT";                    // 汞含量控制
        public static final String CD_CONTENT = "CD_CONTENT";                    // 镉含量控制
        public static final String AS_NI_CONTENT = "AS_NI_CONTENT";              // 砷镍含量控制
        public static final String PB_CONTENT = "PB_CONTENT";                    // 铅含量控制
        public static final String HEAVY_METALS_TOTAL = "HEAVY_METALS_TOTAL";    // 重金属总量控制
        public static final String TOTAL_AMOUNT = "TOTAL_AMOUNT";                // 总量控制
    }

    /**
     * 系统配置组（与数据库数据匹配）
     */
    public static class ConfigGroup {
        public static final String PERFORMANCE = "performance";
        public static final String MATCHING = "matching";
        public static final String FILE_STORAGE = "file_storage";
        public static final String UI = "ui";
    }

    /**
     * 理化特性分类代码（与数据库数据匹配）
     */
    public static class PropertyCategory {
        public static final String ELEMENT = "ELEMENT";                    // 元素组成
        public static final String THERMAL = "THERMAL";                    // 热值特性
        public static final String PHYSICAL = "PHYSICAL";                  // 物理特性
        public static final String MOISTURE = "MOISTURE";                  // 含水率
        public static final String FLASH_POINT = "FLASH_POINT";            // 闪点
        public static final String HEAVY_METAL = "HEAVY_METAL";            // 重金属
        public static final String ALKALI_METAL = "ALKALI_METAL";          // 碱金属
        public static final String ORGANIC_POLLUTANT = "ORGANIC_POLLUTANT"; // 有机污染物
    }

    /**
     * 物理特性查询分类代码
     */
    public static class PhysicalPropertyCategory {
        public static final String ELEMENT_COMPOSITION = "ELEMENT_COMPOSITION";  // 元素组成
        public static final String HEAT_VALUE = "HEAT_VALUE";                    // 热值
        public static final String PH = "PH";                                    // pH值
        public static final String WATER_CONTENT = "WATER_CONTENT";              // 含水率
        public static final String FLASH_POINT = "FLASH_POINT";                  // 闪点
        public static final String HEAVY_METALS = "HEAVY_METALS";                // 重金属
        public static final String ALKALI_METALS = "ALKALI_METALS";              // 碱金属
    }

    /**
     * 约束限值（与数据库数据匹配）
     */
    public static class ConstraintLimits {
        // 🔧 FIX: Correct unit documentation - constraints are stored in kJ/kg
        // 热值控制范围 (注意：约束存储单位为 kJ/kg，数据库热值单位为 cal/g)
        public static final double HEAT_VALUE_MIN = 12500.0; // kJ/kg
        public static final double HEAT_VALUE_MAX = 16800.0; // kJ/kg
        
        // Unit conversion factor: cal/g to kJ/kg
        public static final BigDecimal CAL_TO_KJ_CONVERSION_FACTOR = new BigDecimal("4.184");
        
        // 各组分含量上限 (%)
        public static final double WATER_CONTENT_MAX = 45.0;
        public static final double N_CONTENT_MAX = 2.0;
        public static final double S_CONTENT_MAX = 3.0;
        public static final double CL_CONTENT_MAX = 1.5;
        public static final double F_CONTENT_MAX = 1.0;
        
        // 重金属含量上限 (mg/kg)
        public static final double HG_CONTENT_MAX = 4.0;
        public static final double CD_CONTENT_MAX = 1.0;
        public static final double AS_NI_CONTENT_MAX = 95.0;
        public static final double PB_CONTENT_MAX = 70.0;
        public static final double HEAVY_METALS_TOTAL_MAX = 800.0;
    }

    /**
     * 性能配置
     */
    public static class Performance {
        public static final int MAX_CONCURRENT_USERS = 50;
        public static final int QUERY_TIMEOUT_SECONDS = 2;
        public static final int CALCULATION_TIMEOUT_SECONDS = 10;
        public static final int MAX_FILE_SIZE_MB = 100;
    }

    /**
     * 配伍算法配置
     */
    public static class MatchingConfig {
        public static final double DEFAULT_TARGET_HEAT_VALUE = 14000.0;
        public static final int MIN_WASTE_TYPES = 2;
        public static final int MAX_WASTE_TYPES = 10;
    }

    /**
     * 常用单位
     */
    public static class Units {
        // 重量单位
        public static final String KG = "kg";
        public static final String G = "g";
        public static final String T = "t";
        
        // 百分比单位
        public static final String PERCENT = "%";
        
        // 能量单位
        public static final String CAL_PER_G = "cal/g";
        public static final String KJ_PER_KG = "kJ/kg";
        
        // 温度单位
        public static final String CELSIUS = "℃";
        
        // 浓度单位
        public static final String MG_PER_KG = "mg/kg";
        public static final String MG_PER_L = "mg/L";
    }

    /**
     * 危险废物特性常量
     */
    public static class WasteCharacteristics {
        public static final String OXIDIZING = "oxidizing";                      // 氧化性
        public static final String REDUCING = "reducing";                        // 还原性
        public static final String VOLATILE = "volatile";                        // 挥发性
        public static final String FLAMMABLE = "flammable";                     // 易燃性
        public static final String TOXIC = "toxic";                             // 毒性
        public static final String REACTIVE = "reactive";                       // 反应性
        public static final String INFECTIOUS = "infectious";                   // 感染性
        public static final String CORROSIVE = "corrosive";                     // 腐蚀性
        public static final String HALOGENATED_HYDROCARBON = "halogenated_hydrocarbon"; // 卤化烃类
        public static final String CYANIDE_CONTAINING = "cyanide_containing";    // 含氰化物废物
    }

    /**
     * 响应状态码
     */
    public static class ResponseCode {
        public static final int SUCCESS = 200;
        public static final int CREATED = 201;
        public static final int BAD_REQUEST = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
        public static final int NOT_FOUND = 404;
        public static final int INTERNAL_ERROR = 500;
    }

    /**
     * 响应消息
     */
    public static class ResponseMessage {
        public static final String SUCCESS = "操作成功";
        public static final String FAILED = "操作失败";
        public static final String NOT_FOUND = "数据不存在";
        public static final String PARAMETER_ERROR = "参数错误";
        public static final String PERMISSION_DENIED = "权限不足";
    }
}
