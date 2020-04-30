package com.how2j.tmall.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.how2j.tmall.service.OrderService;

@Component
//工具类：通过反射再拿到一次该类
public class SpringContextUtil implements ApplicationContextAware {

    private SpringContextUtil() {

    }

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext){
        SpringContextUtil.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

}
