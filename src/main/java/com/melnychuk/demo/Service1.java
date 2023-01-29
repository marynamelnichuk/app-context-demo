package com.melnychuk.demo;

import com.melnychuk.annotations.Bean;

@Bean("service")
public class Service1 implements IService {
    @Override
    public void sayHello() {
        System.out.println("say hello from service 1");
    }
}
