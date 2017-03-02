package com.huawei.hic.so.iam.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

/**
 * Created by qsma on 2017/2/10.
 */
@Configuration
@EnableAutoConfiguration
@EnableResourceServer
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfigurer.class);

    public void configure(HttpSecurity http) throws Exception {
        http
            .antMatcher("/**")
            .authorizeRequests()
            .antMatchers("/", "/login**", "/webjars/**")
            .permitAll()
            .anyRequest()
            .authenticated();
    }
}

