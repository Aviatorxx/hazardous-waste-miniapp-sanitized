package org.gsu.hwtttt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 应用自定义配置属性
 *
 * @author WenXin
 * @date 2025/06/05
 */
@Data
@Component
@ConfigurationProperties(prefix = "hwtttt")
public class HwttttProperties {

    /**
     * 缓存配置
     */
    private Cache cache = new Cache();

    /**
     * 安全配置
     */
    private Security security = new Security();

    /**
     * 文件上传配置
     */
    private FileUpload fileUpload = new FileUpload();

    @Data
    public static class Cache {
        /**
         * 是否启用缓存
         */
        private boolean enabled = true;

        /**
         * 默认过期时间（秒）
         */
        private int defaultExpireTime = 1800;
    }

    @Data
    public static class Security {
        /**
         * API访问频率限制（每分钟）
         */
        private int rateLimit = 100;

        /**
         * 是否启用IP白名单
         */
        private boolean ipWhitelistEnabled = false;
    }

    @Data
    public static class FileUpload {
        /**
         * 文件上传路径
         */
        private String path = "../uploads";

        /**
         * 文件大小限制
         */
        private String maxSize = "50MB";

        /**
         * 允许的文件扩展名
         */
        private String allowedExtensions = ".jpg,.jpeg,.png,.gif,.pdf,.doc,.docx,.xls,.xlsx,.txt,.csv";

        /**
         * 文件访问URL
         */
        private String accessUrl = "http://localhost:8080";
    }
} 