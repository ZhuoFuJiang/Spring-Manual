package com.zhuofu.service;


import com.spring.*;

@Component(value = "userService")
@Scope("singleton")
public class UserService implements InitializingBean {

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



    public void test() {
        System.out.println(orderService);
        System.out.println(beanName);
        System.out.println(name);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("初始化");
    }
}
