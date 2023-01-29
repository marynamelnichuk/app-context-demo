package com.melnychuk.demo;

import com.melnychuk.annotations.Bean;
import com.melnychuk.annotations.Inject;

@Bean("strange")
public class Service4 {

    @Inject("service2")
    private IService iService;

    @Inject
    private Service3 service3;

    void greet() {
        iService.sayHello();
        service3.askQuestion();
    }

}
