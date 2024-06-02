package com.spring;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ManualApplicationContext {

    private Class configClass;

    private ConcurrentHashMap<String, Object> singleObjects = new ConcurrentHashMap<>();  // 单例池
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public ManualApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 解析配置类
        // ComponentScan注解 -- 扫描路径 -- 扫描 -- Beandefinition -- BeanDefinitionMap
        scan(configClass);

        for (Map.Entry<String, BeanDefinition> entry: beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if(beanDefinition.getScope().equals("singleton")) {
                // TODO 这里存在一个问题，如果依赖注入的bean还没生成，就不能依赖注入了，所以应该分为两个方法，一个是生成，一个是初始化
                // TODO 单例依赖注入多例 没问题
                // TODO 多例依赖注入多例 也没问题
                // TODO 依赖注入单例就有问题，必须解析bean之间的关系，或者就是先创建每个single，再create，进行单独处理
                Object bean = createBean(beanName, beanDefinition);
                singleObjects.put(beanName, bean);
            }
        }

    }

    public Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getClazz();
        Object o = null;
        try {
            o = clazz.getDeclaredConstructor().newInstance();

            // 依赖注入
            for(Field declaredField: clazz.getDeclaredFields()) {
                if(declaredField.isAnnotationPresent(Autowired.class)) {
                    Object bean = getBean(declaredField.getName());
                    declaredField.setAccessible(true);
                    declaredField.set(o, bean);
                }
            }

            // 回调
            if(o instanceof BeanNameAware) {
                ((BeanNameAware)o).setBeanName(beanName);
            }

            for(BeanPostProcessor beanPostProcessor: beanPostProcessorList) {
                o = beanPostProcessor.postProcessBeforeInitialization(o, beanName);
            }

            // 初始化
            if(o instanceof InitializingBean) {
                ((InitializingBean)o).afterPropertiesSet();
            }

            // BeanPostProcessor
            for(BeanPostProcessor beanPostProcessor: beanPostProcessorList) {
                o = beanPostProcessor.postProcessAfterInitialization(o, beanName);
            }

            return o;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void scan(Class configClass) {
        // 解析配置类
        // ComponentScan注解 -> 扫描路径

        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.value();
        System.out.println("Path: " + path);
        // path的路径进行替换
        path = path.replace(".", "/");

        // 扫描
        // Bootstrap -- jre/lib
        // Ext -- jre/ext/lib
        // App -- classpath

        ClassLoader classLoader = ManualApplicationContext.class.getClassLoader();
        URL resource = classLoader.getResource(path);
        File file = new File(resource.getFile());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                System.out.println(f);
                String fileName = f.getAbsolutePath();
                if (fileName.endsWith(".class")) {

                    String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                    className = className.replace("\\", ".");

                    System.out.println(className);

                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        if (clazz.isAnnotationPresent(Component.class)) {
                            // 表示当前这个类是一个Bean
                            if(BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                                beanPostProcessorList.add(instance);
                            }

                            // BeanDefinition
                            Component component = clazz.getDeclaredAnnotation(Component.class);
                            String beanName = component.value();

                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setClazz(clazz);

                            if (clazz.isAnnotationPresent(Scope.class)) {
                                Scope scope = clazz.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scope.value());
                            } else {
                                beanDefinition.setScope("singleton");
                            }

                            beanDefinitionMap.put(beanName, beanDefinition);

                        }
                    } catch (ClassNotFoundException | NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        throw new RuntimeException(e);
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }


                }
            }
        }
    }

    public Object getBean(String beanName) {
        if(beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {
                Object o = singleObjects.get(beanName);
                return o;
            } else {
                // 创建Bean对象
                return createBean(beanName, beanDefinition);

            }

        } else {
            // 不存在该Bean
            throw new NullPointerException();
        }
    }
}
