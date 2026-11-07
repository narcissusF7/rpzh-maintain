package com.rpzh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * @Author liubc
 * @create 2021/4/15 16:28
 */
@Configuration //声明为一个配置类
@EnableResourceServer  //声明为一个资源微服务,表示只有授权才能被访问,只有携带令牌才能被访问,令牌校验
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)//激活方法上的PreAuthorize注解
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    //公钥
    private static final String PUBLIC_KEY = "public.key";

    /***
     * 定义JwtTokenStore
     * @param jwtAccessTokenConverter
     * @return
     */
    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    /***
     * 定义JJwtAccessTokenConverter
     * @return
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setVerifierKey(getPubKey());
        return converter;
    }
    /**
     * 获取非对称加密公钥 Key
     * @return 公钥 Key
     */
    private String getPubKey() {
        Resource resource = new ClassPathResource(PUBLIC_KEY);
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
            BufferedReader br = new BufferedReader(inputStreamReader);
            return br.lines().collect(Collectors.joining("\n"));
        } catch (IOException ioe) {
            return null;
        }
    }

    /***
     * Http安全配置，对每个到达系统的http请求链接进行校验
     * @param http
     * @throws Exception
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        // 仅放行服务间 Feign 内部端点（调用方无用户令牌）与探活接口。
        // 这些路径网关不对外路由，生产环境必须限制微服务端口仅内网可达。
        http.authorizeRequests()
                .antMatchers("/actuator/health",
                        "/username/findPhoneNumberById",
                        "/rfid/findIps",
                        "/rfid/updateStatus").permitAll()
                // 角色/菜单/部门的写操作集中要求管理权限，读操作仍需认证
                .antMatchers("/role/addRole", "/role/editRole", "/role/changeStatus", "/role/dataScope", "/role/delectRole",
                        "/menu/addMenu", "/menu/editMenu", "/menu/changeMenuStatus", "/menu/deleteMenu",
                        "/dept/addDept", "/dept/editDept", "/dept/changeDeptStutas", "/dept/deleteDept")
                .hasAnyAuthority("admin",
                        "system:role:edit", "system:menu:edit", "system:dept:edit",
                        "system:role:remove", "system:menu:remove", "system:dept:remove")
                .anyRequest().authenticated();
    }
}
