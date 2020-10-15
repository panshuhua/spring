package com.luban;

import com.luban.service.OrderService;
import com.spring.LubanApplicationContext;

public class Test {
    public static void main(String[] args) {
        LubanApplicationContext lubanApplicationContext=new LubanApplicationContext(AppConfig.class);
        //属性注入测试
        OrderService orderService= (OrderService) lubanApplicationContext.getBean("orderService");
        System.out.println(orderService);
//        orderService.test();
//
        //单例模式与原型模式对比测试
//        System.out.println(lubanApplicationContext.getBean("orderService"));
//        System.out.println(lubanApplicationContext.getBean("orderService"));
//        System.out.println(lubanApplicationContext.getBean("orderService"));
//
//        System.out.println("------------------------------------------");
//
//        System.out.println(lubanApplicationContext.getBean("userService"));
//        System.out.println(lubanApplicationContext.getBean("userService"));
//        System.out.println(lubanApplicationContext.getBean("userService"));

//          orderService.test1(); //todo:lazy逻辑有待优化
    }

}
