package com.melnychuk.demo;

import com.melnychuk.ApplicationContext;
import com.melnychuk.exceptions.NoSuchBeanException;
import com.melnychuk.exceptions.NoUniqueBeanException;
import com.melnychuk.impl.ApplicationContextImpl;

public class Demo {
    public static void main(String[] args) {
        ApplicationContext context = new ApplicationContextImpl("com.melnychuk.demo");
        System.out.println(context.getAllBeans(IService.class));
        System.out.println(context.getAllBeans(Service2.class));
        System.out.println(context.getBean(Service1.class));
        System.out.println(context.getBean(Service3.class));
        System.out.println(context.getBean("service", IService.class));
        try {
            System.out.println(context.getBean(IService.class));
        } catch (NoUniqueBeanException ex) {
            System.out.println("NoUniqueBeanException occurred.");
        }
        try {
            System.out.println(context.getBean(Service5.class));
        } catch (NoSuchBeanException ex) {
            System.out.println("NoSuchBeanException occurred.");
        }
        try {
            System.out.println(context.getBean("unknown", IService.class));
        } catch (NoSuchBeanException ex) {
            System.out.println("NoSuchBeanException occurred.");
        }
        Service4 service4 = context.getBean(Service4.class);
        service4.greet();
    }
}
