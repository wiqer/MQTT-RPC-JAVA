package com.wiqer.rpc.impl.ioc;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * 类扫描器 - 扫描指定包下的所有类
 */
@Slf4j
public class ClassScanner {
    
    /**
     * 扫描指定包下的所有类
     */
    public static Set<Class<?>> scanClasses(String basePackage) {
        Set<Class<?>> classes = new HashSet<>();
        
        try {
            // 获取类加载器
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            
            // 将包名转换为路径
            String packagePath = basePackage.replace('.', '/');
            URL url = classLoader.getResource(packagePath);
            
            if (url != null) {
                File packageDir = new File(url.getFile());
                if (packageDir.exists() && packageDir.isDirectory()) {
                    scanDirectory(packageDir, basePackage, classes, classLoader);
                }
            }
            
        } catch (Exception e) {
            log.error("扫描包失败: {}", basePackage, e);
        }
        
        return classes;
    }
    
    /**
     * 递归扫描目录
     */
    private static void scanDirectory(File directory, String packageName, Set<Class<?>> classes, ClassLoader classLoader) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 递归扫描子目录
                    String subPackageName = packageName + "." + file.getName();
                    scanDirectory(file, subPackageName, classes, classLoader);
                } else if (file.getName().endsWith(".class")) {
                    // 加载类文件
                    String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                    try {
                        Class<?> clazz = classLoader.loadClass(className);
                        classes.add(clazz);
                    } catch (ClassNotFoundException e) {
                        log.warn("无法加载类: {}", className);
                    }
                }
            }
        }
    }
} 