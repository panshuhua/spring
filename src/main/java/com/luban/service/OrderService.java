package com.luban.service;

import com.spring.BeanNameAware;
import com.spring.InitializingBean;
import com.spring.annotation.Autowired;
import com.spring.annotation.Component;

@Component("orderService")
public class OrderService implements BeanNameAware, InitializingBean {
    @Autowired
    private UserService userService;
    @Autowired
    private LazyService lazyService;

    private  String beanName;

    public OrderService() {

    }

    public void  test(){
        System.out.println(userService);
    }

    public void  test1(){
        System.out.println(lazyService);
    }

    public void setBeanName(String name) {
        System.out.println("beanNameAware:"+name);
        this.beanName=name;
    }

    public void afterPropertiesSet() {
        System.out.println("InitializingBean 初始化");
    }

}
