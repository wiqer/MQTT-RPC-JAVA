# RPC框架最佳实践指南

## 概述

本文档提供了EFRPC框架的最佳实践指南，包括设计原则、使用模式、性能优化、错误处理等方面的建议，帮助开发者更好地使用RPC框架。

## 设计原则

### 1. 接口设计原则

#### 1.1 单一职责原则
```java
// 好的设计：每个接口只负责一个功能领域
public interface UserService {
    User getUserById(Long id);
    void createUser(User user);
    void updateUser(User user);
    void deleteUser(Long id);
}

public interface OrderService {
    Order createOrder(OrderRequest request);
    Order getOrderById(Long id);
    List<Order> getUserOrders(Long userId);
}

// 不好的设计：一个接口包含多个不相关的功能
public interface BusinessService {
    User getUserById(Long id);
    Order createOrder(OrderRequest request);
    Product getProductById(Long id);
    void sendEmail(String to, String content);
}
```

#### 1.2 接口稳定性原则
```java
// 好的设计：接口稳定，向后兼容
public interface UserService {
    // 版本1.0
    User getUserById(Long id);
    
    // 版本1.1：添加新方法，不破坏现有接口
    User getUserById(Long id, boolean includeDetails);
    
    // 版本1.2：使用默认方法提供向后兼容
    default User getUserById(Long id) {
        return getUserById(id, false);
    }
}
```

#### 1.3 参数设计原则
```java
// 好的设计：使用DTO对象传递参数
public class CreateUserRequest {
    private String username;
    private String email;
    private String password;
    // getters and setters
}

public interface UserService {
    User createUser(CreateUserRequest request);
}

// 不好的设计：参数过多，难以维护
public interface UserService {
    User createUser(String username, String email, String password, 
                   String firstName, String lastName, String phone, 
                   String address, Date birthDate, String gender);
}
```

### 2. 服务设计原则

#### 2.1 服务粒度控制
```java
// 好的设计：适中的服务粒度
@RpcService(version = "v1", serviceName = "UserService")
public class UserServiceImpl implements UserService {
    // 用户相关的核心业务逻辑
}

@RpcService(version = "v1", serviceName = "OrderService")
public class OrderServiceImpl implements OrderService {
    // 订单相关的核心业务逻辑
}

// 不好的设计：服务粒度过细
@RpcService(version = "v1", serviceName = "UserQueryService")
public class UserQueryServiceImpl implements UserQueryService {
    // 只包含查询方法
}

@RpcService(version = "v1", serviceName = "UserCreateService")
public class UserCreateServiceImpl implements UserCreateService {
    // 只包含创建方法
}
```

#### 2.2 方法设计原则
```java
// 好的设计：方法职责清晰，参数合理
@RpcService(version = "v1", serviceName = "UserService")
public class UserServiceImpl implements UserService {
    
    @RpcMethod(timeout = 3000, description = "根据ID获取用户信息")
    public User getUserById(Long id) {
        // 实现逻辑
    }
    
    @RpcMethod(timeout = 5000, description = "创建新用户")
    public User createUser(CreateUserRequest request) {
        // 实现逻辑
    }
    
    @RpcMethod(async = true, timeout = 10000, description = "批量处理用户数据")
    public CompletableFuture<List<User>> batchProcessUsers(List<Long> userIds) {
        // 异步实现逻辑
    }
}
```

## 配置管理最佳实践

### 1. 配置分层管理

#### 1.1 环境配置
```java
// 开发环境配置
@Configuration
public class DevRpcConfig {
    
    @Bean
    public IRpcConfig rpcConfig() {
        IRpcConfig config = new DefaultRpcConfig();
        config.setTimeout(5000);
        config.setRetryCount(3);
        config.setConnectionPoolSize(10);
        config.setEnableLogging(true);
        config.setLogLevel("DEBUG");
        return config;
    }
}

// 生产环境配置
@Configuration
@Profile("production")
public class ProdRpcConfig {
    
    @Bean
    public IRpcConfig rpcConfig() {
        IRpcConfig config = new DefaultRpcConfig();
        config.setTimeout(10000);
        config.setRetryCount(5);
        config.setConnectionPoolSize(50);
        config.setEnableLogging(true);
        config.setLogLevel("INFO");
        config.setEnableMonitoring(true);
        return config;
    }
}
```

