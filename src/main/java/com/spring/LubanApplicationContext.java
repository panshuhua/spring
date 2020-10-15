package com.spring;

import com.spring.annotation.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LubanApplicationContext {

    private ConcurrentHashMap<String,BeanDefinition> beanDefinitionMap=new ConcurrentHashMap();
    private ConcurrentHashMap<String,Object> singletonObjects=new ConcurrentHashMap();
    private List<BeanPostProcessor> beanPostProcessorList=new ArrayList<BeanPostProcessor>();

    public LubanApplicationContext(Class configClass) {
       // 扫描类 得到 BeanDefinition
        scan(configClass);

        // 实例化非懒加载单例bean
        //   1. 实例化
        //   2. 属性填充
        //   3. Aware回调
        //   4. 初始化
        //   5. 添加到单例池
        instanceSingletonBean();
    }

    private void scan(Class configClass){
        // 扫描class，转化为BeanDefinition对象，最后添加到beanDefinitionMap中
        // 先得到包路径
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
        String packagePath = componentScanAnnotation.value();
        System.out.println(packagePath); //扫描包路径
        // 扫描包路径得到classList
        List<Class> classList = getBeanClasses(packagePath);
        // 遍历class得到BeanDefinition
        for(Class clazz:classList){
            if(clazz.isAnnotationPresent(Component.class)){ //判断一个类中是否含有某个注解
                BeanDefinition beanDefinition=new BeanDefinition();
                beanDefinition.setBeanClass(clazz);

                Component component = (Component) clazz.getAnnotation(Component.class);
                String beanName=component.value();

                //判断一个类是否实现了某个接口
                //BeanPostProcessor对spring进行扩展
                if(BeanPostProcessor.class.isAssignableFrom(clazz)){
                    try {
                        BeanPostProcessor instance = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                        beanPostProcessorList.add(instance);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }

                //解析scope
                if(clazz.isAnnotationPresent(Scope.class)){
                    Scope scope = (Scope) clazz.getAnnotation(Scope.class);
                    String scopeValue=scope.value();
                    beanDefinition.setScope(scopeValue);
                }else{
                    beanDefinition.setScope("singleton");
                }

                //判断lazy
                if(clazz.isAnnotationPresent(Lazy.class)){
                    beanDefinition.setLazy(true);
                }else{
                    beanDefinition.setLazy(false);
                }

                beanDefinitionMap.put(beanName,beanDefinition);
            }
        }


    }

    private List<Class> getBeanClasses(String packagePath){
        List<Class> beanClasses=new ArrayList<Class>();

        ClassLoader classLoader=LubanApplicationContext.class.getClassLoader();
        packagePath=packagePath.replace(".","/");
        URL resource = classLoader.getResource(packagePath); //通过资源文件路径获取类文件所在的路径
        File file =new File(resource.getFile());

        if(file.isDirectory()){
            File[] files=file.listFiles();
            
            for(File f:files){
                String fileName=f.getAbsolutePath();
                System.out.println(fileName);
                if(fileName.endsWith(".class")){
                    String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                    className=className.replace("\\",".");
                    System.out.println(className);

                    try {
                        //由类名获取到加载的类
                        Class<?> clazz = classLoader.loadClass(className);
                        beanClasses.add(clazz);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                }
            }

        }
        return  beanClasses;

    }

    public Object getBean(String beanName){
        if(singletonObjects.containsKey(beanName)){
            return  singletonObjects.get(beanName);
        }else{
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            return doGreateBean(beanName,beanDefinition);
        }

    }

    //根据beanDefinition创建单例非懒加载的bean，并保存到单例池中
    private  void instanceSingletonBean(){
        for(String beanName:beanDefinitionMap.keySet()){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

            if (beanDefinition.getScope().equals("singleton")){
                if(!beanDefinition.isLazy()){
                    Object bean=doGreateBean(beanName,beanDefinition);
                    singletonObjects.put(beanName,bean);
                }
            }
        }
    }

    //基于BeanDefinition创建Bean
    private Object doGreateBean(String beanName, BeanDefinition beanDefinition) {
        Class beanClass=beanDefinition.getBeanClass();

        try {
            //实例化
            Constructor declaredConstructor = beanClass.getDeclaredConstructor();
            Object instance = declaredConstructor.newInstance(); //通过构造方法创建类的实例

            //填充属性
            Field[] fields=beanClass.getDeclaredFields();
            for(Field field:fields){
                if(field.isAnnotationPresent(Autowired.class)){
                    String fieldName=field.getName();
                    Object bean = getBean(fieldName);
                    field.setAccessible(true);
                    field.set(instance,bean); //填充属性关键代码
                }
            }

            // Aware回调
            // 判断一个对象（Object类型）是否实现了某个接口使用instanceof
            if(instance instanceof BeanNameAware){
                ((BeanNameAware)instance).setBeanName(beanName);
            }

            //初始化
            if(instance instanceof InitializingBean){
                ((InitializingBean)instance).afterPropertiesSet();
            }

            //执行 BeanPostProcessor 对spring进行扩展
            for(BeanPostProcessor beanPostProcessor:beanPostProcessorList){
                beanPostProcessor.postProcessAfterInitialization(instance,beanName);
            }
            return instance;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
         return  null;
    }

}
