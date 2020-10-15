package com.luban.service;

import com.spring.BeanPostProcessor;
import com.spring.annotation.Component;

/**
 * spring扩展类
 */
@Component
public class LubanBeanPostProcessor implements BeanPostProcessor{

    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return null;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println(beanName+"-----postProcessAfterInitialization");
        return bean;
    }
}