#### 1.2 服务特定配置
```java
// 为不同服务设置不同的配置
@Configuration
public class ServiceSpecificConfig {
    
    @Bean
    @Qualifier("userServiceConfig")
    public IRpcConfig userServiceConfig() {
        IRpcConfig config = new DefaultRpcConfig();
        config.setTimeout(3000);
        config.setRetryCount(2);
        config.setProperty("cache.enabled", "true");
        config.setProperty("cache.expire", "300");
        return config;
    }
    
    @Bean
    @Qualifier("orderServiceConfig")
    public IRpcConfig orderServiceConfig() {
        IRpcConfig config = new DefaultRpcConfig();
        config.setTimeout(10000);
        config.setRetryCount(5);
        config.setProperty("transaction.enabled", "true");
        return config;
    }
}
```

### 2. 配置验证
```java
// 配置验证器
@Component
public class RpcConfigValidator {
    
    public void validate(IRpcConfig config) {
        if (config.getTimeout() <= 0) {
            throw new IllegalArgumentException("超时时间必须大于0");
        }
        
        if (config.getRetryCount() < 0) {
            throw new IllegalArgumentException("重试次数不能为负数");
        }
        
        if (config.getConnectionPoolSize() <= 0) {
            throw new IllegalArgumentException("连接池大小必须大于0");
        }
        
        // 验证自定义属性
        validateCustomProperties(config);
    }
    
    private void validateCustomProperties(IRpcConfig config) {
        // 验证缓存配置
        if (Boolean.parseBoolean(config.getProperty("cache.enabled", "false"))) {
            String expireStr = config.getProperty("cache.expire", "300");
            try {
                long expire = Long.parseLong(expireStr);
                if (expire <= 0) {
                    throw new IllegalArgumentException("缓存过期时间必须大于0");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("缓存过期时间必须是数字");
            }
        }
    }
}
```

## 错误处理最佳实践

### 1. 异常设计

#### 1.1 自定义异常类型
```java
// 业务异常
public class BusinessException extends RpcException {
    public BusinessException(String errorCode, String message) {
        super(errorCode, message);
    }
}

// 参数验证异常
public class ValidationException extends RpcException {
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }
}

// 资源不存在异常
public class ResourceNotFoundException extends RpcException {
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super("RESOURCE_NOT_FOUND", 
              String.format("资源 %s (ID: %s) 不存在", resourceType, resourceId));
    }
}
```

#### 1.2 异常处理策略
```java
@RpcService(version = "v1", serviceName = "UserService")
public class UserServiceImpl implements UserService {
    
    @RpcMethod(timeout = 3000, description = "根据ID获取用户信息")
    public User getUserById(Long id) {
        try {
            // 参数验证
            if (id == null || id <= 0) {
                throw new ValidationException("用户ID不能为空且必须大于0");
            }
            
            // 业务逻辑
            User user = userRepository.findById(id);
            if (user == null) {
                throw new ResourceNotFoundException("User", id.toString());
            }
            
            return user;
            
        } catch (ValidationException | ResourceNotFoundException e) {
            // 业务异常，直接抛出
            throw e;
        } catch (Exception e) {
            // 系统异常，包装后抛出
            throw new RpcException("SYSTEM_ERROR", "获取用户信息失败", e);
        }
    }
}
```

### 2. 重试策略

#### 2.1 智能重试
```java
// 重试策略配置
@Configuration
public class RetryConfig {
    
    @Bean
    public RetryPolicy retryPolicy() {
        return RetryPolicy.builder()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(100))
                .retryOnException(RetryableException.class)
                .ignoreException(NonRetryableException.class)
                .build();
    }
}

// 可重试异常
public class RetryableException extends RpcException {
    public RetryableException(String message) {
        super("RETRYABLE_ERROR", message);
    }
}

// 不可重试异常
public class NonRetryableException extends RpcException {
    public NonRetryableException(String message) {
        super("NON_RETRYABLE_ERROR", message);
    }
}
```

