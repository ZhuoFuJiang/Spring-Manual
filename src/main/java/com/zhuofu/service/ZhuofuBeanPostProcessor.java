package com.zhuofu.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;
import com.spring.Scope;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


@Component
public class ZhuofuBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
//        System.out.println("初始化前");
//        if(beanName.equals("userService")) {
//            ((UserService)bean).setName("周瑜好帅");
//        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后");

        if(beanName.equals("userServiceImpl")) {
            Object proxyInstance = Proxy.newProxyInstance(ZhuofuBeanPostProcessor.class.getClassLoader(),
                    bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("代理对象");
                    return method.invoke(bean, args);
                }
            });
            return proxyInstance;
        }
        return bean;
    }
}
