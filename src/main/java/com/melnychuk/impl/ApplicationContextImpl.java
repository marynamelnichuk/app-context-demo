package com.melnychuk.impl;

import com.melnychuk.ApplicationContext;
import com.melnychuk.annotations.Bean;
import com.melnychuk.annotations.Inject;
import com.melnychuk.exceptions.NoSuchBeanException;
import com.melnychuk.exceptions.NoUniqueBeanException;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class ApplicationContextImpl implements ApplicationContext {

    private final String packageName;
    private final Map<String, Object> beans;

    public ApplicationContextImpl(String packageName) {
        this.packageName = packageName;
        List<? extends Class<?>> beanClasses = getClassesAnnotatedWithBeanAnnotation();
        this.beans = initBeans(beanClasses);
        injectBeansDependencies();
    }

    @Override
    public <T> T getBean(Class<T> beanType) throws NoSuchBeanException, NoUniqueBeanException {
        Map<String, T> allBeansOfCurrentType = getAllBeans(beanType);
        if(allBeansOfCurrentType.size() > 1) {
            throw new NoUniqueBeanException("Not unique bean, please specify bean name " +
                    "by using method with bean name parameter");
        }
        return allBeansOfCurrentType
                    .values().stream()
                    .findFirst()
                    .orElseThrow(() -> new NoSuchBeanException("No bean registered by class: " + beanType.getSimpleName()));
    }

    @Override
    public <T> T getBean(String name, Class<T> beanType) throws NoSuchBeanException {
        T bean = (T) beans.get(name);
        if(bean == null) {
            throw new NoSuchBeanException("No bean with name: " + name);
        }
        return bean;
    }

    @Override
    public <T> Map<String, T> getAllBeans(Class<T> beanType) {
        return beans.entrySet().stream()
                .filter(entry -> beanType.isAssignableFrom(entry.getValue().getClass()))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> (T) entry.getValue()));
    }

    private Map<String, Object> initBeans(List<? extends Class<?>> classes) {
        return classes.stream()
                .collect(toMap(this::getBeanName, this::createInstanceOfClass));
    }

    private List<? extends Class<?>> getClassesAnnotatedWithBeanAnnotation() {
        return getClassesInPackage()
                .stream()
                .filter(c -> c.isAnnotationPresent(Bean.class))
                .toList();
    }

    private List<? extends Class<?>> getClassesInPackage() {
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(getPackageNameAsUrl());
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return reader
                .lines()
                .map(this::getClassNameWithPackage)
                .map(this::getClassForName)
                .toList();
    }

    private String getPackageNameAsUrl() {
        return packageName.replaceAll("[.]", "/");
    }

    private String getClassNameWithPackage(String className) {
        return packageName + "." + className.replace(".class", "");
    }

    @SneakyThrows
    private Class<?> getClassForName(String claasName) {
        return Class.forName(claasName);
    }

    @SneakyThrows
    private Object createInstanceOfClass(Class<?> clazz) {
        return clazz.getConstructor().newInstance();
    }

    private String getBeanName(Class<?> clazz) {
        String beanAnnotationValue = clazz.getDeclaredAnnotation(Bean.class).value();
        if(beanAnnotationValue.isEmpty()) {
            String className = clazz.getSimpleName();
            return className.substring(0, 1).toLowerCase() + className.substring(1);
        }
        return beanAnnotationValue;
    }

    private void injectBeansDependencies() {
        beans.forEach(this::injectBeanDependencies);
    }

    private <T> void injectBeanDependencies(String beanName, T bean) {
        Class<T> beanClass = (Class<T>) bean.getClass();
        Arrays.stream(beanClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Inject.class))
                .forEach(field -> setBeanFieldValue(field, bean));
    }

    @SneakyThrows
    private void setBeanFieldValue(Field field, Object bean) {
        Object valueToInject = getValueToInject(field);
        field.setAccessible(true);
        field.set(bean, valueToInject);
    }

    private Object getValueToInject(Field field) {
        String injectValue = field.getAnnotation(Inject.class).value();
        if(field.getAnnotation(Inject.class).value().isEmpty()) {
            return getBean(field.getType());
        } else {
            return getBean(injectValue, field.getType());
        }
    }

}