## 性能优化最佳实践

### 1. 连接池管理

#### 1.1 连接池配置
```java
@Configuration
public class ConnectionPoolConfig {
    
    @Bean
    public ConnectionPool connectionPool() {
        return ConnectionPool.builder()
                .maxConnections(50)
                .minConnections(5)
                .maxIdleTime(Duration.ofMinutes(30))
                .connectionTimeout(Duration.ofSeconds(10))
                .validationQuery("SELECT 1")
                .build();
    }
}
```

#### 1.2 连接监控
```java
@Component
public class ConnectionMonitor {
    
    private final ConnectionPool connectionPool;
    
    public ConnectionMonitor(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }
    
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void monitorConnections() {
        ConnectionPoolStats stats = connectionPool.getStats();
        
        if (stats.getActiveConnections() > stats.getMaxConnections() * 0.8) {
            log.warn("连接池使用率过高: {}%", 
                    (double) stats.getActiveConnections() / stats.getMaxConnections() * 100);
        }
        
        if (stats.getIdleConnections() < stats.getMinConnections()) {
            log.warn("空闲连接数过低: {}", stats.getIdleConnections());
        }
    }
}
```

### 2. 序列化优化

#### 2.1 序列化器选择
```java
// 高性能场景：使用Protobuf
@Configuration
@Profile("high-performance")
public class HighPerformanceSerializerConfig {
    
    @Bean
    public ISerializer serializer() {
        return new ProtobufSerializer();
    }
}

// 开发调试场景：使用JSON
@Configuration
@Profile("development")
public class DevelopmentSerializerConfig {
    
    @Bean
    public ISerializer serializer() {
        return new JsonSerializer();
    }
}
```

#### 2.2 序列化缓存
```java
// 启用序列化缓存
@Configuration
public class SerializerCacheConfig {
    
    @Bean
    public ISerializer cachedSerializer() {
        JsonSerializer serializer = new JsonSerializer();
        serializer.setCacheEnabled(true);
        serializer.setCompressionEnabled(true);
        return serializer;
    }
}
```

### 3. 异步处理

#### 3.1 异步方法设计
```java
@RpcService(version = "v1", serviceName = "BatchProcessingService")
public class BatchProcessingServiceImpl implements BatchProcessingService {
    
    @RpcMethod(async = true, timeout = 30000, description = "批量处理数据")
    public CompletableFuture<BatchResult> processBatch(List<DataItem> items) {
        return CompletableFuture.supplyAsync(() -> {
            BatchResult result = new BatchResult();
            
            // 分批处理，避免内存溢出
            int batchSize = 1000;
            for (int i = 0; i < items.size(); i += batchSize) {
                int end = Math.min(i + batchSize, items.size());
                List<DataItem> batch = items.subList(i, end);
                processBatchInternal(batch, result);
            }
            
            return result;
        });
    }
    
    private void processBatchInternal(List<DataItem> batch, BatchResult result) {
        // 处理逻辑
    }
}
```

## 监控和日志最佳实践

### 1. 监控指标

#### 1.1 关键指标定义
```java
@Component
public class RpcMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public RpcMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    // 请求计数
    public void incrementRequestCount(String serviceName, String methodName) {
        Counter.builder("rpc.requests.total")
                .tag("service", serviceName)
                .tag("method", methodName)
                .register(meterRegistry)
                .increment();
    }
    
    // 响应时间
    public void recordResponseTime(String serviceName, String methodName, long duration) {
        Timer.builder("rpc.response.time")
                .tag("service", serviceName)
                .tag("method", methodName)
                .register(meterRegistry)
                .record(duration, TimeUnit.MILLISECONDS);
    }
    
    // 错误计数
    public void incrementErrorCount(String serviceName, String methodName, String errorType) {
        Counter.builder("rpc.errors.total")
                .tag("service", serviceName)
                .tag("method", methodName)
                .tag("error_type", errorType)
                .register(meterRegistry)
                .increment();
    }
}
```

