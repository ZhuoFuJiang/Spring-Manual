package com.zhuofu;

import com.spring.ManualApplicationContext;
import com.zhuofu.service.UserService;

public class Test {

    public static void main(String[] args) {
        ManualApplicationContext applicationContext = new ManualApplicationContext(AppConfig.class);

//        Object userService1 = applicationContext.getBean("userService");
//        Object userService2 = applicationContext.getBean("userService");
//        Object userService3 = applicationContext.getBean("userService");
//
//        System.out.println(userService1 == userService2);

        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.test();
    }
}
