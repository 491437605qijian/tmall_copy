package com.how2j.tmall.config;

import com.how2j.tmall.interceptor.LoginInterceptor;
import com.how2j.tmall.interceptor.OtherInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

//配置LoginInterceptor这个拦截器
@Configuration
class WebMvcConfigurer extends WebMvcConfigurerAdapter{

    @Bean
    public LoginInterceptor getLoginIntercepter() {
        return new LoginInterceptor();
    }

    @Bean
    public OtherInterceptor getOtherIntercepter() {
        return new OtherInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(getLoginIntercepter())
                .addPathPatterns("/**");
        registry.addInterceptor(getOtherIntercepter())
                .addPathPatterns("/**");
    }
}