#### 1.2 监控集成
```java
@Aspect
@Component
public class RpcMonitoringAspect {
    
    private final RpcMetrics rpcMetrics;
    
    public RpcMonitoringAspect(RpcMetrics rpcMetrics) {
        this.rpcMetrics = rpcMetrics;
    }
    
    @Around("@annotation(rpcMethod)")
    public Object monitorRpcCall(ProceedingJoinPoint joinPoint, RpcMethod rpcMethod) throws Throwable {
        String serviceName = getServiceName(joinPoint);
        String methodName = getMethodName(joinPoint);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 记录请求
            rpcMetrics.incrementRequestCount(serviceName, methodName);
            
            // 执行方法
            Object result = joinPoint.proceed();
            
            // 记录响应时间
            long duration = System.currentTimeMillis() - startTime;
            rpcMetrics.recordResponseTime(serviceName, methodName, duration);
            
            return result;
            
        } catch (Exception e) {
            // 记录错误
            rpcMetrics.incrementErrorCount(serviceName, methodName, e.getClass().getSimpleName());
            throw e;
        }
    }
}
```

### 2. 日志管理

#### 2.1 结构化日志
```java
@RpcService(version = "v1", serviceName = "UserService")
public class UserServiceImpl implements UserService {
    
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    
    @RpcMethod(timeout = 3000, description = "根据ID获取用户信息")
    public User getUserById(Long id) {
        log.info("开始获取用户信息", 
                StructuredArguments.keyValue("userId", id),
                StructuredArguments.keyValue("service", "UserService"),
                StructuredArguments.keyValue("method", "getUserById"));
        
        try {
            User user = userRepository.findById(id);
            
            if (user != null) {
                log.info("用户信息获取成功", 
                        StructuredArguments.keyValue("userId", id),
                        StructuredArguments.keyValue("username", user.getUsername()));
            } else {
                log.warn("用户不存在", 
                        StructuredArguments.keyValue("userId", id));
            }
            
            return user;
            
        } catch (Exception e) {
            log.error("获取用户信息失败", 
                    StructuredArguments.keyValue("userId", id),
                    StructuredArguments.keyValue("error", e.getMessage()),
                    e);
            throw e;
        }
    }
}
```

#### 2.2 日志配置
```yaml
# logback-spring.xml
<configuration>
    <appender name="RPC_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/rpc.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/rpc.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>100MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <loggerName/>
                <message/>
                <mdc/>
                <arguments/>
            </providers>
        </encoder>
    </appender>
    
    <logger name="com.wiqer.rpc" level="INFO" additivity="false">
        <appender-ref ref="RPC_FILE"/>
    </logger>
</configuration>
```

## 安全最佳实践

### 1. 认证和授权

#### 1.1 服务间认证
```java
@Component
public class RpcAuthenticationInterceptor implements RpcInterceptor {
    
    @Override
    public Object intercept(RpcInvocation invocation) throws Throwable {
        // 验证服务间认证
        String token = invocation.getMetadata().get("auth-token");
        if (!validateToken(token)) {
            throw new RpcException("AUTHENTICATION_FAILED", "认证失败");
        }
        
        return invocation.proceed();
    }
    
    private boolean validateToken(String token) {
        // 实现token验证逻辑
        return token != null && token.startsWith("Bearer ");
    }
}
```

#### 1.2 权限控制
```java
@RpcService(version = "v1", serviceName = "UserService")
public class UserServiceImpl implements UserService {
    
    @RpcMethod(timeout = 3000, description = "删除用户")
    @RequiresPermission("user:delete")
    public void deleteUser(Long id) {
        // 检查权限
        if (!hasPermission("user:delete")) {
            throw new RpcException("PERMISSION_DENIED", "权限不足");
        }
        
        // 业务逻辑
        userRepository.deleteById(id);
    }
}
```

### 2. 数据加密

#### 2.1 传输加密
```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public ISerializer encryptedSerializer() {
        JsonSerializer serializer = new JsonSerializer();
        serializer.setEncryptionEnabled(true);
        serializer.setEncryptionKey("your-secret-key");
        return serializer;
    }
}
```

## 测试最佳实践

### 1. 单元测试

