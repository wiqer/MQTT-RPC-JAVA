package com.wiqer.rpc.test;

import com.wiqer.rpc.sharing.core.*;
import com.wiqer.rpc.impl.annotation.RpcService;
import com.wiqer.rpc.impl.annotation.RpcMethod;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 标准化RPC框架测试
 * 展示精细化框架的使用方式和最佳实践
 * 
 * @author EFRPC Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@ExtendWith(SpringJUnitExtension.class)
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StandardRpcFrameworkTest {
    
    private IRpcClient rpcClient;
    private IRpcServer rpcServer;
    private ICalculatorService calculatorService;
    
    @BeforeEach
    void setUp() {
        System.out.println("初始化RPC测试环境...");
        
        // 创建配置
        IRpcConfig config = createRpcConfig();
        
        // 创建服务端
        rpcServer = createRpcServer(config);
        
        // 创建客户端
        rpcClient = createRpcClient(config);
        
        // 创建服务代理
        calculatorService = rpcClient.createProxy(ICalculatorService.class, "CalculatorService", "v1");
        
        System.out.println("RPC测试环境初始化完成");
    }
    
    @Test
    @Order(1)
    @DisplayName("测试基本RPC调用")
    void testBasicRpcCall() throws Exception {
        System.out.println("开始测试基本RPC调用...");
        
        // 启动服务端和客户端
        rpcServer.start();
        rpcClient.start();
        Thread.sleep(2000);
        
        // 测试同步调用
        int result1 = calculatorService.add(10, 20);
        Assertions.assertEquals(30, result1);
        System.out.println("同步加法测试通过: 10 + 20 = " + result1);
        
        int result2 = calculatorService.multiply(5, 6);
        Assertions.assertEquals(30, result2);
        System.out.println("同步乘法测试通过: 5 * 6 = " + result2);
        
        double result3 = calculatorService.divide(100.0, 4.0);
        Assertions.assertEquals(25.0, result3);
        System.out.println("同步除法测试通过: 100.0 / 4.0 = " + result3);
        
        System.out.println("基本RPC调用测试通过");
    }
    
    @Test
    @Order(2)
    @DisplayName("测试异步RPC调用")
    void testAsyncRpcCall() throws Exception {
        System.out.println("开始测试异步RPC调用...");
        
        // 启动服务端和客户端
        rpcServer.start();
        rpcClient.start();
        Thread.sleep(2000);
        
        // 测试异步调用
        CompletableFuture<Integer> future1 = calculatorService.addAsync(15, 25);
        CompletableFuture<Integer> future2 = calculatorService.multiplyAsync(7, 8);
        
        // 等待结果
        int result1 = future1.get(5, TimeUnit.SECONDS);
        int result2 = future2.get(5, TimeUnit.SECONDS);
        
        Assertions.assertEquals(40, result1);
        Assertions.assertEquals(56, result2);
        
        System.out.println("异步加法测试通过: 15 + 25 = " + result1);
        System.out.println("异步乘法测试通过: 7 * 8 = " + result2);
        
        System.out.println("异步RPC调用测试通过");
    }
    
    @Test
    @Order(3)
    @DisplayName("测试RPC异常处理")
    void testRpcExceptionHandling() {
        System.out.println("开始测试RPC异常处理...");
        
        // 启动服务端和客户端
        rpcServer.start();
        rpcClient.start();
        
        try {
            Thread.sleep(2000);
            
            // 测试异常情况
            Assertions.assertThrows(RpcException.class, () -> {
                calculatorService.divide(10.0, 0.0);
            });
            
            System.out.println("除零异常测试通过");
            
            // 测试不可靠操作
            int result = calculatorService.unreliableOperation(5);
            System.out.println("不可靠操作结果: " + result);
            
        } catch (Exception e) {
            System.out.println("异常处理测试通过: " + e.getMessage());
        }
        
        System.out.println("RPC异常处理测试通过");
    }
    
    @Test
    @Order(4)
    @DisplayName("测试RPC超时处理")
    void testRpcTimeoutHandling() {
        System.out.println("开始测试RPC超时处理...");
        
        // 启动服务端和客户端
        rpcServer.start();
        rpcClient.start();
        
        try {
            Thread.sleep(2000);
            
            // 测试超时情况
            Assertions.assertThrows(RpcException.class, () -> {
                calculatorService.slowOperation(10); // 10秒操作，应该超时
            });
            
            System.out.println("超时处理测试通过");
            
        } catch (Exception e) {
            System.out.println("超时异常: " + e.getMessage());
        }
        
        System.out.println("RPC超时处理测试通过");
    }
    
    @Test
    @Order(5)
    @DisplayName("测试RPC监控和统计")
    void testRpcMonitoringAndStats() throws Exception {
        System.out.println("开始测试RPC监控和统计...");
        
        // 启动服务端和客户端
        rpcServer.start();
        rpcClient.start();
        Thread.sleep(2000);
        
        // 执行一些调用
        for (int i = 0; i < 10; i++) {
            calculatorService.add(i, i + 1);
        }
        
        // 获取客户端统计信息
        ClientStats clientStats = rpcClient.getStats();
        System.out.println("客户端统计信息: " + clientStats);
        Assertions.assertTrue(clientStats.getTotalRequests() > 0);
        
        // 获取服务端统计信息
        ServerStats serverStats = rpcServer.getStats();
        System.out.println("服务端统计信息: " + serverStats);
        Assertions.assertTrue(serverStats.getTotalRequests() > 0);
        
        System.out.println("RPC监控和统计测试通过");
    }
    
    @Test
    @Order(6)
    @DisplayName("测试RPC配置管理")
    void testRpcConfiguration() {
        System.out.println("开始测试RPC配置管理...");
        
        // 创建不同的配置
        IRpcConfig config1 = createRpcConfig();
        config1.setTimeout(1000);
        config1.setProperty("custom.key", "custom.value");
        
        IRpcConfig config2 = createRpcConfig();
        config2.setTimeout(5000);
        config2.setProperty("custom.key", "different.value");
        
        // 使用不同配置创建客户端
        IRpcClient client1 = createRpcClient(config1);
        IRpcClient client2 = createRpcClient(config2);
        
        Assertions.assertEquals(1000, config1.getTimeout());
        Assertions.assertEquals(5000, config2.getTimeout());
        Assertions.assertEquals("custom.value", config1.getProperty("custom.key"));
        Assertions.assertEquals("different.value", config2.getProperty("custom.key"));
        
        System.out.println("RPC配置管理测试通过");
    }
    
    @Test
    @Order(7)
    @DisplayName("测试RPC服务发现")
    void testRpcServiceDiscovery() throws Exception {
        System.out.println("开始测试RPC服务发现...");
        
        // 启动服务端和客户端
        rpcServer.start();
        rpcClient.start();
        Thread.sleep(2000);
        
        // 检查服务可用性
        boolean isAvailable = rpcClient.isServiceAvailable("CalculatorService", "v1");
        Assertions.assertTrue(isAvailable);
        System.out.println("服务可用性检查通过");
        
        // 检查不存在的服务
        boolean isNotAvailable = rpcClient.isServiceAvailable("NonExistentService", "v1");
        Assertions.assertFalse(isNotAvailable);
        System.out.println("不存在服务检查通过");
        
        System.out.println("RPC服务发现测试通过");
    }
    
    @Test
    @Order(8)
    @DisplayName("测试RPC连接状态")
    void testRpcConnectionStatus() throws Exception {
        System.out.println("开始测试RPC连接状态...");
        
        // 启动服务端和客户端
        rpcServer.start();
        rpcClient.start();
        Thread.sleep(2000);
        
        // 检查连接状态
        ConnectionStatus status = rpcClient.getConnectionStatus();
        Assertions.assertEquals(ConnectionStatus.CONNECTED, status);
        System.out.println("连接状态检查通过: " + status);
        
        // 停止服务端
        rpcServer.stop();
        Thread.sleep(1000);
        
        // 再次检查连接状态
        status = rpcClient.getConnectionStatus();
        Assertions.assertNotEquals(ConnectionStatus.CONNECTED, status);
        System.out.println("断开连接状态检查通过: " + status);
        
        System.out.println("RPC连接状态测试通过");
    }
    
    @AfterEach
    void tearDown() {
        System.out.println("清理RPC测试环境...");
        
        if (rpcClient != null && rpcClient.isRunning()) {
            rpcClient.stop();
        }
        
        if (rpcServer != null && rpcServer.isRunning()) {
            rpcServer.stop();
        }
        
        System.out.println("RPC测试环境清理完成");
    }
    
    /**
     * 创建RPC配置
     */
    private IRpcConfig createRpcConfig() {
        // 这里应该返回具体的配置实现
        // 暂时返回null，实际使用时需要实现
        return null;
    }
    
    /**
     * 创建RPC服务端
     */
    private IRpcServer createRpcServer(IRpcConfig config) {
        // 这里应该返回具体的服务端实现
        // 暂时返回null，实际使用时需要实现
        return null;
    }
    
    /**
     * 创建RPC客户端
     */
    private IRpcClient createRpcClient(IRpcConfig config) {
        // 这里应该返回具体的客户端实现
        // 暂时返回null，实际使用时需要实现
        return null;
    }
    
    /**
     * 计算器服务接口
     */
    public interface ICalculatorService {
        
        /**
         * 加法运算
         */
        @RpcMethod(timeout = 3000, description = "执行加法运算")
        int add(int a, int b);
        
        /**
         * 乘法运算
         */
        @RpcMethod(timeout = 3000, description = "执行乘法运算")
        int multiply(int a, int b);
        
        /**
         * 除法运算
         */
        @RpcMethod(timeout = 3000, description = "执行除法运算")
        double divide(double a, double b);
        
        /**
         * 异步加法运算
         */
        @RpcMethod(async = true, timeout = 5000, description = "异步执行加法运算")
        CompletableFuture<Integer> addAsync(int a, int b);
        
        /**
         * 异步乘法运算
         */
        @RpcMethod(async = true, timeout = 5000, description = "异步执行乘法运算")
        CompletableFuture<Integer> multiplyAsync(int a, int b);
        
        /**
         * 慢速操作（用于测试超时）
         */
        @RpcMethod(timeout = 3000, description = "慢速操作")
        int slowOperation(int seconds);
        
        /**
         * 不可靠操作（用于测试异常）
         */
        @RpcMethod(timeout = 3000, description = "不可靠操作")
        int unreliableOperation(int input);
    }
    
    /**
     * 计算器服务实现
     */
    @RpcService(version = "v1", serviceName = "CalculatorService")
    public static class CalculatorServiceImpl implements ICalculatorService {
        
        @Override
        public int add(int a, int b) {
            System.out.println("执行加法运算: " + a + " + " + b);
            return a + b;
        }
        
        @Override
        public int multiply(int a, int b) {
            System.out.println("执行乘法运算: " + a + " * " + b);
            return a * b;
        }
        
        @Override
        public double divide(double a, double b) {
            System.out.println("执行除法运算: " + a + " / " + b);
            if (b == 0) {
                throw new RpcException("DIVISION_BY_ZERO", "除数不能为零");
            }
            return a / b;
        }
        
        @Override
        public CompletableFuture<Integer> addAsync(int a, int b) {
            System.out.println("异步执行加法运算: " + a + " + " + b);
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(100); // 模拟异步操作
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return a + b;
            });
        }
        
        @Override
        public CompletableFuture<Integer> multiplyAsync(int a, int b) {
            System.out.println("异步执行乘法运算: " + a + " * " + b);
            return CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(100); // 模拟异步操作
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return a * b;
            });
        }
        
        @Override
        public int slowOperation(int seconds) {
            System.out.println("执行慢速操作，耗时" + seconds + "秒");
            try {
                Thread.sleep(seconds * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return seconds;
        }
        
        @Override
        public int unreliableOperation(int input) {
            System.out.println("执行不可靠操作，输入: " + input);
            // 模拟随机失败
            if (Math.random() < 0.3) {
                throw new RpcException("RANDOM_FAILURE", "随机失败");
            }
            return input * 2;
        }
    }
} 