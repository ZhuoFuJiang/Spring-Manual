package com.zhuofu.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;
import com.spring.Scope;


@Component
public class ZhuofuBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("初始化前");
        if(beanName.equals("userService")) {
            ((UserService)bean).setName("周瑜好帅");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后");
        return bean;
    }
}