#### 1.1 服务测试
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    void testGetUserById_Success() {
        // Given
        Long userId = 1L;
        User expectedUser = new User(userId, "testuser", "test@example.com");
        when(userRepository.findById(userId)).thenReturn(expectedUser);
        
        // When
        User actualUser = userService.getUserById(userId);
        
        // Then
        assertThat(actualUser).isEqualTo(expectedUser);
        verify(userRepository).findById(userId);
    }
    
    @Test
    void testGetUserById_UserNotFound() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(null);
        
        // When & Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("用户不存在");
    }
}
```

#### 1.2 集成测试
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserServiceIntegrationTest {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testCreateAndGetUser() {
        // Given
        CreateUserRequest request = new CreateUserRequest("testuser", "test@example.com", "password");
        
        // When
        User createdUser = userService.createUser(request);
        User retrievedUser = userService.getUserById(createdUser.getId());
        
        // Then
        assertThat(retrievedUser).isNotNull();
        assertThat(retrievedUser.getUsername()).isEqualTo("testuser");
        assertThat(retrievedUser.getEmail()).isEqualTo("test@example.com");
    }
}
```

### 2. 性能测试

#### 2.1 负载测试
```java
@Test
@DisplayName("负载测试：并发1000个请求")
void testLoadTest() throws Exception {
    int concurrentUsers = 1000;
    int requestsPerUser = 10;
    
    ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
    CountDownLatch latch = new CountDownLatch(concurrentUsers);
    
    long startTime = System.currentTimeMillis();
    
    for (int i = 0; i < concurrentUsers; i++) {
        executor.submit(() -> {
            try {
                for (int j = 0; j < requestsPerUser; j++) {
                    calculatorService.add(j, j + 1);
                }
            } finally {
                latch.countDown();
            }
        });
    }
    
    latch.await(30, TimeUnit.SECONDS);
    long endTime = System.currentTimeMillis();
    
    long totalTime = endTime - startTime;
    long totalRequests = (long) concurrentUsers * requestsPerUser;
    double requestsPerSecond = (double) totalRequests / (totalTime / 1000.0);
    
    System.out.printf("总请求数: %d, 总时间: %dms, 每秒请求数: %.2f%n", 
                     totalRequests, totalTime, requestsPerSecond);
    
    assertThat(requestsPerSecond).isGreaterThan(1000); // 期望每秒至少1000个请求
}
```

## 部署最佳实践

### 1. 容器化部署

#### 1.1 Dockerfile
```dockerfile
# 多阶段构建
FROM maven:3.8.4-openjdk-11 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 1.2 Docker Compose
```yaml
version: '3.8'

services:
  rpc-server:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - RPC_SERVER_PORT=8080
    depends_on:
      - redis
      - rabbitmq
    restart: unless-stopped
    
  redis:
    image: redis:6.2-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
      
  rabbitmq:
    image: rabbitmq:3.9-management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      - RABBITMQ_DEFAULT_USER=admin
      - RABBITMQ_DEFAULT_PASS=admin123
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq

volumes:
  redis-data:
  rabbitmq-data:
```

### 2. 监控和告警

#### 2.1 Prometheus配置
```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'rpc-server'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
```

#### 2.2 Grafana仪表板
```json
{
  "dashboard": {
    "title": "RPC服务监控",
    "panels": [
      {
        "title": "请求速率",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(rpc_requests_total[5m])",
            "legendFormat": "{{service}} - {{method}}"
          }
        ]
      },
      {
        "title": "响应时间",
        "type": "graph",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(rpc_response_time_bucket[5m]))",
            "legendFormat": "95th percentile"
          }
        ]
      },
      {
        "title": "错误率",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(rpc_errors_total[5m]) / rate(rpc_requests_total[5m])",
            "legendFormat": "Error Rate"
          }
        ]
      }
    ]
  }
}
```

## 总结

遵循这些最佳实践可以帮助您：

1. **提高代码质量**：通过良好的设计原则和编码规范
2. **提升性能**：通过合理的配置和优化策略
3. **增强可靠性**：通过完善的错误处理和监控机制
4. **简化维护**：通过标准化的开发模式和部署流程
5. **保障安全**：通过多层次的安全防护措施

建议根据实际项目需求，选择合适的最佳实践进行应用，并在实践中不断优化和改进。 