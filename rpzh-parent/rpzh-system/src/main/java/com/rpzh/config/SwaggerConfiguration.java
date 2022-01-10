package com.rpzh.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.service.Contact;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import java.util.HashSet;

/**
 * 接口文档配置类
 * 访问地址:http://localhost:16883/doc.html
 * @Author liubc
 * @create 2022/1/10 16:18
 * @Description: good good study,day day up
 */
@Configuration
@EnableSwagger2
@EnableKnife4j
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerConfiguration {
    @Bean
    public Docket buildDocket() {
        HashSet<String> strings = new HashSet<>();
        strings.add("application/json");
        Docket docket=new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(buildApiInfo())
                //设置返回数据类型
                .produces(strings)
                //分组名称
                .groupName("1.0")
                .select()
                //这里指定Controller扫描包路径
                .apis(RequestHandlerSelectors.basePackage("com.rpzh"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }
    private ApiInfo buildApiInfo() {
        Contact contact = new Contact("rpzh","","");
        return new ApiInfoBuilder()
                .title("智慧养护-平台管理API文档")
                .description("平台管理服务api")
                .contact(contact)
                .version("1.0.0").build();
    }
}