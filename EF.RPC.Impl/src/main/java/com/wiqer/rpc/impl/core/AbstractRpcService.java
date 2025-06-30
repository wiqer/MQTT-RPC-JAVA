package com.wiqer.rpc.impl.core;

import com.wiqer.rpc.serialize.SerializerInterface;
import com.wiqer.rpc.serialize.JsonSerializer;
import lombok.Data;

/**
 * 抽象RPC服务基类
 * 改进设计：清晰的继承层次，统一的配置管理
 */
@Data
public abstract class AbstractRpcService {
    
    /**
     * 序列化器
     */
    protected SerializerInterface serializer;
    
    /**
     * 版本号
     */
    protected String version = "v1";
    
    /**
     * 运行模式
     */
    protected String runMode = "auto";
    
    /**
     * 超时时间
     */
    protected long timeout = 5000;
    
    /**
     * 服务名称
     */
    protected String serviceName;
    
    /**
     * 包名
     */
    protected String packageName;
    
    /**
     * 全限定名
     */
    protected String fullName;
    
    /**
     * 接口全限定名
     */
    protected String interfaceFullName;
    
    /**
     * 类名
     */
    protected String className;
    
    public AbstractRpcService() {
        this.serializer = JsonSerializer.getSerializer();
    }
    
    /**
     * 初始化服务
     */
    public abstract void initialize();
    
    /**
     * 启动服务
     */
    public abstract void start();
    
    /**
     * 停止服务
     */
    public abstract void stop();
    
    /**
     * 检查服务状态
     */
    public abstract boolean isRunning();
    
    // IRpcConfig 接口方法实现
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getRunMode() {
        return runMode;
    }
    
    public void setRunMode(String runMode) {
        this.runMode = runMode;
    }
    
    public long getTimeout() {
        return timeout;
    }
    
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
} 