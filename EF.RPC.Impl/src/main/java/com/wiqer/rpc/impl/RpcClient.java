package com.wiqer.rpc.impl;

import com.wiqer.rpc.impl.annotation.EFRpcAutowired;
import com.wiqer.rpc.impl.proxy.ObjectProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class RpcClient implements ApplicationContextAware, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);
    protected String address;

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
            600L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));

    public RpcClient(String address) {
        this.address = address;

    }

    @SuppressWarnings("unchecked")
    public  <T, P> T createService(Class<T> interfaceClass, String version) {
        ObjectProxy objectProxy=  createObjectProxy(interfaceClass, version);
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                objectProxy
        );
    }

    public abstract  <T, P>ObjectProxy createObjectProxy(Class<T> interfaceClass, String version) ;

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

    public void stop() {
        threadPoolExecutor.shutdown();
    }

    @Override
    public void destroy() throws Exception {
        this.stop();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Field[] fields = bean.getClass().getDeclaredFields();
            try {
                for (Field field : fields) {
                    EFRpcAutowired rpcAutowired = field.getAnnotation(EFRpcAutowired.class);
                    if (rpcAutowired != null) {
                        String version = rpcAutowired.version();
                        field.setAccessible(true);
                        Object object= createService(field.getType(), version);
                        field.set(bean,object);
                    }
                }
            } catch (IllegalAccessException e) {
                logger.error(e.toString());
            }
        }
    }
}
