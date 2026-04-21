package org.gsu.hwtttt.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * OpenAPI 3 配置类
 *
 * @author WenXin
 * @date 2025/06/10
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("危废热处理智库系统API文档")
                        .description("危废热处理智库系统后端接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Admin")
                                .email("admin@hwtttt.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("本地开发环境"),
                        new Server().url("http://localhost:8080").description("开发环境")
                ));
    }
} 
