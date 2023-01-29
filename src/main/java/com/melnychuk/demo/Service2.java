package com.melnychuk.demo;

import com.melnychuk.annotations.Bean;

@Bean
public class Service2 implements IService {
    @Override
    public void sayHello() {
        System.out.println("say hello from service 2");
    }
}
