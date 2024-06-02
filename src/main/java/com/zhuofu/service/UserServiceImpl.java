package com.zhuofu.service;

import com.spring.Autowired;
import com.spring.Component;
import com.spring.Scope;


@Component(value = "userServiceImpl")
@Scope(value = "prototype")
public class UserServiceImpl implements UserInterface {

    @Autowired
    private OrderService orderService;


    private String beanName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    @Override
    public void test() {
        System.out.println(orderService);
        System.out.println(beanName);
        System.out.println(name);
    }
}
