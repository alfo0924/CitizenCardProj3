package org.example._citizncardproj3.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("市民卡系統 API文檔")
                        .description("市民卡系統3.0版本 RESTful API文檔")
                        .version("3.0")
                        .contact(new Contact()
                                .name("System Admin")
                                .email("admin@example.com")
                                .url("https://www.example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(getServers())
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .tags(Arrays.asList(
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("認證")
                                .description("認證相關接口"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("會員")
                                .description("會員管理接口"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("電影")
                                .description("電影相關接口"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("訂票")
                                .description("訂票相關接口"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("電子錢包")
                                .description("電子錢包相關接口"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("優惠")
                                .description("優惠管理接口")
                ));
    }

    private List<Server> getServers() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("本地開發環境");

        Server devServer = new Server();
        devServer.setUrl("http://dev-api.example.com");
        devServer.setDescription("開發測試環境");

        Server prodServer = new Server();
        prodServer.setUrl("http://api.example.com");
        prodServer.setDescription("生產環境");

        return Arrays.asList(localServer, devServer, prodServer);
    }
}