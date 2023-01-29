package com.melnychuk.demo;

import com.melnychuk.annotations.Bean;

@Bean
public class Service3 {

    void askQuestion() {
        System.out.println("How are you doing?");
    }

}
