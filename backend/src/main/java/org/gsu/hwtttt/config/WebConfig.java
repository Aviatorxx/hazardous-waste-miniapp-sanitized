package org.gsu.hwtttt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 
 * @author WenXin
 * @date 2025/06/13
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加热力学图像静态资源映射
        registry.addResourceHandler("/static/thermal-images/**")
                .addResourceLocations("classpath:/static/thermal-images/");
        
        // 添加API访问热力学图像的映射
        registry.addResourceHandler("/api/v3/thermal-properties/image/**")
                .addResourceLocations("classpath:/static/thermal-images/");
    }
} 